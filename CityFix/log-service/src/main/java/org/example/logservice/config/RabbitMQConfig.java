package org.example.logservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.audit:cityfix.audit}")
    private String auditExchange;

    @Value("${rabbitmq.queue.audit-logs:audit.logs.queue}")
    private String auditLogsQueue;

    @Value("${rabbitmq.routing-key.audit:audit.#}")
    private String auditRoutingKey;

    @Bean
    public TopicExchange auditExchange() {
        return new TopicExchange(auditExchange);
    }

    @Bean
    public Queue auditLogsQueue() {
        return new Queue(auditLogsQueue, true);
    }

    @Bean
    public Binding auditLogsBinding() {
        return BindingBuilder
                .bind(auditLogsQueue())
                .to(auditExchange())
                .with(auditRoutingKey);
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

