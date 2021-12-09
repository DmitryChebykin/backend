package ru.region_stat.service;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.region_stat.domain.dto.municipalFilesReference.*;
import ru.region_stat.domain.entity.municipalFiles.MunicipalFilesReferenceEntity;
import ru.region_stat.domain.repository.MunicipalFilesReferenceRepository;
import javax.annotation.Resource;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MunicipalFilesReferenceService {

    @Resource
    private MunicipalFilesReferenceRepository municipalFilesReferenceRepository;

    @Resource
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<MunicipalFilesReferenceResultDto> getAll() {

        List<MunicipalFilesReferenceEntity> municipalFilesReferenceEntityList = municipalFilesReferenceRepository.findAll();

        return municipalFilesReferenceEntityList.stream()
                .map(municipalFilesReferenceEntity -> modelMapper.map(municipalFilesReferenceEntity, MunicipalFilesReferenceResultDto.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MunicipalFilesReferenceResultDto getMunicipalFilesReferenceResultDtoById(UUID id) {

        return modelMapper.map(municipalFilesReferenceRepository.findById(id)
                .orElseThrow(RuntimeException::new), MunicipalFilesReferenceResultDto.class);
    }

    @Transactional
    public MunicipalFilesReferenceResultDto create(MunicipalFilesReferenceCreateDto municipalFilesReferenceCreateDto) {

        MunicipalFilesReferenceEntity municipalFilesReferenceEntity = modelMapper.map(municipalFilesReferenceCreateDto, MunicipalFilesReferenceEntity.class);

        return modelMapper.map(municipalFilesReferenceRepository.save(municipalFilesReferenceEntity), MunicipalFilesReferenceResultDto.class);
    }

    @Transactional
    public MunicipalFilesReferenceResultDto update(MunicipalFilesReferenceUpdateDto municipalFilesReferenceUpdateDto, UUID id) {

        MunicipalFilesReferenceEntity municipalFilesReferenceEntity = municipalFilesReferenceRepository.findById(id).orElseThrow(RuntimeException::new);

        modelMapper.map(municipalFilesReferenceUpdateDto, municipalFilesReferenceEntity);

        return modelMapper.map(municipalFilesReferenceEntity, MunicipalFilesReferenceResultDto.class);
    }

    @Transactional
    public void deleteById(UUID id) {

        municipalFilesReferenceRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public MunicipalFilesReferenceEntity getById(UUID id) {
        return municipalFilesReferenceRepository.findById(id).orElseThrow(RuntimeException::new);
    }

    @Transactional(readOnly = true)
    public MunicipalFilesReferencePreviewDto getMunicipalFilesReferencePreviewDto(Path file) {
        List<MunicipalFilesReferenceEntity> entityList = getMunicipalFilesReferenceEntities(file);
        return modelMapper.map(entityList.get(0), MunicipalFilesReferencePreviewDto.class);
    }

    @NotNull
    private List<MunicipalFilesReferenceEntity> getMunicipalFilesReferenceEntities(Path file) {
        String fileName = file.getFileName().toString();
        String extension = "." + FilenameUtils.getExtension(fileName);
        List<MunicipalFilesReferenceEntity> entityList;
        MunicipalFilesReferenceEntity.MunicipalFilesReferenceEntityBuilder municipalFilesReferenceEntityBuilder = MunicipalFilesReferenceEntity.builder().fileNamePattern(fileName);
        Example<MunicipalFilesReferenceEntity> entityExample = Example.of(municipalFilesReferenceEntityBuilder.build());
        entityList = municipalFilesReferenceRepository.findAll(entityExample);

        if (entityList.size() <= 0) {

            ExampleMatcher withStringMatcher = ExampleMatcher.matchingAll()
                    .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

            entityExample = Example.of(municipalFilesReferenceEntityBuilder.fileNamePattern("хххххх").build(), withStringMatcher);

            entityList = municipalFilesReferenceRepository.findAll(entityExample);

            List<String> stringList = entityList.stream().filter(e -> e.getFileNamePattern().endsWith(extension))
                    .map(e -> e.getFileNamePattern().replaceAll("хххххх", "").replaceAll(extension, "")).collect(Collectors.toList());

            entityList = stringList.stream().map(e -> {

                if (fileName.startsWith(e)) {

                    ExampleMatcher matcher = ExampleMatcher.matchingAll()
                            .withStringMatcher(ExampleMatcher.StringMatcher.STARTING);

                    Example<MunicipalFilesReferenceEntity> example = Example.of(municipalFilesReferenceEntityBuilder.fileNamePattern(e).build(), matcher);

                    List<MunicipalFilesReferenceEntity> filesReferenceEntities = municipalFilesReferenceRepository.findAll(example);

                    if (filesReferenceEntities.size() > 0) {
                        filesReferenceEntities.add(MunicipalFilesReferenceEntity.builder().build());

                        return filesReferenceEntities;
                    }
                }

                return new ArrayList<MunicipalFilesReferenceEntity>();
            }).
                    filter(e -> e.size() > 0).findFirst().orElse(Collections.singletonList(MunicipalFilesReferenceEntity.builder().build()));
        }
        return entityList;
    }

    @Transactional(readOnly = true)
    public MunicipalFilesReferencePreviewWithYearDto getMunicipalFilesReferencePreviewWithYearDto(Path file) {
        List<MunicipalFilesReferenceEntity> entityList = getMunicipalFilesReferenceEntities(file);
        MunicipalFilesReferencePreviewWithYearDto municipalFilesReferencePreviewWithYearDto = modelMapper.map(entityList.get(0), MunicipalFilesReferencePreviewWithYearDto.class);
        return municipalFilesReferencePreviewWithYearDto;
    }
}