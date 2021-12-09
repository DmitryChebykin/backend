package ru.region_stat.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.region_stat.elastic.ElasticPublicationEntity;
import ru.region_stat.elastic.ElasticRepository;
import ru.region_stat.elastic.ElasticResultSearchDto;
import ru.region_stat.elastic.ElasticService;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RequestMapping("/elastic")
@RestController
@Profile("!import")
public class ElasticController {
    @Resource
    private ElasticRepository elasticRepository;
    @Resource
    private ElasticService elasticService;

    @GetMapping("/all")
    @ApiOperation(value = "getAllElasticPublications", nickname = "getAllElasticPublications")
    public ResponseEntity<List<ElasticPublicationEntity>> getAll() {
        List<ElasticPublicationEntity> publicationEntities = new ArrayList<>();

        Iterable<ElasticPublicationEntity> entityIterable = elasticRepository.findAll();

        for (ElasticPublicationEntity elasticPublicationEntity : entityIterable) {
            publicationEntities.add(elasticPublicationEntity);
        }

        return ResponseEntity.ok(publicationEntities);
    }

    @GetMapping("/check")
    public ResponseEntity<Void> check() {
        elasticService.checkNotIndexedPublications();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/search")
    public ResponseEntity<List<Object>> search(@RequestBody String word) {
        elasticService.search(word);
        return ResponseEntity.ok(elasticService.search(word));
    }

    @GetMapping("/search_new")
    @ApiOperation(value = "getElasticPublicationsByWords", nickname = "getElasticPublicationsByWords")
    public ResponseEntity<ElasticResultSearchDto> getElasticPublicationsByWords(@RequestParam String word, @RequestParam int from) {
        ElasticResultSearchDto searchHits = elasticService.getElasticPublicationsByWords(word, from);
        return ResponseEntity.ok(searchHits);
    }
}