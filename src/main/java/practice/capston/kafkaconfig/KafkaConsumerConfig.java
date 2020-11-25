package practice.capston.kafkaconfig;

import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.StickyAssignor;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import practice.capston.domain.dto.KafkaDto;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {
    @Value(value = "${kafka.my.bootstrapAddress}")
    private String bootstrapAddress;

    @Value(value = "${kafka.my.groupId}")
    private String groupId;


    @Bean
    public ConsumerFactory<String, KafkaDto> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress
        );
        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                groupId
        );
        props.put(
                ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG,
                StickyAssignor.class.getName()
        );
        props.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );
        props.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class
        );
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaDto>
    kafkaDtoConcurrentKafkaListenerContainerFactory () {
        ConcurrentKafkaListenerContainerFactory<String, KafkaDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}