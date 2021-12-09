package ru.region_stat.security;

import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.region_stat.domain.dto.user.UserCreateDto;
import ru.region_stat.domain.dto.user.UserResultDto;
import ru.region_stat.domain.entity.user.UserEntity;
import ru.region_stat.service.UserService;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Resource
    private UserService userService;

    private final JwtTokenConverter jwtTokenUtil;
    @Resource
    private JwtTokenConverter jwtTokenConverter;
    @Resource
    private ModelMapper modelMapper;

    public JwtRequestFilter(JwtTokenConverter jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST,PATCH,DELETE,PUT,OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Max-Age", "86400");

        if ((request.getServletPath().equals("/login"))) {
            chain.doFilter(request, response);
            return;
        }

        String requestSsoHeader = request.getHeader("x-sso");

        if (requestSsoHeader != null) {

            UserResultDto user;

            try{
                user = userService.findByNtlmLogin(requestSsoHeader);
            } catch (Exception e){
                user = null;
            }

            if (user == null) {
                String requestSsoNameHeader = request.getHeader("x-sso-name");

                UserCreateDto userDto = UserCreateDto.builder()
                        .fullName(requestSsoNameHeader)
                        .name(requestSsoNameHeader)
                        .NtlmLogin(requestSsoHeader)
                        .build();

                user = userService.create(userDto);
            }

            UserPrincipal userPrincipal = UserPrincipal.builder()
                    .id(user.getId())
                    .username(user.getName())
                    .roles(user.getRole())
                    .build();

            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                    userPrincipal, null, userPrincipal.getAuthorities());

            usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

            String jwt = jwtTokenConverter.generateToken(modelMapper.map(user, UserEntity.class));
            if (!response.getHeaderNames().contains("jwt")) {
                response.addHeader("jwt", jwt);
            }

        } else{

            String requestTokenHeader = request.getHeader("Authorization");

            String jwtToken = null;

            UserResultDto connectedUser = null;

            if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
                jwtToken = requestTokenHeader.substring(7);
                connectedUser = jwtTokenUtil.getUsernameFromToken(jwtToken);
            }

            if (connectedUser != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                ArrayList<String> roles = new ArrayList<>(connectedUser.getRole());

                UserPrincipal userPrincipal = UserPrincipal.builder()
                        .id(connectedUser.getId())
                        .username(connectedUser.getName())
                        .roles(roles)
                        .build();

                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userPrincipal, null, userPrincipal.getAuthorities());

                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }

        chain.doFilter(request, response);
    }
}