package ru.region_stat.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.region_stat.domain.dto.user.UserResultDto;
import ru.region_stat.domain.entity.user.UserEntity;
import ru.region_stat.service.UserService;
import javax.annotation.Resource;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletResponse;
import java.security.Key;
import java.util.ArrayList;
import java.util.Base64;
import java.util.UUID;
import java.util.stream.Collectors;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;

@Component
public class JwtTokenConverter {

    @Value("${auth.secret}")
    private String secret;

    @Resource
    private UserService userService;

    public UserResultDto getUsernameFromToken(String token) {

        try {

            Claims body = Jwts.parserBuilder().setSigningKey(secret).build().parseClaimsJws(token).getBody();

            if (body != null) {

                return userService.getUserResultDtoById(UUID.fromString(body.get("userId", String.class)));
            }

            return null;
        } catch (SecurityException e) {
            throw new RuntimeException("token error");
        }
    }

    public String generateToken(UserEntity userEntity) {

        ArrayList<String> roles = userEntity.getRole().stream().map(Enum::name).collect(Collectors.toCollection(ArrayList::new));

        UserPrincipal userPrincipal = UserPrincipal.builder()
                .id(userEntity.getId())
                .username(userEntity.getName())
                .roles(roles)
                .build();

        Key hmacKey = new SecretKeySpec(Base64.getDecoder().decode(secret), SignatureAlgorithm.HS256.getJcaName());

        try {
//            Instant now = Instant.now();
            String jws = Jwts.builder()
                    .claim("userId", userPrincipal.getId())
                    .claim("username", userPrincipal.getUsername())
                    .claim("roles", userPrincipal.getRoles())
                    //TODO will set expiration token
//                    .setIssuedAt(Date.from(now))
//                    .setExpiration(Date.from(now.plus(5l, ChronoUnit.MINUTES)))
                    .signWith(hmacKey)
                    .compact();

            return jws;

        } catch (SecurityException e) {
            throw new RuntimeException();
        }
    }
}