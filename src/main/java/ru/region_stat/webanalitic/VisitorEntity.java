package ru.region_stat.webanalitic;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name ="rs_visit_history")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VisitorEntity {
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private UUID id;
    @Column(columnDefinition = "TEXT")
    private String loggedInUserId;
    @Column(columnDefinition = "TEXT")
    private String ip;
    @Column(columnDefinition = "TEXT")
    private String method;
    @Column(columnDefinition = "TEXT")
    private String url;
    @Column(columnDefinition = "TEXT")
    private String page;
    @Column(columnDefinition = "TEXT")
    private String queryString;
    @Column(columnDefinition = "TEXT")
    private String refererPage;
    @Column(columnDefinition = "TEXT")
    private String userAgent;
    @Column(columnDefinition = "TEXT")
    private String loggedTime;
    @Column(columnDefinition = "TEXT")
    private String requestHeaders;
    private boolean uniqueVisit;
}