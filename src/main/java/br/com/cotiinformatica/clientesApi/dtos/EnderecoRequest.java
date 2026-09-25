package br.com.cotiinformatica.clientesApi.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EnderecoRequest(

        @NotBlank(message = "O logradouro é obrigatório.")
        String logradouro,

        String complemento,

        @NotBlank(message = "O número é obrigatório.")
        String numero,

        @NotBlank(message = "O bairro é obrigatório.")
        String bairro,

        @NotBlank(message = "A cidade é obrigatória.")
        String cidade,

        @Pattern(regexp = "^[A-Za-z]{2}$", message = "A UF deve conter 2 letras.")
        @NotBlank(message = "A UF é obrigatória.")
        String uf,

        @Pattern(regexp = "^\\d{8}$", message = "O CEP deve conter exatamente 8 dígitos numéricos.")
        @NotBlank(message = "O CEP é obrigatório.")
        String cep
) {
}
