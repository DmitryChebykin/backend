package ru.region_stat.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.region_stat.domain.dto.config.ConfigCreateDto;
import ru.region_stat.domain.dto.config.ConfigResultDto;
import ru.region_stat.domain.dto.config.ConfigUpdateDto;
import ru.region_stat.domain.entity.config.ConfigEntity;
import ru.region_stat.domain.repository.ConfigRepository;
import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ConfigService {
    @Resource
    private ConfigRepository configRepository;

    @Resource
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<ConfigResultDto> getAll() {

        List<ConfigEntity> configEntityList = configRepository.findAll();

        return configEntityList.stream()
                .map(configEntity -> modelMapper.map(configEntity, ConfigResultDto.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public ConfigResultDto create(ConfigCreateDto configCreateDto) {

        ConfigEntity configEntity = modelMapper.map(configCreateDto, ConfigEntity.class);

        ConfigEntity entity = configRepository.save(configEntity);
        return modelMapper.map(entity, ConfigResultDto.class);
    }

    @Transactional
    public void deleteById(UUID id) {
        configRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ConfigResultDto getResultDtoById(UUID id) {
        ConfigEntity configEntity = configRepository.findById(id).orElseThrow(RuntimeException::new);

        return modelMapper.map(configEntity, ConfigResultDto.class);
    }

    @Transactional
    public ConfigResultDto update(ConfigUpdateDto configUpdateDto, UUID id) {
        ConfigEntity configEntity = configRepository.findById(id).orElseThrow(RuntimeException::new);

        modelMapper.map(configUpdateDto, configEntity);

        return modelMapper.map(configEntity, ConfigResultDto.class);
    }
}