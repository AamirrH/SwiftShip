package com.code.prodapp.trackingservice.configs;

import com.code.prodapp.trackingservice.DTOs.TrackingSessionResponseDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, TrackingSessionResponseDTO> trackingRedisTemplate(
            RedisConnectionFactory connectionFactory
    ) {
        RedisTemplate<String, TrackingSessionResponseDTO> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        JacksonJsonRedisSerializer<TrackingSessionResponseDTO> valueSerializer =
                new JacksonJsonRedisSerializer<>(TrackingSessionResponseDTO.class);

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(valueSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
