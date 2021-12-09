package ru.region_stat.webanalitic;

import lombok.*;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Visitor {
    private String loggedInUserId;
    private String ip;
    private String method;
    private String url;
    private String page;
    private String queryString;
    private String refererPage;
    private String userAgent;
    private String loggedTime;
    private Map<String, List<String>> requestHeaders;
    private boolean uniqueVisit;
}