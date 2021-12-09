package ru.region_stat.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.region_stat.webanalitic.VisitorService;
import ru.region_stat.webanalitic.dto.VisitorEntityDto;

@RequestMapping("/visitor")
@RestController
public class VisitorController {
    private final VisitorService visitorService;

    public VisitorController(VisitorService visitorService) {
        this.visitorService = visitorService;
    }

    @GetMapping("/page-query")
    public ResponseEntity<Page<VisitorEntityDto>> pageQuery(VisitorEntityDto visitorEntityDto, @PageableDefault Pageable pageable) {
        Page<VisitorEntityDto> visitorPage = visitorService.findByCondition(visitorEntityDto, pageable);
        return ResponseEntity.ok(visitorPage);
    }
}