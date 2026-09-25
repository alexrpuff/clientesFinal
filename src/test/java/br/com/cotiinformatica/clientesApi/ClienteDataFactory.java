package br.com.cotiinformatica.clientesApi;

import com.github.javafaker.Faker;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/*
    Classe auxiliar para gerar dados de clientes
    e endereços utilizando o Java Faker
 */
public class ClienteDataFactory {

    private static final Faker faker = new Faker(Locale.of("pt", "BR"));

    public static Map<String, Object> novoCliente() {
        var cliente = new HashMap<String, Object>();
        cliente.put("nome", nome());
        cliente.put("email", email());
        cliente.put("cpf", faker.number().digits(11));
        cliente.put("dataNascimento", faker.date().birthday(18, 80)
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString());
        cliente.put("endereco", novoEndereco());
        return cliente;
    }

    public static Map<String, Object> novoEndereco() {
        var endereco = new HashMap<String, Object>();
        endereco.put("logradouro", faker.address().streetName());
        endereco.put("complemento", "Apto " + faker.number().numberBetween(1, 999));
        endereco.put("numero", faker.address().buildingNumber());
        endereco.put("bairro", faker.address().cityName());
        endereco.put("cidade", faker.address().city());
        endereco.put("uf", faker.address().stateAbbr());
        endereco.put("cep", faker.number().digits(8));
        return endereco;
    }

    //Nome com no mínimo 8 e no máximo 100 caracteres
    public static String nome() {
        var nome = faker.name().firstName() + " " + faker.name().lastName() + " " + faker.name().lastName();
        return nome.length() > 100 ? nome.substring(0, 100) : nome;
    }

    public static String email() {
        return faker.bothify("????????##").toLowerCase() + "@teste.com.br";
    }

    public static String cpf() {
        return faker.number().digits(11);
    }
}
