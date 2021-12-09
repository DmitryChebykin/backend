package ru.region_stat.service;

import org.modelmapper.ModelMapper;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.region_stat.domain.dto.rubric.RubricCreateDto;
import ru.region_stat.domain.dto.rubric.RubricResultDto;
import ru.region_stat.domain.dto.rubric.RubricUpdateDto;
import ru.region_stat.domain.entity.rubric.RubricEntity;
import ru.region_stat.domain.repository.RubricRepository;
import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RubricService {

    @Resource
    private RubricRepository rubricRepository;

    @Resource
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<RubricResultDto> getAll() {

        List<RubricEntity> publicationTypeEntityList = rubricRepository.getContainsPublicationsRubrics();

        return publicationTypeEntityList.stream()
                .map(publicationTypeEntity -> modelMapper.map(publicationTypeEntity, RubricResultDto.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RubricResultDto getById(UUID id) {

        return modelMapper.map(rubricRepository.findById(id)
                .orElseThrow(RuntimeException::new), RubricResultDto.class);
    }

    @Transactional
    public RubricResultDto create(RubricCreateDto rubricCreateDto) {

        RubricEntity rubricEntity = modelMapper.map(rubricCreateDto, RubricEntity.class);

        //TODO: check without verify
        //TODO: check error if entity doesn't found
        if (rubricCreateDto.getParentId() != null) {

            RubricEntity parentRubric = rubricRepository.findById(UUID.fromString(rubricCreateDto.getParentId())).orElseThrow(RuntimeException::new);

            rubricEntity.setParent(parentRubric);
        }

        return modelMapper.map(rubricRepository.save(rubricEntity), RubricResultDto.class);
    }

    @Transactional
    public RubricResultDto update(RubricUpdateDto publicationDto, UUID id) {

        RubricEntity rubricEntity = rubricRepository.findById(id).orElseThrow(RuntimeException::new);

        modelMapper.map(publicationDto, rubricEntity);

        return modelMapper.map(rubricEntity, RubricResultDto.class);
    }

    @Transactional
    public void deleteById(UUID id) {

        rubricRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Boolean existsByName(String name) {
        return rubricRepository.existsByName(name);
    }

    @Transactional(readOnly = true)
    public RubricEntity getRubricEntityById(UUID id) {
        return rubricRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public RubricEntity getRubricEntityByStringId(String rubricId) {
        return rubricId != null ? getRubricEntityById(UUID.fromString(rubricId)) : null;
    }

    @Transactional(readOnly = true)
    public UUID getIdByName(String rubricName) {
        return rubricRepository.findByName(rubricName).orElseThrow(ResourceNotFoundException::new).getId();
    }

    @Transactional(readOnly = true)
    public List<RubricEntity> findByIdIn(List<UUID> rubricEntityUUIDList) {
        return rubricRepository.findByIdIsIn(rubricEntityUUIDList);
    }

    @Transactional(readOnly = true)
    public List<RubricResultDto> getContainsArchivePublicationsRubrics() {

        List<RubricEntity> publicationTypeEntityList = rubricRepository.getContainsArchivePublicationsRubrics();

        return publicationTypeEntityList.stream()
                .map(publicationTypeEntity -> modelMapper.map(publicationTypeEntity, RubricResultDto.class))
                .collect(Collectors.toList());
    }
}