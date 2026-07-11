package com.opentms.dealing.integration;

import com.opentms.common.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Basedata 规则匹配客户端（v1.0 - 2026-07-11）
 *
 * <p>跨服务调用 {@code /api/v1/default-bank-account-rules/match}，为 dealing 模块
 * 的 AC/AT/FX cashflow 自动填充 bank_account_id / counterparty_bank_account_id。</p>
 *
 * <p>设计要点：
 * <ul>
 *   <li>5 分钟内存缓存（基于 ConcurrentHashMap + TTL）</li>
 *   <li>失败 / 超时一律降级为 null，不阻断 cashflow 写入（PRD §5.1 失败降级）</li>
 *   <li>不抛异常给调用方（避免 cashflow 录入失败）</li>
 *   <li>依赖 Spring Web 的 RestTemplate（dealing pom 已引入）</li>
 * </ul>
 *
 * <p>调用方应当捕获任何 RuntimeException（即使本类已 try-catch，亦作防御性）。</p>
 *
 * @author Open-TMS Backend Developer
 * @since 2026-07-11
 */
@Component
public class BasedataMatchClient {

    private static final Logger log = LoggerFactory.getLogger(BasedataMatchClient.class);

    private static final Duration CACHE_TTL = Duration.ofMinutes(5);
    private static final Duration CACHE_SWEEP_INTERVAL = Duration.ofMinutes(1);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    /** key = "managementEntityId|counterpartyId|instrumentId|direction|currency|dualDirection" */
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    private volatile Instant lastSweep = Instant.now();

