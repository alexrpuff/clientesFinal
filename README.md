# clientesApi

API REST em **Spring Boot** para gerenciamento de clientes e seus endereços — Projeto Final da formação Java WebDeveloper.

## Tecnologias

Spring Boot 4 (Java 25) · Spring Web · Spring Data JPA · Bean Validation · Swagger/OpenAPI (springdoc) · PostgreSQL · RabbitMQ · Spring Mail · Docker Compose · Lombok · DevTools · JUnit 5 + Java Faker

## Executando

```bash
docker compose up -d      # PostgreSQL, pgAdmin, RabbitMQ e Mailpit
./mvnw spring-boot:run    # API em http://localhost:8083
```

> Com a dependência *Docker Compose Support*, o `spring-boot:run` também sobe os containers automaticamente.

| Serviço | Endereço |
|---|---|
| Swagger UI | http://localhost:8083/swagger-ui/index.html |
| api-docs (importar no POSTMAN) | http://localhost:8083/v3/api-docs (cópia em `docs/api-docs.json`) |
| RabbitMQ (painel) | http://localhost:15672 — usuário `coti` / senha `coti` |
| Mailpit (emails enviados) | http://localhost:8025 |
| pgAdmin | http://localhost:5053 — `coti@email.com` / `Coti@2026` |
| PostgreSQL | `localhost:5436`, banco `bd-clientesapi`, usuário `coti` / senha `coti` |

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/clientes` | Cadastra um cliente com um endereço (envia email de boas vindas) |
| PUT | `/api/clientes` | Edita o cliente e um endereço (`endereco.id` nulo adiciona um novo endereço) |
| DELETE | `/api/clientes/{id}` | Exclui o cliente e os seus endereços |
| GET | `/api/clientes` | Consulta todos os clientes com endereços, em ordem alfabética (JPQL) |
| GET | `/api/clientes/{id}` | Consulta 1 cliente com endereços (JPQL) |

Respostas: `201`/`200` com o cliente, `400` com os erros de validação por campo, `404` cliente/endereço não encontrado, `409` CPF já cadastrado.

### Regras de validação

- **nome**: obrigatório, de 8 a 100 caracteres
- **email**: obrigatório, formato válido
- **cpf**: obrigatório, exatamente 11 dígitos numéricos (REGEX) e único
- **dataNascimento**: obrigatória (`yyyy-MM-dd`)

## Mensageria (envio de email)

Ao cadastrar um cliente, o `ClienteService` usa o **PRODUCER** (`components/MensagemProducer`) para gravar a mensagem na fila `clientes-notificacoes` do RabbitMQ. O **CONSUMER** (`components/MensagemConsumer`) lê a fila e envia o email pelo `EmailComponent` (SMTP do Mailpit em desenvolvimento).

## Estrutura em camadas

```
configurations  Swagger, CORS (qualquer origem) e RabbitMQ
controllers     ENDPOINTS REST
dtos            Contratos de dados (records) com Bean Validation
entities        Cliente 1:N Endereco (JPA)
repositories    Consultas JPQL
services        Regras de negócio
components      Producer, Consumer e envio de email
exceptions      Exceções de negócio
handlers        Tratamento global dos erros de validação
```

## Testes

Testes de integração com JUnit 5, MockMvc e Java Faker para cada endpoint e para o fluxo de mensageria (o email é verificado pela API do Mailpit). Requer os containers em execução:

```bash
docker compose up -d
./mvnw test
```
# clientesFinal
# clientesFinal
