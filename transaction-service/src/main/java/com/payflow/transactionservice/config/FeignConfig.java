package com.payflow.transactionservice.config;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;

@Configuration
public class FeignConfig {

    @Value("${payflow.service.internal-key}")
    private String internalKey;

    @Bean
    public RequestInterceptor requestInterceptor() {

        return requestTemplate -> {

            String url = requestTemplate.url();

            /*
             * Internal service-to-service request
             */
            if (url.startsWith("/internal/")) {

                requestTemplate.header(
                        "X-Internal-Service-Key",
                        internalKey
                );

                // Do NOT forward user's JWT
                return;
            }

            /*
             * Normal user-facing request
             * Forward the user's JWT
             */
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes)
                            RequestContextHolder.getRequestAttributes();

            if (attributes == null) {
                return;
            }

            HttpServletRequest request = attributes.getRequest();

            String authorization =
                    request.getHeader("Authorization");

            if (authorization != null
                    && authorization.startsWith("Bearer ")) {

                requestTemplate.header(
                        "Authorization",
                        authorization
                );
            }
        };
    }
}