package ru.region_stat.webanalitic.dto;

import lombok.*;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VisitorEntityDto {
    private UUID id;
    private String loggedInUserId;
    private String ip;
    private String method;
    private String url;
    private String page;
    private String queryString;
    private String refererPage;
    private String userAgent;
    private String loggedTime;
    private String requestHeaders;
    private boolean uniqueVisit;
}