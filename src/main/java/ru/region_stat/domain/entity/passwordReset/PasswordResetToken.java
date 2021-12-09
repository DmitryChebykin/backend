package ru.region_stat.domain.entity.passwordReset;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import ru.region_stat.domain.entity.user.UserEntity;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rs_password_reset_token")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PasswordResetToken {
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(targetEntity = UserEntity.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private UserEntity userEntity;

    @Column(nullable = false)
    private LocalDateTime expirationDate;
}