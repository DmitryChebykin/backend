package ru.region_stat.controller.fileViewer.municipalities;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.ResourceUrlProvider;
import ru.region_stat.domain.dto.municipalFilesReference.MunicipalFilesReferencePreviewDto;
import ru.region_stat.domain.dto.municipalFilesReference.MunicipalFilesReferencePreviewWithYearDto;
import ru.region_stat.domain.entity.municipalFiles.IndicatorEntity;
import ru.region_stat.domain.entity.municipalFiles.MunicipalFilesReferenceEntity;
import ru.region_stat.domain.repository.IndicatorEntityRepository;
import ru.region_stat.domain.repository.MunicipalFilesReferenceRepository;
import ru.region_stat.service.GotenbergService;
import ru.region_stat.service.MunicipalFilesReferenceService;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import static ru.region_stat.dataloader.DataLoader.TRUNCATE_TABLE_TEMPLATE_QUERY;

@RequestMapping
@RestController
@Profile("!import")
@Api(value = "municipalsFiles")
public class MunicipalFilesController {
    @Value("${sharepoint.basePath}")
    private String basePath;

    @Value("${sharepoint.regexForYear}")
    private String regexForYear;

    @Value("${sharepoint.formatForFileTime}")
    private String formatForFileTime;

    @Resource
    private MunicipalFilesReferenceService municipalFilesReferenceService;

    @Resource
    private Tika tika;

    @Resource
    private GotenbergService gotenbergService;

    @Resource
    private IndicatorEntityRepository indicatorEntityRepository;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private MunicipalFilesReferenceRepository municipalFilesReferenceRepository;

