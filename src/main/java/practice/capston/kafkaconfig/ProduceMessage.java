package practice.capston.kafkaconfig;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import practice.capston.domain.dto.KafkaDto;

@Component
@RequiredArgsConstructor
public class ProduceMessage {

    private KafkaTemplate<String, KafkaDto> kafkaDtoKafkaTemplate;

    @Value(value = "${kafka.my.topicName}")
    private String topicName;

    @Autowired
    public ProduceMessage(KafkaTemplate<String, KafkaDto> kafkaDtoKafkaTemplate) {
        this.kafkaDtoKafkaTemplate = kafkaDtoKafkaTemplate;
    }

    public void sendMessage(KafkaDto kafkaDto) {
        Long imgId = kafkaDto.getImgId();
        int partition = imgId.intValue() % 3;
        System.out.println("partition = " + partition);
        ListenableFuture<SendResult<String, KafkaDto>> sm = kafkaDtoKafkaTemplate.send(topicName, partition, null, kafkaDto);
    }
}