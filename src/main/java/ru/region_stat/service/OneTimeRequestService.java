package ru.region_stat.service;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.region_stat.controller.fileViewer.oneTimeRequestAnswer.OneTimeRequestAnswerPreviewDto;
import ru.region_stat.domain.dto.oneTimeRequest.*;
import ru.region_stat.domain.entity.department.DepartmentEntity;
import ru.region_stat.domain.entity.oneTimeRequest.OneTimeRequestEntity;
import ru.region_stat.domain.entity.oneTimeRequest.OneTimeRequestFileContentEntity;
import ru.region_stat.domain.entity.oneTimeRequest.OneTimeRequestFileEntity;
import ru.region_stat.domain.entity.oneTimeRequest.OneTimeRequestStatusEntity;
import ru.region_stat.domain.repository.*;
import ru.region_stat.security.UserPrincipal;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OneTimeRequestService {

    public static final String ANSWER_PREFIX = "ОТВЕТ_";
    @Resource
    private DocXGenerator docXGenerator;

    @Resource
    private OneTimeRequestRepository oneTimeRequestRepository;

    @Resource
    private ModelMapper modelMapper;

    @Resource
    private OneTimeRequestFileRepository oneTimeRequestFileRepository;

    @Resource
    private OneTimeRequestFileContentRepository oneTimeRequestFileContentRepository;

    @Resource
    private DepartmentRepository departmentRepository;
    @Resource
    private OneTimeRequestStatusRepository oneTimeRequestStatusRepository;

    @Resource
    private JdbcTemplate jdbcTemplate;

    public OneTimeRequestExtendedResultDto saveExtended(OneTimeRequestCreateDto oneTimeRequestCreateDto, List<MultipartFile> multipartFiles) {

        UUID id = save(oneTimeRequestCreateDto, multipartFiles).getId();

        String query = "SELECT DISTINCT\n" +
                "	rs_departments.name as department_name,\n" +
                "	rs_one_time_requests.*,\n" +
                "	rs_request_statuses.name as status_name,\n" +
                "	creator.full_name AS creator_full_name, \n" +
                "	modifier.full_name AS modifier_full_name,\n" +
                "	DATE_PART('year', rs_one_time_requests.created_time::date) as \"year\",\n" +
                "	DATE_PART('month', rs_one_time_requests.created_time::date) as \"month\"\n" +
                "FROM\n" +
                "	rs_one_time_requests\n" +
                "LEFT JOIN rs_departments ON rs_one_time_requests.department_id = rs_departments.\n" +
                "ID LEFT JOIN rs_request_statuses ON rs_one_time_requests.publication_status_id = rs_request_statuses.\n" +
                "ID LEFT JOIN rs_users AS creator ON creator.ID = UUID ( rs_one_time_requests.created_by_user )\n" +
                "LEFT JOIN rs_users AS modifier ON modifier.ID = UUID ( rs_one_time_requests.modified_by_user )" +
                "WHERE rs_one_time_requests.id = '" + id.toString() + "'";

        List<Map<String, Object>> maps = jdbcTemplate.queryForList(query);

        List<OneTimeRequestExtendedResultDto> resultDtos = maps.stream().map(e -> getOneTimeRequestExtendedResultDto(e)).collect(Collectors.toList());

        return resultDtos.get(0);
    }

    @Transactional
    public OneTimeRequestResultDto save(OneTimeRequestCreateDto oneTimeRequestCreateDto, List<MultipartFile> multipartFiles) {

        OneTimeRequestEntity oneTimeRequestEntity = modelMapper.map(oneTimeRequestCreateDto, OneTimeRequestEntity.class);

        UUID departmentId = UUID.fromString(oneTimeRequestCreateDto.getDepartmentEntityId());
        DepartmentEntity departmentEntity = departmentRepository.findById(departmentId).orElseThrow(RuntimeException::new);

        oneTimeRequestEntity.setDepartmentEntity(departmentEntity);

        UUID statusId = UUID.fromString(oneTimeRequestCreateDto.getOneTimeRequestStatusEntityId());
        OneTimeRequestStatusEntity oneTimeRequestStatusEntity = oneTimeRequestStatusRepository.findById(statusId).orElseThrow(RuntimeException::new);

        oneTimeRequestEntity.setOneTimeRequestStatusEntity(oneTimeRequestStatusEntity);

        OneTimeRequestFileContentEntity oneTimeRequestFileContentEntity = null;

        List<OneTimeRequestFileEntity> oneTimeRequestFileEntityList = new ArrayList<>();

        if (multipartFiles != null) {

            for (MultipartFile multipartFile : multipartFiles) {
                OneTimeRequestFileEntity oneTimeRequestFileEntity = OneTimeRequestFileEntity.builder()
                        .fileName(multipartFile.getOriginalFilename())
                        .fileSize(multipartFile.getSize())
                        .fileExtension(FilenameUtils.getExtension(multipartFile.getOriginalFilename()))
                        .build();

                try {
                    oneTimeRequestFileContentEntity = OneTimeRequestFileContentEntity.builder()
                            .content(multipartFile.getBytes())
                            .build();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                oneTimeRequestFileEntity.setRequestFileContent(oneTimeRequestFileContentEntity);

                oneTimeRequestFileEntityList.add(oneTimeRequestFileEntity);
            }
        }

        oneTimeRequestEntity.setOneTimeRequestFileEntities(oneTimeRequestFileEntityList);

        Optional<OneTimeRequestEntity> findTopByOrderByInternalNumberDesc = oneTimeRequestRepository.findTopByOrderByInternalNumberDesc();

        String number = "0";

        if (findTopByOrderByInternalNumberDesc.isPresent()) {
            Integer latestInternalNumber = findTopByOrderByInternalNumberDesc.get().getInternalNumber();
            int internalNumber = latestInternalNumber + 1;
            oneTimeRequestEntity.setInternalNumber(internalNumber);
            number = String.valueOf(internalNumber);
        }

        number = String.format("№%s/%d%s", number, Calendar.getInstance().get(Calendar.YEAR) - 2000, "-ЭП");
        oneTimeRequestEntity.setPetrostatNumber(number);

        oneTimeRequestRepository.save(oneTimeRequestEntity);

        return modelMapper.map(oneTimeRequestEntity, OneTimeRequestResultDto.class);
    }

    @Transactional
    public OneTimeRequestResultDto saveFromSharePoint(OneTimeRequestCreateDto oneTimeRequestCreateDto, List<MultipartFile> multipartFiles) {

        OneTimeRequestEntity oneTimeRequestEntity = modelMapper.map(oneTimeRequestCreateDto, OneTimeRequestEntity.class);

        Optional<String> stringOptional = Optional.ofNullable(oneTimeRequestCreateDto.getDepartmentEntityId());

        if (stringOptional.isPresent()) {

            UUID departmentId = UUID.fromString(stringOptional.get());
            DepartmentEntity departmentEntity = departmentRepository.findById(departmentId).orElseThrow(RuntimeException::new);

            oneTimeRequestEntity.setDepartmentEntity(departmentEntity);
        }

        UUID statusId = UUID.fromString(oneTimeRequestCreateDto.getOneTimeRequestStatusEntityId());
        OneTimeRequestStatusEntity oneTimeRequestStatusEntity = oneTimeRequestStatusRepository.findById(statusId).orElseThrow(RuntimeException::new);

        oneTimeRequestEntity.setOneTimeRequestStatusEntity(oneTimeRequestStatusEntity);

        OneTimeRequestFileContentEntity oneTimeRequestFileContentEntity = null;

        List<OneTimeRequestFileEntity> oneTimeRequestFileEntityList = new ArrayList<>();

        if (multipartFiles != null) {

            for (MultipartFile multipartFile : multipartFiles) {
                OneTimeRequestFileEntity oneTimeRequestFileEntity = OneTimeRequestFileEntity.builder()
                        .fileName(multipartFile.getOriginalFilename())
                        .fileSize(multipartFile.getSize())
                        .fileExtension(FilenameUtils.getExtension(multipartFile.getOriginalFilename()))
                        .build();

                try {
                    oneTimeRequestFileContentEntity = OneTimeRequestFileContentEntity.builder()
                            .content(multipartFile.getBytes())
                            .build();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                oneTimeRequestFileEntity.setRequestFileContent(oneTimeRequestFileContentEntity);

                oneTimeRequestFileContentEntity.setOneTimeRequestFileEntity(oneTimeRequestFileEntity);

                oneTimeRequestFileEntity.setOneTimeRequestEntity(oneTimeRequestEntity);

                oneTimeRequestFileEntityList.add(oneTimeRequestFileEntity);
            }
        }

        oneTimeRequestEntity.setOneTimeRequestFileEntities(oneTimeRequestFileEntityList);

        Optional<OneTimeRequestEntity> findTopByOrderByInternalNumberDesc = oneTimeRequestRepository.findTopByOrderByInternalNumberDesc();

        if (!findTopByOrderByInternalNumberDesc.isPresent()) {
            oneTimeRequestEntity.setInternalNumber(1);
        } else {
            Integer latestInternalNumber = findTopByOrderByInternalNumberDesc.get().getInternalNumber();
            int internalNumber = latestInternalNumber + 1;
            oneTimeRequestEntity.setInternalNumber(internalNumber);
        }

        oneTimeRequestRepository.save(oneTimeRequestEntity);

        return modelMapper.map(oneTimeRequestEntity, OneTimeRequestResultDto.class);
    }

    @Transactional
    public OneTimeRequestEntity save(OneTimeRequestEntity statisticalPublicationEntity) {
        return oneTimeRequestRepository.save(statisticalPublicationEntity);
    }

    @CacheEvict("requests")
    @Transactional
    public void deleteById(UUID id) {
        oneTimeRequestRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public OneTimeRequestResultDto findById(UUID id) {

        return modelMapper.map(getOneTimeRequestEntityById(id)
                .orElseThrow(ResourceNotFoundException::new), OneTimeRequestResultDto.class);
    }

    @Transactional(readOnly = true)
    public Optional<OneTimeRequestEntity> getOneTimeRequestEntityById(UUID id) {
        return oneTimeRequestRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<OneTimeRequestResultDto> getAll() {
        List<OneTimeRequestEntity> oneTimeRequestEntityList = oneTimeRequestRepository.getAllDetailedByQuery();

        return oneTimeRequestEntityList.stream()
                .map(publication -> modelMapper.map(publication, OneTimeRequestResultDto.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FileContentDto getFileContentDto(UUID fileId) {
        OneTimeRequestFileEntity oneTimeRequestFileEntity = oneTimeRequestFileRepository.findById(fileId).orElseThrow(RuntimeException::new);

        byte[] blobAsBytes = oneTimeRequestFileEntity.getRequestFileContent().getContent();

        String fileName = oneTimeRequestFileEntity.getFileName();

        return FileContentDto.builder()
                .byteArrayResource(new ByteArrayResource(blobAsBytes))
                .fileName(fileName)
                .build();
    }

    @Transactional
    public Optional<OneTimeRequestResultDto> saveDocxFileForRequestByRequestId(String id, OneTimeRequestUpdateDto oneTimeRequestUpdateDto) {
        OneTimeRequestEntity oneTimeRequestEntity = oneTimeRequestRepository.findById(UUID.fromString(id)).orElseThrow(RuntimeException::new);

        modelMapper.map(oneTimeRequestUpdateDto, oneTimeRequestEntity);

        UUID departmentId = UUID.fromString(oneTimeRequestUpdateDto.getDepartmentEntityId());
        DepartmentEntity departmentEntity = departmentRepository.findById(departmentId).orElseThrow(RuntimeException::new);

        oneTimeRequestEntity.setDepartmentEntity(departmentEntity);

        UUID statusId = UUID.fromString(oneTimeRequestUpdateDto.getOneTimeRequestStatusEntityId());
        OneTimeRequestStatusEntity oneTimeRequestStatusEntity = oneTimeRequestStatusRepository.findById(statusId).orElseThrow(RuntimeException::new);

        oneTimeRequestEntity.setOneTimeRequestStatusEntity(oneTimeRequestStatusEntity);

        byte[] bytes = new byte[0];

        try {
            bytes = docXGenerator.generateDocxFileByRequestId(oneTimeRequestEntity.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (bytes.length == 0) {
            return Optional.empty();
        } else {
            OneTimeRequestFileEntity oneTimeRequestFileEntity = OneTimeRequestFileEntity.builder()
                    .oneTimeRequestEntity(oneTimeRequestEntity)
                    .fileExtension("docx")
                    .fileName("Запрос " + oneTimeRequestEntity.getPetrostatNumber() + ".docx")
                    .fileSize((long) bytes.length)
                    .build();

            oneTimeRequestFileEntity = oneTimeRequestFileRepository.save(oneTimeRequestFileEntity);
            OneTimeRequestFileContentEntity oneTimeRequestFileContentEntity = OneTimeRequestFileContentEntity.builder().oneTimeRequestFileEntity(oneTimeRequestFileEntity).build();
            oneTimeRequestFileContentEntity.setContent(bytes);
            oneTimeRequestFileContentEntity.setOneTimeRequestFileEntity(oneTimeRequestFileEntity);
            oneTimeRequestFileContentRepository.save(oneTimeRequestFileContentEntity);
            oneTimeRequestFileEntity.setRequestFileContent(oneTimeRequestFileContentEntity);

            oneTimeRequestEntity.setOneTimeRequestStatusEntity(oneTimeRequestStatusRepository.findByName("Отправлено в Петростат").orElseThrow(RuntimeException::new));

            return Optional.of(modelMapper.map(oneTimeRequestFileEntity, OneTimeRequestResultDto.class));
        }
    }

    @Transactional(readOnly = true)
    public OneTimeRequestAnswerPreviewDto getoneTimeRequestAnswerPreviewDtos(Path file) {
        String fileName = file.getFileName().toString();

        String fileNameWithoutExtension = FilenameUtils.getBaseName(fileName);

        String extension = FilenameUtils.getExtension(fileName);

        OneTimeRequestAnswerPreviewDto.OneTimeRequestAnswerPreviewDtoBuilder dtoBuilder = OneTimeRequestAnswerPreviewDto.builder()
                .isAnswerRecognized(false)
                .filName(fileName)
                .fileExtension(extension);

        if (fileNameWithoutExtension.startsWith(ANSWER_PREFIX) && StringUtils.removeStart(fileNameWithoutExtension, ANSWER_PREFIX).length() > 0) {
            String number = StringUtils.removeStart(fileNameWithoutExtension, ANSWER_PREFIX)
                    .replaceAll("№_", "№")
                    .replaceAll("_ЭП", "-ЭП")
                    .replaceAll("_", "/");

            Optional<OneTimeRequestEntity> optionalOneTimeRequestEntity = oneTimeRequestRepository.findByPetrostatNumber(number);

            optionalOneTimeRequestEntity.ifPresent(oneTimeRequestEntity -> dtoBuilder.isAnswerRecognized(true).oneTimeRequestResultDto(modelMapper.map(oneTimeRequestEntity, OneTimeRequestResultDto.class)));
        }
        return dtoBuilder.build();
    }

    @Transactional
    public Map<OneTimeRequestFileResultDto, OneTimeRequestResultDto> addFileToRequest(File file) {
        OneTimeRequestFileResultDto oneTimeRequestFileResultDto = null;
        OneTimeRequestResultDto oneTimeRequestResultDto = null;
        Map<OneTimeRequestFileResultDto, OneTimeRequestResultDto> resultDtoMap = new HashMap<>();

        String fileName = file.getName();

        String fileNameWithoutExtension = FilenameUtils.getBaseName(fileName);

        if (fileNameWithoutExtension.startsWith(ANSWER_PREFIX) && StringUtils.removeStart(fileNameWithoutExtension, ANSWER_PREFIX).length() > 0) {
            String number = StringUtils.removeStart(fileNameWithoutExtension, ANSWER_PREFIX)
                    .replaceAll("_", "/")
                    .replaceAll("_ЭП", "-ЭП");

            String extension = FilenameUtils.getExtension(fileName);

            OneTimeRequestEntity oneTimeRequestEntity = oneTimeRequestRepository.findByPetrostatNumber(number).orElseThrow(RuntimeException::new);

            byte[] content = new byte[0];

            try {
                content = Files.readAllBytes(file.getAbsoluteFile().toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }

            OneTimeRequestFileEntity oneTimeRequestFileEntity = OneTimeRequestFileEntity.builder()
                    .fileExtension(extension)
                    .fileName(fileName)
                    .fileSize((long) content.length)
                    .oneTimeRequestEntity(oneTimeRequestEntity)
                    .build();

            oneTimeRequestFileEntity = oneTimeRequestFileRepository.save(oneTimeRequestFileEntity);

            OneTimeRequestFileContentEntity contentEntity = OneTimeRequestFileContentEntity.builder()
                    .oneTimeRequestFileEntity(oneTimeRequestFileEntity)
                    .content(content)
                    .build();

            contentEntity = oneTimeRequestFileContentRepository.save(contentEntity);

            oneTimeRequestFileEntity.setRequestFileContent(contentEntity);

            List<OneTimeRequestFileEntity> oneTimeRequestFileEntities = oneTimeRequestEntity.getOneTimeRequestFileEntities();

            oneTimeRequestFileEntities.add(oneTimeRequestFileEntity);

            oneTimeRequestFileResultDto = modelMapper.map(oneTimeRequestFileEntity, OneTimeRequestFileResultDto.class);

            oneTimeRequestResultDto = modelMapper.map(oneTimeRequestEntity, OneTimeRequestResultDto.class);

            file.delete(); //Files.move(source, target, REPLACE_EXISTING);
        }

        resultDtoMap.put(oneTimeRequestFileResultDto, oneTimeRequestResultDto);

        return resultDtoMap;
    }

    @Transactional
    public OneTimeRequestResultDto update(String requestId, OneTimeRequestUpdateDto oneTimeRequestUpdateDto, List<MultipartFile> multipartFiles) {
        OneTimeRequestEntity oneTimeRequestEntity = oneTimeRequestRepository.findById(UUID.fromString(requestId)).orElseThrow(RuntimeException::new);

        modelMapper.map(oneTimeRequestUpdateDto, oneTimeRequestEntity);

        UUID departmentId = UUID.fromString(oneTimeRequestUpdateDto.getDepartmentEntityId());
        DepartmentEntity departmentEntity = departmentRepository.findById(departmentId).orElseThrow(RuntimeException::new);

        oneTimeRequestEntity.setDepartmentEntity(departmentEntity);

        UUID statusId = UUID.fromString(oneTimeRequestUpdateDto.getOneTimeRequestStatusEntityId());
        OneTimeRequestStatusEntity oneTimeRequestStatusEntity = oneTimeRequestStatusRepository.findById(statusId).orElseThrow(RuntimeException::new);

        oneTimeRequestEntity.setOneTimeRequestStatusEntity(oneTimeRequestStatusEntity);

        OneTimeRequestFileContentEntity oneTimeRequestFileContentEntity = null;

        List<OneTimeRequestFileEntity> oneTimeRequestFileEntityList = new ArrayList<>();

        if (multipartFiles != null) {

            for (MultipartFile multipartFile : multipartFiles) {
                OneTimeRequestFileEntity oneTimeRequestFileEntity = OneTimeRequestFileEntity.builder()
                        .fileName(multipartFile.getOriginalFilename())
                        .fileSize(multipartFile.getSize())
                        .fileExtension(FilenameUtils.getExtension(multipartFile.getOriginalFilename()))
                        .build();

                try {
                    oneTimeRequestFileContentEntity = OneTimeRequestFileContentEntity.builder()
                            .content(multipartFile.getBytes())
                            .build();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                oneTimeRequestFileEntity.setRequestFileContent(oneTimeRequestFileContentEntity);

                oneTimeRequestFileContentEntity.setOneTimeRequestFileEntity(oneTimeRequestFileEntity);

                oneTimeRequestFileEntity.setOneTimeRequestEntity(oneTimeRequestEntity);

                oneTimeRequestFileEntityList.add(oneTimeRequestFileEntity);
            }

            oneTimeRequestEntity.setOneTimeRequestFileEntities(oneTimeRequestFileEntityList);
        }

        return modelMapper.map(oneTimeRequestEntity, OneTimeRequestResultDto.class);
    }

    @Transactional
    public List<Map<UUID, UUID>> updateStatusByListIds(OneTimeRequestsStatusesUpdateDto oneTimeRequestsStatusesUpdateDto) {

        UUID userId = getUuid();

        return oneTimeRequestRepository.updateStatusByListIds(oneTimeRequestsStatusesUpdateDto.getRequestIds(), oneTimeRequestsStatusesUpdateDto.getStatusId(), userId, new Date());
    }

    @Nullable
    private UUID getUuid() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        UUID userId = ((UserPrincipal) authentication.getPrincipal()).getId();
        return userId;
    }

    public List<OneTimeRequestExtendedResultDto> getAllExtended() {

        String query = "SELECT DISTINCT\n" +
                "	rs_departments.name as department_name,\n" +
                "	rs_one_time_requests.*,\n" +
                "	rs_request_statuses.name as status_name,\n" +
                "	creator.full_name AS creator_full_name, \n" +
                "	modifier.full_name AS modifier_full_name,\n" +
                "	DATE_PART('year', rs_one_time_requests.created_time::date) as \"year\",\n" +
                "	DATE_PART('month', rs_one_time_requests.created_time::date) as \"month\"\n" +
                "FROM\n" +
                "	rs_one_time_requests\n" +
                "LEFT JOIN rs_departments ON rs_one_time_requests.department_id = rs_departments.\n" +
                "ID LEFT JOIN rs_request_statuses ON rs_one_time_requests.publication_status_id = rs_request_statuses.\n" +
                "ID LEFT JOIN rs_users AS creator ON creator.ID = UUID ( rs_one_time_requests.created_by_user )\n" +
                "LEFT JOIN rs_users AS modifier ON modifier.ID = UUID ( rs_one_time_requests.modified_by_user )";

        List<Map<String, Object>> maps = jdbcTemplate.queryForList(query);

        List<OneTimeRequestExtendedResultDto> resultDtos = maps.stream().map(e -> getOneTimeRequestExtendedResultDto(e)).collect(Collectors.toList());

        return resultDtos;
    }

    private OneTimeRequestExtendedResultDto getOneTimeRequestExtendedResultDto(Map<String, Object> e) {
        OneTimeRequestExtendedResultDto extendedResultDto = OneTimeRequestExtendedResultDto.builder()
                .author((String) e.get("author"))
                .content((String) e.get("content"))
                .createdByUser((String) e.get("created_by_user"))
                .createdTime(e.get("created_time").toString())
                .creatorFullName((String) e.get("creator_full_name"))
                .departmentName((String) e.get("department_name"))
                .id(((UUID) e.get("id")).toString())
                .identificator(String.valueOf(e.get("identificator")))
                .importance((String) e.get("importance"))
                .lastModifiedTime(e.get("last_modified_time").toString())
                .modifiedByUser((String) e.get("modified_by_user"))
                .modifierFullName((String) e.get("modifier_full_name"))
                .month(Month.of(((Double) e.get("month")).intValue()).getDisplayName(TextStyle.FULL_STANDALONE, new Locale("ru")))
                .petrostatRequestNumber((String) e.get("petrostat_request_number"))
                .petrostatText((String) e.get("petrostat_text"))
                .producerEmail((String) e.get("producer_email"))
                .producerPerson((String) e.get("producer_person"))
                .producerPhone((String) e.get("producer_phone"))
                .producerPosition((String) e.get("producer_position"))
                .resolution((String) e.get("resolution"))
                .publicationStatusId(((UUID) e.get("publication_status_id")).toString())
                .signatoryPerson((String) e.get("signatory_person"))
                .signatoryPosition((String) e.get("signatory_position"))
                .statusName((String) e.get("status_name"))
                .theme((String) e.get("theme"))
                .year(((Double) e.get("year")).intValue())
                .build();

        Optional.ofNullable(e.get("department_id")).ifPresent(o -> extendedResultDto.setDepartmentId(((UUID) o).toString()));

        return extendedResultDto;
    }
}