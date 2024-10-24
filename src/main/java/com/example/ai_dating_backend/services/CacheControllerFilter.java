package com.example.ai_dating_backend.services;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class CacheControllerFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;

        String uri = req.getRequestURI();
        String eTag = generateETag(req);

        if (uri.startsWith("/images/")) {
            res.setHeader("Cache-Control", "public,max-age=3600");
            res.setHeader("ETag", eTag);

            String ifNoneMatch = req.getHeader("If-None-Match");
            if (ifNoneMatch != null && ifNoneMatch.equals(eTag)) {
                res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private String generateETag(HttpServletRequest request) {

        File imageFile = new File(request.getRequestURI());
        if (!imageFile.exists() || !imageFile.isFile()) {
            return null;
        }

        return "eTag-" + imageFile.getName();
    }
}
