package ru.region_stat.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.region_stat.domain.dto.statisticalPublication.StatisticalPublicationCreateDto;
import ru.region_stat.domain.dto.statisticalPublication.StatisticalPublicationResultDto;
import ru.region_stat.domain.dto.statisticalPublication.StatisticalPublicationUpdateDto;
import ru.region_stat.service.StatisticalPublicationService;
import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/statistical_publication")
public class StatisticalPublicationController {

    @Resource
    private StatisticalPublicationService statisticalPublicationService;

    @GetMapping("/all")
    @ApiOperation(value = "getAllStatisticalPublications", nickname = "getAllStatisticalPublications")
    public ResponseEntity<List<StatisticalPublicationResultDto>> getAll() {

        return ResponseEntity.ok(statisticalPublicationService.getAll());
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "findByIdStatisticalPublication", nickname = "findByIdStatisticalPublication")
    public ResponseEntity<StatisticalPublicationResultDto> findById(@PathVariable("id") UUID id) {

        return ResponseEntity.ok(statisticalPublicationService.findById(id));
    }

    @GetMapping("/all/{rubricId}")
    @ApiOperation(value = "getAllStatisticalPublicationsByRubric", nickname = "getAllStatisticalPublicationsByRubric")
    public ResponseEntity<List<StatisticalPublicationResultDto>> getAllStatisticalPublicationsByRubric(@PathVariable("rubricId") String rubricId) {

        return ResponseEntity.ok(statisticalPublicationService.getAllByRubric(UUID.fromString(rubricId)));
    }

    @PostMapping
    @ApiOperation(value = "createStatisticalPublication", nickname = "createStatisticalPublication")
    public ResponseEntity<StatisticalPublicationResultDto> create(
            @RequestBody @Validated StatisticalPublicationCreateDto statisticalPublicationCreateDto
    ) {

        return ResponseEntity.ok(statisticalPublicationService.create(statisticalPublicationCreateDto));
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "updateStatisticalPublication", nickname = "updateStatisticalPublication")
    public ResponseEntity<StatisticalPublicationResultDto> update(
            @RequestBody @Validated StatisticalPublicationUpdateDto statisticalPublicationUpdateDto,
            @PathVariable("id") UUID id
    ) {

        return ResponseEntity.ok(statisticalPublicationService.update(statisticalPublicationUpdateDto, id));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "deleteStatisticalPublication", nickname = "deleteStatisticalPublication")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {

        statisticalPublicationService.deleteById(id);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/invert-is-archive")
    @ApiOperation(value = "invertIsArchiveOfPublicationIdList", nickname = "invertIsArchiveOfPublicationIdList")
    public ResponseEntity<List<StatisticalPublicationResultDto>> invertIsArchiveOfPublicationList(@RequestBody @Validated List<String> idList) {
        return ResponseEntity.ok(statisticalPublicationService.invertIsArchiveOfPublicationList(idList));
    }

    @GetMapping("/archived-rubric-list")
    public ResponseEntity<List<String>> getArchivedRubricNames() {

        return ResponseEntity.ok(statisticalPublicationService.getArchivedRubricNames());
    }

    @GetMapping("/publication-by-archived-tag")
    public ResponseEntity<List<StatisticalPublicationResultDto>> getPublicationsByArchivedRubricNames(@RequestParam String tag) {

        return ResponseEntity.ok(statisticalPublicationService.getPublicationsByArchivedRubricNames(tag));
    }
}