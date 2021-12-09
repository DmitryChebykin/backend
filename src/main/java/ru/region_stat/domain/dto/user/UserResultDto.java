package ru.region_stat.domain.dto.user;

import lombok.*;
import ru.region_stat.domain.dto.BaseResultDto;
import ru.region_stat.domain.dto.department.DepartmentResultDto;
import java.util.Date;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResultDto extends BaseResultDto {
    private String name;
    private String surname;
    private String familyName;
    private String fullName;
    private String password;
    private String login;
    private String email;
    private String workPhone;
    private String position;
    private Boolean isActive;
    private DepartmentResultDto departmentResultDto;
    private Date createAt;
    private Date modifiedAt;
    private String createdBy;
    private String lastModifiedBy;
    private String NtlmLogin;
    private String departmentSubdivision;
    private List<String> role;
}