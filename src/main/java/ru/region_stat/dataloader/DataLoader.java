package ru.region_stat.dataloader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.region_stat.domain.dto.department.DepartmentCreateDto;
import ru.region_stat.domain.dto.file.PublicationFileCreateDto;
import ru.region_stat.domain.dto.format.PublicationFormatCreateDto;
import ru.region_stat.domain.dto.municipalFilesReference.MunicipalFilesReferencePreviewWithYearDto;
import ru.region_stat.domain.dto.oneTimeRequest.OneTimeRequestCreateDto;
import ru.region_stat.domain.dto.oneTimeRequestStatus.OneTimeRequestStatusCreateDto;
import ru.region_stat.domain.dto.publicationType.PublicationTypeCreateDto;
import ru.region_stat.domain.dto.rubric.RubricCreateDto;
import ru.region_stat.domain.dto.statisticalPublication.StatisticalPublicationCreateDto;
import ru.region_stat.domain.entity.MoFiles.MunicFileContentEntity;
import ru.region_stat.domain.entity.MoFiles.MunicFileContentRepository;
import ru.region_stat.domain.entity.MoFiles.MunicFileEntity;
import ru.region_stat.domain.entity.MoFiles.MunicFileRepository;
import ru.region_stat.domain.entity.department.DepartmentEntity;
import ru.region_stat.domain.entity.municipalFiles.IndicatorEntity;
import ru.region_stat.domain.entity.municipalFiles.MunicipalFilesReferenceEntity;
import ru.region_stat.domain.entity.oneTimeRequest.Importance;
import ru.region_stat.domain.entity.oneTimeRequest.OneTimeRequestEntity;
import ru.region_stat.domain.entity.oneTimeRequest.OneTimeRequestStatusEntity;
import ru.region_stat.domain.entity.publicationFormat.PublicationFormatEntity;
import ru.region_stat.domain.entity.publicationType.PublicationTypeEntity;
import ru.region_stat.domain.entity.publicationType.Subscription;
import ru.region_stat.domain.entity.rubric.RubricEntity;
import ru.region_stat.domain.entity.statisticalPublication.StatisticalPublicationEntity;
import ru.region_stat.domain.entity.user.UserEntity;
import ru.region_stat.domain.repository.*;
import ru.region_stat.service.*;
import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static ru.region_stat.domain.entity.BaseEntity.CREATED_TIME;
import static ru.region_stat.domain.entity.BaseEntity.LAST_MODIFIED_TIME;

@Component
@Profile("import")
public class DataLoader {
    public static final String TRUNCATE_TABLE_TEMPLATE_QUERY = "TRUNCATE TABLE %s RESTART IDENTITY CASCADE;";
    public static final String REQUEST = "????????????";
    private static final String DEPARTMENTS_QUERY = "SELECT * FROM  sharepoint_departments";
    private static final String USERS_QUERY = "SELECT * FROM  sharepoint_users";
    private static final String FORMATS_QUERY = "SELECT DISTINCT ??????????????value FROM  sharepoint_statistical_publication WHERE ??????????????value IS NOT NULL";
    private static final String PUB_FILES_COUNT_QUERY = "SELECT COUNT(*) FROM  sharepoint_statistical_publication";
    private static final String PUB_FILES_COUNT_TEMPLATE_QUERY = "SELECT * FROM  sharepoint_statistical_publication WHERE id =";
    private static final String ONE_TIME_REQUESTS_QUERY = "SELECT * FROM sharepoint_one_time_requests";
    private static final String ONE_TIME_REQUESTS_ATTACHMENTS_QUERY = "SELECT * FROM sharepoint_one_time_requests_attachments WHERE itemid =";
    private static final String REGULAR_REQUESTS_REGISTRY_QUERY = "SELECT * FROM sharepoint_regular_request_registry";
    private static final String PUBLICATIONS_REGISTRY_QUERY = "SELECT * FROM sharepoint_statistical_publication_reference";
    private final List<String> monthList = Arrays.asList("????????????", "??????????????", "????????", "????????????", "??????", "????????", "????????", "????????????", "????????????????", "??????????????", "????????????", "??????????????");
    @Value("${sharepoint.literate-rubric}")
    private List<String> literateRubricList;
    @Value("${sharepoint.rubric}")
    private List<String> rubricList;
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private DepartmentService departmentService;
    @Resource
    private UserRepository userRepository;
    @Resource
    private PublicationTypeService publicationTypeService;
    @Resource
    private RubricService rubricService;
    @Resource
    private RubricRepository rubricRepository;
    @Resource
    private PublicationFileService publicationFileService;
    @Resource
    private StatisticalPublicationService statisticalPublicationService;
    @Resource
    private PublicationFormatService publicationFormatService;
    @Resource
    private OneTimeRequestService oneTimeRequestService;
    @Resource
    private DepartmentRepository departmentRepository;
    @Resource
    private StatisticalPublicationRepository statisticPublicationRepository;
    @Resource
    private PublicationFormatRepository publicationFormatRepository;
    @Resource
    private PublicationTypeRepository publicationTypeRepository;
    @Resource
    private OneTimeRequestStatusService oneTimeRequestStatusService;
    @Resource
    private OneTimeRequestRepository oneTimeRequestRepository;
    @Resource
    private MunicipalFilesReferenceRepository municipalFilesReferenceRepository;
    @Resource
    private MunicFileRepository municFileRepository;
    @Resource
    private MunicFileContentRepository municFileContentRepository;
    @Resource
    private MunicipalFilesReferenceService municipalFilesReferenceService;