    @GetMapping("/municipals/**")
    @ApiOperation(value = "getFileSystemResourceList", nickname = "getFileSystemResourceList")
    public ResponseEntity<List<MunicipalFilesReferencePreviewDto>> getFileSystemResourceList(HttpServletRequest request) {
        ResourceUrlProvider urlProvider = (ResourceUrlProvider) request
                .getAttribute(ResourceUrlProvider.class.getCanonicalName());

        String restOfUrl = urlProvider.getPathMatcher().extractPathWithinPattern(
                String.valueOf(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)),
                String.valueOf(request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)));

        String fullPath = basePath;

        if (!restOfUrl.equals("**")) {
            fullPath = basePath + "\\" + restOfUrl;
        }

        List<MunicipalFilesReferencePreviewDto> municipalFilesReferencePreviewDtos = new ArrayList<>();

        try {
            Path file = Paths.get(fullPath);

            if (Files.isDirectory(file)) {
                try {
                    Files.walkFileTree(file, EnumSet.noneOf(FileVisitOption.class), 1, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            MunicipalFilesReferencePreviewDto municipalFilesReferencePreviewDto = new MunicipalFilesReferencePreviewDto();

                            if (Files.isDirectory(file)) {
                                municipalFilesReferencePreviewDto.setRealFileName(file.getFileName().toString());
                                municipalFilesReferencePreviewDto.setFolder(true);
                            } else {
                                municipalFilesReferencePreviewDto = municipalFilesReferenceService.getMunicipalFilesReferencePreviewDto(file);
                                municipalFilesReferencePreviewDto.setRealFileName(file.getFileName().toString());
                                municipalFilesReferencePreviewDto.setFolder(false);
                            }

                            municipalFilesReferencePreviewDtos.add(municipalFilesReferencePreviewDto);

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

        return ResponseEntity.ok(municipalFilesReferencePreviewDtos);
    }

    @ApiOperation(value = "downloadMoFile", nickname = "downloadMoFileFromPath")
    @GetMapping(path = "municipals-download/**/{fileName:.+}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadMoFromPath(@PathVariable String fileName, HttpServletRequest request) {
        ResourceUrlProvider urlProvider = (ResourceUrlProvider) request
                .getAttribute(ResourceUrlProvider.class.getCanonicalName());

        String restOfUrl = urlProvider.getPathMatcher().extractPathWithinPattern(
                String.valueOf(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)),
                String.valueOf(request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)));

        String fullPath = basePath + FileSystems.getDefault().getSeparator() + restOfUrl;

        byte[] byteContent = new byte[0];

        try {
            byteContent = Files.readAllBytes(Paths.get(fullPath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String content = tika.detect(byteContent);

        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(fileName, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .contentType(MediaType.valueOf(content))
                .body(byteContent);
    }

    @ApiOperation(value = "downloadMoFileAsPdf", nickname = "downloadMoFileAsPdf")
    @GetMapping(path = "municipals-pdf-download/**/{fileName:.+}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadFromPathAsPdf(@PathVariable String fileName, HttpServletRequest request) {
        ResourceUrlProvider urlProvider = (ResourceUrlProvider) request
                .getAttribute(ResourceUrlProvider.class.getCanonicalName());

        String restOfUrl = urlProvider.getPathMatcher().extractPathWithinPattern(
                String.valueOf(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)),
                String.valueOf(request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)));

        String fullPath = basePath + FileSystems.getDefault().getSeparator() + restOfUrl;

        byte[] byteContent = new byte[0];

        try {
            byteContent = Files.readAllBytes(Paths.get(fullPath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] pdfContent = gotenbergService.convertOfficeFile(byteContent, fileName);

        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(fileName + ".pdf", StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .contentType(MediaType.valueOf("application/pdf"))
                .body(pdfContent);
    }

    @GetMapping("/all-municipals/")
    @ApiOperation(value = "getFileSystemResourceListWithSubFolders", nickname = "getFileSystemResourceListWithSubFolders")
    public ResponseEntity<List<MunicipalFilesReferencePreviewWithYearDto>> getFileSystemResourceList() {

        String fullPath = basePath;

        List<MunicipalFilesReferencePreviewWithYearDto> municipalFilesReferencePreviewWithYearDtos = new ArrayList<>();

        try {
            Path file = Paths.get(fullPath);

            if (Files.isDirectory(file)) {
                try {
                    Files.walkFileTree(file, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            MunicipalFilesReferencePreviewWithYearDto municipalFilesReferencePreviewWithYearDto = new MunicipalFilesReferencePreviewWithYearDto();

                            if (!Files.isDirectory(file)) {
                                municipalFilesReferencePreviewWithYearDto = municipalFilesReferenceService.getMunicipalFilesReferencePreviewWithYearDto(file);
                                municipalFilesReferencePreviewWithYearDto.setRealFileName(file.getFileName().toString());
                                municipalFilesReferencePreviewWithYearDto.setFolder(false);

                                try {
                                    BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
                                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(formatForFileTime);

                                    LocalDateTime creationLocalDateTime = attr.creationTime().toInstant()
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDateTime();
                                    municipalFilesReferencePreviewWithYearDto.setCreatedDate(creationLocalDateTime.format(dateTimeFormatter));

                                    LocalDateTime lastModifiedLocalDateTime = attr.lastModifiedTime().toInstant()
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDateTime();

                                    municipalFilesReferencePreviewWithYearDto.setModifiedDate(lastModifiedLocalDateTime.format(dateTimeFormatter));
                                } catch (IOException ignored) {

                                }

                                String relativePath = file.getParent().toString().substring(basePath.length());
                                relativePath = relativePath.replace("\\", "/");
                                relativePath = relativePath.replaceAll("^[/]", "");

                                String[] parent = relativePath.split("/");
                                municipalFilesReferencePreviewWithYearDto.setPath(relativePath);

                                Pattern yearPattern = Pattern.compile(regexForYear);

                                String yearFolder = Arrays.stream(parent).filter(folder -> yearPattern.matcher(folder).matches()).findFirst().orElse(null);
                                municipalFilesReferencePreviewWithYearDto.setYear(yearFolder);
                            }

                            municipalFilesReferencePreviewWithYearDtos.add(municipalFilesReferencePreviewWithYearDto);

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

//        List<String> collect = municipalFilesReferencePreviewWithYearDtos.stream().map(e -> e.getRealFileName()).distinct().collect(Collectors.toList());
//
//        FileWriter writer = null;
//        try {
//            writer = new FileWriter("output.txt");
//            for (String str : collect) {
//                writer.write(str + System.lineSeparator());
//            }
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        return ResponseEntity.ok(municipalFilesReferencePreviewWithYearDtos);
    }

    @GetMapping("/import-csv/")
    public ResponseEntity<Void> importCsv() {
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

        return ResponseEntity.ok().build();
    }
}