package com.vpgh.dms.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.host}")
    private String host;

    @Value("${rabbitmq.port}")
    private int port;

    @Value("${rabbitmq.username}")
    private String username;

    @Value("${rabbitmq.password}")
    private String password;

    @Value("${rabbitmq.document.queue}")
    private String documentQueueName;

    @Value("${rabbitmq.document.exchange}")
    private String documentExchangeName;

    @Value("${rabbitmq.document.routing-key}")
    private String documentRoutingKey;

    @Value("${rabbitmq.document.processing-result.queue}")
    private String processingResultQueueName;

    @Bean
    public ConnectionFactory rabbitConnectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory(host, port);
        factory.setUsername(username);
        factory.setPassword(password);
        return factory;
    }

    @Bean
    public Jackson2JsonMessageConverter rabbitJsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory rabbitConnectionFactory,
            Jackson2JsonMessageConverter rabbitJsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(rabbitConnectionFactory);
        template.setMessageConverter(rabbitJsonMessageConverter);
        return template;
    }

    @Bean
    public Queue documentQueue() {
        return new Queue(documentQueueName, true);
    }

    @Bean
    public Queue processingResultQueue() {
        return new Queue(processingResultQueueName, true);
    }

    @Bean
    public TopicExchange documentExchange() {
        return new TopicExchange(documentExchangeName, true, false);
    }

    @Bean
    public Binding documentBinding(Queue documentQueue, TopicExchange documentExchange) {
        return BindingBuilder
                .bind(documentQueue)
                .to(documentExchange)
                .with(documentRoutingKey);
    }
}

