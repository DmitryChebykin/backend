package ru.region_stat.controller.passwordRestore;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.region_stat.service.UserService;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@Profile("!import")
public class PasswordRestoreController {

    @Resource
    UserService userService;

    @PostMapping("/user/reset_password")
    public ResponseEntity<PasswordResetResponseDto> requestResetPassword(HttpServletRequest request,
                                                                         @RequestParam("email") String userEmail) {
        PasswordResetResponseDto passwordResetResponseDto = userService.requestResetPassword(request, userEmail);
        return ResponseEntity.ok().body(passwordResetResponseDto);
    }

    @GetMapping("/reset_password")
    public ResponseEntity<PasswordResetResponseDto> resetPassword(@RequestParam(value = "token") String token, @RequestBody String password) {
        PasswordResetResponseDto passwordResetResponseDto = userService.resetPassword(token, password);

        return ResponseEntity.ok().body(passwordResetResponseDto);
    }
}