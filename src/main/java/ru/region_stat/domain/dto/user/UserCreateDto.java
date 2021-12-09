package ru.region_stat.domain.dto.user;

import lombok.*;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCreateDto {
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
    private String departmentEntityId;
    private String NtlmLogin;
    private String departmentSubdivision;
    private List<String> role;
}