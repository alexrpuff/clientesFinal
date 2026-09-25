package br.com.cotiinformatica.clientesApi.configurations;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    /*
        Configuração da documentação do Swagger
        Documentação: http://localhost:8083/swagger-ui/index.html
        api-docs (importação no POSTMAN): http://localhost:8083/v3/api-docs
     */
    @Bean
    public OpenAPI openAPI() {

        return new OpenAPI()
                .info(
                        new Info()
                                .title("Clientes API - Projeto Final Java WebDeveloper")
                                .description("API REST para gerenciamento de clientes e seus endereços.")
                                .version("1.0")
                                .contact(
                                        new Contact()
                                                .name("COTI Informática")
                                )
                );
    }
}
