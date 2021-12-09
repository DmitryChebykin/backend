package ru.region_stat.webanalitic;

import org.modelmapper.ModelMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.region_stat.configuration.RabbitMQConfig;
import ru.region_stat.webanalitic.dto.VisitorEntityDto;
import ru.region_stat.webanalitic.repository.VisitorRepository;
import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VisitorService {
    private final VisitorRepository visitorRepository;
    private final ModelMapper modelMapper;

    @Resource
    private RabbitTemplate rabbitTemplate;

    public VisitorService(VisitorRepository visitorRepository, ModelMapper modelMapper) {
        this.visitorRepository = visitorRepository;
        this.modelMapper = modelMapper;
    }

    public Page<VisitorEntityDto> findByCondition(VisitorEntityDto visitorDto, Pageable pageable) {
        Page<VisitorEntity> entityPage = visitorRepository.findAll(pageable);
        List<VisitorEntity> entities = entityPage.getContent();
        return new PageImpl<>((entities.stream().map(e -> modelMapper.map(e, VisitorEntityDto.class)).collect(Collectors.toList())), pageable, entityPage.getTotalElements());
    }

    public void sendVisitorInfoToRabbitMQ(Visitor visitor) {

        rabbitTemplate.convertAndSend(RabbitMQConfig.VISITOR_TOPIC_EXCHANGE, "visitor.#", visitor);
    }

    @Transactional
    public void saveAll(List<Visitor> visitors) {
        List<VisitorEntity> visitorEntities = visitors.stream().map(e -> modelMapper.map(e, VisitorEntity.class)).collect(Collectors.toList());
        visitorRepository.saveAll(visitorEntities);
    }
}