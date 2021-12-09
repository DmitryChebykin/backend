package ru.region_stat.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.region_stat.domain.dto.publicationType.PublicationTypeCreateDto;
import ru.region_stat.domain.dto.publicationType.PublicationTypeCreateNewDto;
import ru.region_stat.domain.dto.publicationType.PublicationTypeResultDto;
import ru.region_stat.domain.dto.publicationType.PublicationTypeUpdateDto;
import ru.region_stat.service.PublicationTypeService;
import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@RequestMapping("/publication-type")
@RestController
public class PublicationTypeController {
    @Resource
    private PublicationTypeService publicationTypeService;

    @GetMapping("/all")
    @ApiOperation(value = "getAllPublicationType", nickname = "getAllPublicationType")
    public ResponseEntity<List<PublicationTypeResultDto>> getAll() {
        List<PublicationTypeResultDto> publicationTypeResultDtoList = publicationTypeService.getAll();
        return ResponseEntity.ok(publicationTypeResultDtoList);
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "getByIdPublicationType", nickname = "getByIdPublicationType")
    public ResponseEntity<PublicationTypeResultDto> getById(@PathVariable("id") UUID id) {
        PublicationTypeResultDto publicationType = publicationTypeService.getById(id);
        return ResponseEntity.ok(publicationType);
    }

    @PostMapping
    @ApiOperation(value = "createPublicationType", nickname = "createPublicationType")
    public ResponseEntity<PublicationTypeResultDto> create(@RequestBody @Validated PublicationTypeCreateDto publicationTypeCreateDto) {
        PublicationTypeResultDto publicationTypeResultDto = publicationTypeService.save(publicationTypeCreateDto);
        return ResponseEntity.ok(publicationTypeResultDto);
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "updatePublicationType", nickname = "updatePublicationType")
    public ResponseEntity<PublicationTypeResultDto> update(@RequestBody @Validated PublicationTypeUpdateDto publicationTypeUpdateDto, @PathVariable("id") UUID id) {
        PublicationTypeResultDto publicationFormatResultDto = publicationTypeService.update(publicationTypeUpdateDto, id);
        return ResponseEntity.ok(publicationFormatResultDto);
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "deletePublicationType", nickname = "deletePublicationType")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        publicationTypeService.deleteById(id);
        return ResponseEntity.ok().build();
    }


    @PostMapping(("/create-new"))
    @ApiOperation(value = "createNewPublicationType", nickname = "createNewPublicationType")
    public ResponseEntity<PublicationTypeResultDto> create(@RequestBody @Validated PublicationTypeCreateNewDto publicationTypeCreateNewDto) {
        PublicationTypeResultDto publicationTypeResultDto = publicationTypeService.saveNew(publicationTypeCreateNewDto);
        return ResponseEntity.ok(publicationTypeResultDto);
    }
}