package com.dnd5.timoapi.global.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class ApiLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiLoggingFilter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Pattern SUBMISSION_URI =
            Pattern.compile("/problems/\\d+/submissions");

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

            if ("POST".equals(request.getMethod())
                    && SUBMISSION_URI.matcher(request.getRequestURI()).matches()) {
                logData.put(
                        "requestBody",
                        new String(wrappedRequest.getContentAsByteArray())
                );
            }

            String jsonLog;
            try {
                jsonLog = objectMapper.writeValueAsString(logData);
            } catch (Exception e) {
                jsonLog = logData.toString();
            }

            if (wrappedResponse.getStatus() >= 500) {
                MDC.put("logType", "ERROR");
                log.error(jsonLog);
            } else {
                MDC.put("logType", "API");
                log.info(jsonLog);
            }
            MDC.clear();

            wrappedResponse.copyBodyToResponse();
        }
    }
}
