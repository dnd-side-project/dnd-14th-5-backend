package com.dnd5.timoapi.global.swagger.diff;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class SwaggerDiffService {

    private static final String REDIS_KEY = "swagger:spec:snapshot";
    private static final int MAX_DEPTH = 2;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public Optional<SwaggerDiffResult> compareAndSave(String currentSpec) {
        String previousSpec = redisTemplate.opsForValue().get(REDIS_KEY);
        redisTemplate.opsForValue().set(REDIS_KEY, currentSpec);

        if (previousSpec == null) {
            log.info("Swagger 스펙 초기 스냅샷 저장 완료");
            return Optional.empty();
        }

        SwaggerDiffResult diff = computeDiff(previousSpec, currentSpec);
        log.info("Swagger diff 계산 완료 (추가={}, 삭제={}, 변경={})",
                diff.added().size(), diff.removed().size(), diff.changed().size());
        return Optional.of(diff);
    }

    private SwaggerDiffResult computeDiff(String previousSpecStr, String currentSpecStr) {
        Map<String, Object> previousRoot = parseSpec(previousSpecStr);
        Map<String, Object> currentRoot = parseSpec(currentSpecStr);

        Map<String, String> previousEndpoints = extractEndpointStrings(previousRoot);
        Map<String, String> currentEndpoints = extractEndpointStrings(currentRoot);

        Map<String, Object> currentPaths = getPaths(currentRoot);
        Map<String, Object> currentSchemas = getSchemas(currentRoot);
        Map<String, Object> previousPaths = getPaths(previousRoot);
        Map<String, Object> previousSchemas = getSchemas(previousRoot);

        List<SwaggerDiffResult.EndpointDetail> added = new ArrayList<>();
        List<SwaggerDiffResult.EndpointDetail> removed = new ArrayList<>();
        List<SwaggerDiffResult.EndpointDetail> changed = new ArrayList<>();

        for (Map.Entry<String, String> entry : currentEndpoints.entrySet()) {
            if (!previousEndpoints.containsKey(entry.getKey())) {
                added.add(buildDetail(entry.getKey(), currentPaths, currentSchemas));
            } else if (!previousEndpoints.get(entry.getKey()).equals(entry.getValue())) {
                changed.add(buildChangedDetail(entry.getKey(), previousPaths, previousSchemas, currentPaths, currentSchemas));
            }
        }

        for (String endpoint : previousEndpoints.keySet()) {
            if (!currentEndpoints.containsKey(endpoint)) {
                removed.add(buildDetail(endpoint, previousPaths, previousSchemas));
            }
        }

        return new SwaggerDiffResult(added, removed, changed);
    }

    private SwaggerDiffResult.EndpointDetail buildDetail(
            String key, Map<String, Object> paths, Map<String, Object> schemas) {
        String[] parts = key.split(" ", 2);
        if (parts.length != 2) return new SwaggerDiffResult.EndpointDetail(key, null, null, null, null, null);

        String method = parts[0].toLowerCase();
        String path = parts[1];

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> pathItem = (Map<String, Object>) paths.get(path);
            if (pathItem == null) return new SwaggerDiffResult.EndpointDetail(key, null, null, null, null, null);

            @SuppressWarnings("unchecked")
            Map<String, Object> operation = (Map<String, Object>) pathItem.get(method);
            if (operation == null) return new SwaggerDiffResult.EndpointDetail(key, null, null, null, null, null);

            String summary = (String) operation.get("summary");
            String requestExample = extractRequestExample(operation, schemas);
            String responseExample = extractResponseExample(operation, schemas);
            return new SwaggerDiffResult.EndpointDetail(key, summary, requestExample, responseExample, null, null);
        } catch (Exception e) {
            log.warn("엔드포인트 상세 추출 실패: {}", key);
            return new SwaggerDiffResult.EndpointDetail(key, null, null, null, null, null);
        }
    }

    @SuppressWarnings("unchecked")
    private SwaggerDiffResult.EndpointDetail buildChangedDetail(
            String key,
            Map<String, Object> previousPaths, Map<String, Object> previousSchemas,
            Map<String, Object> currentPaths, Map<String, Object> currentSchemas) {
        SwaggerDiffResult.EndpointDetail current = buildDetail(key, currentPaths, currentSchemas);
        String[] parts = key.split(" ", 2);
        if (parts.length != 2) return current;

        String method = parts[0].toLowerCase();
        String path = parts[1];

        try {
            Map<String, Object> pathItem = (Map<String, Object>) previousPaths.get(path);
            if (pathItem == null) return current;
            Map<String, Object> operation = (Map<String, Object>) pathItem.get(method);
            if (operation == null) return current;

            String prevRequest = extractRequestExample(operation, previousSchemas);
            String prevResponse = extractResponseExample(operation, previousSchemas);

            String previousRequestExample = Objects.equals(prevRequest, current.requestExample()) ? null : prevRequest;
            String previousResponseExample = Objects.equals(prevResponse, current.responseExample()) ? null : prevResponse;

            return new SwaggerDiffResult.EndpointDetail(
                    current.key(), current.summary(),
                    current.requestExample(), current.responseExample(),
                    previousRequestExample, previousResponseExample
            );
        } catch (Exception e) {
            log.warn("변경된 엔드포인트 이전 스펙 추출 실패: {}", key);
            return current;
        }
    }

    @SuppressWarnings("unchecked")
    private String extractRequestExample(Map<String, Object> operation, Map<String, Object> schemas) {
        try {
            Map<String, Object> requestBody = (Map<String, Object>) operation.get("requestBody");
            if (requestBody == null) return null;
            Map<String, Object> content = (Map<String, Object>) requestBody.get("content");
            if (content == null) return null;
            Map<String, Object> jsonContent = (Map<String, Object>) content.get("application/json");
            if (jsonContent == null) return null;
            Map<String, Object> schema = (Map<String, Object>) jsonContent.get("schema");
            if (schema == null) return null;
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(generateExampleObject(schema, schemas, 0, null));
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private String extractResponseExample(Map<String, Object> operation, Map<String, Object> schemas) {
        try {
            Map<String, Object> responses = (Map<String, Object>) operation.get("responses");
            if (responses == null) return null;

            Map<String, Object> response = (Map<String, Object>) responses.get("200");
            if (response == null) response = (Map<String, Object>) responses.get("201");
            if (response == null && !responses.isEmpty()) {
                response = (Map<String, Object>) responses.values().iterator().next();
            }
            if (response == null) return null;

            Map<String, Object> content = (Map<String, Object>) response.get("content");
            if (content == null) return null;

            Map<String, Object> jsonContent = (Map<String, Object>) content.get("application/json");
            if (jsonContent == null) jsonContent = (Map<String, Object>) content.get("*/*");
            if (jsonContent == null) return null;

            Map<String, Object> schema = (Map<String, Object>) jsonContent.get("schema");
            if (schema == null) return null;
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(generateExampleObject(schema, schemas, 0, null));
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Object generateExampleObject(Object schemaObj, Map<String, Object> schemas, int depth, String fieldName) {
        if (depth > MAX_DEPTH || schemaObj == null) return "...";

        Map<String, Object> schema;
        try {
            schema = (Map<String, Object>) schemaObj;
        } catch (ClassCastException e) {
            return "...";
        }

        String ref = (String) schema.get("$ref");
        if (ref != null && ref.startsWith("#/components/schemas/")) {
            String schemaName = ref.substring("#/components/schemas/".length());
            Object refSchema = schemas.get(schemaName);
            return refSchema != null ? generateExampleObject(refSchema, schemas, depth + 1, fieldName) : Map.of();
        }

        List<?> allOf = (List<?>) schema.get("allOf");
        if (allOf != null && !allOf.isEmpty()) {
            return generateExampleObject(allOf.get(0), schemas, depth + 1, fieldName);
        }

        List<?> enumValues = (List<?>) schema.get("enum");
        if (enumValues != null && !enumValues.isEmpty()) {
            return enumValues.get(0);
        }

        String type = (String) schema.getOrDefault("type", "object");
        return switch (type) {
            case "string" -> {
                String format = (String) schema.get("format");
                if ("date-time".equals(format)) yield "2024-01-01T09:00:00";
                if ("date".equals(format)) yield "2024-01-01";
                yield inferStringValue(fieldName);
            }
            case "integer" -> inferIntegerValue(fieldName);
            case "number" -> inferNumberValue(fieldName);
            case "boolean" -> inferBooleanValue(fieldName);
            case "array" -> {
                Object items = schema.get("items");
                yield items != null
                        ? List.of(generateExampleObject(items, schemas, depth + 1, fieldName))
                        : List.of();
            }
            default -> {
                Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
                if (properties == null || properties.isEmpty()) yield Map.of();
                Map<String, Object> result = new LinkedHashMap<>();
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    result.put(entry.getKey(),
                            generateExampleObject(entry.getValue(), schemas, depth + 1, entry.getKey()));
                }
                yield result;
            }
        };
    }

    private String inferStringValue(String fieldName) {
        if (fieldName == null) return "string";
        String lower = fieldName.toLowerCase();
        if (lower.contains("email")) return "user@example.com";
        if (lower.contains("nickname") || lower.contains("username")) return "닉네임";
        if (lower.contains("name")) return "홍길동";
        if (lower.contains("imageurl") || lower.contains("image") || lower.endsWith("url")) return "https://example.com/image.png";
        if (lower.contains("token")) return "example-fcm-token";
        if (lower.contains("code")) return "ABC123";
        if (lower.contains("content") || lower.contains("body") || lower.contains("text")) return "예시 내용입니다";
        if (lower.contains("description")) return "예시 설명입니다";
        if (lower.contains("title")) return "예시 제목";
        if (lower.contains("type")) return "TYPE_A";
        if (lower.contains("status")) return "ACTIVE";
        if (lower.contains("provider")) return "GOOGLE";
        if (lower.contains("category")) return "FUTURE";
        if (lower.contains("timezone")) return "Asia/Seoul";
        if (lower.contains("phone")) return "010-1234-5678";
        if (lower.contains("password")) return "password123!";
        return "string";
    }

    private int inferIntegerValue(String fieldName) {
        if (fieldName == null) return 1;
        String lower = fieldName.toLowerCase();
        if (lower.contains("count") || lower.contains("size") || lower.contains("total")) return 5;
        if (lower.contains("score")) return 3;
        if (lower.contains("version")) return 1;
        return 1;
    }

    private double inferNumberValue(String fieldName) {
        if (fieldName == null) return 1.0;
        String lower = fieldName.toLowerCase();
        if (lower.contains("score") || lower.contains("rate") || lower.contains("ratio")) return 3.5;
        return 1.0;
    }

    private boolean inferBooleanValue(String fieldName) {
        if (fieldName == null) return true;
        String lower = fieldName.toLowerCase();
        if (lower.contains("deleted") || lower.contains("banned") || lower.contains("disabled")) return false;
        return true;
    }

    private Map<String, String> extractEndpointStrings(Map<String, Object> root) {
        Map<String, Object> paths = getPaths(root);
        Map<String, String> endpoints = new LinkedHashMap<>();
        for (Map.Entry<String, Object> pathEntry : paths.entrySet()) {
            String path = pathEntry.getKey();
            @SuppressWarnings("unchecked")
            Map<String, Object> methods = (Map<String, Object>) pathEntry.getValue();
            for (Map.Entry<String, Object> methodEntry : methods.entrySet()) {
                String key = methodEntry.getKey().toUpperCase() + " " + path;
                try {
                    endpoints.put(key, objectMapper.writeValueAsString(methodEntry.getValue()));
                } catch (Exception e) {
                    endpoints.put(key, "");
                }
            }
        }
        return endpoints;
    }

    private Map<String, Object> parseSpec(String spec) {
        try {
            return objectMapper.readValue(spec, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Swagger 스펙 파싱 실패: {}", e.getMessage());
            return Map.of();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getPaths(Map<String, Object> root) {
        return (Map<String, Object>) root.getOrDefault("paths", Map.of());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getSchemas(Map<String, Object> root) {
        Map<String, Object> components = (Map<String, Object>) root.getOrDefault("components", Map.of());
        return (Map<String, Object>) components.getOrDefault("schemas", Map.of());
    }
}
