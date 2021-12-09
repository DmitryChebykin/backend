package ru.region_stat.configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String ITEM_TOPIC_EXCHANGE = "publication_file_topic_exchange";

    public static final String ITEM_QUEUE = "publication_file_queue";


    public static final String VISITOR_TOPIC_EXCHANGE = "visitor_topic_exchange";

    public static final String VISITOR_QUEUE = "visitor_queue";

    @Bean("publicationFileTopicExchange")
    public Exchange topicExchange() {
        return ExchangeBuilder.topicExchange(ITEM_TOPIC_EXCHANGE).durable(true).build();
    }

    @Bean("publicationFileQueue")
    public Queue itemQueue() {
        return QueueBuilder.durable(ITEM_QUEUE).build();
    }

    @Bean("publicationExchange")
    public Binding itemQueueExchange(@Qualifier("publicationFileQueue") Queue queue,
                                     @Qualifier("publicationFileTopicExchange") Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("pub_file.#").noargs();
    }


    @Bean("visitorTopicExchange")
    public Exchange visitorTopicExchange() {
        return ExchangeBuilder.topicExchange(VISITOR_TOPIC_EXCHANGE).durable(true).build();
    }

    @Bean("visitorQueue")
    public Queue visitorItemQueue() {
        return QueueBuilder.durable(VISITOR_QUEUE).build();
    }

    @Bean("visitorQueueExchange")
    public Binding visitorQueueExchange(@Qualifier("visitorQueue") Queue queue,
                                     @Qualifier("visitorTopicExchange") Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("visitor.#").noargs();
    }


    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(producerJackson2MessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}