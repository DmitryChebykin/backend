package ru.region_stat.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.region_stat.domain.dto.calendarEvent.CalendarEventCreateDto;
import ru.region_stat.domain.dto.calendarEvent.CalendarEventResultDto;
import ru.region_stat.domain.dto.calendarEvent.CalendarEventUpdateDto;
import ru.region_stat.domain.entity.calendarEvent.CalendarEvent;
import ru.region_stat.domain.entity.publicationType.PublicationTypeEntity;
import ru.region_stat.domain.repository.CalendarEventRepository;
import ru.region_stat.domain.repository.PublicationTypeRepository;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CalendarEventService {

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.BASIC_ISO_DATE;

    @Resource
    private CalendarEventRepository calendarEventRepository;

    @Resource
    private PublicationTypeRepository publicationTypeRepository;

    @Resource
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<CalendarEventResultDto> getAll() {
        List<CalendarEvent> calendarEvents = calendarEventRepository.findAll();

        return calendarEvents.stream()
                .map(calendarEvent -> modelMapper.map(calendarEvent, CalendarEventResultDto.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public CalendarEventResultDto create(CalendarEventCreateDto calendarEventCreateDto) {

        try {
            LocalDate localDate = LocalDate.of(calendarEventCreateDto.getYear(), calendarEventCreateDto.getMonth(), calendarEventCreateDto.getDay());
        } catch (
                DateTimeParseException e) {
            throw new RuntimeException();
        }

        CalendarEvent calendarEvent = modelMapper.map(calendarEventCreateDto, CalendarEvent.class);
        List<UUID> publicationTypeIdList = calendarEventCreateDto.getPublicationTypeIdList();
        List<PublicationTypeEntity> publicationTypeEntities = publicationTypeRepository.findByIdIn(publicationTypeIdList).orElseThrow(RuntimeException::new);
        calendarEvent.setPublicationTypeEntityList(publicationTypeEntities);
        CalendarEvent event = calendarEventRepository.save(calendarEvent);
        return modelMapper.map(event, CalendarEventResultDto.class);
    }

    @Transactional(readOnly = true)
    public CalendarEventResultDto getById(UUID id) {
        return modelMapper.map(calendarEventRepository.findById(id).orElseThrow(RuntimeException::new), CalendarEventResultDto.class);
    }

    @Transactional
    public CalendarEventResultDto update(CalendarEventUpdateDto calendarEventUpdateDto, UUID id) {
        CalendarEvent calendarEvent = calendarEventRepository.findById(id).orElseThrow(RuntimeException::new);
        modelMapper.map(calendarEventUpdateDto, calendarEvent);
        List<UUID> publicationTypeIdList = calendarEventUpdateDto.getPublicationTypeIdList();
        List<PublicationTypeEntity> publicationTypeEntities = publicationTypeRepository.findByIdIn(publicationTypeIdList).orElseThrow(RuntimeException::new);
        calendarEvent.setPublicationTypeEntityList(publicationTypeEntities);
        return modelMapper.map(calendarEvent, CalendarEventResultDto.class);
    }

    @Transactional
    public void deleteById(UUID id) {
        calendarEventRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<CalendarEventResultDto> getCalendarEventBetweenDates(String startDate, String finishDate) {
        LocalDate endDate;
        LocalDate beginDate;

        try {
            beginDate = LocalDate.parse(startDate, dateFormatter);
            endDate = LocalDate.parse(finishDate, dateFormatter);
        } catch (
                DateTimeParseException e) {
            throw new RuntimeException();
        }

        Date fromDate = Date.from(beginDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date toDate = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<CalendarEvent> calendarEvents = calendarEventRepository.findAllByLocalDateIsBetween(fromDate, toDate).orElseThrow(RuntimeException::new);

        return calendarEvents.stream()
                .map(calendarEvent -> modelMapper.map(calendarEvent, CalendarEventResultDto.class))
                .collect(Collectors.toList());
    }
}