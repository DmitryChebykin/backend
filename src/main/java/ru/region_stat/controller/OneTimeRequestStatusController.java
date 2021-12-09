package ru.region_stat.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.region_stat.domain.dto.oneTimeRequestStatus.OneTimeRequestStatusCreateDto;
import ru.region_stat.domain.dto.oneTimeRequestStatus.OneTimeRequestStatusResultDto;
import ru.region_stat.domain.dto.oneTimeRequestStatus.OneTimeRequestStatusUpdateDto;
import ru.region_stat.service.OneTimeRequestStatusService;
import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@RequestMapping("/one-time-request-status")
@RestController
public class OneTimeRequestStatusController {
    @Resource
    private OneTimeRequestStatusService oneTimeRequestStatusService;

    @GetMapping("/all")
    @ApiOperation(value = "getAllOneTimeRequestStatuss", nickname = "getAllOneTimeRequestStatuss")
    public ResponseEntity<List<OneTimeRequestStatusResultDto>> getAll() {

        return ResponseEntity.ok(oneTimeRequestStatusService.getAll());
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "getByIdOneTimeRequestStatus", nickname = "getByIdOneTimeRequestStatus")
    public ResponseEntity<OneTimeRequestStatusResultDto> getById(@PathVariable("id") UUID id) {

        return ResponseEntity.ok(oneTimeRequestStatusService.getOneTimeRequestStatusResultDtoById(id));
    }

    @PostMapping
    @ApiOperation(value = "createOneTimeRequestStatus", nickname = "createOneTimeRequestStatus")
    public ResponseEntity<OneTimeRequestStatusResultDto> create(@RequestBody OneTimeRequestStatusCreateDto departmentCreateDto) {

        return new ResponseEntity<>(oneTimeRequestStatusService.create(departmentCreateDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "updateOneTimeRequestStatus", nickname = "updateOneTimeRequestStatus")
    public ResponseEntity<OneTimeRequestStatusResultDto> update(@RequestBody OneTimeRequestStatusUpdateDto oneTimeRequestStatusUpdateDto, @PathVariable("id") UUID id) {

        return ResponseEntity.ok(oneTimeRequestStatusService.update(oneTimeRequestStatusUpdateDto, id));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "deleteOneTimeRequestStatus", nickname = "deleteOneTimeRequestStatus")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {

        oneTimeRequestStatusService.deleteById(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}