    @Value("${sharepoint.basePath}")
    private String basePath;

    @Value("${sharepoint.regexForYear}")
    private String regexForYear;

    @Value("${sharepoint.formatForFileTime}")
    private String formatForFileTime;

    @Resource
    private IndicatorEntityRepository indicatorEntityRepository;

    public void run() {

        truncateTables();
        loadData();

        System.out.println("???????????????? ???????????? ??????????????????");
    }

    private void loadData() {
        System.out.println("???????????????? ??????????????????????????");
        loadDepartments();
        System.out.println("???????????????? ??????????????????????????");
        loadUsers();
        System.out.println("???????????????? ????????????");
        loadRubrics();
        System.out.println("???????????????? ????????????????");
        loadFormats();
        System.out.println("???????????????? ?????????? ????????????????????");
        loadPublicationTypes();
        System.out.println("???????????????? ???????????????????? ?? ??????????????");
        loadPubsAndFiles();
        createMissedPublicationTypes();
        System.out.println("???????????????? ?????????????? ????????????????");
        loadOneTimeRequest();
    }

    private void truncateTables() {
        jdbcTemplate.execute(String.format(TRUNCATE_TABLE_TEMPLATE_QUERY, "rs_departments"));
        jdbcTemplate.execute(String.format(TRUNCATE_TABLE_TEMPLATE_QUERY, "rs_contents"));
        jdbcTemplate.execute(String.format(TRUNCATE_TABLE_TEMPLATE_QUERY, "rs_pub_files"));
        jdbcTemplate.execute(String.format(TRUNCATE_TABLE_TEMPLATE_QUERY, "rs_pub_formats"));
        jdbcTemplate.execute(String.format(TRUNCATE_TABLE_TEMPLATE_QUERY, "rs_pub_rubrics"));
        jdbcTemplate.execute(String.format(TRUNCATE_TABLE_TEMPLATE_QUERY, "rs_pub_types"));
        jdbcTemplate.execute(String.format(TRUNCATE_TABLE_TEMPLATE_QUERY, "rs_publications"));
        jdbcTemplate.execute(String.format(TRUNCATE_TABLE_TEMPLATE_QUERY, "rs_users"));
        jdbcTemplate.execute(String.format(TRUNCATE_TABLE_TEMPLATE_QUERY, "rs_rubric_publication_link"));
        jdbcTemplate.execute(String.format(TRUNCATE_TABLE_TEMPLATE_QUERY, "rs_rubric_type_link"));
        jdbcTemplate.execute(String.format(TRUNCATE_TABLE_TEMPLATE_QUERY, "rs_one_time_request_files"));
        jdbcTemplate.execute(String.format(TRUNCATE_TABLE_TEMPLATE_QUERY, "rs_one_time_request_files_contents"));
        jdbcTemplate.execute(String.format(TRUNCATE_TABLE_TEMPLATE_QUERY, "rs_one_time_requests"));
    }

