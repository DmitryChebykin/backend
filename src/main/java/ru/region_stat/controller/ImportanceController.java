package ru.region_stat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.region_stat.domain.entity.oneTimeRequest.Importance;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController("/importance")
public class ImportanceController {

    @GetMapping("/all")
    public ResponseEntity<Map<String, String>> getAll() {
        Importance[] values = Importance.values();
        Map<String, String> stringMap = Stream.of(values).collect(Collectors.toMap(Importance::toString, Importance::getName));
        return ResponseEntity.ok(stringMap);
    }
}