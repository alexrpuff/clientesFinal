package br.com.cotiinformatica.clientesApi.dtos;

public record EnderecoResponse(
        Integer id,
        String logradouro,
        String complemento,
        String numero,
        String bairro,
        String cidade,
        String uf,
        String cep
) {
}