    @Transactional
    public void createMissedPublicationTypes() {
        List<Map<String, Object>> requestData = jdbcTemplate.queryForList("SELECT DISTINCT ON (name) name, complex_name, id from  rs_publications WHERE type_id IS NULL");
        int i = 0;
        for (Map<String, Object> map : requestData) {
            i++;
            String complexName = (String) map.get("complex_name");
            String name = (String) map.get("name");

            LinkedList<String> matchList = new LinkedList<>();
            Pattern regex = Pattern.compile("\\((.*?)\\)");
            Matcher regexMatcher = regex.matcher(complexName);

            while (regexMatcher.find()) {
                matchList.add(regexMatcher.group(1));
            }

            String formatName = matchList.getLast();
            PublicationFormatEntity publicationFormatEntity;

            if (publicationFormatRepository.existsByName(formatName)) {
                publicationFormatEntity = publicationFormatRepository.findByName(formatName).get();
            } else {
                publicationFormatEntity = PublicationFormatEntity.builder().name(formatName).build();
                publicationFormatEntity = publicationFormatRepository.save(publicationFormatEntity);
            }

            List<Map<String, Object>> publicationList = jdbcTemplate.queryForList("SELECT * from rs_publications WHERE name = '" + name + "'");

            List<UUID> uuid = publicationList.stream().map(e -> (UUID) e.get("id")).collect(Collectors.toList());

            String inSql = String.join(",", Collections.nCopies(uuid.size(), "?"));

            List<Map<String, Object>> rubricLink = jdbcTemplate.queryForList(String.format("SELECT rubric_id from rs_rubric_publication_link WHERE publication_id IN (%s)", inSql), uuid.toArray());

            List<UUID> uuidList = rubricLink.stream().map(e -> ((UUID) e.get("rubric_id"))).collect(Collectors.toList());
            List<RubricEntity> rubricEntityList = rubricRepository.findByIdIsIn(uuidList);

            String prefix = (formatName.equals(REQUEST)) ? "req_" : "pub_";
            Subscription subscriptionType = (formatName.equals(REQUEST)) ? Subscription.REQUEST : Subscription.PUBLICATION;
            LocalDate localDate = LocalDate.now();

            PublicationTypeEntity publicationTypeEntity = PublicationTypeEntity.builder()
                    .name(name)
                    .code(prefix + localDate.getYear() + String.format("%02d", localDate.getMonth().getValue()) + String.format("%02d", localDate.getDayOfMonth()) + "_" + i)
                    .subscription(subscriptionType)
                    .build();

            PublicationTypeEntity typeEntity = publicationTypeRepository.save(publicationTypeEntity);
            typeEntity.setPublicationFormatEntity(publicationFormatEntity);
            typeEntity.setRubricEntityList(rubricEntityList);
            publicationTypeRepository.save(typeEntity);

            StatisticalPublicationEntity statisticalPublicationEntity = statisticPublicationRepository.findById((UUID) map.get("id")).get();
            statisticalPublicationEntity.setPublicationTypeEntity(typeEntity);
        }
    }

    @Transactional
    public void loadOneTimeRequest() {
        List<Map<String, Object>> requestData = getDataList(ONE_TIME_REQUESTS_QUERY);
        int size = requestData.size();
        int i = 0;

        for (Map<String, Object> map : requestData) {
            i++;

            String departmentId = (String) map.get("????????????????????????????????????????????????????id");
            List<Map<String, Object>> departmentList = getDataList("SELECT * from sharepoint_departments WHERE ?????????????????????????? = '" + departmentId + "'");

            DepartmentEntity departmentEntity = null;
            String departmentName = null;
            String departmentEntityId = null;

            if (!departmentList.isEmpty()) {
                departmentName = (String) departmentList.get(0).get("????????????????");
                departmentEntity = departmentService.getByName(departmentName);

                if (departmentEntity == null) {
                    UUID id = departmentService.create(DepartmentCreateDto.builder().name(departmentName).build()).getId();
                    departmentEntity = departmentService.getById(id);
                }

                departmentEntityId = departmentEntity.getId().toString();
            }

            String statusString = (String) map.get("??????????????????????????value");

            OneTimeRequestStatusEntity oneTimeRequestStatusEntity;

            if (!oneTimeRequestStatusService.existsByName(statusString)) {
                UUID id = oneTimeRequestStatusService.create(OneTimeRequestStatusCreateDto.builder().name(statusString).build()).getId();
                oneTimeRequestStatusEntity = oneTimeRequestStatusService.getById(id);
            } else {
                oneTimeRequestStatusEntity = oneTimeRequestStatusService.getByName(statusString);
            }

            OneTimeRequestCreateDto oneTimeRequestCreateDto = OneTimeRequestCreateDto.builder()
                    .content((String) map.computeIfPresent("????????????????????????????????????????????????", (k, v) -> (v.equals("null") ? null : v)))
                    .petrostatText((String) map.computeIfPresent("????????????????????????????????????????????", (k, v) -> (v.equals("null") ? null : v)))
                    .identificator(Integer.parseInt((String) map.get("??????????????????????????")))
                    .resolution((String) map.computeIfPresent("??????????????????", (k, v) -> (v.equals("null") ? null : v)))
                    .departmentEntityId(departmentEntityId)
                    .petrostatNumber((String) map.computeIfPresent("????????????????????????????????????????????", (k, v) -> (v.equals("null") ? null : v)))
                    .importanceName(Importance.of(((String) map.get("????????????????value"))).toString())
                    .producerEmail((String) map.computeIfPresent("??????????????????????????????????????????????????????", (k, v) -> (v.equals("null") ? null : v)))
                    .producerPerson((String) map.computeIfPresent("????????????????????????????????????????", (k, v) -> (v.equals("null") ? null : v)))
                    .producerPhone((String) map.computeIfPresent("????????????????????????????????????????????????????????", (k, v) -> (v.equals("null") ? null : v)))
                    .producerPosition((String) map.computeIfPresent("????????????????????????????????????????", (k, v) -> (v.equals("null") ? null : v)))
                    .signatoryPerson((String) map.computeIfPresent("??????????????????????????????????", (k, v) -> (v.equals("null") ? null : v)))
                    .signatoryPosition((String) map.computeIfPresent("??????????????????????????????????????????", (k, v) -> (v.equals("null") ? null : v)))
                    .theme((String) map.computeIfPresent("??????????????????????", (k, v) -> (v.equals("null") ? null : v)))
                    .author((String) map.computeIfPresent("????????????????????????", (k, v) -> (v.equals("null") ? null : v)))
                    .oneTimeRequestStatusEntityId(oneTimeRequestStatusEntity.getId().toString())
                    .build();

            List<MultipartFile> multipartFileArrayList = new ArrayList<>();

            String requestId = map.get("id").toString();
            List<Map<String, Object>> attachmentsData = getDataList(ONE_TIME_REQUESTS_ATTACHMENTS_QUERY + "'" + requestId + "'");

            for (Map<String, Object> attachment : attachmentsData) {
                byte[] content = (byte[]) attachment.get("file_content");

                MultipartFile file;

                file = new MockMultipartFile("data", (String) attachment.get("name"), "application/octet-stream", content);

                multipartFileArrayList.add(file);
            }

            UUID id = oneTimeRequestService.saveFromSharePoint(oneTimeRequestCreateDto, multipartFileArrayList).getId();
            OneTimeRequestEntity oneTimeRequestEntity = oneTimeRequestRepository.findById(id).get();
            oneTimeRequestEntity.setInternalNumber(i);
            setBaseColumns(map, "rs_one_time_requests", id);
            System.out.println("?????????????????? " + i + " ???????????????? ???? " + size);
        }
    }

