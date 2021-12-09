package ru.region_stat.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.region_stat.domain.dto.municipalFilesReference.MunicipalFilesReferenceCreateDto;
import ru.region_stat.domain.dto.municipalFilesReference.MunicipalFilesReferenceResultDto;
import ru.region_stat.domain.dto.municipalFilesReference.MunicipalFilesReferenceUpdateDto;
import ru.region_stat.service.MunicipalFilesReferenceService;
import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@RequestMapping("/municipal-files-reference")
public class MunicipalFilesReferenceController {
    @Resource
    private MunicipalFilesReferenceService municipalFilesReferenceService;

    @GetMapping("/all")
    @ApiOperation(value = "getAllMunicipalFilesReferences", nickname = "getAllMunicipalFilesReferences")
    public ResponseEntity<List<MunicipalFilesReferenceResultDto>> getAll() {

        return ResponseEntity.ok(municipalFilesReferenceService.getAll());
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "getByIdMunicipalFilesReference", nickname = "getByIdMunicipalFilesReference")
    public ResponseEntity<MunicipalFilesReferenceResultDto> getById(@PathVariable("id") UUID id) {

        return ResponseEntity.ok(municipalFilesReferenceService.getMunicipalFilesReferenceResultDtoById(id));
    }

    @PostMapping
    @ApiOperation(value = "createMunicipalFilesReference", nickname = "createMunicipalFilesReference")
    public ResponseEntity<MunicipalFilesReferenceResultDto> create(@RequestBody MunicipalFilesReferenceCreateDto municipalFilesReferenceCreateDto) {

        return new ResponseEntity<>(municipalFilesReferenceService.create(municipalFilesReferenceCreateDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "updateMunicipalFilesReference", nickname = "updateMunicipalFilesReference")
    public ResponseEntity<MunicipalFilesReferenceResultDto> update(@RequestBody MunicipalFilesReferenceUpdateDto municipalFilesReferenceUpdateDto, @PathVariable("id") UUID id) {

        return ResponseEntity.ok(municipalFilesReferenceService.update(municipalFilesReferenceUpdateDto, id));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "deleteMunicipalFilesReference", nickname = "deleteMunicipalFilesReference")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {

        municipalFilesReferenceService.deleteById(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}