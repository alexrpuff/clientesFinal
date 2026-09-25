package br.com.cotiinformatica.clientesApi;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
    Testes de integração dos ENDPOINTS da API de clientes.
    Pré-requisito: containers do docker-compose.yml em execução
    (docker compose up -d)
 */
@SpringBootTest
@AutoConfigureMockMvc
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------- POST /api/clientes ----------

    @Test
    @DisplayName("POST /api/clientes - deve cadastrar um cliente com sucesso")
    void deveCadastrarCliente() throws Exception {
        var cliente = ClienteDataFactory.novoCliente();
        var endereco = (Map<?, ?>) cliente.get("endereco");

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value(cliente.get("nome")))
                .andExpect(jsonPath("$.email").value(cliente.get("email")))
                .andExpect(jsonPath("$.cpf").value(cliente.get("cpf")))
                .andExpect(jsonPath("$.dataNascimento").value(cliente.get("dataNascimento")))
                .andExpect(jsonPath("$.enderecos", hasSize(1)))
                .andExpect(jsonPath("$.enderecos[0].id").isNumber())
                .andExpect(jsonPath("$.enderecos[0].logradouro").value(endereco.get("logradouro")))
                .andExpect(jsonPath("$.enderecos[0].cep").value(endereco.get("cep")));
    }

    @Test
    @DisplayName("POST /api/clientes - não deve permitir CPF duplicado")
    void naoDeveCadastrarClienteComCpfDuplicado() throws Exception {
        var cliente = ClienteDataFactory.novoCliente();
        cadastrar(cliente);

        var outroCliente = ClienteDataFactory.novoCliente();
        outroCliente.put("cpf", cliente.get("cpf"));

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(outroCliente)))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("CPF")));
    }

    @Test
    @DisplayName("POST /api/clientes - deve validar os dados do cliente")
    void naoDeveCadastrarClienteComDadosInvalidos() throws Exception {
        var cliente = ClienteDataFactory.novoCliente();
        cliente.put("nome", "Ana");                 //menos de 8 caracteres
        cliente.put("email", "email-invalido");     //formato inválido
        cliente.put("cpf", "123.456.789-01");       //não contém somente 11 dígitos
        cliente.remove("dataNascimento");           //obrigatório

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.nome").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.cpf").exists())
                .andExpect(jsonPath("$.dataNascimento").exists());
    }

    @Test
    @DisplayName("POST /api/clientes - nome deve ter no máximo 100 caracteres")
    void naoDeveCadastrarClienteComNomeMuitoGrande() throws Exception {
        var cliente = ClienteDataFactory.novoCliente();
        cliente.put("nome", "A".repeat(101));

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.nome").exists());
    }

    @Test
    @DisplayName("POST /api/clientes - data de nascimento não pode ser futura")
    void naoDeveCadastrarClienteComDataNascimentoFutura() throws Exception {
        var cliente = ClienteDataFactory.novoCliente();
        cliente.put("dataNascimento", LocalDate.now().plusDays(1).toString());

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.dataNascimento").exists());
    }

    // ---------- PUT /api/clientes ----------

    @Test
    @DisplayName("PUT /api/clientes - deve editar um cliente e o seu endereço com sucesso")
    void deveEditarCliente() throws Exception {
        var criado = cadastrar(ClienteDataFactory.novoCliente());
        Integer id = JsonPath.read(criado, "$.id");
        Integer enderecoId = JsonPath.read(criado, "$.enderecos[0].id");

        var edicao = ClienteDataFactory.novoCliente();
        edicao.put("id", id);
        var endereco = ClienteDataFactory.novoEndereco();
        endereco.put("id", enderecoId);
        edicao.put("endereco", endereco);

        mockMvc.perform(put("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(edicao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nome").value(edicao.get("nome")))
                .andExpect(jsonPath("$.email").value(edicao.get("email")))
                .andExpect(jsonPath("$.cpf").value(edicao.get("cpf")))
                .andExpect(jsonPath("$.enderecos", hasSize(1)))
                .andExpect(jsonPath("$.enderecos[0].id").value(enderecoId))
                .andExpect(jsonPath("$.enderecos[0].logradouro").value(endereco.get("logradouro")));
    }

    @Test
    @DisplayName("PUT /api/clientes - deve adicionar um endereço quando o id do endereço não for informado")
    void deveAdicionarEnderecoNaEdicao() throws Exception {
        var cliente = ClienteDataFactory.novoCliente();
        var criado = cadastrar(cliente);
        Integer id = JsonPath.read(criado, "$.id");

        cliente.put("id", id);
        cliente.put("endereco", ClienteDataFactory.novoEndereco());

        mockMvc.perform(put("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enderecos", hasSize(2)));
    }

    @Test
    @DisplayName("PUT /api/clientes - deve retornar 404 para cliente inexistente")
    void naoDeveEditarClienteInexistente() throws Exception {
        var edicao = ClienteDataFactory.novoCliente();
        edicao.put("id", Integer.MAX_VALUE);

        mockMvc.perform(put("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(edicao)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/clientes - não deve permitir CPF de outro cliente")
    void naoDeveEditarClienteComCpfDeOutroCliente() throws Exception {
        var cliente1 = ClienteDataFactory.novoCliente();
        cadastrar(cliente1);
        var cliente2 = ClienteDataFactory.novoCliente();
        Integer id2 = JsonPath.read(cadastrar(cliente2), "$.id");

        cliente2.put("id", id2);
        cliente2.put("cpf", cliente1.get("cpf"));

        mockMvc.perform(put("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente2)))
                .andExpect(status().isConflict());
    }

    // ---------- DELETE /api/clientes/{id} ----------

    @Test
    @DisplayName("DELETE /api/clientes/{id} - deve excluir um cliente com sucesso")
    void deveExcluirCliente() throws Exception {
        var cliente = ClienteDataFactory.novoCliente();
        Integer id = JsonPath.read(cadastrar(cliente), "$.id");

        mockMvc.perform(delete("/api/clientes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nome").value(cliente.get("nome")))
                .andExpect(jsonPath("$.enderecos", hasSize(1)));

        mockMvc.perform(get("/api/clientes/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/clientes/{id} - deve retornar 404 para cliente inexistente")
    void naoDeveExcluirClienteInexistente() throws Exception {
        mockMvc.perform(delete("/api/clientes/" + Integer.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    // ---------- GET /api/clientes ----------

    @Test
    @DisplayName("GET /api/clientes - deve consultar os clientes em ordem alfabética com os seus endereços")
    void deveConsultarClientesEmOrdemAlfabetica() throws Exception {
        var clienteZ = ClienteDataFactory.novoCliente();
        clienteZ.put("nome", "Zzzz " + clienteZ.get("nome"));
        var clienteA = ClienteDataFactory.novoCliente();
        clienteA.put("nome", "Aaaa " + clienteA.get("nome"));

        //Cadastrando fora de ordem
        cadastrar(clienteZ);
        cadastrar(clienteA);

        var response = mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].enderecos").isArray())
                .andReturn().getResponse().getContentAsString();

        List<String> nomes = JsonPath.read(response, "$[*].nome");
        assertTrue(nomes.indexOf(clienteA.get("nome")) < nomes.indexOf(clienteZ.get("nome")),
                "Os clientes devem estar em ordem alfabética");
    }

    // ---------- GET /api/clientes/{id} ----------

    @Test
    @DisplayName("GET /api/clientes/{id} - deve consultar um cliente através do ID")
    void deveConsultarClientePorId() throws Exception {
        var cliente = ClienteDataFactory.novoCliente();
        Integer id = JsonPath.read(cadastrar(cliente), "$.id");

        mockMvc.perform(get("/api/clientes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nome").value(cliente.get("nome")))
                .andExpect(jsonPath("$.cpf").value(cliente.get("cpf")))
                .andExpect(jsonPath("$.enderecos", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/clientes/{id} - deve retornar 404 para cliente inexistente")
    void naoDeveConsultarClienteInexistente() throws Exception {
        mockMvc.perform(get("/api/clientes/" + Integer.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    //Método auxiliar para cadastrar um cliente e retornar o JSON da resposta
    private String cadastrar(Map<String, Object> cliente) throws Exception {
        return mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }
}
