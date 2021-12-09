package ru.region_stat.service;

import org.modelmapper.ModelMapper;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.region_stat.domain.dto.format.PublicationFormatCreateDto;
import ru.region_stat.domain.dto.format.PublicationFormatResultDto;
import ru.region_stat.domain.dto.format.PublicationFormatUpdateDto;
import ru.region_stat.domain.entity.publicationFormat.PublicationFormatEntity;
import ru.region_stat.domain.repository.PublicationFormatRepository;
import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PublicationFormatService {
    @Resource
    private PublicationFormatRepository publicationFormatRepository;

    @Resource
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<PublicationFormatResultDto> getAll() {
        List<PublicationFormatEntity> publicationFormatEntityList = publicationFormatRepository.findAll();
        return publicationFormatEntityList.stream()
                .map(publicationFormatEntity -> modelMapper.map(publicationFormatEntity, PublicationFormatResultDto.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PublicationFormatResultDto getById(UUID id) {
        return modelMapper.map(publicationFormatRepository.findById(id).orElseThrow(ResourceNotFoundException::new), PublicationFormatResultDto.class);
    }

    @Transactional
    public PublicationFormatResultDto create(PublicationFormatCreateDto publicationFormatCreateDto) {
        PublicationFormatEntity publicationFormatEntity = modelMapper.map(publicationFormatCreateDto, PublicationFormatEntity.class);
        return modelMapper.map(publicationFormatRepository.save(publicationFormatEntity), PublicationFormatResultDto.class);
    }

    @Transactional
    public PublicationFormatResultDto update(PublicationFormatUpdateDto publicationFormatUpdateDto, UUID id) {

        PublicationFormatEntity publicationFormatEntity = publicationFormatRepository.findById(id).orElseThrow(RuntimeException::new);

        modelMapper.map(publicationFormatUpdateDto, publicationFormatEntity);

        return modelMapper.map(publicationFormatEntity, PublicationFormatResultDto.class);
    }

    @Transactional
    public void deleteById(UUID id) {
        publicationFormatRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Boolean existsByName(String name) {
        return publicationFormatRepository.existsByName(name);
    }

    @Transactional(readOnly = true)
    public PublicationFormatEntity getPublicationFormatByStringId(String formatId) {
        return formatId != null ? getPublicationFormatById(UUID.fromString(formatId)) : null;
    }

    @Transactional(readOnly = true)
    public PublicationFormatEntity getPublicationFormatById(UUID id) {
        return publicationFormatRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public UUID getIdByName(String name) {
        return publicationFormatRepository.findByName(name).orElseThrow(ResourceNotFoundException::new).getId();
    }
}