    @Transactional
    public void loadFormats() {
        List<Map<String, Object>> data = getDataList(FORMATS_QUERY);
        String formatName;
        for (Map<String, Object> map : data) {
            formatName = (String) map.get("??????????????value");

            if (!publicationFormatService.existsByName(formatName)) {
                publicationFormatService.create(PublicationFormatCreateDto.builder().name(formatName).build());
            }
        }
    }

    @Transactional
    public void loadPubsAndFiles() {
        List<Map<String, Object>> data = getDataList(PUB_FILES_COUNT_QUERY);
        Map<String, Object> stringObjectMap = data.get(0);
        Long fileRowsCount = (Long) stringObjectMap.get("count");
        StringBuilder stringBuilder = new StringBuilder(PUB_FILES_COUNT_TEMPLATE_QUERY);
        int stringBuilderLength = stringBuilder.length();

        byte[] fileContent = new byte[0];

        for (long i = 1; i <= fileRowsCount; i++) {
            stringBuilder.append("'").append(i).append("'");
            List<Map<String, Object>> fileData = getDataList(stringBuilder.toString());

            Map<String, Object> pubMap = fileData.get(0);

            if (pubMap.containsKey("file_content")) {
                fileContent = (byte[]) pubMap.get("file_content");
            }

            stringBuilder.setLength(stringBuilderLength);

            String publicationFormat = (String) pubMap.get("??????????????value");
            if (publicationFormat == null) {
                publicationFormat = (String) pubMap.get("????????????????????value");
            }

            String rubricName = "??????????????";

            String rubricJson = (String) pubMap.get("??????????????");

            if (rubricJson != null) {

                JSONObject jsonObject = new JSONObject(rubricJson);

                String rubricJsonType = (String) jsonObject.getJSONObject("__metadata").get("type");

                rubricName = rubricJsonType.replace("Microsoft.SharePoint.DataService.", "").replace("??????????????Value", "");

                if (rubricName.equals("????????????????????????????????????????????????????????Item")) {
                    rubricName = "??????????????";
                }
            }

            int indexRubricName = rubricList.indexOf(rubricName);
            rubricName = literateRubricList.get(indexRubricName);

            String formatId = publicationFormatService.getIdByName(publicationFormat).toString();

            String rubricId = rubricService.getIdByName(rubricName).toString();

            String typeId = null;

            if (publicationTypeService.existsByName((String) pubMap.get("????????????????"))) {
                typeId = publicationTypeService.getIdByName((String) pubMap.get("????????????????")).orElse(null).toString();
            }

            String pubName = pubMap.get("????????????????????????") == null
                    ? (String) pubMap.get("??????????????????????")
                    : (String) pubMap.get("????????????????????????");

            String restLink = (String) pubMap.get("rest_link");

            Boolean isArchive = restLink.contains("arhive");

            StatisticalPublicationCreateDto statisticalPublicationCreateDto = StatisticalPublicationCreateDto.builder()
                    .formatEntityId(formatId)
                    .complexName(pubName)
                    .name((String) pubMap.get("????????????????"))
                    .rubricEntityId(Collections.singletonList(rubricId))
                    .publicationTypeEntityId(typeId)
                    .isArchive(isArchive)
                    .build();

            UUID id = statisticalPublicationService.create(statisticalPublicationCreateDto).getId();
            setBaseColumns(pubMap, "rs_publications", id);
            String publicationUuidString = id.toString();

            String monthString = ((String) pubMap.get("??????????")).substring(3);
            int monthNumber = monthList.indexOf(monthString.toLowerCase()) + 1;
            String fileName = (String) pubMap.get("??????");
            String dayNumber = ((String) pubMap.get("??????????")).substring(0, 2).replaceAll("^0+(?!$)", "");
            Integer year = null;
            String yearString = (String) pubMap.get("??????");

            if (yearString != null && yearString.length() == 4) {
                year = Integer.parseInt(yearString);
            }

            PublicationFileCreateDto publicationFileCreateDto = PublicationFileCreateDto.builder()
                    .day(Integer.parseInt(dayNumber))
                    .content(fileContent)
                    .fileExtension(FilenameUtils.getExtension(fileName))
                    .fileName(fileName)
                    .fileSize((long) fileContent.length)
                    .isArchive(isArchive)
                    .month(monthNumber)
                    .publicationId(publicationUuidString)
                    .year(year)
                    .build();

            publicationFileService.create(publicationFileCreateDto);
            stringBuilder.setLength(stringBuilderLength);
            System.out.println("?????????????????? " + i + " ???? " + fileRowsCount);
        }
    }

