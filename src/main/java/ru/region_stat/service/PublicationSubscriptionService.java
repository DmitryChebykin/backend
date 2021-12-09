package ru.region_stat.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import ru.region_stat.domain.dto.publicationSubscription.PublicationSubscriptionCreateDto;
import ru.region_stat.domain.dto.publicationSubscription.PublicationSubscriptionResultDto;
import ru.region_stat.domain.dto.publicationSubscription.PublicationSubscriptionUpdateDto;
import ru.region_stat.domain.entity.publicationSubscription.PublicationSubscriptionEntity;
import ru.region_stat.domain.repository.PublicationSubscriptionRepository;
import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PublicationSubscriptionService {
    @Resource
    private PublicationSubscriptionRepository publicationSubscriptionRepository;
    @Resource
    private ModelMapper modelMapper;

    public PublicationSubscriptionResultDto create(PublicationSubscriptionCreateDto publicationSubscriptionCreateDto) {
        PublicationSubscriptionEntity publicationSubscriptionEntity = modelMapper.map(publicationSubscriptionCreateDto, PublicationSubscriptionEntity.class);
        return modelMapper.map(publicationSubscriptionRepository.save(publicationSubscriptionEntity), PublicationSubscriptionResultDto.class);
    }

    public void deleteById(UUID id) {
        publicationSubscriptionRepository.deleteById(id);
    }

    public PublicationSubscriptionResultDto getPublicationSubscriptionResultDtoById(UUID id) {
        return modelMapper.map(publicationSubscriptionRepository.findById(id)
                .orElseThrow(RuntimeException::new), PublicationSubscriptionResultDto.class);
    }

    public PublicationSubscriptionResultDto update(PublicationSubscriptionUpdateDto publicationSubscriptionUpdateDto, UUID id) {
        PublicationSubscriptionEntity publicationSubscriptionEntity = publicationSubscriptionRepository.findById(id).orElseThrow(RuntimeException::new);

        modelMapper.map(publicationSubscriptionUpdateDto, publicationSubscriptionEntity);

        return modelMapper.map(publicationSubscriptionEntity, PublicationSubscriptionResultDto.class);
    }

    public List<PublicationSubscriptionResultDto> getAll() {
        List<PublicationSubscriptionEntity> subscriptionList = publicationSubscriptionRepository.findAll();

        return subscriptionList.stream()
                .map(subscription -> modelMapper.map(subscription, PublicationSubscriptionResultDto.class))
                .collect(Collectors.toList());
    }

    public List<PublicationSubscriptionResultDto>  getPublicationSubscriptionResultDtoListByUserId(UUID id) {
        List<PublicationSubscriptionEntity> allByUserEntityId = publicationSubscriptionRepository.findAllByUserEntityId(id);
        return allByUserEntityId.stream()
                .map(publicationSubscriptionEntity -> modelMapper.map(publicationSubscriptionEntity, PublicationSubscriptionResultDto.class))
                .collect(Collectors.toList());

    }
}