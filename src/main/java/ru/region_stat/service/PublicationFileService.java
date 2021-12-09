package ru.region_stat.service;

import org.modelmapper.ModelMapper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.region_stat.domain.dto.file.PublicationFileCreateDto;
import ru.region_stat.domain.dto.file.PublicationFileExtendedResultDto;
import ru.region_stat.domain.dto.file.PublicationFileResultDto;
import ru.region_stat.domain.dto.file.PublicationFileUpdateDto;
import ru.region_stat.domain.entity.byteContent.ByteContentEntity;
import ru.region_stat.domain.entity.publicationFile.PublicationFileEntity;
import ru.region_stat.domain.entity.statisticalPublication.StatisticalPublicationEntity;
import ru.region_stat.domain.repository.ByteContentRepository;
import ru.region_stat.domain.repository.PublicationFileRepository;
import ru.region_stat.domain.repository.StatisticalPublicationRepository;
import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PublicationFileService {
    public static final String ATTACHMENT_FILENAME = "attachment; filename=\"";
    public static final String SUFFIX = "\"";

    @Resource
    private PublicationFileRepository publicationFileRepository;

    @Resource
    private ModelMapper modelMapper;

    @Resource
    private StatisticalPublicationRepository statisticalPublicationRepository;

    @Resource
    private ByteContentRepository contentRepository;

    @Resource
    private GotenbergService gotenbergService;

    @Transactional
    public List<PublicationFileResultDto> getAll() {
        List<PublicationFileEntity> publicationFileEntityList = publicationFileRepository.findAll();
        return publicationFileEntityList.stream()
                .map(publicationFileEntity -> modelMapper.map(publicationFileEntity, PublicationFileResultDto.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PublicationFileResultDto getById(UUID id) {
        return modelMapper.map(publicationFileRepository.findById(id).orElseThrow(ResourceNotFoundException::new), PublicationFileResultDto.class);
    }

    @Transactional
    public PublicationFileResultDto create(PublicationFileCreateDto publicationFileCreateDto) {
        PublicationFileEntity publicationFileEntity = modelMapper.map(publicationFileCreateDto, PublicationFileEntity.class);
        StatisticalPublicationEntity publicationEntity = statisticalPublicationRepository.findById(UUID.fromString(publicationFileCreateDto.getPublicationId())).orElseThrow(RuntimeException::new);
        publicationFileEntity.setStatisticalPublication(publicationEntity);

        ByteContentEntity byteContentEntity = new ByteContentEntity();
        byteContentEntity.setContent(publicationFileCreateDto.getContent());
        byteContentEntity.setPublicationFileEntity(publicationFileEntity);
        contentRepository.save(byteContentEntity);

        return modelMapper.map(publicationFileRepository.save(publicationFileEntity), PublicationFileResultDto.class);
    }

    @Transactional
    public PublicationFileResultDto update(PublicationFileUpdateDto publicationFileUpdateDto, UUID id) {
        PublicationFileEntity publicationFileEntity = publicationFileRepository.findById(id).orElseThrow(RuntimeException::new);
        modelMapper.map(publicationFileUpdateDto, PublicationFileEntity.class);

        ByteContentEntity byteContentEntity = findByPublicationFileEntity(publicationFileEntity).orElseThrow(RuntimeException::new);
        byteContentEntity.setContent(publicationFileUpdateDto.getContent());

        StatisticalPublicationEntity publicationEntity = statisticalPublicationRepository.findById(UUID.fromString(publicationFileUpdateDto.getPublicationId())).orElseThrow(RuntimeException::new);
        publicationFileEntity.setStatisticalPublication(publicationEntity);

        return modelMapper.map(publicationFileEntity, PublicationFileResultDto.class);
    }

    private Optional<ByteContentEntity> findByPublicationFileEntity(PublicationFileEntity publicationFileEntity) {
        UUID publicationFileEntityId = publicationFileEntity.getId();

        String contentIdString = contentRepository.getContentId(publicationFileEntityId);

        UUID contentId = UUID.fromString(Optional.ofNullable(contentIdString).orElseThrow(RuntimeException::new));

        return contentRepository.findById(contentId);
    }

    @Transactional
    public void deleteById(UUID id) {
        publicationFileRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public String getDocumentFileName(UUID id) {
        return getById(id).getFileName();
    }

    @Transactional(readOnly = true)
    public byte[] getByteContent(UUID id) {

        return contentRepository.getContentByFileId(id).getContent();
    }

    public String getContentType(UUID id) {

        return ATTACHMENT_FILENAME + getDocumentFileName(id) + SUFFIX;
    }

    @Transactional(readOnly = true)
    public ByteArrayResource getByteArrayResource(UUID id) {
        byte[] blobAsBytes = getByteContent(id);

        return new ByteArrayResource(blobAsBytes);
    }

    public List<PublicationFileResultDto> getAllByStatPubId(UUID id) {
        List<PublicationFileEntity> publicationFileEntityList = publicationFileRepository.getByStatisticalPublicationId(id).orElseThrow(ResourceNotFoundException::new);
        return publicationFileEntityList.stream()
                .map(publicationFileEntity -> modelMapper.map(publicationFileEntity, PublicationFileResultDto.class))
                .collect(Collectors.toList());
    }

    public List<PublicationFileExtendedResultDto> getArchiveFilteredFilesByRubricId(String isArchive, String rubricId) {
        UUID rubricId1 = UUID.fromString(rubricId);
        List<PublicationFileExtendedResultDto> archiveFilteredFilesByRubricId = publicationFileRepository.getArchiveFilteredFilesByRubricId(Boolean.valueOf(isArchive), rubricId1);
        return archiveFilteredFilesByRubricId;
    }

    public byte[] getContentAsPdf(UUID file_id, String fileName) {
        byte[] bytes = gotenbergService.convertOfficeFile(getByteContent(file_id), fileName);
        return bytes;
    }
}