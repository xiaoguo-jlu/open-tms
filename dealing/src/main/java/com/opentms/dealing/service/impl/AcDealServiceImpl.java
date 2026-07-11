package com.opentms.dealing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opentms.dealing.dto.AcDealDTO;
import com.opentms.dealing.dto.AcDealDetailVO;
import com.opentms.dealing.entity.*;
import com.opentms.dealing.mapper.*;
import com.opentms.dealing.service.AcDealService;
import com.opentms.dealing.service.CashflowService;
import com.opentms.dealing.service.DealMapService;
import com.opentms.dealing.service.EntityNameLookup;
import com.opentms.dealing.vo.ActionVO;
import com.opentms.dealing.vo.CashflowVO;
import com.opentms.dealing.vo.DealMapVO;
import com.opentms.dealing.vo.DealVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AC 交易 Service 实现（v2.0 - DealMap 字段精简 + Action 多对一 + 审批仅作用于 Action）
 */
@Service
public class AcDealServiceImpl extends ServiceImpl<DealMapper, Deal> implements AcDealService {

    private static final Logger log = LoggerFactory.getLogger(AcDealServiceImpl.class);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final String DEAL_TYPE_AC = "AC";

    private static final String DEAL_STATUS_NEW = "New";
    private static final String DEAL_STATUS_CANCELED = "Canceled";

    private static final String ACTION_TYPE_CREATE = "CREATE";
    private static final String ACTION_TYPE_UPDATE = "UPDATE";
    private static final String ACTION_TYPE_DELETE = "DELETE";

    private static final String APPROVAL_STATUS_PENDING = "Pending";
    private static final String APPROVAL_STATUS_APPROVED = "Approved";
    private static final String APPROVAL_STATUS_REJECTED = "Rejected";

    private static final String EVENT_TYPE_ACTUAL_CASHFLOW = "ActualCashflow";

    private static final String IMAGE_TYPE_UPDATE = "UPDATE";
    private static final String IMAGE_TYPE_DELETE = "DELETE";

    private final AcDealMapper acDealMapper;
    private final ActionMapper actionMapper;
    private final DealImageMapper dealImageMapper;
    private final AcDealImageMapper acDealImageMapper;
    private final CashflowMapper cashflowMapper;
    private final DealMapService dealMapService;
    private final CashflowService cashflowService;
    /**
     * 现金流镜像服务 (v1.0 - 2026-07-11, 自动写 tms_cashflow_image_t)
     */
    private final com.opentms.dealing.service.CashflowImageService cashflowImageService;
    /**
     * 跨模块关联实体名称查询 (用于 copy 端点补全名称字段, 2026-07-05)
     */
    private final EntityNameLookup entityNameLookup;

    public AcDealServiceImpl(AcDealMapper acDealMapper,
                             ActionMapper actionMapper,
                             DealImageMapper dealImageMapper,
                             AcDealImageMapper acDealImageMapper,
                             CashflowMapper cashflowMapper,
                             @Lazy DealMapService dealMapService,
                             @Lazy CashflowService cashflowService,
                             @Lazy com.opentms.dealing.service.CashflowImageService cashflowImageService,
                             EntityNameLookup entityNameLookup) {
        this.acDealMapper = acDealMapper;
        this.actionMapper = actionMapper;
        this.dealImageMapper = dealImageMapper;
        this.acDealImageMapper = acDealImageMapper;
        this.cashflowMapper = cashflowMapper;
        this.dealMapService = dealMapService;
        this.cashflowService = cashflowService;
        this.cashflowImageService = cashflowImageService;
        this.entityNameLookup = entityNameLookup;
    }

    // ==================== Query ====================

