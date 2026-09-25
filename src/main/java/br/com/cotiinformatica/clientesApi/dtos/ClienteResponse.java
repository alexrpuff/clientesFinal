package br.com.cotiinformatica.clientesApi.dtos;

import java.time.LocalDate;
import java.util.List;

public record ClienteResponse(
        Integer id,
        String nome,
        String email,
        String cpf,
        LocalDate dataNascimento,
        List<EnderecoResponse> enderecos
) {
}
