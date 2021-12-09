package ru.region_stat.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.region_stat.domain.dto.format.PublicationFormatCreateDto;
import ru.region_stat.domain.dto.format.PublicationFormatResultDto;
import ru.region_stat.domain.dto.format.PublicationFormatUpdateDto;
import ru.region_stat.service.PublicationFormatService;
import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@RequestMapping("/publication-format")
@RestController
public class PublicationFormatController {
    @Resource
    private PublicationFormatService publicationFormatService;

    @GetMapping("/all")
    @ApiOperation(value = "getAllFormat", nickname = "getAllFormat")
    public ResponseEntity<List<PublicationFormatResultDto>> getAll() {
        List<PublicationFormatResultDto> publicationFormatResultDtoList = publicationFormatService.getAll();
        return ResponseEntity.ok(publicationFormatResultDtoList);
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "getByIdFormat", nickname = "getByIdFormat")
    public ResponseEntity<PublicationFormatResultDto> getById(@PathVariable("id") UUID id) {
        PublicationFormatResultDto publicationFormat = publicationFormatService.getById(id);
        return ResponseEntity.ok(publicationFormat);
    }

    @PostMapping
    @ApiOperation(value = "createFormat", nickname = "createFormat")
    public ResponseEntity<PublicationFormatResultDto> create(@RequestBody @Validated PublicationFormatCreateDto publicationFormatCreateDto) {
        PublicationFormatResultDto publicationFormatResultDto = publicationFormatService.create(publicationFormatCreateDto);
        return ResponseEntity.ok(publicationFormatResultDto);
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "updateFormat", nickname = "updateFormat")
    public ResponseEntity<PublicationFormatResultDto> update(@RequestBody @Validated PublicationFormatUpdateDto publicationFormatUpdateDto, @PathVariable("id") UUID id) {
        PublicationFormatResultDto publicationFormatResultDto = publicationFormatService.update(publicationFormatUpdateDto, id);
        return ResponseEntity.ok(publicationFormatResultDto);
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "deleteFormat", nickname = "deleteFormat")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        publicationFormatService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}