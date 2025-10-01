package ua.edu.practice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфігурація RabbitMQ: черги, exchange, routing keys
 */
@Configuration
public class RabbitMQConfig {

    @Value("${queue.order.exchanges.main}")
    private String mainExchange;

    @Value("${queue.order.queues.urgent}")
    private String urgentQueue;

    @Value("${queue.order.queues.vip}")
    private String vipQueue;

    @Value("${queue.order.queues.standard}")
    private String standardQueue;

    @Value("${queue.order.queues.background}")
    private String backgroundQueue;

    @Value("${queue.order.routing-keys.urgent}")
    private String urgentRoutingKey;

    @Value("${queue.order.routing-keys.vip}")
    private String vipRoutingKey;

    @Value("${queue.order.routing-keys.standard}")
    private String standardRoutingKey;

    @Value("${queue.order.routing-keys.background}")
    private String backgroundRoutingKey;

    // Exchange
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(mainExchange);
    }

    // Queues з пріоритетами
    @Bean
    public Queue urgentOrderQueue() {
        return QueueBuilder.durable(urgentQueue)
                .withArgument("x-max-priority", 10)
                .build();
    }

    @Bean
    public Queue vipOrderQueue() {
        return QueueBuilder.durable(vipQueue)
                .withArgument("x-max-priority", 8)
                .build();
    }

    @Bean
    public Queue standardOrderQueue() {
        return QueueBuilder.durable(standardQueue)
                .withArgument("x-max-priority", 5)
                .build();
    }

    @Bean
    public Queue backgroundTaskQueue() {
        return QueueBuilder.durable(backgroundQueue)
                .withArgument("x-max-priority", 1)
                .build();
    }

    // Bindings
    @Bean
    public Binding urgentBinding() {
        return BindingBuilder
                .bind(urgentOrderQueue())
                .to(orderExchange())
                .with(urgentRoutingKey);
    }

    @Bean
    public Binding vipBinding() {
        return BindingBuilder
                .bind(vipOrderQueue())
                .to(orderExchange())
                .with(vipRoutingKey);
    }

    @Bean
    public Binding standardBinding() {
        return BindingBuilder
                .bind(standardOrderQueue())
                .to(orderExchange())
                .with(standardRoutingKey);
    }

    @Bean
    public Binding backgroundBinding() {
        return BindingBuilder
                .bind(backgroundTaskQueue())
                .to(orderExchange())
                .with(backgroundRoutingKey);
    }

    // Message Converter
    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    // Listener Container Factory для urgent orders
    @Bean
    public SimpleRabbitListenerContainerFactory urgentRabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(6);
        factory.setPrefetchCount(5);
        return factory;
    }

    // Listener Container Factory для VIP orders
    @Bean
    public SimpleRabbitListenerContainerFactory vipRabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(2);
        factory.setMaxConcurrentConsumers(5);
        factory.setPrefetchCount(10);
        return factory;
    }

    // Listener Container Factory для standard orders
    @Bean
    public SimpleRabbitListenerContainerFactory standardRabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(2);
        factory.setMaxConcurrentConsumers(4);
        factory.setPrefetchCount(20);
        return factory;
    }
}

