package br.com.cotiinformatica.clientesApi.dtos;

/*
    Mensagem gravada na fila do RabbitMQ
    contendo os dados do email a ser enviado
 */
public record EmailMessageDto(
        String destinatario,
        String assunto,
        String corpo
) {
}
