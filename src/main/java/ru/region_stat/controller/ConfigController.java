package ru.region_stat.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.region_stat.domain.dto.config.ConfigCreateDto;
import ru.region_stat.domain.dto.config.ConfigResultDto;
import ru.region_stat.domain.dto.config.ConfigUpdateDto;
import ru.region_stat.service.ConfigService;
import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@RequestMapping("/config")
@RestController
public class ConfigController {
    @Resource
    private ConfigService configService;

    @GetMapping("/all")
    @ApiOperation(value = "getAllConfigs", nickname = "getAllConfigs")
    public ResponseEntity<List<ConfigResultDto>> getAll() {

        return ResponseEntity.ok(configService.getAll());
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "getConfigByIdConfig", nickname = "getConfigByIdConfig")
    public ResponseEntity<ConfigResultDto> getConfigById(@PathVariable("id") UUID id) {

        ConfigResultDto configResultDto = configService.getResultDtoById(id);

        return ResponseEntity.ok(configResultDto);
    }

    @PostMapping
    @ApiOperation(value = "createConfig", nickname = "createConfig")
    public ResponseEntity<ConfigResultDto> create(@RequestBody ConfigCreateDto configCreateDto) {

        return new ResponseEntity<>(configService.create(configCreateDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "updateConfig", nickname = "updateConfig")
    public ResponseEntity<ConfigResultDto> update(@RequestBody @Validated ConfigUpdateDto configUpdateDto, @PathVariable("id") UUID id) {

        return ResponseEntity.ok(configService.update(configUpdateDto, id));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "deleteConfig", nickname = "deleteConfig")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {

        configService.deleteById(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}