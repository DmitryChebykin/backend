package ru.region_stat.domain.dto.user;

import lombok.Data;

@Data
public class AuthUserRequest {

    private String login;

    private String password;

}