    @Override
    public Page<DealVO> queryPage(String keyword, String status, String direction,
                                  String managementEntity, int pageNum, int pageSize) {
        LambdaQueryWrapper<Deal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Deal::getDealType, DEAL_TYPE_AC);

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Deal::getDealNumber, keyword);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Deal::getStatus, status);
        }
        if (StringUtils.hasText(direction)) {
            wrapper.eq(Deal::getDirection, direction);
        }
        if (StringUtils.hasText(managementEntity)) {
            wrapper.eq(Deal::getManagementEntity, managementEntity);
        }

        wrapper.orderByDesc(Deal::getCreatedAt);

        Page<Deal> page = page(new Page<>(pageNum, pageSize), wrapper);
        Page<DealVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());

        List<DealVO> voList = page.getRecords().stream().map(this::convertToVO).toList();
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public AcDealDetailVO getDetail(Long id) {
        Deal deal = getById(id);
        if (deal == null) {
            return null;
        }
        return buildDetail(deal);
    }

    @Override
    public AcDealDetailVO getDetailByDealNumber(String dealNumber) {
        Deal deal = getByDealNumber(dealNumber);
        if (deal == null) {
            return null;
        }
        return buildDetail(deal);
    }

    private AcDealDetailVO buildDetail(Deal deal) {
        AcDealDetailVO vo = new AcDealDetailVO();
        BeanUtils.copyProperties(deal, vo);

        // AC Deal
        AcDeal acDeal = getAcDealByDealNumber(deal.getDealNumber());
        if (acDeal != null) {
            vo.setBankAccountId(acDeal.getBankAccountId());
            vo.setCounterpartyAccountId(acDeal.getCounterpartyAccountId());
            vo.setPaymentMethod(acDeal.getPaymentMethod());
        }

        // DealMap 时间线
        List<DealMapVO> dealMapList = dealMapService.listByDealNumber(deal.getDealNumber());
        vo.setDealMapList(dealMapList);

        // Cashflow 列表（按 deal_number 查询，因为 v2.0 通过 dealmap_number 关联，但用户可从 deal 视角查询）
        List<CashflowVO> cashflowList = cashflowService.listByDealNumber(deal.getDealNumber());
        vo.setCashflowList(cashflowList);

        // Action 列表（v2.0 多 Action/Deal）
        List<ActionVO> actionList = listActionsByDealNumber(deal.getDealNumber());
        vo.setActionList(actionList);

        // ===== 2026-07-05: 补全关联实体名称 (前端 BaseDataPicker preloadRow 需求) =====
        // 复用 getCopyData() 同样的 EntityNameLookup 逻辑,但写到 AcDealDetailVO 上
        populateEntityNames(vo, deal, acDeal);

        return vo;
    }

    /**
     * 详情响应中补全 *Name 字段 (跨模块 JdbcTemplate, 失败容错: 单个 lookup 失败不影响其他字段)
     * 注: AcDealDetailVO.managementEntity 是 code 字符串(非 ID),无对应 Name 字段,跳过
     */
    private void populateEntityNames(AcDealDetailVO vo, Deal deal, AcDeal acDeal) {
        // counterparty
        try {
            Map<String, Object> cp = entityNameLookup.findCounterparty(deal.getCounterpartyId());
            vo.setCounterpartyName(nameOf(cp));
        } catch (Exception ignore) {
        }
        // trader
        try {
            Map<String, Object> tr = entityNameLookup.findTrader(deal.getTraderId());
            vo.setTraderName(nameOf(tr));
        } catch (Exception ignore) {
        }
        // instrument
        try {
            Map<String, Object> inst = entityNameLookup.findInstrument(deal.getInstrumentId());
            vo.setInstrumentName(instrumentDisplay(inst));
        } catch (Exception ignore) {
        }
        // bankAccount
        if (acDeal != null) {
            try {
                Map<String, Object> ba = entityNameLookup.findBankAccount(acDeal.getBankAccountId());
                vo.setBankAccountName(bankAccountDisplay(ba));
            } catch (Exception ignore) {
            }
            try {
                Map<String, Object> ca = entityNameLookup.findCounterpartyAccount(acDeal.getCounterpartyAccountId());
                vo.setCounterpartyAccountName(bankAccountDisplay(ca));
            } catch (Exception ignore) {
            }
        }
    }

    // (公用 nameOf / instrumentDisplay / bankAccountDisplay 已声明于下方 getCopyData 附近)

    // ==================== v2.0 Create ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createAcDeal(AcDealDTO dto) {
        validateAcDealDTO(dto);

        LocalDateTime now = LocalDateTime.now();
        String dealNumber = generateDealNumber();
        String actionNumber = generateActionNumber();

        // ① INSERT Action(CREATE)
        Action action = new Action();
        action.setActionNumber(actionNumber);
        action.setDealNumber(dealNumber);
        action.setDealType(DEAL_TYPE_AC);
        action.setActionType(ACTION_TYPE_CREATE);
        action.setActionStatus(APPROVAL_STATUS_PENDING);
        action.setOperator(dto.getOperator());
        action.setOperateAt(now);
        action.setApprovalStatus1(APPROVAL_STATUS_PENDING);
        action.setApprovalStatus2(APPROVAL_STATUS_PENDING);
        action.setCreatedBy(dto.getOperator());
        action.setCreatedAt(now);
        action.setVersion(1);
        actionMapper.insert(action);

        // ② INSERT Deal
        Deal deal = new Deal();
        BeanUtils.copyProperties(dto, deal);
        deal.setDealNumber(dealNumber);
        deal.setDealType(DEAL_TYPE_AC);
        deal.setStatus(DEAL_STATUS_NEW);
        deal.setLatestActionNumber(actionNumber);
        deal.setVersion(1);
        deal.setCreatedBy(dto.getOperator());
        deal.setCreatedAt(now);
        deal.setUpdatedBy(dto.getOperator());
        deal.setUpdatedAt(now);
        save(deal);

        // ③ INSERT AcDeal
        AcDeal acDeal = new AcDeal();
        acDeal.setDealNumber(dealNumber);
        acDeal.setBankAccountId(dto.getBankAccountId());
        acDeal.setCounterpartyAccountId(dto.getCounterpartyAccountId());
        acDeal.setPaymentMethod(dto.getPaymentMethod());
        acDeal.setCreatedBy(dto.getOperator());
        acDeal.setCreatedAt(now);
        acDeal.setUpdatedBy(dto.getOperator());
        acDeal.setUpdatedAt(now);
        acDeal.setVersion(1);
        acDealMapper.insert(acDeal);

        // ④ ✅ INSERT DealMap(ActualCashflow) - 自动创建
        String dealMapNumber = dealMapService.generateDealMapNumber();
        DealMap dealMap = new DealMap();
        dealMap.setDealmapNumber(dealMapNumber);
        dealMap.setDealNumber(dealNumber);
        dealMap.setActionNumber(actionNumber);
        dealMap.setEventType(EVENT_TYPE_ACTUAL_CASHFLOW);
        dealMap.setEventStatus("Active");
        dealMap.setAmount(dto.getAmount());
        dealMap.setCurrency(dto.getCurrency());
        dealMap.setDirection(dto.getDirection());
        dealMap.setEventDate(dto.getValueDate() != null ? dto.getValueDate() : dto.getDealDate());
        dealMap.setValueDate(dto.getValueDate());
        dealMap.setIsReversal("0");
        dealMap.setDescription("AC Deal created - actual cashflow event");
        dealMap.setCreatedBy(dto.getOperator());
        dealMap.setCreatedAt(now);
        dealMap.setVersion(1);
        dealMapService.save(dealMap);

        // ⑤ ✅ INSERT Cashflow - 自动创建（dealmap_number 关联）
        String cflowNumber = cashflowService.generateCflowNumber();
        Cashflow cashflow = new Cashflow();
        cashflow.setCflowNumber(cflowNumber);
        cashflow.setDealNumber(dealNumber);
        cashflow.setDealmapNumber(dealMapNumber);
        cashflow.setManagementEntity(dto.getManagementEntity());
        cashflow.setBankAccount(dto.getBankAccountId() != null ? String.valueOf(dto.getBankAccountId()) : null);
        cashflow.setCounterpartyAccount(dto.getCounterpartyAccountId() != null ? String.valueOf(dto.getCounterpartyAccountId()) : null);
        // v1.0: 直接写入 v1.1 规则匹配的 ID（DTO 已通过 BankAccountLookup 解析过,避免再触发 HTTP 规则匹配）
        cashflow.setBankAccountId(dto.getBankAccountId());
        cashflow.setCounterpartyBankAccountId(dto.getCounterpartyAccountId());
        cashflow.setDirection(dto.getDirection());
        cashflow.setAmount(dto.getAmount());
        cashflow.setCurrency(dto.getCurrency());
        cashflow.setCflowDate(dto.getValueDate() != null ? dto.getValueDate() : dto.getDealDate());
        cashflow.setValueDate(dto.getValueDate());
        cashflow.setSourceType("AC_DEAL");
        cashflow.setSourceRef(dealNumber);
        cashflow.setStatus("Created");
        cashflow.setCreatedBy(dto.getOperator());
        cashflow.setCreatedAt(now);
        cashflow.setVersion(1);
        cashflowService.save(cashflow);

        // ⑤b v1.0: 写 CREATE 镜像（@Transactional 整体回滚,镜像失败 → AC 创建失败）
        try {
            cashflowImageService.append(cashflow, "CREATE");
        } catch (RuntimeException e) {
            log.error("[AcDealService] CREATE 镜像写入失败,事务回滚: cflowNumber={}, err={}",
                    cashflow.getCflowNumber(), e.getMessage(), e);
            throw e;
        }

        // ⑥ ❌ CREATE 不生成 DealImage

        return true;
    }

    // ==================== v2.0 Update ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAcDeal(AcDealDTO dto) {
        if (!StringUtils.hasText(dto.getDealNumber())) {
            throw new IllegalArgumentException("dealNumber 不能为空");
        }
        validateAcDealDTO(dto);

        LocalDateTime now = LocalDateTime.now();
        String dealNumber = dto.getDealNumber();

        Deal existingDeal = getByDealNumber(dealNumber);
        if (existingDeal == null) {
            throw new RuntimeException("Deal not found: " + dealNumber);
        }
        AcDeal existingAcDeal = getAcDealByDealNumber(dealNumber);
        if (existingAcDeal == null) {
            throw new RuntimeException("AC Deal not found: " + dealNumber);
        }

        int newVersion = existingDeal.getVersion() + 1;

        // ① INSERT Action(UPDATE) - 新独立 Action
        String actionNumber = generateActionNumber();
        Action action = new Action();
        action.setActionNumber(actionNumber);
        action.setDealNumber(dealNumber);
        action.setDealType(DEAL_TYPE_AC);
        action.setActionType(ACTION_TYPE_UPDATE);
        action.setActionStatus(APPROVAL_STATUS_PENDING);
        action.setOperator(dto.getOperator());
        action.setOperateAt(now);
        action.setApprovalStatus1(APPROVAL_STATUS_PENDING);
        action.setApprovalStatus2(APPROVAL_STATUS_PENDING);
        action.setRemark(dto.getRemark());
        action.setCreatedBy(dto.getOperator());
        action.setCreatedAt(now);
        action.setVersion(1);
        actionMapper.insert(action);

        // ② INSERT DealImage(v+1) - 记录修改前旧值
        String oldImageNumber = generateImageNumber();
        DealImage oldImage = new DealImage();
        oldImage.setImageNumber(oldImageNumber);
        oldImage.setDealNumber(dealNumber);
        oldImage.setDealType(existingDeal.getDealType());
        oldImage.setVersion(newVersion);
        BeanUtils.copyProperties(existingDeal, oldImage);
        oldImage.setId(null); // 避免主键冲突
        oldImage.setImageType(IMAGE_TYPE_UPDATE);
        oldImage.setOperator(dto.getOperator());
        oldImage.setOperateAt(now);
        oldImage.setCreatedBy(dto.getOperator());
        oldImage.setCreatedAt(now);
        dealImageMapper.insert(oldImage);

        // ③ INSERT AcDealImage(v+1) - 记录修改前旧值
        String oldAcImageNumber = generateImageNumber();
        AcDealImage oldAcImage = new AcDealImage();
        oldAcImage.setImageNumber(oldAcImageNumber);
        oldAcImage.setDealNumber(dealNumber);
        oldAcImage.setVersion(newVersion);
        BeanUtils.copyProperties(existingAcDeal, oldAcImage);
        oldAcImage.setId(null); // 避免主键冲突
        oldAcImage.setImageType(IMAGE_TYPE_UPDATE);
        oldAcImage.setOperator(dto.getOperator());
        oldAcImage.setOperateAt(now);
        oldAcImage.setCreatedBy(dto.getOperator());
        oldAcImage.setCreatedAt(now);
        acDealImageMapper.insert(oldAcImage);

        // ④ UPDATE Deal
        BeanUtils.copyProperties(dto, existingDeal);
        existingDeal.setUpdatedBy(dto.getOperator());
        existingDeal.setUpdatedAt(now);
        existingDeal.setVersion(newVersion);
        existingDeal.setLatestActionNumber(actionNumber);
        updateById(existingDeal);

        // ⑤ UPDATE AcDeal
        BeanUtils.copyProperties(dto, existingAcDeal);
        existingAcDeal.setUpdatedBy(dto.getOperator());
        existingAcDeal.setUpdatedAt(now);
        existingAcDeal.setVersion(newVersion);
        acDealMapper.updateById(existingAcDeal);

        // ⑥ 步骤A：先获取旧 DealMap 的编号（在软删之前）
        String oldDealMapNumber = getLatestActiveDealMapNumber(dealNumber);

        // ⑦ ✅ 软删除旧 DealMap
        dealMapService.softDeleteByDealNumber(dealNumber);

        // ⑧ ✅ INSERT 新 DealMap(ActualCashflow) - 关联新 Action
        String newDealMapNumber = dealMapService.generateDealMapNumber();
        DealMap newDealMap = new DealMap();
        newDealMap.setDealmapNumber(newDealMapNumber);
        newDealMap.setDealNumber(dealNumber);
        newDealMap.setActionNumber(actionNumber);
        newDealMap.setEventType(EVENT_TYPE_ACTUAL_CASHFLOW);
        newDealMap.setEventStatus("Active");
        newDealMap.setAmount(dto.getAmount());
        newDealMap.setCurrency(dto.getCurrency());
        newDealMap.setDirection(dto.getDirection());
        newDealMap.setEventDate(dto.getValueDate() != null ? dto.getValueDate() : dto.getDealDate());
        newDealMap.setValueDate(dto.getValueDate());
        newDealMap.setIsReversal("0");
        newDealMap.setDescription("AC Deal updated - new actual cashflow event");
        newDealMap.setCreatedBy(dto.getOperator());
        newDealMap.setCreatedAt(now);
        newDealMap.setVersion(1);
        dealMapService.save(newDealMap);

        // ⑨ ✅ UPDATE Cashflow - 指向新 DealMap
        if (StringUtils.hasText(oldDealMapNumber)) {
            // v1.0: 先写 DELETE 镜像（旧 cashflow 改前快照）
            List<Cashflow> oldCashflows = cashflowMapper.selectList(
                    new LambdaQueryWrapper<Cashflow>()
                            .eq(Cashflow::getDealmapNumber, oldDealMapNumber)
                            .eq(Cashflow::getDeleted, "0"));
            for (Cashflow oldCf : oldCashflows) {
                try {
                    cashflowImageService.append(oldCf, "DELETE");
                } catch (RuntimeException e) {
                    log.error("[AcDealService] UPDATE 镜像(DELETE)写入失败,事务回滚: cflowNumber={}, err={}",
                            oldCf.getCflowNumber(), e.getMessage(), e);
                    throw e;
                }
                // 软删旧 cashflow
                Cashflow delCf = new Cashflow();
                delCf.setId(oldCf.getId());
                delCf.setDeleted("1");
                delCf.setUpdatedBy(dto.getOperator());
                delCf.setUpdatedAt(now);
                delCf.setVersion(oldCf.getVersion() != null ? oldCf.getVersion() + 1 : 1);
                cashflowMapper.updateById(delCf);
            }
            // INSERT 新 cashflow (改后)
            Cashflow newCf = new Cashflow();
            newCf.setCflowNumber(cashflowService.generateCflowNumber());
            newCf.setDealNumber(dealNumber);
            newCf.setDealmapNumber(newDealMapNumber);
            newCf.setManagementEntity(dto.getManagementEntity());
            newCf.setBankAccount(dto.getBankAccountId() != null ? String.valueOf(dto.getBankAccountId()) : null);
            newCf.setCounterpartyAccount(dto.getCounterpartyAccountId() != null ? String.valueOf(dto.getCounterpartyAccountId()) : null);
            newCf.setBankAccountId(dto.getBankAccountId());
            newCf.setCounterpartyBankAccountId(dto.getCounterpartyAccountId());
            newCf.setDirection(dto.getDirection());
            newCf.setAmount(dto.getAmount());
            newCf.setCurrency(dto.getCurrency());
            newCf.setCflowDate(dto.getValueDate() != null ? dto.getValueDate() : dto.getDealDate());
            newCf.setValueDate(dto.getValueDate());
            newCf.setSourceType("AC_DEAL");
            newCf.setSourceRef(dealNumber);
            newCf.setStatus("Created");
            newCf.setCreatedBy(dto.getOperator());
            newCf.setCreatedAt(now);
            newCf.setUpdatedBy(dto.getOperator());
            newCf.setUpdatedAt(now);
            newCf.setVersion(1);
            newCf.setDeleted("0");
            cashflowService.save(newCf);
            // v1.0: 写 CREATE 镜像
            try {
                cashflowImageService.append(newCf, "CREATE");
            } catch (RuntimeException e) {
                log.error("[AcDealService] UPDATE 镜像(CREATE)写入失败,事务回滚: cflowNumber={}, err={}",
                        newCf.getCflowNumber(), e.getMessage(), e);
                throw e;
            }
        }

        return true;
    }

    /**
     * 获取 Deal 当前最新 Active DealMap 的编号
     */
    private String getLatestActiveDealMapNumber(String dealNumber) {
        LambdaQueryWrapper<DealMap> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DealMap::getDealNumber, dealNumber)
               .eq(DealMap::getDeleted, "0")
               .orderByDesc(DealMap::getCreatedAt)
               .last("LIMIT 1");
        DealMap latest = dealMapService.getOne(wrapper);
        return latest != null ? latest.getDealmapNumber() : null;
    }

    // ==================== v2.0 Delete ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAcDeal(Long id) {
        Deal deal = getById(id);
        if (deal == null) {
            throw new RuntimeException("Deal not found: " + id);
        }

        LocalDateTime now = LocalDateTime.now();
        String dealNumber = deal.getDealNumber();
        int newVersion = deal.getVersion() + 1;

        // ① INSERT Action(DELETE) - 独立 Action
        String actionNumber = generateActionNumber();
        Action action = new Action();
        action.setActionNumber(actionNumber);
        action.setDealNumber(dealNumber);
        action.setDealType(DEAL_TYPE_AC);
        action.setActionType(ACTION_TYPE_DELETE);
        action.setActionStatus(APPROVAL_STATUS_PENDING);
        action.setOperator("system");
        action.setOperateAt(now);
        action.setApprovalStatus1(APPROVAL_STATUS_PENDING);
        action.setApprovalStatus2(APPROVAL_STATUS_PENDING);
        action.setCreatedBy("system");
        action.setCreatedAt(now);
        action.setVersion(1);
        actionMapper.insert(action);

        // ② INSERT DealImage(v+1) - 记录删除前完整状态
        String imageNumber = generateImageNumber();
        DealImage image = new DealImage();
        image.setImageNumber(imageNumber);
        image.setDealNumber(dealNumber);
        image.setDealType(deal.getDealType());
        image.setVersion(newVersion);
        BeanUtils.copyProperties(deal, image);
        image.setId(null); // 避免主键冲突，让 DB 自增
        image.setImageType(IMAGE_TYPE_DELETE);
        image.setOperator("system");
        image.setOperateAt(now);
        image.setCreatedBy("system");
        image.setCreatedAt(now);
        dealImageMapper.insert(image);

        // ③ 软删除 Deal
        deal.setDeleted("1");
        deal.setStatus(DEAL_STATUS_CANCELED);
        deal.setUpdatedBy("system");
        deal.setUpdatedAt(now);
        deal.setVersion(newVersion);
        deal.setLatestActionNumber(actionNumber);
        updateById(deal);

        // ④ 软删除 AcDeal
        AcDeal acDeal = getAcDealByDealNumber(dealNumber);
        if (acDeal != null) {
            acDeal.setDeleted("1");
            acDeal.setUpdatedBy("system");
            acDeal.setUpdatedAt(now);
            acDeal.setVersion(newVersion);
            acDealMapper.updateById(acDeal);
        }

        // ⑤ ✅ 级联软删除 DealMap
        List<DealMapVO> activeDealMaps = dealMapService.listByDealNumber(dealNumber);
        dealMapService.softDeleteByDealNumber(dealNumber);

        // ⑥ ✅ 级联软删除 Cashflow (v1.0: 先写 DELETE 镜像再软删)
        for (DealMapVO dm : activeDealMaps) {
            List<Cashflow> dms = cashflowMapper.selectList(
                    new LambdaQueryWrapper<Cashflow>()
                            .eq(Cashflow::getDealmapNumber, dm.getDealmapNumber())
                            .eq(Cashflow::getDeleted, "0"));
            for (Cashflow cf : dms) {
                try {
                    cashflowImageService.append(cf, "DELETE");
                } catch (RuntimeException e) {
                    log.error("[AcDealService] DELETE 镜像写入失败,事务回滚: cflowNumber={}, err={}",
                            cf.getCflowNumber(), e.getMessage(), e);
                    throw e;
                }
            }
            cashflowService.softDeleteByDealMapNumber(dm.getDealmapNumber());
        }

        return true;
    }

    // ==================== v2.0 Approve / Reject ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveAction(String actionNumber, String approver, String approvalRemark) {
        if (!StringUtils.hasText(actionNumber)) {
            throw new IllegalArgumentException("actionNumber 不能为空");
        }
        if (!StringUtils.hasText(approver)) {
            throw new IllegalArgumentException("approver 不能为空");
        }

        Action action = getActionByActionNumber(actionNumber);
        if (action == null) {
            throw new RuntimeException("Action not found: " + actionNumber);
        }

        // ⚠️ 关键：审批仅作用于 Action，DealMap / Cashflow 状态不变
        action.setApprover1(approver);
        action.setApprovalStatus1(APPROVAL_STATUS_APPROVED);
        if (StringUtils.hasText(approvalRemark)) {
            action.setApprovalRemark(approvalRemark);
        }
        action.setOperator(approver);
        action.setUpdatedBy(approver);
        action.setUpdatedAt(LocalDateTime.now());
        action.setVersion(action.getVersion() + 1);
        actionMapper.updateById(action);

        // 更新 Deal 的 latestActionNumber 与版本号
        Deal deal = getByDealNumber(action.getDealNumber());
        if (deal != null) {
            deal.setLatestActionNumber(action.getActionNumber());
            deal.setUpdatedBy(approver);
            deal.setUpdatedAt(LocalDateTime.now());
            deal.setVersion(deal.getVersion() + 1);
            updateById(deal);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rejectAction(String actionNumber, String approver, String approvalRemark) {
        if (!StringUtils.hasText(actionNumber)) {
            throw new IllegalArgumentException("actionNumber 不能为空");
        }
        if (!StringUtils.hasText(approver)) {
            throw new IllegalArgumentException("approver 不能为空");
        }
        if (!StringUtils.hasText(approvalRemark)) {
            throw new IllegalArgumentException("驳回时审批意见必填");
        }

        Action action = getActionByActionNumber(actionNumber);
        if (action == null) {
            throw new RuntimeException("Action not found: " + actionNumber);
        }

        // ⚠️ 关键：审批仅作用于 Action，DealMap / Cashflow 状态不变
        action.setApprover1(approver);
        action.setApprovalStatus1(APPROVAL_STATUS_REJECTED);
        action.setApprovalRemark(approvalRemark);
        action.setOperator(approver);
        action.setUpdatedBy(approver);
        action.setUpdatedAt(LocalDateTime.now());
        action.setVersion(action.getVersion() + 1);
        actionMapper.updateById(action);

        Deal deal = getByDealNumber(action.getDealNumber());
        if (deal != null) {
            deal.setLatestActionNumber(action.getActionNumber());
            deal.setUpdatedBy(approver);
            deal.setUpdatedAt(LocalDateTime.now());
            deal.setVersion(deal.getVersion() + 1);
            updateById(deal);
        }

        return true;
    }

    // ==================== Helper Methods ====================

    private void validateAcDealDTO(AcDealDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("DTO 不能为空");
        }
        if (!StringUtils.hasText(dto.getManagementEntity())) {
            throw new IllegalArgumentException("managementEntity 不能为空");
        }
        if (dto.getAmount() == null || dto.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("amount 必须大于 0");
        }
        if (!StringUtils.hasText(dto.getCurrency())) {
            throw new IllegalArgumentException("currency 不能为空");
        }
        if (!StringUtils.hasText(dto.getDirection())) {
            throw new IllegalArgumentException("direction 不能为空");
        }
        if (!"Inflow".equals(dto.getDirection()) && !"Outflow".equals(dto.getDirection())) {
            throw new IllegalArgumentException("direction 必须为 Inflow / Outflow");
        }
        if (dto.getDealDate() == null) {
            throw new IllegalArgumentException("dealDate 不能为空");
        }
        if (dto.getValueDate() == null) {
            throw new IllegalArgumentException("valueDate 不能为空");
        }
        if (dto.getValueDate().isBefore(dto.getDealDate())) {
            throw new IllegalArgumentException("valueDate 不能早于 dealDate");
        }
        if (dto.getBankAccountId() == null) {
            throw new IllegalArgumentException("bankAccountId 不能为空");
        }
        if (!StringUtils.hasText(dto.getOperator())) {
            throw new IllegalArgumentException("operator 不能为空");
        }
    }

    private Deal getByDealNumber(String dealNumber) {
        LambdaQueryWrapper<Deal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Deal::getDealNumber, dealNumber);
        return getOne(wrapper);
    }

    private AcDeal getAcDealByDealNumber(String dealNumber) {
        LambdaQueryWrapper<AcDeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AcDeal::getDealNumber, dealNumber);
        return acDealMapper.selectOne(wrapper);
    }

    private Action getActionByActionNumber(String actionNumber) {
        LambdaQueryWrapper<Action> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Action::getActionNumber, actionNumber);
        return actionMapper.selectOne(wrapper);
    }

    private List<ActionVO> listActionsByDealNumber(String dealNumber) {
        LambdaQueryWrapper<Action> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Action::getDealNumber, dealNumber)
               .orderByAsc(Action::getCreatedAt);
        return actionMapper.selectList(wrapper).stream().map(this::convertActionToVO).toList();
    }

    private ActionVO convertActionToVO(Action action) {
        ActionVO vo = new ActionVO();
        BeanUtils.copyProperties(action, vo);
        return vo;
    }

    private DealVO convertToVO(Deal deal) {
        DealVO vo = new DealVO();
        BeanUtils.copyProperties(deal, vo);
        AcDeal acDeal = getAcDealByDealNumber(deal.getDealNumber());
        if (acDeal != null) {
            vo.setBankAccountId(acDeal.getBankAccountId());
            vo.setCounterpartyAccountId(acDeal.getCounterpartyAccountId());
            vo.setPaymentMethod(acDeal.getPaymentMethod());
        }
        return vo;
    }

    private String generateDealNumber() {
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        String prefix = "AC" + dateStr;
        LambdaQueryWrapper<Deal> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(Deal::getDealNumber, prefix)
               .orderByDesc(Deal::getDealNumber)
               .last("LIMIT 1");
        Deal last = getOne(wrapper);
        int seq = 1;
        if (last != null && last.getDealNumber() != null
                && last.getDealNumber().length() > prefix.length()) {
            try {
                String lastSeqStr = last.getDealNumber().substring(prefix.length());
                seq = Integer.parseInt(lastSeqStr) + 1;
            } catch (NumberFormatException ignored) {
            }
        }
        return prefix + String.format("%04d", seq);
    }

    private String generateActionNumber() {
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        String prefix = "ACT" + dateStr;
        LambdaQueryWrapper<Action> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(Action::getActionNumber, prefix)
               .orderByDesc(Action::getActionNumber)
               .last("LIMIT 1");
        Action last = actionMapper.selectOne(wrapper);
        int seq = 1;
        if (last != null && last.getActionNumber() != null
                && last.getActionNumber().length() > prefix.length()) {
            try {
                String lastSeqStr = last.getActionNumber().substring(prefix.length());
                seq = Integer.parseInt(lastSeqStr) + 1;
            } catch (NumberFormatException ignored) {
            }
        }
        return prefix + String.format("%04d", seq);
    }

    // ==================== Copy ====================

    /**
     * 复制 AC 交易 (v2.1 - 2026-07-05: 补全关联实体名称)
     * <p>问题: 旧版只返回 ID，前端 BaseDataPicker 只能显示 ID 数字。</p>
     * <p>修复: 利用 EntityNameLookup (跨模块 JdbcTemplate) 查询名称并填入 DTO,
     * 前端在 copy 模式下用 preloadRow 传给 Picker 直接展示名称。</p>
     */
    @Override
    public AcDealDTO getCopyData(String dealNumber) {
        Deal deal = getByDealNumber(dealNumber);
        if (deal == null) {
            return null;
        }

        AcDeal acDeal = getAcDealByDealNumber(dealNumber);

        AcDealDTO dto = new AcDealDTO();
        // 不复制 id, dealNumber — 系统自动生成新编号
        dto.setDealType(DEAL_TYPE_AC);
        dto.setManagementEntity(deal.getManagementEntity());
        dto.setCounterpartyId(deal.getCounterpartyId());
        dto.setInstrumentId(deal.getInstrumentId());
        dto.setTraderId(deal.getTraderId());
        dto.setDirection(deal.getDirection());
        dto.setAmount(deal.getAmount());
        dto.setCurrency(deal.getCurrency());
        dto.setDealDate(deal.getDealDate());
        dto.setValueDate(deal.getValueDate());
        dto.setDescription(deal.getDescription());
        dto.setRemark(deal.getRemark());

        if (acDeal != null) {
            dto.setBankAccountId(acDeal.getBankAccountId());
            dto.setCounterpartyAccountId(acDeal.getCounterpartyAccountId());
            dto.setPaymentMethod(acDeal.getPaymentMethod());
        }

        // ===== v2.1: 补全关联实体名称 (跨模块 JdbcTemplate) =====
        // 用 managedEntity code 查名称 (Deal.managementEntity 是 code 字符串)
        if (StringUtils.hasText(deal.getManagementEntity())) {
            Map<String, Object> me = entityNameLookup.findManagementEntityByCode(deal.getManagementEntity());
            if (me != null) dto.setManagementEntityName(nameOf(me));
        }
        Map<String, Object> cp = entityNameLookup.findCounterparty(deal.getCounterpartyId());
        if (cp != null) dto.setCounterpartyName(nameOf(cp));

        Map<String, Object> inst = entityNameLookup.findInstrument(deal.getInstrumentId());
        if (inst != null) dto.setInstrumentName(instrumentDisplay(inst));

        Map<String, Object> tr = entityNameLookup.findTrader(deal.getTraderId());
        if (tr != null) dto.setTraderName(nameOf(tr));

        Map<String, Object> ba = entityNameLookup.findBankAccount(acDeal != null ? acDeal.getBankAccountId() : null);
        if (ba != null) dto.setBankAccountName(bankAccountDisplay(ba));

        Map<String, Object> ca = entityNameLookup.findCounterpartyAccount(acDeal != null ? acDeal.getCounterpartyAccountId() : null);
        if (ca != null) dto.setCounterpartyAccountName(bankAccountDisplay(ca));

        // operator 留空，让用户自行填写
        dto.setOperator("");

        return dto;
    }

    /** "code (name)" 形式展示 (Picker.displayFormat 风格) */
    private static String nameOf(Map<String, Object> row) {
        if (row == null) return null;
        Object code = row.get("code");
        Object name = row.get("name");
        StringBuilder sb = new StringBuilder();
        if (code != null && !code.toString().isEmpty()) sb.append(code);
        if (name != null && !name.toString().isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append("(").append(name).append(")");
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    /** 金融工具展示: "code (name)" */
    private static String instrumentDisplay(Map<String, Object> row) {
        if (row == null) return null;
        Object code = row.get("instrumentCode");
        Object name = row.get("instrumentName");
        StringBuilder sb = new StringBuilder();
        if (code != null && !code.toString().isEmpty()) sb.append(code);
        if (name != null && !name.toString().isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append("(").append(name).append(")");
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    /** 银行账户/对手方账户展示: "accountNo (accountName)" */
    private static String bankAccountDisplay(Map<String, Object> row) {
        if (row == null) return null;
        Object no = row.get("accountNo");
        Object name = row.get("accountName");
        StringBuilder sb = new StringBuilder();
        if (no != null && !no.toString().isEmpty()) sb.append(no);
        if (name != null && !name.toString().isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append("(").append(name).append(")");
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    private String generateImageNumber() {
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        String prefix = "IMG" + dateStr;
        LambdaQueryWrapper<DealImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(DealImage::getImageNumber, prefix)
               .orderByDesc(DealImage::getImageNumber)
               .last("LIMIT 1");
        DealImage last = dealImageMapper.selectOne(wrapper);
        int seq = 1;
        if (last != null && last.getImageNumber() != null
                && last.getImageNumber().length() > prefix.length()) {
            try {
                String lastSeqStr = last.getImageNumber().substring(prefix.length());
                seq = Integer.parseInt(lastSeqStr) + 1;
            } catch (NumberFormatException ignored) {
            }
        }
        return prefix + String.format("%04d", seq);
    }
}