    @Transactional
    public void loadRubrics() {
        for (String rubricName : literateRubricList) {
            if (!rubricService.existsByName(rubricName)) {
                rubricService.create(RubricCreateDto.builder()
                        .name(rubricName)
                        .isArchive(false)
                        .build());
            }
        }
    }

    @Transactional
    public void loadDepartments() {
        List<Map<String, Object>> departmentsDataList = getDataList(DEPARTMENTS_QUERY);
        String departmentName;

        for (Map<String, Object> departmentMap : departmentsDataList) {
            String creatorUserId = (String) departmentMap.get("????????????????????id");
            UserEntity creatorUser = createOrGetUserByShPntId(creatorUserId);

            String modUserId = (String) departmentMap.get("??????????????????????id");
            UserEntity modUser = createOrGetUserByShPntId(modUserId);

            DepartmentEntity newDepartment = DepartmentEntity.builder()
                    .name(departmentName = (String) departmentMap.get("????????????????"))
                    .build();

            newDepartment.setCreatedByUser(creatorUser.getId().toString());
            newDepartment.setModifiedByUser(modUser.getId().toString());

            if (!departmentRepository.existsByName(departmentName)) {

                UUID id = departmentRepository.save(newDepartment).getId();
                setBaseColumns(departmentMap, "rs_departments", id);
            }
        }
    }

    @Transactional
    public UserEntity createOrGetUserByShPntId(String creatorUserId) {
        List<Map<String, Object>> userDataList = getDataList
                ("SELECT * FROM sharepoint_users WHERE ?????????????????????????? = '" + creatorUserId + "'");
        Map<String, Object> userMap = userDataList.get(0);
        String creatorNtlmLogin = (String) userMap.get("??????????????????????????");
        UserEntity creatorUserEntity;

        if (userRepository.existsByNtlmLogin(creatorNtlmLogin)) {
            creatorUserEntity = userRepository.findByNtlmLoginIs(creatorNtlmLogin);
        } else {
            creatorUserEntity = UserEntity.builder()
                    .department(null)
                    .email((String) userMap.get("??????????????????????????????????????????"))
                    .familyName(null)
                    .fullName((String) userMap.get("????????????????"))
                    .isActive(!Boolean.parseBoolean((String) userMap.get("????????????")))
                    .login(null)
                    .name(null)
                    .ntlmLogin(creatorNtlmLogin)
                    .password(null)
                    .position((String) userMap.get("??????????????????"))
                    .surname(null)
                    .workPhone(null)
                    .build();

            creatorUserEntity = userRepository.save(creatorUserEntity);

            setBaseColumns(userMap, "rs_users", creatorUserEntity.getId());
        }

        return creatorUserEntity;
    }

