package br.com.cotiinformatica.clientesApi.components;

import br.com.cotiinformatica.clientesApi.dtos.EmailMessageDto;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/*
    CONSUMER: lê as mensagens da fila do RabbitMQ
    e faz o envio dos emails.

    Se o servidor de email estiver fora do ar, o envio é tentado
    novamente algumas vezes (configuração spring.rabbitmq.listener
    no application.yaml). Sem esse limite, a mensagem voltaria para
    a fila e seria reprocessada sem parar.
 */
@Component
public class MensagemConsumer {

    @Autowired
    private EmailComponent emailComponent;

    @RabbitListener(queues = "${rabbitmq.queue.notificacoes}")
    public void receber(EmailMessageDto mensagem) {
        emailComponent.enviar(mensagem);
    }
}
