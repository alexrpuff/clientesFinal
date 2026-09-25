package br.com.cotiinformatica.clientesApi.controllers;

import br.com.cotiinformatica.clientesApi.dtos.CriarClienteRequest;
import br.com.cotiinformatica.clientesApi.dtos.EditarClienteRequest;
import br.com.cotiinformatica.clientesApi.exceptions.CpfJaCadastradoException;
import br.com.cotiinformatica.clientesApi.exceptions.RegistroNaoEncontradoException;
import br.com.cotiinformatica.clientesApi.services.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

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
            return ResponseEntity.status(500).body(e.getMessage());
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
            return ResponseEntity.status(500).body(e.getMessage());
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
            return ResponseEntity.status(500).body(e.getMessage());
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
            return ResponseEntity.status(500).body(e.getMessage());
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
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}
