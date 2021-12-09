package ru.region_stat.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.region_stat.domain.dto.oneTimeRequest.*;
import ru.region_stat.service.OneTimeRequestService;
import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequestMapping("/one-time-request")
@RestController
public class OneTimeRequestController {

    @Resource
    private OneTimeRequestService oneTimeRequestService;

    @GetMapping("/all")
    @ApiOperation(value = "getAllOneTimeRequests", nickname = "getAllOneTimeRequests")
    public ResponseEntity<List<OneTimeRequestResultDto>> getAll() {

        return ResponseEntity.ok(oneTimeRequestService.getAll());
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "getOneTimeRequestById", nickname = "getOneTimeRequestById")
    public ResponseEntity<OneTimeRequestResultDto> getById(@PathVariable("id") UUID id) {

        return ResponseEntity.ok(oneTimeRequestService.findById(id));
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @ApiOperation(value = "createOneTimeRequest", nickname = "createOneTimeRequest")
    public ResponseEntity<OneTimeRequestExtendedResultDto> create(
            @RequestBody OneTimeRequestCreateDto oneTimeRequestCreateDto,
            @RequestBody(required = false) List<MultipartFile> multipartFiles

    ) {

        return ResponseEntity.ok(oneTimeRequestService.saveExtended(oneTimeRequestCreateDto, multipartFiles));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "deleteOneTimeRequestById", nickname = "deleteOneTimeRequestById")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {

        oneTimeRequestService.deleteById(id);

        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "file/{fileId}/content", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ApiOperation(value = "getOneTimeRequestFileContent", nickname = "getOneTimeRequestFileContent")
    public ResponseEntity<ByteArrayResource> getFileContent(@PathVariable UUID fileId) {
        FileContentDto fileContentDto = oneTimeRequestService.getFileContentDto(fileId);

        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(fileContentDto.getFileName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(fileContentDto.getByteArrayResource());
    }

    @GetMapping("/docx/{id}")
    @ApiOperation(value = "saveDocx", nickname = "saveDocx")
    public ResponseEntity<OneTimeRequestResultDto> saveDocx(@PathVariable("id") String id) {
        OneTimeRequestResultDto oneTimeRequestResultDto = oneTimeRequestService.saveDocxFileForRequestByRequestId(id, null).orElseThrow(RuntimeException::new);
        return ResponseEntity.ok(oneTimeRequestResultDto);
    }

    @PutMapping(value = "update-by-admin/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @ApiOperation(value = "adminUpdateOneTimeRequest", nickname = "adminUpdateOneTimeRequest")
    public ResponseEntity<OneTimeRequestResultDto> updateByAdmin(@PathVariable("id") String requestId,
                                                                 @RequestBody OneTimeRequestUpdateDto oneTimeRequestUpdateDto,
                                                                 @RequestBody(required = false) List<MultipartFile> multipartFiles

    ) {

        return ResponseEntity.ok(oneTimeRequestService.update(requestId, oneTimeRequestUpdateDto, multipartFiles));
    }

    @PutMapping(value = "update-by-user/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @ApiOperation(value = "userUpdateOneTimeRequest", nickname = "userUpdateOneTimeRequest")
    public ResponseEntity<OneTimeRequestResultDto> updateByUser(@PathVariable("id") String requestId,
                                                                @RequestBody OneTimeRequestUpdateDto oneTimeRequestUpdateDto,
                                                                @RequestBody(required = false) List<MultipartFile> multipartFiles

    ) {

        return ResponseEntity.ok(oneTimeRequestService.update(requestId, oneTimeRequestUpdateDto, multipartFiles));
    }

    @PutMapping("/update-requests-statuses")
    @ApiOperation(value = "updateRequestsStatuses", nickname = "updateRequestsStatuses")
    public ResponseEntity<List<Map<UUID, UUID>>> updateRequestsStatuses(@RequestBody OneTimeRequestsStatusesUpdateDto oneTimeRequestsStatusesUpdateDto) {
        return ResponseEntity.ok(oneTimeRequestService.updateStatusByListIds(oneTimeRequestsStatusesUpdateDto));
    }

    @GetMapping("/all-extended")
    @ApiOperation(value = "getAllExtendedOneTimeRequests", nickname = "getAllExtendedOneTimeRequests")
    public ResponseEntity<List<OneTimeRequestExtendedResultDto>> getAllExtended() {

        return ResponseEntity.ok(oneTimeRequestService.getAllExtended());
    }

    @PutMapping("/set-send-to-petrostat/{id}")
    @ApiOperation(value = "sendToPetrostat", nickname = "sendToPetrostat")
    public ResponseEntity<OneTimeRequestResultDto> sendToPetrostat(@PathVariable("id") String id, @RequestBody OneTimeRequestUpdateDto oneTimeRequestUpdateDto) {
        OneTimeRequestResultDto oneTimeRequestResultDto = oneTimeRequestService.saveDocxFileForRequestByRequestId(id, oneTimeRequestUpdateDto).orElseThrow(RuntimeException::new);
        return ResponseEntity.ok(oneTimeRequestResultDto);
    }
}