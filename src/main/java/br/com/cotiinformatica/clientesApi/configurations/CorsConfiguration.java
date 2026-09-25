package br.com.cotiinformatica.clientesApi.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfiguration implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        //Permissão para qualquer aplicação acessar a API
        registry
                .addMapping("/**") //permissão para todos os ENDPOINTS
                .allowedOrigins("*") //qualquer origem
                .allowedMethods("POST", "PUT", "DELETE", "GET", "OPTIONS")
                .allowedHeaders("*");
    }
}
