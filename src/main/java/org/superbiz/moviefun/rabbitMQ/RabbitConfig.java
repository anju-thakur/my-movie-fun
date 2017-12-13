package org.superbiz.moviefun.rabbitMQ;

import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.amqp.Amqp;
import org.superbiz.moviefun.albums.AlbumsUpdateMessageConsumer;

@Configuration
public class RabbitConfig {

    @Value("${rabbitmq.uri}") String rabbitMqUri;
    @Value ("${rabbitmq.queue}") String rabbitMqQueue;

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());
    @Bean
    public ConnectionFactory connectionFactory()
    {
        CachingConnectionFactory factory=new CachingConnectionFactory();
        factory.setUri(rabbitMqUri);
        return  factory;
    }

    @Bean
    public IntegrationFlow amqpInbound(ConnectionFactory connectionFactory,AlbumsUpdateMessageConsumer consumer)
    {

        IntegrationFlow myflow = IntegrationFlows
                .from(Amqp.inboundAdapter(connectionFactory, rabbitMqQueue))
                .handle(consumer::consume)
                .get();

        logger.debug(rabbitMqQueue);

          //return IntegrationFlows.from(Amqp.inboundAdapter(connectionFactory,rabbitMqQueue)).handle(consumer::consume).get();
        return myflow;


    }
}
