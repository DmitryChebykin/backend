package ru.region_stat.webanalitic;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.region_stat.security.UserPrincipal;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class HttpRequestResponseUtils {

    private static final String[] IP_HEADER_CANDIDATES = {"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP", "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR", "HTTP_FORWARDED", "HTTP_VIA", "REMOTE_ADDR"};

    private HttpRequestResponseUtils() {
    }

    public static String getClientIpAddress() {

        if (RequestContextHolder.getRequestAttributes() == null) {
            return "0.0.0.0";
        }

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();

        for (String header : IP_HEADER_CANDIDATES) {
            String ipList = request.getHeader(header);
            if (ipList != null && ipList.length() != 0 && !"unknown".equalsIgnoreCase(ipList)) {
                String ip = ipList.split(",")[0];
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    public static String getRequestUrl() {

        if (RequestContextHolder.getRequestAttributes() == null) {
            return "";
        }

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();

        return request.getRequestURL().toString();
    }

    public static String getRequestUri() {

        if (RequestContextHolder.getRequestAttributes() == null) {
            return "";
        }

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();

        return request.getRequestURI();
    }

    public static String getRefererPage() {

        if (RequestContextHolder.getRequestAttributes() == null) {
            return "";
        }

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();

        String referer = request.getHeader("Referer");

        return referer != null ? referer : request.getHeader("referer");
    }

    public static String getPageQueryString() {

        if (RequestContextHolder.getRequestAttributes() == null) {
            return "";
        }

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();

        return request.getQueryString();
    }

    public static String getUserAgent() {

        if (RequestContextHolder.getRequestAttributes() == null) {
            return "";
        }

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();

        String userAgent = request.getHeader("User-Agent");

        return userAgent != null ? userAgent : request.getHeader("user-agent");
    }

    public static String getRequestMethod() {

        if (RequestContextHolder.getRequestAttributes() == null) {
            return "";
        }

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();

        return request.getMethod();
    }

    public static String getLoggedInUserId() {
        String loggedInUserId = "";

        if (SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()
                && !(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken)) {

            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            loggedInUserId = ((UserPrincipal) principal).getId().toString();

            return loggedInUserId;
        }

        return loggedInUserId;
    }

    public static Map<String, List<String>> getRequestHeaders() {

        if (RequestContextHolder.getRequestAttributes() == null) {
            return Collections.emptyMap();
        }

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();

        Enumeration<String> headers = request.getHeaderNames();

        Map<String, List<String>> headersMap = Collections.list(request.getHeaderNames()).stream().collect(Collectors.toMap(h -> h, h -> {
            Enumeration<String> requestHeaders = request.getHeaders(h);
            return Collections.list(requestHeaders);
        }));

        return headersMap;
    }
}