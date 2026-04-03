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

@Component
public class ApiLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiLoggingFilter.class);

    private final ObjectMapper objectMapper;

    @Value("${logging.api.max-body-size:1048576}")
    private int maxBodySize;

    public ApiLoggingFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/actuator") || uri.equals("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest =
                new ContentCachingRequestWrapper(request, 1024 * 1024);
        ContentCachingResponseWrapper wrappedResponse =
                new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

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

            byte[] requestBody = wrappedRequest.getContentAsByteArray();
            if (requestBody.length > 0) {
                logData.put("requestBody", new String(requestBody, StandardCharsets.UTF_8));
            }

            byte[] responseBody = wrappedResponse.getContentAsByteArray();
            if (responseBody.length > 0) {
                logData.put("responseBody", new String(responseBody, StandardCharsets.UTF_8));
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
}
