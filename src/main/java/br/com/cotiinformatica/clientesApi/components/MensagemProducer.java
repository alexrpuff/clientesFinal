package br.com.cotiinformatica.clientesApi.components;

import br.com.cotiinformatica.clientesApi.dtos.EmailMessageDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/*
    PRODUCER: grava as mensagens de email na fila do RabbitMQ.

    Por que uma fila, e não enviar o email direto? O cliente não
    precisa esperar o servidor de email responder para ter o seu
    cadastro concluído. O cadastro termina na hora, a mensagem fica
    guardada na fila e o CONSUMER envia o email em seguida; se o
    servidor de email estiver lento ou fora do ar, o cadastro não é afetado.
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
