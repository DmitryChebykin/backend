package ru.region_stat.service;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.region_stat.controller.passwordRestore.PasswordResetResponseDto;
import ru.region_stat.domain.dto.user.AuthUserRequest;
import ru.region_stat.domain.dto.user.UserCreateDto;
import ru.region_stat.domain.dto.user.UserResultDto;
import ru.region_stat.domain.dto.user.UserUpdateDto;
import ru.region_stat.domain.entity.department.DepartmentEntity;
import ru.region_stat.domain.entity.passwordReset.PasswordResetToken;
import ru.region_stat.domain.entity.user.Roles;
import ru.region_stat.domain.entity.user.UserEntity;
import ru.region_stat.domain.repository.PasswordResetTokenRepository;
import ru.region_stat.domain.repository.UserRepository;
import ru.region_stat.security.JwtTokenConverter;
import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    public static final String REGEX = "^[{]?[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}[}]?$";
    @Resource
    private UserRepository userRepository;
    @Resource
    private JavaMailSender javaMailSender;
    @Resource
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Resource
    private ModelMapper modelMapper;
    @Resource
    private DepartmentService departmentService;


    @Value("${spring.mail.username}")
    private String fromMail;

    @Transactional
    public UserResultDto create(UserCreateDto userCreateDto) {
        UserEntity userEntity = modelMapper.map(userCreateDto, UserEntity.class);

        String departmentEntityId = userCreateDto.getDepartmentEntityId();

        Pattern pattern = Pattern.compile(REGEX);

        if (departmentEntityId == null) {
            userEntity.setRole(new ArrayList<Roles>() {{add(Roles.USER);}} );
            UserEntity save = userRepository.save(userEntity);
            UserResultDto map = modelMapper.map(save, UserResultDto.class);
            return map;
        }

        if (pattern.matcher(departmentEntityId).matches() && departmentService.existsById(UUID.fromString(departmentEntityId))) {
            DepartmentEntity departmentEntity = departmentService.getById(UUID.fromString(departmentEntityId));
            userEntity.setDepartment(departmentEntity);
        }

        return modelMapper.map(userRepository.save(userEntity), UserResultDto.class);
    }

    @Transactional
    public void deleteById(UUID id) {
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public UserResultDto getUserResultDtoById(UUID id) {
        return modelMapper.map(userRepository.findById(id).orElseThrow(ResourceNotFoundException::new), UserResultDto.class);
    }

    @Transactional(readOnly = true)
    public List<UserResultDto> getAll() {
        List<UserEntity> userEntityList = userRepository.findAll();
        return userEntityList.stream()
                .map(userEntity -> modelMapper.map(userEntity, UserResultDto.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResultDto update(UserUpdateDto userUpdateDto, UUID id) {
        UserEntity userEntity = userRepository.findById(id).orElseThrow(RuntimeException::new);
        modelMapper.map(userUpdateDto, userEntity);
        DepartmentEntity departmentEntity = departmentService.getById(UUID.fromString(userUpdateDto.getDepartmentEntityId()));
        userEntity.setDepartment(departmentEntity);
        return modelMapper.map(userEntity, UserResultDto.class);
    }

    @Transactional(readOnly = true)
    public Boolean existsByNtlmLogin(String login) {
        return userRepository.existsByNtlmLogin(login);
    }

    @Transactional(readOnly = true)
    public UserEntity login(AuthUserRequest request) {

        UserEntity user = userRepository.findByLogin(request.getLogin());

        return user;
    }

    @Transactional
    public PasswordResetResponseDto requestResetPassword(HttpServletRequest request, String userEmail) {
        PasswordResetResponseDto passwordResetResponseDto = PasswordResetResponseDto.builder().message("Сообщение не отправлено").error("Нет такого email").build();

        Optional<UserEntity> optionalUserEntity = userRepository.findByEmailIs(userEmail);

        if (!optionalUserEntity.isPresent()) {
            return passwordResetResponseDto;
        }

        String token = UUID.randomUUID().toString();

        createPasswordResetTokenForUser(optionalUserEntity.get(), token);

        String siteURL = request.getRequestURL().toString();
        siteURL = siteURL.replace(request.getServletPath(), "");
        String resetPasswordLink = siteURL + "/reset_password?token=" + token;

        try {
            sendEmail(userEmail, resetPasswordLink);
        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        passwordResetResponseDto.setMessage("Сообщение отправлено");
        passwordResetResponseDto.setError("");

        return passwordResetResponseDto;
    }

    @Transactional
    public void createPasswordResetTokenForUser(UserEntity userEntity, String token) {
        PasswordResetToken myToken = PasswordResetToken.builder()
                .token(token)
                .userEntity(userEntity)
                .expirationDate(LocalDateTime.now().plusSeconds(300))
                .build();

        passwordResetTokenRepository.save(myToken);
    }

    public void sendEmail(String recipientEmail, String link)
            throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(fromMail, "Regionstat support");
        helper.setTo(recipientEmail);

        String subject = "Here's the link to reset your password";

        String content = "<p>Hello,</p>"
                + "<p>You have requested to reset your password.</p>"
                + "<p>Click the link below to change your password:</p>"
                + "<p><a href=\"" + link + "\">Change my password</a></p>"
                + "<br>"
                + "<p>Ignore this email if you do remember your password, "
                + "or you have not made the request.</p>";

        helper.setSubject(subject);

        helper.setText(content, true);

        javaMailSender.send(message);
    }

    @Transactional
    public PasswordResetResponseDto resetPassword(String token, String password) {
        PasswordResetResponseDto passwordResetResponseDto = PasswordResetResponseDto.builder().message("Пароль не изменен").error("Токен недействителен").build();

        Optional<PasswordResetToken> optionalPasswordResetToken = passwordResetTokenRepository.findByToken(token);

        if (optionalPasswordResetToken.isPresent()) {
            PasswordResetToken passwordResetToken = optionalPasswordResetToken.get();
            LocalDateTime expirationDate = passwordResetToken.getExpirationDate();
            passwordResetTokenRepository.delete(passwordResetToken);

            if (!expirationDate.isAfter(LocalDateTime.now())) {

                return passwordResetResponseDto;
            }

            passwordResetToken.getUserEntity().setPassword(password);
            passwordResetResponseDto.setMessage("Пароль изменен");
            passwordResetResponseDto.setError("");
        }

        return passwordResetResponseDto;
    }

    @Transactional(readOnly = true)
    public UserResultDto findByNtlmLogin(String ntlmLogin) {
        UserEntity userEntity = userRepository.findByNtlmLogin(ntlmLogin).orElseThrow(RuntimeException::new);
        return modelMapper.map(userEntity, UserResultDto.class);
    }
}