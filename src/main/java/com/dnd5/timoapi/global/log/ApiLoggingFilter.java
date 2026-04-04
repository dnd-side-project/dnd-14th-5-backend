package com.dnd5.timoapi.global.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class ApiLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiLoggingFilter.class);

    private static final Set<String> SENSITIVE_PATHS = Set.of(
            "/auth/login", "/auth/callback", "/auth/reissue", "/test-auth/login"
    );

    private final ObjectMapper objectMapper;

    @Value("${logging.api.max-body-size:65536}")
    private int maxBodySize;

    public ApiLoggingFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/actuator") || path.equals("/favicon.ico");
    }

    private boolean isSensitivePath(String path) {
        return SENSITIVE_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest =
                new ContentCachingRequestWrapper(request, maxBodySize);
        ContentCachingResponseWrapper wrappedResponse =
                new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();
        MDC.put("traceId", UUID.randomUUID().toString());

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            Map<String, Object> logData = new HashMap<>();
            logData.put("method", request.getMethod());
            logData.put("uri", request.getRequestURI());
            logData.put("queryString", request.getQueryString());
            logData.put("statusCode", wrappedResponse.getStatus());
            logData.put("duration", duration);
            logData.put("clientIp", request.getRemoteAddr());

            if (!isSensitivePath(request.getServletPath())) {
                logRequestBody(request, wrappedRequest, logData);
                logResponseBody(wrappedResponse, logData);
            }

            String jsonLog;
            try {
                jsonLog = objectMapper.writeValueAsString(logData);
            } catch (Exception e) {
                jsonLog = logData.toString();
            }

            int status = wrappedResponse.getStatus();
            if (status >= 500) {
                MDC.put("logType", "ERROR");
                log.error(jsonLog);
            } else if (status >= 400) {
                MDC.put("logType", "WARN");
                log.warn(jsonLog);
            } else {
                MDC.put("logType", "API");
                log.info(jsonLog);
            }
            MDC.clear();

            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logRequestBody(HttpServletRequest request, ContentCachingRequestWrapper wrappedRequest, Map<String, Object> logData) {
        String contentType = request.getContentType();
        if (contentType == null) return;

        byte[] body = wrappedRequest.getContentAsByteArray();
        if (body.length == 0) return;

        String bodyStr = new String(body, StandardCharsets.UTF_8);
        boolean truncated = body.length >= maxBodySize;

        if (contentType.contains("application/json")) {
            logData.put("requestBody", parseJson(bodyStr, truncated));
        }
    }

    private void logResponseBody(ContentCachingResponseWrapper wrappedResponse, Map<String, Object> logData) {
        String contentType = wrappedResponse.getContentType();
        if (contentType == null) return;

        byte[] body = wrappedResponse.getContentAsByteArray();
        if (body.length == 0) return;

        String bodyStr = new String(body, StandardCharsets.UTF_8);
        boolean truncated = body.length >= maxBodySize;

        if (contentType.contains("application/json")) {
            logData.put("responseBody", parseJson(bodyStr, truncated));
        }
    }

    private Object parseJson(String bodyStr, boolean truncated) {
        if (truncated) return bodyStr + " [TRUNCATED]";
        try {
            return objectMapper.readTree(bodyStr);
        } catch (Exception e) {
            return bodyStr;
        }
    }
}
