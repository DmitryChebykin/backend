package ru.region_stat.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.region_stat.domain.dto.calendarEvent.CalendarEventCreateDto;
import ru.region_stat.domain.dto.calendarEvent.CalendarEventResultDto;
import ru.region_stat.domain.dto.calendarEvent.CalendarEventUpdateDto;
import ru.region_stat.service.CalendarEventService;
import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@RequestMapping("/calendar-event")
@RestController
@Api("calendar-event")
public class CalendarEventController {
    @Resource
    private CalendarEventService calendarEventService;

    @GetMapping("/all")
    @ApiOperation(value = "getAllEvents", nickname = "getAllEvents")
    public ResponseEntity<List<CalendarEventResultDto>> getAll() {

        return ResponseEntity.ok(calendarEventService.getAll());
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "getByIdEvent", nickname = "getByIdEvent")
    public ResponseEntity<CalendarEventResultDto> findById(@PathVariable("id") UUID id) {
        CalendarEventResultDto calendarEvent = calendarEventService.getById(id);
        return ResponseEntity.ok(calendarEvent);
    }

    @PostMapping
    @ApiOperation(value = "createEvent", nickname = "createEvent")
    public ResponseEntity<CalendarEventResultDto> create(@RequestBody @Validated CalendarEventCreateDto calendarEventCreateDto) {
        return new ResponseEntity<>(calendarEventService.create(calendarEventCreateDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@RequestBody @Validated CalendarEventUpdateDto calendarEventUpdateDto, @PathVariable("id") UUID id) {
        calendarEventService.update(calendarEventUpdateDto, id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "deleteEvent", nickname = "deleteEvent")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        calendarEventService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/getCalendarEventBetweenDates")
    @ApiOperation(value = "getCalendarEventBetweenDates", nickname = "getCalendarEventBetweenDates", httpMethod = "GET")
    public ResponseEntity<List<CalendarEventResultDto>> getCalendarEventBetweenDates(@RequestParam String startDate, @RequestParam String finishDate) {
        List<CalendarEventResultDto> eventResultDtos = calendarEventService.getCalendarEventBetweenDates(startDate, finishDate);
        return ResponseEntity.ok().body(eventResultDtos);
    }
}