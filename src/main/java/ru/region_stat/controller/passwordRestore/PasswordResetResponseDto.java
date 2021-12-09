package ru.region_stat.controller.passwordRestore;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PasswordResetResponseDto {
    private String message;
    private String error;

}