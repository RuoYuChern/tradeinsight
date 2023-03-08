package com.tao.trade;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class TaoFilter extends OncePerRequestFilter {
    private Map<String, Boolean> urlMap;
    private AtomicInteger accessTimes;
    public TaoFilter(){
        urlMap = new HashMap<>();
        urlMap.put("/", Boolean.TRUE);
        urlMap.put("/index", Boolean.TRUE);
        urlMap.put("/detail", Boolean.TRUE);
        urlMap.put("/rebound", Boolean.TRUE);
        urlMap.put("/recommend", Boolean.TRUE);
        urlMap.put("/market-sentiment", Boolean.TRUE);
        urlMap.put("/crypto-market", Boolean.TRUE);
        urlMap.put("/symbol-go", Boolean.TRUE);
        urlMap.put("/view-symbol", Boolean.TRUE);
        urlMap.put("/view-vs", Boolean.TRUE);
        urlMap.put("/favicon.ico", Boolean.TRUE);
        urlMap.put("/api/admin/history-load", Boolean.TRUE);
        accessTimes = new AtomicInteger(0);
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String url = request.getServletPath();
        accessTimes.incrementAndGet();
        if(StringUtils.hasLength(url) && (!url.startsWith("/static"))) {
            boolean right = isRight(url);
            log.info("Remote:{} access Url:{}, is Right:{}, accessTimes:{}", getIP(request), url, right, accessTimes.get());
            if(!right) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Young man, you don't talk about martial arts!");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isRight(String url){
        Boolean b = urlMap.get(url);
        return (b != null);
    }

    private static String getIP(HttpServletRequest request){
        String ip=request.getHeader("x-forwarded-for");
        if(!StringUtils.hasLength(ip) || "unknown".equalsIgnoreCase(ip)){
            ip = request.getHeader("Proxy-Client-IP");
        }
        if(!StringUtils.hasLength(ip) || "unknown".equalsIgnoreCase(ip)){
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if(!StringUtils.hasLength(ip) || "unknown".equalsIgnoreCase(ip)){
            ip = request.getHeader("X-Real-IP");
        }
        if(!StringUtils.hasLength(ip) || "unknown".equalsIgnoreCase(ip)){
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
