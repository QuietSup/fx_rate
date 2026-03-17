package com.goodsoup.fx_rate.mq;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue fxImportQueue(@Value("${app.rabbit.fx-import-queue}") String name) {
        return new Queue(name, true);
    }

    @Bean
    public MessageConverter messageConverter() {
        SimpleMessageConverter converter = new SimpleMessageConverter();
        converter.setAllowedListPatterns(java.util.List.of("com.goodsoup.fx_rate.*", "java.util.*"));
        return converter;
    }
}
