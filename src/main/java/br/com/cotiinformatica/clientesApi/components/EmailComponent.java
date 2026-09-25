package br.com.cotiinformatica.clientesApi.components;

import br.com.cotiinformatica.clientesApi.dtos.EmailMessageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/*
    Componente para envio de emails através do servidor SMTP
    (em desenvolvimento, o Mailpit: http://localhost:8025)
 */
@Component
public class EmailComponent {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${mail.remetente}")
    private String remetente;

    public void enviar(EmailMessageDto mensagem) {

        var email = new SimpleMailMessage();
        email.setFrom(remetente);
        email.setTo(mensagem.destinatario());
        email.setSubject(mensagem.assunto());
        email.setText(mensagem.corpo());

        mailSender.send(email);
    }
}
