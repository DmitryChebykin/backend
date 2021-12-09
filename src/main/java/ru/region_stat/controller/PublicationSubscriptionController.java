package ru.region_stat.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.region_stat.domain.dto.publicationSubscription.PublicationSubscriptionCreateDto;
import ru.region_stat.domain.dto.publicationSubscription.PublicationSubscriptionResultDto;
import ru.region_stat.domain.dto.publicationSubscription.PublicationSubscriptionUpdateDto;
import ru.region_stat.service.PublicationSubscriptionService;
import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@RequestMapping("/publication-subscription")
@RestController
public class PublicationSubscriptionController {
    @Resource
    private PublicationSubscriptionService publicationSubscriptionService;

    @GetMapping("/all")
    @ApiOperation(value = "getAllPublicationSubscription", nickname = "getAllPublicationSubscription")
    public ResponseEntity<List<PublicationSubscriptionResultDto>> getAll() {
        return ResponseEntity.ok(publicationSubscriptionService.getAll());
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "getByIdPublicationSubscription", nickname = "getByIdPublicationSubscription")
    public ResponseEntity<PublicationSubscriptionResultDto> getById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(publicationSubscriptionService.getPublicationSubscriptionResultDtoById(id));
    }

    @PostMapping
    public ResponseEntity<PublicationSubscriptionResultDto> save(@RequestBody @Validated PublicationSubscriptionCreateDto publicationSubscriptionCreateDto) {
        return new ResponseEntity<>(publicationSubscriptionService.create(publicationSubscriptionCreateDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PublicationSubscriptionResultDto> update(@RequestBody @Validated PublicationSubscriptionUpdateDto publicationSubscriptionUpdateDto, @PathVariable("id") UUID id) {
        publicationSubscriptionService.update(publicationSubscriptionUpdateDto, id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        publicationSubscriptionService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @GetMapping("user/{id}")
    @ApiOperation(value = "getPublicationSubscriptionByUserId", nickname = "getPublicationSubscriptionByUserId")
    public ResponseEntity<List<PublicationSubscriptionResultDto>> getByUserId(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(publicationSubscriptionService.getPublicationSubscriptionResultDtoListByUserId(id));
    }

}