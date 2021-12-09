package ru.region_stat.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.region_stat.domain.dto.rubric.RubricCreateDto;
import ru.region_stat.domain.dto.rubric.RubricResultDto;
import ru.region_stat.domain.dto.rubric.RubricUpdateDto;
import ru.region_stat.service.RubricService;
import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@RequestMapping("/rubric")
@RestController
public class RubricController {

    @Resource
    private RubricService rubricService;

    @GetMapping("/all")
    @ApiOperation(value = "getAllRubrics", nickname = "getAllRubrics")
    public ResponseEntity<List<RubricResultDto>> getAll() {

        return ResponseEntity.ok(rubricService.getAll());
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "findByIdRubric", nickname = "findByIdRubric")
    public ResponseEntity<RubricResultDto> findById(@PathVariable("id") UUID id) {

        return ResponseEntity.ok(rubricService.getById(id));
    }

    @PostMapping
    @ApiOperation(value = "createRubric", nickname = "createRubric")
    public ResponseEntity<RubricResultDto> create(@RequestBody @Validated RubricCreateDto rubricCreateDto) {

        return new ResponseEntity<>(rubricService.create(rubricCreateDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "updateRubric", nickname = "updateRubric")
    public ResponseEntity<RubricResultDto> update(@RequestBody @Validated RubricUpdateDto rubricUpdateDto, @PathVariable("id") UUID id) {

        return ResponseEntity.ok(rubricService.update(rubricUpdateDto, id));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "deleteRubric", nickname = "deleteRubric")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {

        rubricService.deleteById(id);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/contains-archive-publication")
    @ApiOperation(value = "getContainsArchivePublicationsRubrics", nickname = "getContainsArchivePublicationsRubrics")
    public ResponseEntity<List<RubricResultDto>> getContainsArchivePublicationsRubrics() {

        return ResponseEntity.ok(rubricService.getContainsArchivePublicationsRubrics());
    }
}