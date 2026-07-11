package com.opentms.basedata.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 基于数据(CXF)模块的 OpenAPI 暴露端点。
 * <p>
 * 通过 {@link OpenApiCxfScanner} 反射扫描所有 JAX-RS Resource 类,生成 OpenAPI 3.0 规范
 * 并以 JSON 形式返回。前端 Swagger UI 与外部脚本可直接消费。
 * </p>
 *
 * @author Open-TMS
 * @since 2026-07-10
 */
@RestController
public class OpenApiController {

    @Autowired
    private OpenApiCxfScanner scanner;

    private final ObjectMapper jackson = new ObjectMapper();

    /**
     * GET /api/v1/openapi/cxf — 基于数据 OpenAPI JSON(包装在 { module, framework, serverUrl, spec })
     */
    @GetMapping(value = "/api/v1/openapi/cxf", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> cxfOpenApi() {
        OpenAPI openAPI = scanner.scan();
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("module", "basedata");
        resp.put("framework", "cxf");
        resp.put("serverUrl", "/opentms/basedata");
        try {
            // 使用 swagger 自带的 Json 工具(已注册专用 ObjectMapper,会过滤 exampleSetFlag 等)
            String json = Json.pretty(openAPI);
            Object spec = jackson.readValue(json, Object.class);
            resp.put("spec", spec);
        } catch (Exception e) {
            resp.put("error", e.getMessage());
        }
        return ResponseEntity.ok(resp);
    }

    /**
     * GET /api/v1/openapi/cxf-spec — 仅返回 OpenAPI 规范本身(供 Swagger UI urls 选择器直接消费)
     */
    @GetMapping(value = "/api/v1/openapi/cxf-spec", produces = "application/json")
    public String cxfOpenApiSpec() {
        OpenAPI openAPI = scanner.scan();
        return Json.pretty(openAPI);
    }
}