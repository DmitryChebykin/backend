package ru.region_stat.controller;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.region_stat.domain.dto.user.AuthUserRequest;
import ru.region_stat.domain.dto.user.UserResultDto;
import ru.region_stat.domain.entity.user.UserEntity;
import ru.region_stat.security.JwtTokenConverter;
import ru.region_stat.security.UserPrincipal;
import ru.region_stat.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
public class AuthenticationController {

    @Resource
    private UserService userService;
    @Resource
    private ModelMapper modelMapper;

    @Resource
    private JwtTokenConverter jwtTokenConverter;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<UserResultDto> login(@RequestBody @Valid AuthUserRequest request, HttpServletResponse response) {

        UserEntity user = userService.login(request);

        if (user != null && user.getPassword().equals(request.getPassword())) {
            String jwt = jwtTokenConverter.generateToken(user);
            if (!response.getHeaderNames().contains("jwt")) {
                response.addHeader("jwt", jwt);
            }
        } else {
            throw new RuntimeException();
        }

        return ResponseEntity.ok().body(modelMapper.map(user, UserResultDto.class));
    }

    @GetMapping("/check-login-state")
    @RequestMapping(value = "/check-login-state", method = RequestMethod.GET)
    public ResponseEntity<UserResultDto> NtlmLogin() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserResultDto user = userService.getUserResultDtoById(userPrincipal.getId());
        return ResponseEntity.ok(user);
    }
}
