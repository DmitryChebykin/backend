package ru.region_stat.controller.fileViewer;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.ResourceUrlProvider;
import ru.region_stat.domain.dto.file.PublicationFileResultDto;
import ru.region_stat.domain.dto.statisticalPublication.StatisticalPublicationPreviewDto;
import ru.region_stat.domain.dto.statisticalPublication.StatisticalPublicationResultDto;
import ru.region_stat.service.GotenbergService;
import ru.region_stat.service.StatisticalPublicationService;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequestMapping("/inbox/")
@RestController
@Api(value = "inboxPublicationsFiles")
@CrossOrigin
public class PublicationInboxFolderController {
    @Value("${sharepoint.savedFolder}")
    private String savedFolder;

    @Resource
    private StatisticalPublicationService statisticalPublicationService;

    @Resource
    private Tika tika;

    @Resource
    private GotenbergService gotenbergService;

    @GetMapping
    @ApiOperation(value = "getPublicationFilesList", nickname = "getPublicationFilesList")
    public ResponseEntity<List<StatisticalPublicationPreviewDto>> getStatisticalPublicationPreviewDtoList() {

        String fullPath = savedFolder;

        List<StatisticalPublicationPreviewDto> statisticalPublicationPreviewDtos = new ArrayList<>();

        try {
            Path file = Paths.get(fullPath);

            if (Files.isDirectory(file)) {
                try {
                    Files.walkFileTree(file, EnumSet.noneOf(FileVisitOption.class), 1, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            if (!Files.isDirectory(file)) {
                                StatisticalPublicationPreviewDto statisticalPublicationPreviewDto = statisticalPublicationService.getStatisticalPublicationPreviewDto(file);
                                statisticalPublicationPreviewDtos.add(statisticalPublicationPreviewDto);
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

        return ResponseEntity.ok(statisticalPublicationPreviewDtos);
    }

    @PostMapping
    @ApiOperation(value = "saveFileToPublication", nickname = "saveFileToPublication")
    public ResponseEntity<Map<PublicationFileResultDto, StatisticalPublicationResultDto>> saveFile(@RequestBody String fileName) {
        Path path = Paths.get(savedFolder + File.separator + fileName);
        File file = path.toFile();

        Map<PublicationFileResultDto, StatisticalPublicationResultDto> publicationFileResultDtoStatisticalPublicationResultDtoMap = statisticalPublicationService.saveByFile(file);
        return new ResponseEntity<>(publicationFileResultDtoStatisticalPublicationResultDtoMap, HttpStatus.CREATED);
    }

    @PostMapping("saveMultiple")
    @ApiOperation(value = "saveFilesToPublication", nickname = "saveFilesToPublication")
    public ResponseEntity<List<Map<PublicationFileResultDto, StatisticalPublicationResultDto>>> saveFiles(@RequestBody List<String> fileNames) {
        List<Map<PublicationFileResultDto, StatisticalPublicationResultDto>> mapList = fileNames.stream().map(e -> {
            Path path = Paths.get(savedFolder + File.separator + e);
            File file = path.toFile();
            return statisticalPublicationService.saveByFile(file);
        }).collect(Collectors.toList());

        return new ResponseEntity<>(mapList, HttpStatus.CREATED);
    }

    @ApiOperation(value = "downloadFile", nickname = "downloadFileFromPath")
    @GetMapping(path = "inbox-download/**/{fileName:.+}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadMoFromPath(@PathVariable String fileName, HttpServletRequest request) {
        ResourceUrlProvider urlProvider = (ResourceUrlProvider) request
                .getAttribute(ResourceUrlProvider.class.getCanonicalName());

        String restOfUrl = urlProvider.getPathMatcher().extractPathWithinPattern(
                String.valueOf(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)),
                String.valueOf(request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)));

        String fullPath = savedFolder + FileSystems.getDefault().getSeparator() + restOfUrl;

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

    @ApiOperation(value = "downloadFileAsPdf", nickname = "downloadFileAsPdf")
    @GetMapping(path = "inbox-pdf-download/**/{fileName:.+}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadFromPathAsPdf(@PathVariable String fileName, HttpServletRequest request) {
        ResourceUrlProvider urlProvider = (ResourceUrlProvider) request
                .getAttribute(ResourceUrlProvider.class.getCanonicalName());

        String restOfUrl = urlProvider.getPathMatcher().extractPathWithinPattern(
                String.valueOf(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)),
                String.valueOf(request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)));

        String fullPath = savedFolder + FileSystems.getDefault().getSeparator() + restOfUrl;

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
}