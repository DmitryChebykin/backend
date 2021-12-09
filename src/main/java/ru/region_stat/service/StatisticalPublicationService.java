package ru.region_stat.service;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.QueryHints;
import org.jetbrains.annotations.Nullable;
import org.modelmapper.ModelMapper;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.region_stat.aspects.rabbit.Doc;
import ru.region_stat.domain.dto.file.PublicationFileCreateDto;
import ru.region_stat.domain.dto.file.PublicationFileResultDto;
import ru.region_stat.domain.dto.statisticalPublication.StatisticalPublicationCreateDto;
import ru.region_stat.domain.dto.statisticalPublication.StatisticalPublicationPreviewDto;
import ru.region_stat.domain.dto.statisticalPublication.StatisticalPublicationResultDto;
import ru.region_stat.domain.dto.statisticalPublication.StatisticalPublicationUpdateDto;
import ru.region_stat.domain.entity.publicationFormat.PublicationFormatEntity;
import ru.region_stat.domain.entity.publicationType.PublicationTypeEntity;
import ru.region_stat.domain.entity.rubric.RubricEntity;
import ru.region_stat.domain.entity.statisticalPublication.StatisticalPublicationEntity;
import ru.region_stat.domain.repository.PublicationFileRepository;
import ru.region_stat.domain.repository.RubricRepository;
import ru.region_stat.domain.repository.StatisticalPublicationRepository;
import ru.region_stat.security.UserPrincipal;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticalPublicationService {

    public static final String UNKNOWN = "Не определено";
    public static final String PREFIX = "\"";
    public static final String SUFFIX = "\"";

    @Resource
    private StatisticalPublicationRepository statisticalPublicationRepository;

    @Resource
    private PublicationTypeService publicationTypeService;

    @Resource
    private RubricService rubricService;

    @Resource
    private PublicationFormatService publicationFormatService;

    @Resource
    private RubricRepository rubricRepository;

    @Resource
    private PublicationFileService publicationFileService;

    @Resource
    private PublicationFileRepository publicationFileRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private ModelMapper modelMapper;

    @Transactional
    public StatisticalPublicationResultDto create(StatisticalPublicationCreateDto statisticalPublicationCreateDto) {

        StatisticalPublicationEntity statisticalPublicationEntity = getStatisticalPublicationEntity(statisticalPublicationCreateDto);

        StatisticalPublicationEntity publicationEntity = statisticalPublicationRepository.save(statisticalPublicationEntity);
        return modelMapper.map(publicationEntity, StatisticalPublicationResultDto.class);
    }

    @Transactional
    public StatisticalPublicationEntity getStatisticalPublicationEntity(StatisticalPublicationCreateDto statisticalPublicationCreateDto) {
        StatisticalPublicationEntity statisticalPublicationEntity = modelMapper.map(statisticalPublicationCreateDto, StatisticalPublicationEntity.class);

        List<String> rubricEntityIdList = statisticalPublicationCreateDto.getRubricEntityId();
        List<RubricEntity> rubricEntityList = rubricEntityIdList.stream().map(id -> rubricService.getRubricEntityByStringId(id)).collect(Collectors.toList());

        statisticalPublicationEntity.setRubricEntities(rubricEntityList);

        PublicationFormatEntity publicationFormatEntity = publicationFormatService.getPublicationFormatByStringId(statisticalPublicationCreateDto.getFormatEntityId());
        statisticalPublicationEntity.setPublicationFormatEntity(publicationFormatEntity);

        PublicationTypeEntity publicationTypeEntity = publicationTypeService.getPublicationTypeByStringId(statisticalPublicationCreateDto.getPublicationTypeEntityId());
        statisticalPublicationEntity.setPublicationTypeEntity(publicationTypeEntity);

        return statisticalPublicationEntity;
    }

    @Transactional
    public void deleteById(UUID id) {
        statisticalPublicationRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public StatisticalPublicationResultDto findById(UUID id) {

        StatisticalPublicationEntity publicationEntity = statisticalPublicationRepository.findById(id).orElseThrow(ResourceNotFoundException::new);

        return modelMapper.map(publicationEntity, StatisticalPublicationResultDto.class);
    }

    @Transactional(readOnly = true)
    public List<StatisticalPublicationResultDto> getAll() {
        List<StatisticalPublicationEntity> resultList = entityManager.createQuery("select distinct p from StatisticalPublicationEntity p left join fetch p.publicationFileEntities left join fetch p.publicationTypeEntity " +
                "left join fetch p.publicationFormatEntity", StatisticalPublicationEntity.class)
                .setHint(QueryHints.PASS_DISTINCT_THROUGH, false)
                .getResultList();

        resultList = entityManager.createQuery("select distinct p from StatisticalPublicationEntity p left join fetch p.rubricEntities  where p in :resultList", StatisticalPublicationEntity.class)
                .setParameter("resultList", resultList)
                .setHint(QueryHints.PASS_DISTINCT_THROUGH, false)
                .getResultList();

        return resultList.stream()
                .map(publication -> modelMapper.map(publication, StatisticalPublicationResultDto.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StatisticalPublicationResultDto> getAllByRubric(UUID rubricId) {

        RubricEntity rubricEntity = rubricRepository.findById(rubricId).orElseThrow(RuntimeException::new);

        ArrayList<RubricEntity> rubricEntityList = new ArrayList<>();

        rubricEntityList.add(rubricEntity);

        List<StatisticalPublicationEntity> statisticalPublicationEntityList = statisticalPublicationRepository.findAllByRubricEntitiesIn(rubricEntityList);

        return statisticalPublicationEntityList.stream()
                .map(entity -> modelMapper.map(entity, StatisticalPublicationResultDto.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public StatisticalPublicationResultDto update(StatisticalPublicationUpdateDto statisticalPublicationUpdateDto, UUID id) {

        StatisticalPublicationEntity statisticalPublicationEntity = statisticalPublicationRepository.findById(id).orElseThrow(RuntimeException::new);

        //TODO: check set id
        modelMapper.map(statisticalPublicationUpdateDto, statisticalPublicationEntity);

        return modelMapper.map(statisticalPublicationEntity, StatisticalPublicationResultDto.class);
    }

    @Doc
    @Transactional
    public Map<PublicationFileResultDto, StatisticalPublicationResultDto> saveByFile(File file) {
        PublicationFileResultDto publicationFileResultDto = null;
        StatisticalPublicationEntity statisticalPublicationEntity = null;
        StatisticalPublicationResultDto statisticalPublicationResultDto = null;

        Map<PublicationFileResultDto, StatisticalPublicationResultDto> resultMap = new HashMap<>(Collections.emptyMap());

        if (file.exists()) {
            String fileName = file.getName();

            String code;
            String complexCode;

            if (fileName.startsWith("ПЗ")) {
                code = StringUtils.substringBetween(fileName, "ПЗ", "_");
                complexCode = "req_" + code;
            } else {
                code = StringUtils.substring(fileName, 0, 6);
                complexCode = "pub_" + code;
            }

            if (code.length() > 0 && publicationTypeService.existByCode(complexCode)) {

                PublicationTypeEntity publicationTypeByCode = publicationTypeService.getPublicationTypeByCode(complexCode);

                PublicationFormatEntity publicationFormatEntity = publicationTypeByCode.getPublicationFormatEntity();

                String publicationTypeByCodeName = publicationTypeByCode.getName();

                List<RubricEntity> rubricEntityList = publicationTypeByCode.getRubricEntityList();

                List<UUID> uuidList = rubricEntityList.stream().map(r -> r.getId()).collect(Collectors.toList());

                List<RubricEntity> rubricEntitiesByIds = rubricRepository.findByIdIsIn(uuidList);

                statisticalPublicationEntity = StatisticalPublicationEntity.builder()
                        .publicationTypeEntity(publicationTypeByCode)
                        .publicationFormatEntity(publicationFormatEntity)
                        .name(publicationTypeByCodeName)
                        .complexName(publicationTypeByCodeName + " (" + publicationFormatEntity.getName() + ")")
                        .rubricEntities(rubricEntitiesByIds)
                        .isArchive(false)
                        .build();

                statisticalPublicationEntity = statisticalPublicationRepository.save(statisticalPublicationEntity);

                long bytesSize = 0;

                try {
                    bytesSize = Files.size(file.getAbsoluteFile().toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String parsedFileData = StringUtils.substringAfter(fileName, "_");
                String year = StringUtils.substring(parsedFileData, 2, 6);
                String month = StringUtils.substring(parsedFileData, 0, 2);

                byte[] content = new byte[0];

                try {
                    content = Files.readAllBytes(file.getAbsoluteFile().toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String id = statisticalPublicationEntity.getId().toString();

                PublicationFileCreateDto publicationFileCreateDto = PublicationFileCreateDto.builder()
                        .fileName(fileName)
                        .fileSize(bytesSize)
                        .fileExtension(FilenameUtils.getExtension(fileName))
                        .publicationId(id)
                        .day(LocalDate.now().getDayOfMonth())
                        .year(Integer.parseInt(year))
                        .month(Integer.parseInt(month))
                        .content(content)
                        .isArchive(false)
                        .build();

                publicationFileResultDto = publicationFileService.create(publicationFileCreateDto);

                statisticalPublicationEntity.setPublicationFileEntities(publicationFileRepository.getByStatisticalPublicationId(statisticalPublicationEntity.getId()).orElseThrow(RuntimeException::new));
            }

            statisticalPublicationResultDto = modelMapper.map(statisticalPublicationEntity, StatisticalPublicationResultDto.class);

            file.delete();
        }

        if (publicationFileResultDto != null) {
            resultMap.put(publicationFileResultDto, statisticalPublicationResultDto);
        }

        return resultMap;
    }

    @Transactional
    public StatisticalPublicationPreviewDto getStatisticalPublicationPreviewDto(Path file) {
        String fileName = file.getFileName().toString();

        String code;
        String complexCode;

        if (fileName.startsWith("ПЗ")) {
            code = StringUtils.substringBetween(fileName, "ПЗ", "_");
            complexCode = "req_" + code;
        } else {
            code = StringUtils.substring(fileName, 0, 6);
            complexCode = "pub_" + code;
        }

        PublicationTypeEntity publicationTypeByCode;
        PublicationFormatEntity publicationFormatEntity;

        List<String> rubricEntityNameList = null;

        long bytesSize = 0;

        String name = UNKNOWN;
        String formatEntityName = UNKNOWN;
        String subscription = UNKNOWN;

        String[] splitFileName = FilenameUtils.getBaseName(fileName).split("_");
        int length = splitFileName.length;
        boolean isDateValid = false;

        Integer month = null;
        Integer year = null;

        if (length == 2) {
            String monthYear = splitFileName[1];

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMyyyy", new Locale("ru"))
                    .withResolverStyle(ResolverStyle.STRICT);

            try {
                TemporalAccessor accessor = dateFormatter.parse(monthYear);
                month = accessor.get(ChronoField.MONTH_OF_YEAR);
                year = accessor.get(ChronoField.YEAR_OF_ERA);
                isDateValid = true;
            } catch (DateTimeParseException e) {
            }
        }

        boolean validFileName = (length == 2 && isDateValid == true);

        if (code.length() > 0 && validFileName && publicationTypeService.existByCode(complexCode)) {
            publicationTypeByCode = publicationTypeService.getPublicationTypeByCode(complexCode);
            publicationFormatEntity = publicationTypeByCode.getPublicationFormatEntity();
            List<RubricEntity> rubricEntityList = publicationTypeByCode.getRubricEntityList();
            rubricEntityNameList = rubricEntityList.stream().map(RubricEntity::getName).collect(Collectors.toList());
            name = publicationTypeByCode.getName();
            formatEntityName = publicationFormatEntity.getName();
            subscription = publicationTypeByCode.getSubscription().getName();
            try {
                bytesSize = Files.size(file.toFile().getAbsoluteFile().toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String monthName = null;

        if (month != null) {
            monthName = Month.of(month).getDisplayName(TextStyle.FULL_STANDALONE, new Locale("ru"));
        }

        return StatisticalPublicationPreviewDto.builder()
                .name(name)
                .publicationFormatEntityName(formatEntityName)
                .publicationTypeEntityName(name)
                .fileName(fileName)
                .fileSize(bytesSize)
                .rubricEntityNameList(rubricEntityNameList)
                .publicationTypeEntitySubscription(subscription)
                .year(year)
                .month(month)
                .monthName(monthName)
                .build();
    }

    @Transactional
    public List<StatisticalPublicationResultDto> invertIsArchiveOfPublicationList(List<String> idList) {
        List<UUID> uuidList = idList.stream().map(UUID::fromString).collect(Collectors.toList());

        UUID uuid = getUuid();

        List<StatisticalPublicationEntity> statisticalPublicationEntities = statisticalPublicationRepository.invertIsArchive(uuidList, uuid, new Date());

        statisticalPublicationEntities.stream().filter(StatisticalPublicationEntity::getIsArchive)
                .forEach(entity -> entity.setArchiveRubricName(entity.getRubricEntities().stream().map(RubricEntity::getName).collect(Collectors.joining("\", \"", "\"", "\""))));

        return statisticalPublicationEntities.stream().map(publication -> modelMapper.map(publication, StatisticalPublicationResultDto.class)).collect(Collectors.toList());
    }

    @Doc
    @Transactional(readOnly = true)
    public Map<PublicationFileResultDto, StatisticalPublicationResultDto> getMapForRabbitAspect(UUID id) {
        Map<PublicationFileResultDto, StatisticalPublicationResultDto> objectMap = new HashMap<>(Collections.emptyMap());
        StatisticalPublicationResultDto statisticalPublicationResultDto = findById(id);
        List<PublicationFileResultDto> publicationFileResultDto = publicationFileService.getAllByStatPubId(id);
        objectMap.put(publicationFileResultDto.get(0), statisticalPublicationResultDto);
        return objectMap;
    }

    @Transactional
    public List<String> getArchivedRubricNames() {
        List<String> archiveRubricsNames = statisticalPublicationRepository.getArchiveRubricsNames();
        Set<String> resultSet = new HashSet<>();
        for (String s : archiveRubricsNames) {
            String[] split = s.split(",");
            Set<String> collect = Arrays.stream(split).collect(Collectors.toSet());
            resultSet.addAll(collect);
        }
        List<String> resultList = new ArrayList<>(resultSet);

        List<String> collect = resultList.stream().map(p -> p.replaceAll("\"", "")).collect(Collectors.toList());

        return collect;
    }

    @Transactional(readOnly = true)
    public List<StatisticalPublicationResultDto> getPublicationsByArchivedRubricNames(String tag) {
        tag = PREFIX + tag + SUFFIX;

        List<StatisticalPublicationEntity> statisticalPublicationEntities = statisticalPublicationRepository.getPublicationsByArchivedRubricNames(tag);

        return statisticalPublicationEntities.stream().map(publication -> modelMapper.map(publication, StatisticalPublicationResultDto.class)).collect(Collectors.toList());
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
}