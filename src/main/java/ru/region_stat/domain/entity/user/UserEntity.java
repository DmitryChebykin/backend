package ru.region_stat.domain.entity.user;

import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import ru.region_stat.domain.entity.BaseEntity;
import ru.region_stat.domain.entity.department.DepartmentEntity;
import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "rs_users")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEntity extends BaseEntity {
    @Column(name = "name")
    private String name;

    @Column(name = "surname")
    private String surname;

    @Column(name = "family_name")
    private String familyName;
    @Column(name = "full_name")
    private String fullName;

    @Column(name = "password")
    private String password;

    @Column(name = "login")
    private String login;

    @Column(name = "email")
    private String email;

    @Column(name = "work_phone")
    private String workPhone;

    @Column(name = "position")
    private String position;

    @Column(name = "ntlm_login")
    private String ntlmLogin;

    @Column(name = "is_active")
    private Boolean isActive;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private DepartmentEntity department;

    @Column(name = "subdivision")
    private String departmentSubdivision;

    @ElementCollection(targetClass = Roles.class,fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "rs_roles", joinColumns = @JoinColumn(name = "user_id"))
    private List<Roles> role;
}