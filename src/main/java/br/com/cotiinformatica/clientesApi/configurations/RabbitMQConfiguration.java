package br.com.cotiinformatica.clientesApi.configurations;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfiguration {

    @Value("${rabbitmq.queue.notificacoes}")
    private String queueName;

    /*
        Criação da fila no RabbitMQ (durable = true, ou seja,
        a fila e as mensagens sobrevivem a reinicialização do servidor)
     */
    @Bean
    public Queue queue() {
        return new Queue(queueName, true);
    }

    /*
        Serialização das mensagens da fila no formato JSON
     */
    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
