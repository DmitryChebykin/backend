package ru.region_stat.service;

import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.region_stat.domain.dto.oneTimeRequestStatus.OneTimeRequestStatusCreateDto;
import ru.region_stat.domain.dto.oneTimeRequestStatus.OneTimeRequestStatusResultDto;
import ru.region_stat.domain.dto.oneTimeRequestStatus.OneTimeRequestStatusUpdateDto;
import ru.region_stat.domain.entity.oneTimeRequest.OneTimeRequestStatusEntity;
import ru.region_stat.domain.repository.OneTimeRequestStatusRepository;
import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OneTimeRequestStatusService {
    @Resource
    private OneTimeRequestStatusRepository oneTimeRequestStatusRepository;

    @Resource
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<OneTimeRequestStatusResultDto> getAll() {

        List<OneTimeRequestStatusEntity> oneTimeRequestStatusEntities = oneTimeRequestStatusRepository.findAll();

        return oneTimeRequestStatusEntities.stream()
                .map(oneTimeRequestStatusEntity -> modelMapper.map(oneTimeRequestStatusEntity, OneTimeRequestStatusResultDto.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OneTimeRequestStatusResultDto getOneTimeRequestStatusResultDtoById(UUID id) {

        return modelMapper.map(oneTimeRequestStatusRepository.findById(id)
                .orElseThrow(RuntimeException::new), OneTimeRequestStatusResultDto.class);
    }

    @CacheEvict("requests")
    @Transactional
    public OneTimeRequestStatusResultDto create(OneTimeRequestStatusCreateDto oneTimeRequestStatusCreateDto) {

        OneTimeRequestStatusEntity oneTimeRequestStatusEntity = modelMapper.map(oneTimeRequestStatusCreateDto, OneTimeRequestStatusEntity.class);

        return modelMapper.map(oneTimeRequestStatusRepository.save(oneTimeRequestStatusEntity), OneTimeRequestStatusResultDto.class);
    }

    @CacheEvict("requests")
    @Transactional
    public OneTimeRequestStatusResultDto update(OneTimeRequestStatusUpdateDto oneTimeRequestStatusUpdateDto, UUID id) {

        OneTimeRequestStatusEntity oneTimeRequestStatusEntity = oneTimeRequestStatusRepository.findById(id).orElseThrow(RuntimeException::new);

        modelMapper.map(oneTimeRequestStatusUpdateDto, oneTimeRequestStatusEntity);

        return modelMapper.map(oneTimeRequestStatusEntity, OneTimeRequestStatusResultDto.class);
    }

    @Transactional
    public void deleteById(UUID id) {

        oneTimeRequestStatusRepository.deleteById(id);
    }

    public Boolean existsByName(String name) {
        return oneTimeRequestStatusRepository.existsByName(name);
    }

    @Transactional
    public OneTimeRequestStatusEntity getByName(String name) {
        return oneTimeRequestStatusRepository.findByName(name).orElseThrow(RuntimeException::new);
    }

    @Transactional(readOnly = true)
    public OneTimeRequestStatusEntity getById(UUID id) {
        return oneTimeRequestStatusRepository.findById(id).orElseThrow(RuntimeException::new);
    }
}