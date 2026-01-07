package org.example.reportservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.reports:cityfix.reports}")
    private String reportsExchange;

    @Value("${rabbitmq.exchange.audit:cityfix.audit}")
    private String auditExchange;

    @Value("${rabbitmq.queue.report-created:report.created.queue}")
    private String reportCreatedQueue;

    @Value("${rabbitmq.routing-key.report-created:report.created}")
    private String reportCreatedRoutingKey;

    @Bean
    public TopicExchange reportsExchange() {
        return new TopicExchange(reportsExchange);
    }

    @Bean
    public TopicExchange auditExchange() {
        return new TopicExchange(auditExchange);
    }

    @Bean
    public Queue reportCreatedQueue() {
        return new Queue(reportCreatedQueue, true);
    }

    @Bean
    public Binding reportCreatedBinding() {
        return BindingBuilder
                .bind(reportCreatedQueue())
                .to(reportsExchange())
                .with(reportCreatedRoutingKey);
    }

    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}
