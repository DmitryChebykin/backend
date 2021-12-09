package ru.region_stat.service;

import org.modelmapper.ModelMapper;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.region_stat.domain.dto.publicationType.*;
import ru.region_stat.domain.entity.BaseEntity;
import ru.region_stat.domain.entity.calendarEvent.CalendarEvent;
import ru.region_stat.domain.entity.publicationFormat.PublicationFormatEntity;
import ru.region_stat.domain.entity.publicationType.PublicationTypeEntity;
import ru.region_stat.domain.entity.publicationType.Subscription;
import ru.region_stat.domain.entity.rubric.RubricEntity;
import ru.region_stat.domain.repository.CalendarEventRepository;
import ru.region_stat.domain.repository.PublicationTypeRepository;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PublicationTypeService {
    @Resource
    private PublicationTypeRepository publicationTypeRepository;
    @Resource
    private RubricService rubricService;
    @Resource
    private PublicationFormatService publicationFormatService;
    @Resource
    private ModelMapper modelMapper;

    @Resource
    private CalendarEventRepository calendarEventRepository;

    @Transactional(readOnly = true)
    public List<PublicationTypeResultDto> getAll() {
        List<PublicationTypeEntity> publicationTypeEntityList = publicationTypeRepository.findAll();
        return publicationTypeEntityList.stream()
                .map(publicationTypeEntity -> modelMapper.map(publicationTypeEntity, PublicationTypeResultDto.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PublicationTypeResultDto getById(UUID id) {
        return modelMapper.map(publicationTypeRepository.findById(id).orElseThrow(ResourceNotFoundException::new), PublicationTypeResultDto.class);
    }

    @Transactional
    public PublicationTypeResultDto save(PublicationTypeCreateDto publicationTypeCreateDto) {
        PublicationTypeEntity publicationTypeEntity = modelMapper.map(publicationTypeCreateDto, PublicationTypeEntity.class);

        List<String> rubricEntityIdList = publicationTypeCreateDto.getRubricEntityId();
        List<UUID> rubricEntityUUIDList = rubricEntityIdList.stream().map(UUID::fromString).collect(Collectors.toList());
        List<RubricEntity> rubricEntityList = rubricService.findByIdIn(rubricEntityUUIDList);

        publicationTypeEntity.setRubricEntityList(rubricEntityList);

        PublicationFormatEntity publicationFormatEntity = publicationFormatService.getPublicationFormatByStringId(publicationTypeCreateDto.getFormatEntityId());
        publicationTypeEntity.setPublicationFormatEntity(publicationFormatEntity);

        return modelMapper.map(publicationTypeRepository.save(publicationTypeEntity), PublicationTypeResultDto.class);
    }

    @Transactional
    public PublicationTypeResultDto update(PublicationTypeUpdateDto publicationTypeUpdateDto, UUID id) {

        PublicationTypeEntity publicationTypeEntity = publicationTypeRepository.findById(id).orElseThrow(RuntimeException::new);

        modelMapper.map(publicationTypeUpdateDto, publicationTypeEntity);

        publicationTypeEntity.setSubscription(Subscription.of(publicationTypeUpdateDto.getSubscription()));

        publicationTypeEntity.setPublicationFormatEntity(publicationFormatService.getPublicationFormatByStringId(publicationTypeUpdateDto.getFormatEntityId()));

        List<String> rubricEntityIdList = publicationTypeUpdateDto.getRubricEntityId();
        List<UUID> rubricEntityUUIDList = rubricEntityIdList.stream().map(UUID::fromString).collect(Collectors.toList());
        List<RubricEntity> rubricEntityList = rubricService.findByIdIn(rubricEntityUUIDList);

        publicationTypeEntity.setRubricEntityList(rubricEntityList);

        List<CalendarEventDto> calendarEventDtoList = publicationTypeUpdateDto.getCalendarEventDtoList();

        List<CalendarEvent> calendarEvents = new ArrayList<>();

        for (CalendarEventDto c : calendarEventDtoList) {
            CalendarEvent calendarEvent = CalendarEvent.builder().day(c.getDay()).month(c.getMonth()).year(c.getYear()).build();
            CalendarEvent existEvent = calendarEventRepository.findByDayAndMonthAndYear(c.getDay(), c.getMonth(), c.getYear())
                    .orElseGet(() -> calendarEventRepository.saveAndFlush(calendarEvent));

            calendarEvents.add(existEvent);
            List<PublicationTypeEntity> publicationTypeEntityList = Optional.ofNullable(existEvent.getPublicationTypeEntityList()).orElse(new ArrayList<>());

            if (!calendarEventRepository.publicationEventExistByCalendarEventIdAndPublicationId(existEvent.getId(), publicationTypeEntity.getId())) {
                publicationTypeEntityList.add(publicationTypeEntity);
            }

            existEvent.setPublicationTypeEntityList(publicationTypeEntityList);
        }

        List<UUID> actualCalendarEventIdList = calendarEvents.stream().map(BaseEntity::getId).collect(Collectors.toList());

        calendarEventRepository.removeUnnesessaryCalendarEvents(actualCalendarEventIdList, publicationTypeEntity.getId());

        PublicationTypeResultDto publicationTypeResultDto = modelMapper.map(publicationTypeEntity, PublicationTypeResultDto.class);

        Optional<List<CalendarEvent>> calendarEventListOptional = calendarEventRepository.getEventsByPublicationType(publicationTypeEntity.getId());

        List<CalendarEventDto> calendarEventDtos = null;

        if (calendarEventListOptional.isPresent()) {
            calendarEventDtos = calendarEventListOptional.get().stream().map(e -> modelMapper.map(e, CalendarEventDto.class)).collect(Collectors.toList());
        }

        publicationTypeResultDto.setCalendarEventDtoList(calendarEventDtos);

        return publicationTypeResultDto;
    }

    @Transactional
    public void deleteById(UUID id) {
        publicationTypeRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Boolean existsByName(String name) {
        return publicationTypeRepository.existsByName(name);
    }

    @Transactional(readOnly = true)
    public PublicationTypeEntity getPublicationTypeByStringId(String formatId) {
        return formatId != null ? getPublicationTypeById(UUID.fromString(formatId)) : null;
    }

    @Transactional(readOnly = true)
    public PublicationTypeEntity getPublicationTypeById(UUID id) {
        return publicationTypeRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public Optional<UUID> getIdByName(String name) {
        return Optional.ofNullable(publicationTypeRepository.findByName(name).orElseGet(null).getId());
    }

    @Transactional(readOnly = true)
    public PublicationTypeEntity getPublicationTypeByCode(String code) {
        return publicationTypeRepository.findByCodeIs(code);
    }

    @Transactional(readOnly = true)
    public Boolean existByCode(String code) {
        return publicationTypeRepository.existsByCode(code);
    }

    @Transactional
    public PublicationTypeResultDto saveNew(PublicationTypeCreateNewDto publicationTypeCreateNewDto) {

        PublicationTypeEntity publicationTypeEntity = PublicationTypeEntity.builder()
                .name(publicationTypeCreateNewDto.getName())
                .code(publicationTypeCreateNewDto.getCode())
                .period(publicationTypeCreateNewDto.getPeriod())
                .subscription(Subscription.of(publicationTypeCreateNewDto.getSubscription()))
                .submissionTime(publicationTypeCreateNewDto.getSubmissionTime())
                .build();

        List<String> rubricEntityIdList = publicationTypeCreateNewDto.getRubricEntityId();
        List<UUID> rubricEntityUUIDList = rubricEntityIdList.stream().map(UUID::fromString).collect(Collectors.toList());
        List<RubricEntity> rubricEntityList = rubricService.findByIdIn(rubricEntityUUIDList);

        publicationTypeEntity.setRubricEntityList(rubricEntityList);

        PublicationFormatEntity publicationFormatEntity = publicationFormatService.getPublicationFormatByStringId(publicationTypeCreateNewDto.getFormatEntityId());
        publicationTypeEntity.setPublicationFormatEntity(publicationFormatEntity);
        publicationTypeEntity = publicationTypeRepository.save(publicationTypeEntity);

        List<CalendarEventDto> calendarEventDtoList = publicationTypeCreateNewDto.getCalendarEventDtoList();

        for (CalendarEventDto c : calendarEventDtoList) {
            CalendarEvent calendarEvent = CalendarEvent.builder().day(c.getDay()).month(c.getMonth()).year(c.getYear()).build();
            CalendarEvent existEvent = calendarEventRepository.findByDayAndMonthAndYear(c.getDay(), c.getMonth(), c.getYear())
                    .orElseGet(() -> calendarEventRepository.save(calendarEvent));

            Optional<List<PublicationTypeEntity>> optionalPublicationTypeEntityList = Optional.ofNullable(existEvent.getPublicationTypeEntityList());

            List<PublicationTypeEntity> publicationTypeEntityList = optionalPublicationTypeEntityList.orElse(new ArrayList<>());

            publicationTypeEntityList.add(publicationTypeEntity);
        }

        PublicationTypeResultDto resultDto = modelMapper.map(publicationTypeEntity, PublicationTypeResultDto.class);

        Optional<List<CalendarEvent>> calendarEventListOptional = calendarEventRepository.getEventsByPublicationType(publicationTypeEntity.getId());

        List<CalendarEventDto> calendarEventDtos = null;

        if (calendarEventListOptional.isPresent()) {
            calendarEventDtos = calendarEventListOptional.get().stream().map(e -> modelMapper.map(e, CalendarEventDto.class)).collect(Collectors.toList());
        }

        resultDto.setCalendarEventDtoList(calendarEventDtos);

        resultDto.setRubricEntityId(publicationTypeEntity.getRubricEntityList().stream().map(e -> e.getId().toString()).collect(Collectors.toList()));

        return resultDto;
    }
}