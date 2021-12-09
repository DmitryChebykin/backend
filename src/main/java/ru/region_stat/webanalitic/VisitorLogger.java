package ru.region_stat.webanalitic;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component

public class VisitorLogger implements HandlerInterceptor {

    @Resource
    private VisitorService visitorService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        final String ip = HttpRequestResponseUtils.getClientIpAddress();
        final Map<String, List<String>> requestHeaders = HttpRequestResponseUtils.getRequestHeaders();
        final String url = HttpRequestResponseUtils.getRequestUrl();
        final String page = HttpRequestResponseUtils.getRequestUri();
        final String refererPage = HttpRequestResponseUtils.getRefererPage();
        final String queryString = HttpRequestResponseUtils.getPageQueryString();
        final String userAgent = HttpRequestResponseUtils.getUserAgent();
        final String requestMethod = HttpRequestResponseUtils.getRequestMethod();
        final String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Visitor visitor = new Visitor();
        visitor.setLoggedInUserId(HttpRequestResponseUtils.getLoggedInUserId());
        visitor.setIp(ip);
        visitor.setMethod(requestMethod);
        visitor.setUrl(url);
        visitor.setPage(page);
        visitor.setQueryString(queryString);
        visitor.setRefererPage(refererPage);
        visitor.setUserAgent(userAgent);
        visitor.setLoggedTime(timestamp);
        visitor.setUniqueVisit(true);
        visitor.setRequestHeaders(requestHeaders);

        visitorService.sendVisitorInfoToRabbitMQ(visitor);

        return true;
    }
}