package ru.region_stat.controller;

import io.swagger.annotations.ApiOperation;
import org.apache.tika.Tika;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.region_stat.domain.dto.file.PublicationFileExtendedResultDto;
import ru.region_stat.domain.dto.file.PublicationFileResultDto;
import ru.region_stat.service.PublicationFileService;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@RequestMapping("/publication-file")
@RestController
@CrossOrigin(exposedHeaders = {"Content-Disposition"})
public class PublicationFileController {

    @Resource
    private PublicationFileService publicationFileService;

    @Resource
    private Tika tika;

    @GetMapping("/all")
    @ApiOperation(value = "getAllFile", nickname = "getAllFile")
    public ResponseEntity<List<PublicationFileResultDto>> getAll() {
        List<PublicationFileResultDto> publicationFormatResultDtoList = publicationFileService.getAll();
        return ResponseEntity.ok(publicationFormatResultDtoList);
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "getByIdFile", nickname = "getByIdFile")
    public ResponseEntity<PublicationFileResultDto> getById(@PathVariable("id") UUID id) {
        PublicationFileResultDto publicationFile = publicationFileService.getById(id);
        return ResponseEntity.ok(publicationFile);
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "deleteFile", nickname = "deleteFile")
    public ResponseEntity<PublicationFileResultDto> delete(@PathVariable("id") UUID id) {
        publicationFileService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/by-publication/{id}")
    @ApiOperation(value = "getAllByStatPublicationId", nickname = "getAllByStatPublicationId")
    public ResponseEntity<List<PublicationFileResultDto>> getAllByStatPublicationId(@PathVariable("id") UUID id) {
        List<PublicationFileResultDto> publicationFileResultDtoList = publicationFileService.getAllByStatPubId(id);
        return ResponseEntity.ok(publicationFileResultDtoList);
    }

    @GetMapping(path = "/{id}/content", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ApiOperation(value = "getFileContent", nickname = "getFileContent")
    public ResponseEntity<ByteArrayResource> getFileContent(@PathVariable UUID id) {
        String contentType = publicationFileService.getContentType(id);
        ByteArrayResource resource = publicationFileService.getByteArrayResource(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + contentType)
                .body(resource);
    }

    @ApiOperation(value = "downloadFile", nickname = "downloadFileFromPath")
    @GetMapping(path = "/download/{file_id}/{fileName:.+}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadFromUrl(@PathVariable String fileName, @PathVariable UUID file_id) {
        byte[] byteContent = publicationFileService.getByteContent(file_id);
        String content = tika.detect(byteContent);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.valueOf(content))
                .body(byteContent);
    }

    @ApiOperation(value = "downloadFileAsPdf", nickname = "downloadFileAsPdf")
    @GetMapping(path = "/downloadAsPdf/{file_id}/{fileName:.+}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadFromUrlAsPdf(@PathVariable String fileName, @PathVariable UUID file_id) {
        byte[] pdfContent = publicationFileService.getContentAsPdf(file_id, fileName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName + ".pdf")
                .contentType(MediaType.valueOf("application/pdf"))
                .body(pdfContent);
    }

    @GetMapping("/rubric/{rubricId}")
    @ApiOperation(value = "getArchiveFilteredFilesByRubricId", nickname = "getArchiveFilteredFilesByRubricId")
    public ResponseEntity<List<PublicationFileExtendedResultDto>> getAllExtended(@PathVariable("rubricId") String rubricId, @RequestParam(value = "isArchive", defaultValue = "false") String isArchive) {
        List<PublicationFileExtendedResultDto> filesByRubricId = publicationFileService.getArchiveFilteredFilesByRubricId(isArchive, rubricId);
        return ResponseEntity.ok(filesByRubricId);
    }
}