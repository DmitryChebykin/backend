package ru.region_stat.aspects.rabbit;

import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import ru.region_stat.configuration.RabbitMQConfig;
import ru.region_stat.domain.dto.file.PublicationFileResultDto;
import ru.region_stat.domain.dto.statisticalPublication.StatisticalPublicationResultDto;
import ru.region_stat.rabbit.RabbitDocMessageDto;
import ru.region_stat.service.PublicationFileService;
import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Component
@Aspect
public class DocRabbitProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private PublicationFileService publicationFileService;
    @Resource
    private Tika tika;

    @Pointcut("@annotation(ru.region_stat.aspects.rabbit.Doc)")
    public void docPointcut() {
    }

    @AfterReturning(pointcut = "docPointcut()", returning = "result")
    public void docAfterReturning(JoinPoint jp, Object result) throws Throwable {
        Map<PublicationFileResultDto, StatisticalPublicationResultDto> objectMap = (Map<PublicationFileResultDto, StatisticalPublicationResultDto>) result;

        if (!objectMap.isEmpty()) {
            PublicationFileResultDto publicationFileResultDto = objectMap.keySet().iterator().next();
            StatisticalPublicationResultDto statisticalPublicationResultDto = objectMap.get(publicationFileResultDto);

            String textDocument = null;
            byte[] fileBody = publicationFileService.getByteContent(publicationFileResultDto.getId());

            try {
                InputStream stream = TikaInputStream.get(fileBody);

                textDocument = tika.parseToString(stream);
            } catch (IOException | TikaException e) {
                e.printStackTrace();
            }

            String cleanText = null;

            if (textDocument != null) {
                cleanText = StringUtils.normalizeSpace(textDocument.replaceAll("(\\r|\\n|\\t)", " "));
            }

            RabbitDocMessageDto rabbitDocMessageDto = RabbitDocMessageDto.builder()
                    .publicationId(statisticalPublicationResultDto.getId().toString())
                    .fileId(publicationFileResultDto.getId().toString())
                    .fileName(publicationFileResultDto.getFileName())
                    .isArchive(statisticalPublicationResultDto.getIsArchive())
                    .documentText(cleanText)
                    .name(statisticalPublicationResultDto.getName())
                    .build();

            rabbitTemplate.convertAndSend(RabbitMQConfig.ITEM_TOPIC_EXCHANGE, "pub_file.#", rabbitDocMessageDto);
        }
    }
}