package kosukeroku.token_radar;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TokenRadarApplicationTests {

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;

    @MockBean
    private kosukeroku.token_radar.service.kafka.KafkaConsumerService kafkaConsumerService;

    @Test
    void contextLoads() {
    }
}