package com.opentms.basedata.config;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.responses.ApiResponse;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 基于数据(CXF)模块的 OpenAPI 扫描器。
 * <p>
 * 在 Spring Boot 启动时反射扫描 {@code com.opentms.basedata.controller}
 * 包下所有带 {@link Path} 注解的 JAX-RS Resource 类,提取:
 * <ul>
 *   <li>类级 {@code @Path} 作为 prefix</li>
 *   <li>方法级 {@code @GET / @POST / @PUT / @DELETE} + {@code @Path} 作为 Operation</li>
 *   <li>{@code @PathParam / @QueryParam / @DefaultValue} 作为 Parameter</li>
 *   <li>无 JAX-RS 注解的 Java Bean 参数推断为 {@code @RequestBody}</li>
 *   <li>返回类型基于 {@code Result<T>} 反推 T</li>
 * </ul>
 * 扫描结果以 {@code /api/v1/openapi/cxf} (JSON) 端点暴露(由 {@link OpenApiController} 提供)。
 * </p>
 *
 * @author Open-TMS
 * @since 2026-07-10
 */
@Component
public class OpenApiCxfScanner {

    private static final String SCAN_PACKAGE = "com.opentms.basedata.controller";

    @Autowired
    private ApplicationContext applicationContext;

    private volatile OpenAPI cachedOpenAPI;
    private volatile long lastScannedAt;

    /**
     * 启动时或第一次请求时执行扫描;缓存结果,避免每次都重新反射。
     */
    public synchronized OpenAPI scan() {
        long now = System.currentTimeMillis();
        if (cachedOpenAPI != null && (now - lastScannedAt) < 60_000L) {
            return cachedOpenAPI;
        }

        OpenAPI openAPI = new OpenAPI();
        openAPI.setInfo(new Info()
                .title("Open-TMS Basedata API")
                .description("基于数据模块 — 银行账户 / 币种 / 交易对手 / 资金主体 / 金融工具 / 默认银行账户规则")
                .version("1.0.0"));

        Set<Class<?>> resourceClasses = scanPackage(SCAN_PACKAGE);

        for (Class<?> clazz : resourceClasses) {
            try {
                scanResource(clazz, openAPI);
            } catch (Exception e) {
                // 单个 Resource 失败不影响整体
                System.err.println("[OpenApiCxfScanner] failed to scan " + clazz.getName() + ": " + e.getMessage());
            }
        }

        cachedOpenAPI = openAPI;
        lastScannedAt = now;
        return openAPI;
    }

