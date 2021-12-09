package ru.region_stat.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.region_stat.security.UserPrincipal;
import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@MappedSuperclass
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseEntity {

    public static final String CREATED_TIME = "created_time";
    public static final String LAST_MODIFIED_TIME = "last_modified_time";
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private UUID id;

    @Column(name = "created_by_user")
    @CreatedBy
    private String createdByUser;

    @Column(name = "modified_by_user")
    @LastModifiedBy
    private String modifiedByUser;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = CREATED_TIME, nullable = false, updatable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = LAST_MODIFIED_TIME)
    private Date modifiedAt;

    @PrePersist
    public void prePersist() {
        String createdByUser = getUsernameOfAuthenticatedUser();
        this.createdByUser = createdByUser;
        this.modifiedByUser = createdByUser;
    }

    @PreUpdate
    public void preUpdate() {
        this.modifiedByUser = getUsernameOfAuthenticatedUser();
    }

    private String getUsernameOfAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        return ((UserPrincipal) authentication.getPrincipal()).getId().toString();
    }
}
