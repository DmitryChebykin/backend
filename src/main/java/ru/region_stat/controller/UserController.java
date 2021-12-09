package ru.region_stat.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.region_stat.domain.dto.user.UserCreateDto;
import ru.region_stat.domain.dto.user.UserResultDto;
import ru.region_stat.domain.dto.user.UserUpdateDto;
import ru.region_stat.service.UserService;
import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@RequestMapping("/user")
@RestController
public class UserController {
    @Resource
    private UserService userService;

    @GetMapping("/all")
    @ApiOperation(value = "getAllUsers", nickname = "getAllUsers")
    public ResponseEntity<List<UserResultDto>> getAll() {
        List<UserResultDto> userResultDtoList = userService.getAll();
        return ResponseEntity.ok(userResultDtoList);
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "findByIdUser", nickname = "findByIdUser")
    public ResponseEntity<UserResultDto> findById(@PathVariable("id") UUID id) {
        UserResultDto user = userService.getUserResultDtoById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @ApiOperation(value = "createUser", nickname = "createUser")
    public ResponseEntity<UserResultDto> create(@RequestBody UserCreateDto userCreateDto) {
        return ResponseEntity.ok(userService.create(userCreateDto));
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "updateUser", nickname = "updateUser")
    public ResponseEntity<Void> update(@RequestBody UserUpdateDto userUpdateDto, @PathVariable("id") UUID id) {
        userService.update(userUpdateDto, id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "deleteUser", nickname = "deleteUser")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        userService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}