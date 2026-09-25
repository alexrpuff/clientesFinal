package br.com.cotiinformatica.clientesApi.controllers;

import br.com.cotiinformatica.clientesApi.dtos.CriarClienteRequest;
import br.com.cotiinformatica.clientesApi.dtos.EditarClienteRequest;
import br.com.cotiinformatica.clientesApi.exceptions.CpfJaCadastradoException;
import br.com.cotiinformatica.clientesApi.exceptions.RegistroNaoEncontradoException;
import br.com.cotiinformatica.clientesApi.services.ClienteService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*
    ENDPOINTS de clientes. O controller só recebe a requisição, chama
    o ClienteService (onde ficam as regras de negócio) e traduz o
    resultado para o código HTTP certo:

    - 201/200: deu certo, devolve o cliente
    - 400: dados inválidos (tratado no ValidationExceptionHandler)
    - 404: cliente ou endereço não existe
    - 409: o CPF já pertence a outro cliente
    - 500: erro inesperado
 */
@Slf4j
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    //Mensagem amigável para erros inesperados (os detalhes técnicos ficam só no log)
    private static final String MENSAGEM_ERRO_INTERNO =
            "Não foi possível concluir a operação no momento. Tente novamente em alguns instantes.";

    @Autowired
    private ClienteService clienteService;

    /*
        ENDPOINT para cadastro de cliente
     */
    @PostMapping
    public ResponseEntity<?> post(@RequestBody @Valid CriarClienteRequest request) {
        try {
            var response = clienteService.criarCliente(request);
            //HTTP 201 (CREATED)
            return ResponseEntity.status(201).body(response);
        }
        catch (CpfJaCadastradoException e) {
            //HTTP 409 (CONFLICT)
            return ResponseEntity.status(409).body(e.getMessage());
        }
        catch (Exception e) {
            //HTTP 500 (INTERNAL SERVER ERROR)
            return erroInterno(e);
        }
    }

    /*
        ENDPOINT para edição de cliente
     */
    @PutMapping
    public ResponseEntity<?> put(@RequestBody @Valid EditarClienteRequest request) {
        try {
            var response = clienteService.editarCliente(request);
            //HTTP 200 (OK)
            return ResponseEntity.status(200).body(response);
        }
        catch (RegistroNaoEncontradoException e) {
            //HTTP 404 (NOT FOUND)
            return ResponseEntity.status(404).body(e.getMessage());
        }
        catch (CpfJaCadastradoException e) {
            //HTTP 409 (CONFLICT)
            return ResponseEntity.status(409).body(e.getMessage());
        }
        catch (Exception e) {
            //HTTP 500 (INTERNAL SERVER ERROR)
            return erroInterno(e);
        }
    }

    /*
        ENDPOINT para exclusão de cliente
     */
    @DeleteMapping("{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            var response = clienteService.excluirCliente(id);
            //HTTP 200 (OK)
            return ResponseEntity.status(200).body(response);
        }
        catch (RegistroNaoEncontradoException e) {
            //HTTP 404 (NOT FOUND)
            return ResponseEntity.status(404).body(e.getMessage());
        }
        catch (Exception e) {
            //HTTP 500 (INTERNAL SERVER ERROR)
            return erroInterno(e);
        }
    }

    /*
        ENDPOINT para consulta de todos os clientes (ordem alfabética)
     */
    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            var response = clienteService.consultarClientes();
            //HTTP 200 (OK)
            return ResponseEntity.status(200).body(response);
        }
        catch (Exception e) {
            //HTTP 500 (INTERNAL SERVER ERROR)
            return erroInterno(e);
        }
    }

    /*
        ENDPOINT para consulta de 1 cliente através do ID
     */
    @GetMapping("{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        try {
            var response = clienteService.obterClientePorId(id);
            //HTTP 200 (OK)
            return ResponseEntity.status(200).body(response);
        }
        catch (RegistroNaoEncontradoException e) {
            //HTTP 404 (NOT FOUND)
            return ResponseEntity.status(404).body(e.getMessage());
        }
        catch (Exception e) {
            //HTTP 500 (INTERNAL SERVER ERROR)
            return erroInterno(e);
        }
    }

    /*
        Em um erro inesperado, quem usa o sistema recebe uma mensagem
        compreensível. O detalhe técnico (ex.: SQL, nome de tabela)
        vai para o log, onde a equipe consegue investigar, e não é
        exposto na resposta da API.
     */
    private ResponseEntity<String> erroInterno(Exception e) {
        log.error("Erro inesperado ao processar a requisição de clientes", e);
        return ResponseEntity.status(500).body(MENSAGEM_ERRO_INTERNO);
    }
}