    /**
     * 扫描指定包下的所有类,找出带 {@link Path} 注解的 Resource 类。
     * 基于 Spring 已加载的 Bean(因 {@code @Component} 注解,所有 Resource 都已被 Spring 管理)。
     */
    private Set<Class<?>> scanPackage(String packageName) {
        Set<Class<?>> result = new HashSet<>();
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Path.class);
        for (Object bean : beans.values()) {
            Class<?> clazz = bean.getClass();
            // CGLIB proxy 子类的 getSuperclass 才是真实类
            Class<?> target = clazz;
            while (target != null && target.getName().contains("$$")) {
                target = target.getSuperclass();
            }
            if (target != null && target.getPackage() != null
                    && target.getPackage().getName().startsWith(packageName)) {
                result.add(target);
            }
        }
        return result;
    }

    private void scanResource(Class<?> clazz, OpenAPI openAPI) {
        Path classPath = clazz.getAnnotation(Path.class);
        if (classPath == null) {
            return;
        }
        String prefix = normalize(classPath.value());

        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            String httpMethod = extractHttpMethod(method);
            if (httpMethod == null) {
                continue;
            }
            Path methodPath = method.getAnnotation(Path.class);
            String fullPath = prefix + (methodPath != null ? normalize(methodPath.value()) : "");
            fullPath = fullPath.replaceAll("//+", "/");

            PathItem pathItem = openAPI.getPaths() != null ? openAPI.getPaths().get(fullPath) : null;
            if (pathItem == null) {
                pathItem = new PathItem();
            }

            Operation operation = new Operation();
            operation.setOperationId(method.getName());
            operation.setSummary(deriveSummary(method));
            operation.setDescription(method.getName());

            // ★ 注解优先:@Operation / @Tag / @Tags 覆盖默认 summary/description
            applyOperationAnnotation(method, operation);

            // 请求参数(支持 @Parameter 注解描述/example/schema)
            scanParameters(method, operation);

            // 请求体:无注解的 Java Bean 参数 → requestBody
            scanRequestBody(method, operation);

            // 响应(优先 @ApiResponse 注解,否则基于 Result<T> 反推)
            scanResponses(method, operation);

            applyHttpMethod(pathItem, httpMethod, operation);
            openAPI.path(fullPath, pathItem);
        }
    }

    /**
     * 读取方法上的 {@code @Operation} / 类上的 {@code @Tag / @Tags}
     * 覆盖默认 summary / description / tags。
     */
    private void applyOperationAnnotation(Method method, Operation operation) {
        io.swagger.v3.oas.annotations.Operation ann =
                method.getAnnotation(io.swagger.v3.oas.annotations.Operation.class);
        if (ann != null) {
            if (!ann.summary().isEmpty()) operation.setSummary(ann.summary());
            if (!ann.description().isEmpty()) operation.setDescription(ann.description());
            if (!ann.operationId().isEmpty()) operation.setOperationId(ann.operationId());
            if (ann.tags() != null && ann.tags().length > 0) {
                operation.setTags(new java.util.ArrayList<>(java.util.Arrays.asList(ann.tags())));
            }
            if (ann.deprecated()) operation.setDeprecated(true);
        }
        // 类级 @Tag(s) → 默认 tag
        Tag classTag = method.getDeclaringClass().getAnnotation(Tag.class);
        if (classTag != null) {
            if (operation.getTags() == null) operation.setTags(new java.util.ArrayList<>());
            if (!operation.getTags().contains(classTag.name())) {
                operation.getTags().add(classTag.name());
            }
        }
        Tags classTags = method.getDeclaringClass().getAnnotation(Tags.class);
        if (classTags != null) {
            if (operation.getTags() == null) operation.setTags(new java.util.ArrayList<>());
            for (Tag t : classTags.value()) {
                if (!operation.getTags().contains(t.name())) operation.getTags().add(t.name());
            }
        }
    }

    private void scanParameters(Method method, Operation operation) {
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        Class<?>[] paramTypes = method.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            Annotation[] annotations = paramAnnotations[i];
            Class<?> paramType = paramTypes[i];

            // ★ @Parameter 注解:覆盖 description / example / required / schema(隐式 metadata)
            io.swagger.v3.oas.annotations.Parameter paramAnn =
                    findAnnotation(annotations, io.swagger.v3.oas.annotations.Parameter.class);

            PathParam pathParam = findAnnotation(annotations, PathParam.class);
            if (pathParam != null) {
                Parameter p = new Parameter();
                p.setName(pathParam.value());
                p.setIn("path");
                p.setRequired(true);
                p.setSchema(schemaForType(paramType, null));
                applyParameterAnnotation(paramAnn, p);
                operation.addParametersItem(p);
                continue;
            }

            QueryParam queryParam = findAnnotation(annotations, QueryParam.class);
            if (queryParam != null) {
                Parameter p = new Parameter();
                p.setName(queryParam.value());
                p.setIn("query");
                DefaultValue dv = findAnnotation(annotations, DefaultValue.class);
                if (dv != null) {
                    p.setSchema(schemaForType(paramType, null, dv.value()));
                } else {
                    p.setSchema(schemaForType(paramType, null, null));
                }
                applyParameterAnnotation(paramAnn, p);
                operation.addParametersItem(p);
                continue;
            }

            // BeanParam 暂不支持
        }
    }

    /**
     * 将 {@code @Parameter} 注解的 description / example / required / hidden
     * 等元数据合并到 OpenAPI Parameter 对象上。
     */
    private void applyParameterAnnotation(io.swagger.v3.oas.annotations.Parameter ann, Parameter p) {
        if (ann == null) return;
        if (!ann.description().isEmpty()) p.setDescription(ann.description());
        if (!ann.name().isEmpty() && (p.getName() == null || p.getName().isEmpty())) {
            p.setName(ann.name());
        }
        if (!ann.example().isEmpty()) {
            Schema<?> s = p.getSchema();
            if (s != null) s.setExample(ann.example());
        }
        if (ann.required()) p.setRequired(true);
        if (ann.deprecated()) p.setDeprecated(true);
        // hidden 仅在 Operation 级别支持;Parameter 不支持 setHidden() — 跳过
    }

    private void scanRequestBody(Method method, Operation operation) {
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        Class<?>[] paramTypes = method.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            Annotation[] annotations = paramAnnotations[i];
            Class<?> paramType = paramTypes[i];

            if (findAnnotation(annotations, PathParam.class) != null
                    || findAnnotation(annotations, QueryParam.class) != null
                    || findAnnotation(annotations, BeanParam.class) != null
                    || findAnnotation(annotations, jakarta.ws.rs.HeaderParam.class) != null
                    || findAnnotation(annotations, jakarta.ws.rs.CookieParam.class) != null
                    || findAnnotation(annotations, jakarta.ws.rs.FormParam.class) != null
                    || findAnnotation(annotations, jakarta.ws.rs.MatrixParam.class) != null
                    || findAnnotation(annotations, jakarta.ws.rs.core.Context.class) != null) {
                continue;
            }

            // 简单 Java 类型 (String/Long/Integer/Boolean/Double/...) 不当作 requestBody
            if (isSimpleType(paramType)) {
                continue;
            }

            // 推断为 @RequestBody
            Type genericType = method.getGenericParameterTypes()[i];
            RequestBody requestBody = new RequestBody();
            Content bodyContent = new Content();
            MediaType bodyMediaType = new MediaType();
            bodyMediaType.schema(schemaForType(paramType, genericType));
            bodyContent.addMediaType("application/json", bodyMediaType);
            requestBody.setContent(bodyContent);
            operation.setRequestBody(requestBody);
            // 只取第一个 body 参数
            break;
        }
    }

    private void scanResponses(Method method, Operation operation) {
        Type returnType = method.getGenericReturnType();
        Type dataType = unwrapResultType(returnType);

        Schema<?> successSchema;
        if (dataType != null) {
            // 如果解到的是 Object 或 class(无泛型),给一个通用 Result schema
            Class<?> rawDataClass = rawClass(dataType);
            if (rawDataClass == Object.class || rawDataClass == null) {
                successSchema = genericResultSchema(null);
            } else {
                successSchema = schemaForType(rawDataClass, dataType);
            }
        } else {
            successSchema = genericResultSchema(null);
        }

        ApiResponses responses = new ApiResponses();
        ApiResponse ok = new ApiResponse();
        ok.setDescription("OK");
        Content okContent = new Content();
        MediaType okMedia = new MediaType();
        okMedia.setSchema(successSchema);
        okContent.addMediaType("application/json", okMedia);
        ok.setContent(okContent);
        responses.addApiResponse("200", ok);

        ApiResponse bad = new ApiResponse();
        bad.setDescription("Bad Request");
        Content badContent = new Content();
        MediaType badMedia = new MediaType();
        badMedia.setSchema(new Schema<>().type("object"));
        badContent.addMediaType("application/json", badMedia);
        bad.setContent(badContent);
        responses.addApiResponse("400", bad);

        operation.setResponses(responses);
    }

    private void applyHttpMethod(PathItem pathItem, String httpMethod, Operation operation) {
        switch (httpMethod.toUpperCase()) {
            case "GET": pathItem.setGet(operation); break;
            case "POST": pathItem.setPost(operation); break;
            case "PUT": pathItem.setPut(operation); break;
            case "DELETE": pathItem.setDelete(operation); break;
            case "PATCH": pathItem.setPatch(operation); break;
            case "HEAD": pathItem.setHead(operation); break;
            case "OPTIONS": pathItem.setOptions(operation); break;
            default: throw new IllegalArgumentException("Unsupported HTTP method: " + httpMethod);
        }
    }

    // ===== 辅助方法 =====

    /**
     * 通用 Result schema(无法推断 T 时用)
     */
    private Schema<Object> genericResultSchema(Schema<?> dataSchema) {
        Schema<Object> result = new Schema<>();
        result.setType("object");
        result.addProperty("code", new Schema<>().type("integer").example(200));
        result.addProperty("message", new Schema<>().type("string").example("success"));
        result.addProperty("data", dataSchema != null ? dataSchema : new Schema<>().type("object"));
        result.addProperty("timestamp", new Schema<>().type("integer").format("int64"));
        return result;
    }

    private String extractHttpMethod(Method method) {
        if (method.isAnnotationPresent(GET.class)) return "GET";
        if (method.isAnnotationPresent(POST.class)) return "POST";
        if (method.isAnnotationPresent(PUT.class)) return "PUT";
        if (method.isAnnotationPresent(DELETE.class)) return "DELETE";
        if (method.isAnnotationPresent(PATCH.class)) return "PATCH";
        if (method.isAnnotationPresent(HEAD.class)) return "HEAD";
        if (method.isAnnotationPresent(OPTIONS.class)) return "OPTIONS";
        return null;
    }

    private <A extends Annotation> A findAnnotation(Annotation[] annotations, Class<A> target) {
        for (Annotation a : annotations) {
            if (target.isInstance(a)) {
                return target.cast(a);
            }
        }
        return null;
    }

    private boolean isSimpleType(Class<?> clazz) {
        if (clazz.isPrimitive()) return true;
        return clazz == String.class
                || clazz == Integer.class || clazz == Long.class
                || clazz == Short.class || clazz == Byte.class
                || clazz == Float.class || clazz == Double.class
                || clazz == Boolean.class
                || clazz == Character.class
                || Number.class.isAssignableFrom(clazz)
                || java.util.Date.class.isAssignableFrom(clazz)
                || java.time.temporal.Temporal.class.isAssignableFrom(clazz)
                || clazz.isEnum();
    }

    private Schema<?> schemaForType(Class<?> clazz, Type genericType) {
        return schemaForType(clazz, genericType, null);
    }

    private Schema<?> schemaForType(Class<?> clazz, Type genericType, String exampleDefault) {
        // 基本类型
        if (clazz == String.class) {
            Schema<String> s = new Schema<>();
            s.setType("string");
            if (exampleDefault != null) s.setExample(exampleDefault);
            return s;
        }
        if (clazz == Integer.class || clazz == int.class) {
            Schema<Integer> s = new Schema<>();
            s.setType("integer");
            s.setFormat("int32");
            if (exampleDefault != null) s.setExample(exampleDefault);
            return s;
        }
        if (clazz == Long.class || clazz == long.class) {
            Schema<Number> s = new Schema<>();
            s.setType("integer");
            s.setFormat("int64");
            if (exampleDefault != null) s.setExample(exampleDefault);
            return s;
        }
        if (clazz == Double.class || clazz == double.class
                || clazz == Float.class || clazz == float.class) {
            Schema<Number> s = new Schema<>();
            s.setType("number");
            s.setFormat(clazz == Float.class || clazz == float.class ? "float" : "double");
            if (exampleDefault != null) s.setExample(exampleDefault);
            return s;
        }
        if (clazz == Boolean.class || clazz == boolean.class) {
            Schema<Boolean> s = new Schema<>();
            s.setType("boolean");
            if (exampleDefault != null) s.setExample(exampleDefault);
            return s;
        }
        if (clazz == java.math.BigDecimal.class) {
            Schema<Number> s = new Schema<>();
            s.setType("number");
            if (exampleDefault != null) s.setExample(exampleDefault);
            return s;
        }

        // 集合
        if (Collection.class.isAssignableFrom(clazz) || clazz.isArray()) {
            Schema<?> arraySchema = new Schema<>();
            arraySchema.setType("array");
            Type itemType = extractCollectionItemType(genericType, clazz);
            if (itemType != null) {
                arraySchema.setItems(schemaForType(rawClass(itemType), itemType));
            } else {
                arraySchema.setItems(new Schema<>().type("object"));
            }
            return arraySchema;
        }

        // 分页 Page
        if (clazz.getSimpleName().equals("Page")) {
            Schema<Object> page = new Schema<>();
            page.setType("object");
            page.setDescription("MyBatis Plus Page");
            return page;
        }

        // Result<T>
        if (clazz == com.opentms.common.model.Result.class) {
            Type innerType = extractResultDataType(genericType);
            Schema<?> inner = innerType != null
                    ? schemaForType(rawClass(innerType), innerType)
                    : new Schema<>().type("object");
            Schema<Object> result = new Schema<>();
            result.setType("object");
            result.addProperty("code", new Schema<>().type("integer").example(200));
            result.addProperty("message", new Schema<>().type("string").example("success"));
            result.addProperty("data", inner);
            result.addProperty("timestamp", new Schema<>().type("integer").format("int64"));
            return result;
        }

        // 通用对象 → 用反射列字段
        Schema<Object> obj = new Schema<>();
        obj.setType("object");
        Class<?> walk = clazz;
        while (walk != null && walk != Object.class) {
            for (Field f : walk.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) || Modifier.isTransient(f.getModifiers())) {
                    continue;
                }
                String name = f.getName();
                if ("serialVersionUID".equals(name)) continue;
                Schema<?> propSchema = schemaForType(f.getType(), f.getGenericType());
                obj.addProperty(name, propSchema);
            }
            walk = walk.getSuperclass();
        }
        return obj;
    }

    private Type extractCollectionItemType(Type genericType, Class<?> clazz) {
        if (genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericType;
            Type[] args = pt.getActualTypeArguments();
            if (args != null && args.length > 0) return args[0];
        }
        return null;
    }

    private Type extractResultDataType(Type genericType) {
        if (genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericType;
            Type[] args = pt.getActualTypeArguments();
            if (args != null && args.length > 0) return args[0];
        }
        return null;
    }

    private Type unwrapResultType(Type returnType) {
        if (returnType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) returnType;
            Class<?> raw = rawClass(pt);
            if (raw == com.opentms.common.model.Result.class) {
                Type[] args = pt.getActualTypeArguments();
                if (args != null && args.length > 0) return args[0];
            }
        }
        // 未包装的 Object 等
        if (returnType == Object.class || returnType == void.class || returnType == Void.class) {
            return null;
        }
        return returnType;
    }

    private Class<?> rawClass(Type type) {
        if (type instanceof Class<?>) return (Class<?>) type;
        if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        }
        return null;
    }

    private String normalize(String p) {
        if (p == null || p.isEmpty()) return "";
        if (!p.startsWith("/")) p = "/" + p;
        // 去尾斜杠
        while (p.endsWith("/") && p.length() > 1) p = p.substring(0, p.length() - 1);
        return p;
    }

    private String deriveSummary(Method method) {
        StringBuilder sb = new StringBuilder();
        for (String part : method.getName().split("(?=[A-Z])")) {
            if (part.isEmpty()) continue;
            sb.append(part).append(" ");
        }
        String s = sb.toString().trim();
        return s.isEmpty() ? method.getName() : s;
    }
}