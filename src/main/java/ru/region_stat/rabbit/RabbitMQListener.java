package ru.region_stat.rabbit;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ru.region_stat.configuration.RabbitMQConfig;
import ru.region_stat.elastic.ElasticPublicationEntity;
import ru.region_stat.elastic.ElasticRepository;
import ru.region_stat.webanalitic.Visitor;
import ru.region_stat.webanalitic.VisitorService;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Profile("!import")
public class RabbitMQListener {
    private final List<Visitor> visitors = new ArrayList<>();

    @Resource
    private ElasticRepository elasticRepository;

    @Resource
    private VisitorService visitorService;

    @RabbitListener(queues = {RabbitMQConfig.ITEM_QUEUE})
    public void saveOrUpdateElasticDocument(RabbitDocMessageDto rabbitDocMessageDto) {
        String id = rabbitDocMessageDto.getPublicationId();
        Optional<ElasticPublicationEntity> optionalElasticMessage = elasticRepository.findByPublicationId(id);
        if (optionalElasticMessage.isPresent()) {

            ElasticPublicationEntity elasticPublicationEntity = optionalElasticMessage.get();
            BeanUtils.copyProperties(rabbitDocMessageDto, elasticPublicationEntity);
            return;
        }

        ElasticPublicationEntity elasticPublicationEntity = new ElasticPublicationEntity();
        BeanUtils.copyProperties(rabbitDocMessageDto, elasticPublicationEntity);
        elasticRepository.save(elasticPublicationEntity);
    }

    @RabbitListener(queues = {RabbitMQConfig.VISITOR_QUEUE})
    public void saveVisitor(Visitor visitor){

        if (visitors.size() > 50) {
            visitorService.saveAll(visitors);
            visitors.clear();
        } else {
            visitors.add(visitor);
        }
    }
}