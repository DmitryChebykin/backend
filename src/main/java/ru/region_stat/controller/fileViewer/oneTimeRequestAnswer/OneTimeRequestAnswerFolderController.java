package ru.region_stat.controller.fileViewer.oneTimeRequestAnswer;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.region_stat.domain.dto.oneTimeRequest.OneTimeRequestFileResultDto;
import ru.region_stat.domain.dto.oneTimeRequest.OneTimeRequestResultDto;
import ru.region_stat.service.OneTimeRequestService;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/one-time-request-answer/")
public class OneTimeRequestAnswerFolderController {
    @Value("${sharepoint.inbox-one-time-request-folder}")
    private String answerFolder;

    @Resource
    private OneTimeRequestService oneTimeRequestService;

    @GetMapping
    @ApiOperation(value = "getAnswerFilesList", nickname = "getAnswerFilesList")
    public ResponseEntity<List<OneTimeRequestAnswerPreviewDto>> getStatisticalPublicationPreviewDtoList() {

        List<OneTimeRequestAnswerPreviewDto> oneTimeRequestAnswerPreviewDtos = new ArrayList<>();

        try {
            Path file = Paths.get(answerFolder);

            if (Files.isDirectory(file)) {
                try {
                    Files.walkFileTree(file, EnumSet.noneOf(FileVisitOption.class), 1, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            if (!Files.isDirectory(file)) {
                                OneTimeRequestAnswerPreviewDto previewDtos = oneTimeRequestService.getoneTimeRequestAnswerPreviewDtos(file);
                                oneTimeRequestAnswerPreviewDtos.add(previewDtos);
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

        return ResponseEntity.ok(oneTimeRequestAnswerPreviewDtos);
    }

    @PostMapping
    @ApiOperation(value = "saveAnswerFileToRequest", nickname = "saveAnswerFileToRequest")
    public ResponseEntity<Map<OneTimeRequestFileResultDto, OneTimeRequestResultDto>> saveFile(@RequestBody String fileName) {
        Path path = Paths.get(answerFolder + File.separator + fileName);
        File file = path.toFile();

        Map<OneTimeRequestFileResultDto, OneTimeRequestResultDto> resultDtoMap = oneTimeRequestService.addFileToRequest(file);
        return new ResponseEntity<>(resultDtoMap, HttpStatus.CREATED);
    }
}