    public BasedataMatchClient(
            RestTemplate restTemplate,
            @Value("${basedata.base-url:http://localhost:8081}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    /**
     * 命中返回基于数据的 {@link BasedataMatchResult}；未命中或失败返回 null。
     *
     * @param managementEntityId 管理主体 ID
     * @param counterpartyId     对手方 ID
     * @param instrumentId       金融工具 ID（可空）
     * @param direction          Inflow / Outflow
     * @param currency           币种
     * @param dualDirection      是否双方向（FX 必传 true）
     */
    public BasedataMatchResult match(Long managementEntityId, Long counterpartyId,
                                     Long instrumentId, String direction, String currency,
                                     boolean dualDirection) {
        String key = cacheKey(managementEntityId, counterpartyId, instrumentId, direction, currency, dualDirection);
        sweepIfStale();

        CacheEntry entry = cache.get(key);
        Instant now = Instant.now();
        if (entry != null && entry.expiresAt.isAfter(now)) {
            log.debug("[BasedataMatchClient] cache hit key={}", key);
            return entry.result;
        }

        StringBuilder url = new StringBuilder(baseUrl)
                .append("/api/v1/default-bank-account-rules/match")
                .append("?managementEntityId=").append(safeParam(managementEntityId))
                .append("&counterpartyId=").append(safeParam(counterpartyId))
                .append("&direction=").append(safeParam(direction))
                .append("&currency=").append(safeParam(currency))
                .append("&dualDirection=").append(dualDirection);
        if (instrumentId != null) {
            url.append("&instrumentId=").append(instrumentId);
        }

        BasedataMatchResult result = null;
        try {
            Result<?> resp = restTemplate.getForObject(url.toString(), Result.class);
            if (resp != null && resp.getCode() == 200 && resp.getData() != null) {
                Object data = resp.getData();
                if (data instanceof java.util.Map<?, ?> map) {
                    result = parseMatchResult(map, dualDirection);
                } else {
                    log.warn("[BasedataMatchClient] unexpected response data type: {}", data.getClass());
                }
            } else if (resp != null) {
                log.info("[BasedataMatchClient] basedata match returned non-200: code={}, message={}",
                        resp.getCode(), resp.getMessage());
            }
        } catch (RestClientException e) {
            log.warn("[BasedataMatchClient] rest call failed (degrade to null): {}", e.getMessage());
        } catch (RuntimeException e) {
            log.warn("[BasedataMatchClient] unexpected error (degrade to null): {}", e.getMessage());
        }

        cache.put(key, new CacheEntry(result, now.plus(CACHE_TTL)));
        log.info("[BasedataMatchClient] match result key={} result={}", key,
                result != null ? result.toShortString() : "null");
        return result;
    }

    /** 清空缓存（用于单元测试或运维干预） */
    public void clearCache() {
        cache.clear();
        lastSweep = Instant.now();
        log.info("[BasedataMatchClient] cache cleared");
    }

    /** 当前缓存大小（监控/测试用） */
    public int cacheSize() {
        return cache.size();
    }

    private void sweepIfStale() {
        Instant now = Instant.now();
        if (Duration.between(lastSweep, now).compareTo(CACHE_SWEEP_INTERVAL) < 0) {
            return;
        }
        lastSweep = now;
        cache.entrySet().removeIf(e -> e.getValue().expiresAt.isBefore(now));
    }

    private static String cacheKey(Long meId, Long cpId, Long instId, String direction,
                                  String currency, boolean dual) {
        return safeParam(meId) + "|" + safeParam(cpId) + "|" + safeParam(instId) + "|"
                + safeParam(direction) + "|" + safeParam(currency) + "|" + dual;
    }

    private static String safeParam(Object o) {
        return o == null ? "" : o.toString();
    }

    /**
     * 基于数据的 match 端点返回结构为 {@link RuleDualMatchResultVO}（inflow + outflow + cacheHit）。
     * 双方向取 inflow.outflow 任一命中，单方向取对应方向。
     */
    @SuppressWarnings("unchecked")
    private BasedataMatchResult parseMatchResult(java.util.Map<?, ?> data, boolean dualDirection) {
        Long inflowId = extractAccountId((java.util.Map<String, Object>) data.get("inflow"));
        Long outflowId = extractAccountId((java.util.Map<String, Object>) data.get("outflow"));

        BasedataMatchResult r = new BasedataMatchResult();
        r.setInflowBankAccountId(inflowId);
        r.setOutflowBankAccountId(outflowId);
        r.setMatched(inflowId != null || outflowId != null);
        return r;
    }

    private static Long extractAccountId(java.util.Map<String, Object> side) {
        if (side == null) return null;
        Object v = side.get("bankAccountId");
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(v.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 缓存条目 */
    private static final class CacheEntry {
        final BasedataMatchResult result;
        final Instant expiresAt;

        CacheEntry(BasedataMatchResult result, Instant expiresAt) {
            this.result = result;
            this.expiresAt = expiresAt;
        }
    }

    /**
     * match 结果轻量 DTO（避免依赖 basedata VO 形成跨模块循环依赖）。
     */
    public static final class BasedataMatchResult {
        private boolean matched;
        private Long inflowBankAccountId;
        private Long outflowBankAccountId;

        public boolean isMatched() {
            return matched;
        }

        public void setMatched(boolean matched) {
            this.matched = matched;
        }

        public Long getInflowBankAccountId() {
            return inflowBankAccountId;
        }

        public void setInflowBankAccountId(Long inflowBankAccountId) {
            this.inflowBankAccountId = inflowBankAccountId;
        }

        public Long getOutflowBankAccountId() {
            return outflowBankAccountId;
        }

        public void setOutflowBankAccountId(Long outflowBankAccountId) {
            this.outflowBankAccountId = outflowBankAccountId;
        }

        /** 根据方向取账户 ID；FX 双方向场景由调用方分别用 inflow/outflow。 */
        public Long bankAccountIdFor(String direction) {
            if ("Inflow".equalsIgnoreCase(direction)) return inflowBankAccountId;
            if ("Outflow".equalsIgnoreCase(direction)) return outflowBankAccountId;
            return inflowBankAccountId != null ? inflowBankAccountId : outflowBankAccountId;
        }

        public String toShortString() {
            return "matched=" + matched + ",inflow=" + inflowBankAccountId + ",outflow=" + outflowBankAccountId;
        }
    }
}