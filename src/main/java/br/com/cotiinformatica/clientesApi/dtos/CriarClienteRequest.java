package br.com.cotiinformatica.clientesApi.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CriarClienteRequest(

        @Size(min = 8, max = 100, message = "O nome do cliente deve ter de 8 a 100 caracteres.")
        @NotBlank(message = "O nome do cliente é obrigatório.")
        String nome,

        @Email(message = "Informe um endereço de email válido.")
        @NotBlank(message = "O email do cliente é obrigatório.")
        String email,

        @Pattern(regexp = "^\\d{11}$", message = "O CPF deve conter exatamente 11 dígitos numéricos.")
        @NotBlank(message = "O CPF do cliente é obrigatório.")
        String cpf,

        @NotNull(message = "A data de nascimento do cliente é obrigatória.")
        LocalDate dataNascimento,

        @Valid
        @NotNull(message = "O endereço do cliente é obrigatório.")
        EnderecoRequest endereco
) {
}