    private void setBaseColumns(Map<String, Object> dataMap, String tableName, UUID id) {
        UUID authorUserId = createOrGetUserByShPntId((String) dataMap.get("????????????????????id")).getId();
        UUID modUserId = createOrGetUserByShPntId((String) dataMap.get("??????????????????????id")).getId();

        Object[] databind = {authorUserId.toString(), modUserId.toString(), id};

        jdbcTemplate.update("UPDATE " + tableName + " SET created_by_user = ?, modified_by_user = ? WHERE id = ?", databind);

        long millisecond = Long.parseLong(((String) dataMap.get("????????????")).replaceAll("[^\\d.]", ""));
        Date creationDate = Date.from(Instant.ofEpochMilli(millisecond));

        millisecond = Long.parseLong(((String) dataMap.get("??????????????")).replaceAll("[^\\d.]", ""));
        Date modifiedDate = Date.from(Instant.ofEpochMilli(millisecond));

        databind = new Object[]{creationDate, modifiedDate, id};
        jdbcTemplate.update("UPDATE " + tableName + " SET " + CREATED_TIME + " = ?, " + LAST_MODIFIED_TIME + " = ? WHERE id = ?", databind);
    }

    @Transactional
    public void loadUsers() {
        List<Map<String, Object>> userDataList = getDataList(USERS_QUERY);

        for (Map<String, Object> userMap : userDataList) {
            String ntlmLogin = (String) userMap.get("??????????????????????????");
            UserEntity userEntity;

            if (userRepository.existsByNtlmLogin(ntlmLogin)) {
                userEntity = userRepository.findByNtlmLoginIs(ntlmLogin);
            } else {
                userEntity = UserEntity.builder()
                        .email((String) userMap.get("??????????????????????????????????????????"))
                        .fullName((String) userMap.get("????????????????"))
                        .isActive(!Boolean.parseBoolean((String) userMap.get("????????????")))
                        .ntlmLogin(ntlmLogin)
                        .position((String) userMap.get("??????????????????"))
                        .departmentSubdivision((String) userMap.get("??????????"))
                        .build();
            }

            DepartmentEntity departmentEntity = departmentRepository.findByName((String) userMap.get("??????????")).orElse(null);
            userEntity.setDepartment(departmentEntity);

            userEntity = userRepository.save(userEntity);

            setBaseColumns(userMap, "rs_users", userEntity.getId());
        }
    }

    private List<Map<String, Object>> getDataList(String queryString) {
        return jdbcTemplate.queryForList(queryString);
    }

