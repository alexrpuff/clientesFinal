package br.com.cotiinformatica.clientesApi;

import br.com.cotiinformatica.clientesApi.components.MensagemProducer;
import br.com.cotiinformatica.clientesApi.dtos.EmailMessageDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
    Testes do serviço de mensageria (PRODUCER -> RabbitMQ -> CONSUMER -> email).
    Os emails enviados são verificados através da API do Mailpit.
 */
@SpringBootTest
@AutoConfigureMockMvc
class MensageriaTest {

    private static final String MAILPIT_API = "http://localhost:8025/api/v1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MensagemProducer mensagemProducer;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    @DisplayName("Deve enviar o email de boas vindas ao cadastrar um cliente")
    void deveEnviarEmailAoCadastrarCliente() throws Exception {
        var cliente = ClienteDataFactory.novoCliente();

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isCreated());

        assertTrue(aguardarEmail((String) cliente.get("email")),
                "O email de boas vindas não foi recebido no Mailpit");
    }

    @Test
    @DisplayName("PRODUCER deve gravar a mensagem na fila e o CONSUMER deve enviar o email")
    void deveProcessarMensagemDaFila() throws Exception {
        var destinatario = ClienteDataFactory.email();

        mensagemProducer.enviar(new EmailMessageDto(destinatario, "Teste de mensageria", "Mensagem de teste."));

        assertTrue(aguardarEmail(destinatario),
                "A mensagem não foi processada pelo CONSUMER");
    }

    //Consulta a caixa de entrada do Mailpit por até 15 segundos
    private boolean aguardarEmail(String destinatario) throws Exception {
        var query = URLEncoder.encode("to:\"" + destinatario + "\"", StandardCharsets.UTF_8);
        var request = HttpRequest.newBuilder(URI.create(MAILPIT_API + "/search?query=" + query)).GET().build();

        for (int i = 0; i < 30; i++) {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            var total = objectMapper.readTree(response.body()).path("messages_count").asInt();
            if (total > 0) {
                return true;
            }
            Thread.sleep(500);
        }
        return false;
    }
}
