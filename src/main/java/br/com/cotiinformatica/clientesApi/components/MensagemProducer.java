package br.com.cotiinformatica.clientesApi.components;

import br.com.cotiinformatica.clientesApi.dtos.EmailMessageDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/*
    PRODUCER: grava as mensagens de email na fila do RabbitMQ
 */
@Component
public class MensagemProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.queue.notificacoes}")
    private String queueName;

    public void enviar(EmailMessageDto mensagem) {
        rabbitTemplate.convertAndSend(queueName, mensagem);
    }
}
