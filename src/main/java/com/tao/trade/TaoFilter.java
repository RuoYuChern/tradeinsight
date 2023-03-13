package com.tao.trade;

import com.tao.trade.utils.Md5Sig;
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
    private final Md5Sig md5Sig;
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
        urlMap.put("/talk", Boolean.TRUE);
        urlMap.put("/api/admin/history-load", Boolean.TRUE);
        urlMap.put("/api/ai/talk", Boolean.TRUE);
        urlMap.put("/api/ai/set", Boolean.TRUE);
        urlMap.put("/api/hq/get-symbol", Boolean.TRUE);
        accessTimes = new AtomicInteger(0);
        md5Sig = new Md5Sig();
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String url = request.getServletPath();
        accessTimes.incrementAndGet();
        if(StringUtils.hasLength(url) && (!url.startsWith("/static"))) {
            boolean right = isRight(url, request);
            log.info("Remote:{} access Url:{}, is Right:{}, accessTimes:{}", getIP(request), url, right, accessTimes.get());
            if(!right) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Young man, you don't talk about martial arts!");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isRight(String url, HttpServletRequest request){
        Boolean b = urlMap.get(url);
        if(b == null){
            return false;
        }
        if(!url.startsWith("/api/hq")){
            return true;
        }
        String sign = request.getHeader("X-TJ-SIGNATURE");
        String noise = request.getHeader("X-TJ-NOISE");
        String timestamp = request.getHeader("X-TJ-TIME");
        if(!StringUtils.hasLength(sign) || !StringUtils.hasLength(noise) || !StringUtils.hasLength(timestamp)){
            log.info("hase none X-TJ-* headers");
            return false;
        }else {
            long time = Long.valueOf(timestamp);
            if(Math.abs(time - System.currentTimeMillis()/1000) >= 10){
                log.info("Request is too old");
                return false;
            }else {
                String newSign = md5Sig.sign(WebConfig.getTaoSalt(), noise, timestamp);
                if (!sign.equals(newSign)) {
                    log.info("sign error:{} !={}", sign, newSign);
                    return false;
                }
            }
        }
        return true;
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