    @Transactional
    public void loadPublicationTypes() {
        List<Map<String, Object>> dataMapList = getDataList(REGULAR_REQUESTS_REGISTRY_QUERY);
        UUID idByName = publicationFormatService.getIdByName(REQUEST);

        for (Map<String, Object> map : dataMapList) {

            String rubric = (String) map.get("??????????????");
            ObjectMapper mapper = new ObjectMapper();
            JsonNode actualObj = null;

            try {
                actualObj = mapper.readTree(rubric);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            JsonNode atResultNode = actualObj.at("/results");
            List<String> rubricIds = new ArrayList<>();

            if (atResultNode.isArray() && atResultNode.size() > 0) {
                Iterator<JsonNode> elements = atResultNode.elements();
                while (elements.hasNext()) {
                    String asText = elements.next().get("????????????????").asText();

                    if (asText.equals("??????????")) {
                        asText = "?????????? ?? ????????????????";
                    }

                    String rubricUuid = rubricService.getIdByName(asText).toString();

                    rubricIds.add(rubricUuid);
                }
            }

            String name = (String) map.get("????????????????");
            String highLevelName;

            if ((Boolean.parseBoolean((String) map.get("??????????????????????????????????????????????????????????")))) {
                String highLevelNumber = (String) map.get("??????????????????????????????????????");
                highLevelName = (String) getDataList("SELECT ???????????????? FROM sharepoint_regular_request_registry WHERE ?????????????????????????? = " + "'" + highLevelNumber + "'").get(0).get("????????????????");
                name = highLevelName + " " + name;
            }

            PublicationTypeCreateDto publicationTypeCreateDto = PublicationTypeCreateDto.builder()
                    .code("req_" + map.get("??????????????????????????"))
                    .name(name)
                    .period((String) map.get("????????????????????????????????????????????????????"))
                    .submissionTime((String) map.get("????????????????????????????????????"))
                    .formatEntityId(idByName.toString())
                    .rubricEntityId(rubricIds)
                    .subscription(Subscription.REQUEST.name())
                    .build();

            UUID id = publicationTypeService.save(publicationTypeCreateDto).getId();
            setBaseColumns(map, "rs_pub_types", id);
        }

        dataMapList = getDataList(PUBLICATIONS_REGISTRY_QUERY);

        for (Map<String, Object> map : dataMapList) {

            String rubric = (String) map.get("??????????????");

            String valueRubric = (String) map.get("??????????????Value");

            if (!publicationFormatService.existsByName(valueRubric)) {
                publicationFormatService.create(PublicationFormatCreateDto.builder().name(valueRubric).build());
            }

            idByName = publicationFormatService.getIdByName(valueRubric);

            JsonNode actualObj = null;
            try {
                ObjectMapper mapper = new ObjectMapper();
                actualObj = mapper.readTree(rubric);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            JsonNode atResultNode = actualObj.at("/results");
            List<String> rubricIds = new ArrayList<>();

            if (atResultNode.isArray() && atResultNode.size() > 0) {
                Iterator<JsonNode> elements = atResultNode.elements();
                while (elements.hasNext()) {
                    String asText = elements.next().get("????????????????").asText();

                    if (asText.equals("??????????")) {
                        asText = "?????????? ?? ????????????????";
                    }
                    String idString = rubricService.getIdByName(asText).toString();

                    rubricIds.add(idString);
                }
            }

            List<Map<String, Object>> dataList = getDataList("SELECT * FROM sharepoint_statistical_publication_reference WHERE \"??????????????????????????\" = " + "'" + map.get("??????????????????????????") + "'" + "ORDER BY \"??????????????\" DESC");

            Integer identifier = (Integer) map.get("id");
            String identString = (String) map.get("??????????????????????????");

            if (dataList.size() > 1) {
                if (((Integer) dataList.get(0).get("id")).intValue() != identifier.intValue()) {
                    int id = IntStream.range(0, dataList.size())
                            .filter(userInd -> ((Integer) dataList.get(userInd).get("id")).intValue() == identifier.intValue())
                            .findFirst()
                            .getAsInt();

                    identString = identString + "_" + id;
                }
            }

            PublicationTypeCreateDto publicationTypeCreateDto = PublicationTypeCreateDto.builder()
                    .code("pub_" + identString)
                    .name((String) map.get("????????????????"))
                    .period((String) map.get("??????????????????????????"))
                    .submissionTime((String) map.get("????????????????????????????????????"))
                    .formatEntityId(idByName.toString())
                    .rubricEntityId(rubricIds)
                    .subscription(Subscription.PUBLICATION.name())
                    .build();

            UUID id = publicationTypeService.save(publicationTypeCreateDto).getId();
            setBaseColumns(map, "rs_pub_types", id);
        }
    }

    @Transactional
    public void loadFilesMOReference() throws FileNotFoundException {

        jdbcTemplate.execute(String.format(TRUNCATE_TABLE_TEMPLATE_QUERY, "rs_indicator"));
        jdbcTemplate.execute(String.format(TRUNCATE_TABLE_TEMPLATE_QUERY, "rs_municipal_files_reference"));

        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("mo.csv");
        CSVParser csvParser = new CSVParserBuilder().withSeparator(';').build();

        CSVReader csvReader = new CSVReaderBuilder(new InputStreamReader(inputStream)).withCSVParser(csvParser).build();

        List<List<String>> records = new ArrayList<List<String>>();
        String[] values = null;
        while (true) {
            try {
                if ((values = csvReader.readNext()) == null) {
                    break;
                }
            } catch (IOException | CsvValidationException e) {
                e.printStackTrace();
            }
            records.add(Arrays.asList(values));
        }

        for (int i = 1; i < records.size(); i++) {
            List<String> strings = records.get(i);
            String fileNamePattern = strings.get(5);
            MunicipalFilesReferenceEntity municipalFilesReferenceEntity = null;
            Optional<MunicipalFilesReferenceEntity> optionalMunicipalFilesReferenceEntity = municipalFilesReferenceRepository.findByFileNamePattern(fileNamePattern);

            if (optionalMunicipalFilesReferenceEntity.isPresent()) {
                municipalFilesReferenceEntity = optionalMunicipalFilesReferenceEntity.get();
            } else {
                municipalFilesReferenceEntity = MunicipalFilesReferenceEntity.builder()
                        .representationTerm(strings.get(4))
                        .fileNamePattern(fileNamePattern)
                        .periodType(strings.get(6))
                        .build();

                municipalFilesReferenceEntity = municipalFilesReferenceRepository.save(municipalFilesReferenceEntity);
            }

            Optional<List<IndicatorEntity>> optionalIndicatorEntityList = Optional.ofNullable(municipalFilesReferenceEntity.getIndicatorEntityList());

            IndicatorEntity indicatorEntity = null;

            String name = strings.get(1);
            Optional<IndicatorEntity> optionalIndicatorEntity = indicatorEntityRepository.findByName(name);

            if (optionalIndicatorEntity.isPresent()) {
                indicatorEntity = optionalIndicatorEntity.get();
            } else {
                indicatorEntity = IndicatorEntity.builder()
                        .name(name)
                        .moQuantity(Integer.parseInt(strings.get(3)))
                        .cutOff(strings.get(2))
                        .build();

                indicatorEntity = indicatorEntityRepository.save(indicatorEntity);
            }

            indicatorEntity.setMunicipalFilesReferenceEntity(municipalFilesReferenceEntity);

            List<IndicatorEntity> indicatorEntityList = optionalIndicatorEntityList.orElse(new ArrayList<>());
            indicatorEntityList.add(indicatorEntity);

            municipalFilesReferenceEntity.setIndicatorEntityList(indicatorEntityList);
        }
    }

    @Transactional
    public void loadFilesMO() throws FileNotFoundException {
        String fullPath = basePath;

        try {
            Path file = Paths.get(fullPath);

            if (Files.isDirectory(file)) {
                try {
                    Files.walkFileTree(file, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

                            if (!Files.isDirectory(file)) {
                                MunicipalFilesReferencePreviewWithYearDto preview = municipalFilesReferenceService.getMunicipalFilesReferencePreviewWithYearDto(file);

                                if (preview.getFileNamePattern() != null) {
                                    String fileName = file.getFileName().toString();

                                    long bytes = 0;

                                    try {
                                        bytes = Files.size(file);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    MunicFileEntity.MunicFileEntityBuilder municFileEntityBuilder = MunicFileEntity.builder()
                                            .fileExtension(FilenameUtils.getExtension(fileName))
                                            .fileName(fileName)
                                            .fileSize(bytes);

                                    try {
                                        BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
                                        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(formatForFileTime);

                                        LocalDateTime creationLocalDateTime = attr.creationTime().toInstant()
                                                .atZone(ZoneId.systemDefault())
                                                .toLocalDateTime();

                                        municFileEntityBuilder.createdDate(creationLocalDateTime.format(dateTimeFormatter));

                                        LocalDateTime lastModifiedLocalDateTime = attr.lastModifiedTime().toInstant()
                                                .atZone(ZoneId.systemDefault())
                                                .toLocalDateTime();

                                        municFileEntityBuilder.modifiedDate(lastModifiedLocalDateTime.format(dateTimeFormatter));
                                    } catch (IOException ignored) {

                                    }

                                    String relativePath = file.getParent().toString().substring(basePath.length());
                                    relativePath = relativePath.replace("\\", "/");
                                    relativePath = relativePath.replaceAll("^[/]", "");
                                    municFileEntityBuilder.path(relativePath);

                                    Pattern yearPattern = Pattern.compile(regexForYear);
                                    String[] parent = relativePath.split("/");
                                    String yearFolder = Arrays.stream(parent).filter(folder -> yearPattern.matcher(folder).matches()).findFirst().orElse(null);
                                    municFileEntityBuilder.year(yearFolder);

                                    municFileEntityBuilder.representationTerm(preview.getRepresentationTerm());
                                    municFileEntityBuilder.fileNamePattern(preview.getFileNamePattern());
                                    municFileEntityBuilder.realFileName(fileName);
                                    municFileEntityBuilder.periodType(preview.getPeriodType());

                                    List<String> stringList = preview.getIndicatorEntityList().stream().map(e -> {
                                        try {
                                            return new ObjectMapper().writeValueAsString(e);
                                        } catch (JsonProcessingException jsonProcessingException) {
                                            return "";
                                        }
                                    }).collect(Collectors.toList());

                                    String jsonString = null;

                                    try {
                                        jsonString = new ObjectMapper().writeValueAsString(stringList);
                                    } catch (JsonProcessingException e) {
                                        e.printStackTrace();
                                    }

                                    municFileEntityBuilder.indicatorDescription(jsonString);

                                    MunicFileEntity municFileEntity = municFileEntityBuilder.build();

                                    municFileEntity = municFileRepository.save(municFileEntity);

                                    byte[] array = new byte[0];

                                    try {
                                        array = Files.readAllBytes(file);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    MunicFileContentEntity municFileContentEntity = MunicFileContentEntity.builder().content(array).municFileEntity(municFileEntity).build();

                                    municFileContentEntity = municFileContentRepository.save(municFileContentEntity);

                                    municFileEntity.setMunicFileContentEntity(municFileContentEntity);
                                }
                            }

                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                            if (exc instanceof AccessDeniedException) {
                                return FileVisitResult.SKIP_SUBTREE;
                            }

                            return super.visitFileFailed(file, exc);
                        }
                    });
                } catch (IOException ignored) {
                }
            }
        } catch (InvalidPathException ignored) {

        }
    }
}