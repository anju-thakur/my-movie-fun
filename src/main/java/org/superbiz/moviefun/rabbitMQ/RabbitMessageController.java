package org.superbiz.moviefun.rabbitMQ;

import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@RestController
public class RabbitMessageController {

    private final RabbitTemplate rabbitTemplate;
    private final String queue;
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());

    public RabbitMessageController(ConnectionFactory connectionFactory ,@Value("${rabbitmq.queue}") String queue)
    {
        this.rabbitTemplate=new RabbitTemplate(connectionFactory);
        this.queue=queue;
    }

    @PostMapping("/rabbit")
    public Map<String,String> publishMessage()
    {
        rabbitTemplate.convertAndSend(queue,"This text message will trigger the consumer");
        logger.debug("Trigger to consumer started " );
        logger.debug(rabbitTemplate.toString());
        logger.debug(queue);
        Map<String,String> response=new HashMap();
        response.put("response","This is an unrelated JSON response");
        return response;

    }
}
