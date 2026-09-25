package br.com.cotiinformatica.clientesApi.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfiguration implements WebMvcConfigurer {

    /*
        Origens autorizadas a chamar a API (cors.allowed-origins no application.yaml).
        Em desenvolvimento é "*" (qualquer origem); na hospedagem deve ser o
        endereço do front-end, para que outros sites não usem a API pelo navegador.
     */
    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry
                .addMapping("/**") //permissão para todos os ENDPOINTS
                .allowedOrigins(allowedOrigins)
                .allowedMethods("POST", "PUT", "DELETE", "GET", "OPTIONS")
                .allowedHeaders("*");
    }
}
