package br.com.cotiinformatica.clientesApi.services;

import br.com.cotiinformatica.clientesApi.components.MensagemProducer;
import br.com.cotiinformatica.clientesApi.dtos.*;
import br.com.cotiinformatica.clientesApi.entities.Cliente;
import br.com.cotiinformatica.clientesApi.entities.Endereco;
import br.com.cotiinformatica.clientesApi.exceptions.CpfJaCadastradoException;
import br.com.cotiinformatica.clientesApi.exceptions.RegistroNaoEncontradoException;
import br.com.cotiinformatica.clientesApi.repositories.ClienteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

/*
    Regras de negócio dos clientes e dos seus endereços.
    Todos os métodos que gravam são @Transactional: ou tudo dá certo
    (cliente e endereço gravados juntos), ou nada é gravado.
 */
@Slf4j
@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private MensagemProducer mensagemProducer;

    /*
        Método para cadastrar um cliente com o seu endereço.
        Todo cliente nasce com um endereço: a empresa precisa saber
        onde encontrá-lo. Outros endereços podem ser incluídos na edição.
     */
    @Transactional
    public ClienteResponse criarCliente(CriarClienteRequest request) {

        //Regra: a mesma pessoa (CPF) não pode ser cadastrada duas vezes
        if (clienteRepository.findByCpf(request.cpf()) != null) {
            throw new CpfJaCadastradoException("O CPF informado já está cadastrado, tente outro.");
        }

        //Preencher os dados do cliente
        var cliente = new Cliente();
        cliente.setNome(request.nome());
        cliente.setEmail(request.email());
        cliente.setCpf(request.cpf());
        cliente.setDataNascimento(request.dataNascimento());

        //Preencher os dados do endereço e associar ao cliente
        var endereco = new Endereco();
        preencherEndereco(endereco, request.endereco().logradouro(), request.endereco().complemento(),
                request.endereco().numero(), request.endereco().bairro(), request.endereco().cidade(),
                request.endereco().uf(), request.endereco().cep());
        cliente.adicionarEndereco(endereco);

        //Salvar o cliente (e o endereço, via cascade) no banco de dados
        salvar(cliente);

        //Enviar o email de boas vindas somente depois que o cadastro estiver gravado
        enviarMensagemBoasVindasAposGravar(cliente);

        return converterParaResponse(cliente);
    }

    /*
        Método para editar os dados de um cliente e de um endereço.
        A edição trabalha com um endereço por vez: o cliente escolhe
        qual endereço quer corrigir. Se o id do endereço não for
        informado, é um endereço novo e ele é adicionado ao cliente.
     */
    @Transactional
    public ClienteResponse editarCliente(EditarClienteRequest request) {

        //Buscar o cliente no banco de dados através do ID
        var cliente = buscarClientePorId(request.id());

        //Regra: o CPF pode continuar o mesmo, mas não pode ser o CPF de outro cliente
        var clienteCpf = clienteRepository.findByCpf(request.cpf());
        if (clienteCpf != null && !clienteCpf.getId().equals(cliente.getId())) {
            throw new CpfJaCadastradoException("O CPF informado já está cadastrado para outro cliente, tente outro.");
        }

        //Modificar os dados do cliente
        cliente.setNome(request.nome());
        cliente.setEmail(request.email());
        cliente.setCpf(request.cpf());
        cliente.setDataNascimento(request.dataNascimento());

        //Localizar (ou criar) o endereço que será modificado
        var dados = request.endereco();
        Endereco endereco;

        if (dados.id() == null) {
            endereco = new Endereco();
            cliente.adicionarEndereco(endereco);
        } else {
            //Regra: só é possível editar um endereço que pertença a este cliente
            endereco = cliente.getEnderecos().stream()
                    .filter(e -> e.getId().equals(dados.id()))
                    .findFirst()
                    .orElseThrow(() -> new RegistroNaoEncontradoException(
                            "Endereço não encontrado para este cliente. Verifique o ID informado."));
        }

        preencherEndereco(endereco, dados.logradouro(), dados.complemento(), dados.numero(),
                dados.bairro(), dados.cidade(), dados.uf(), dados.cep());

        //Atualizar no banco de dados
        salvar(cliente);

        return converterParaResponse(cliente);
    }

    /*
        Método para excluir um cliente e os seus endereços.
        Os endereços saem junto (cascade) porque não faz sentido
        guardar o endereço de alguém que não é mais cliente.
     */
    @Transactional
    public ClienteResponse excluirCliente(Integer id) {

        var cliente = buscarClientePorId(id);

        //Os dados são guardados antes da exclusão para devolver o que foi excluído
        var response = converterParaResponse(cliente);

        clienteRepository.delete(cliente);

        return response;
    }

    /*
        Método para consultar todos os clientes com os seus
        endereços em ordem alfabética (nome)
     */
    @Transactional(readOnly = true)
    public List<ClienteResponse> consultarClientes() {
        return clienteRepository.findAllComEnderecos()
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    /*
        Método para consultar 1 cliente com os seus endereços através do ID
     */
    @Transactional(readOnly = true)
    public ClienteResponse obterClientePorId(Integer id) {
        return converterParaResponse(buscarClientePorId(id));
    }

    private Cliente buscarClientePorId(Integer id) {
        return clienteRepository.findByIdComEnderecos(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException(
                        "Cliente não encontrado. Verifique o ID informado."));
    }

    /*
        Grava o cliente e confere a regra do CPF único também no banco.
        A consulta por CPF feita antes não basta quando duas pessoas
        enviam o mesmo CPF ao mesmo tempo: as duas passam pela consulta
        e só o banco (restrição UNIQUE) barra a segunda. Nesse caso a
        resposta continua sendo "CPF já cadastrado" (409), e não um
        erro interno (500).
     */
    private void salvar(Cliente cliente) {
        try {
            clienteRepository.saveAndFlush(cliente);
        }
        catch (DataIntegrityViolationException e) {
            throw new CpfJaCadastradoException("O CPF informado já está cadastrado, tente outro.");
        }
    }

    private void preencherEndereco(Endereco endereco, String logradouro, String complemento, String numero,
                                   String bairro, String cidade, String uf, String cep) {
        endereco.setLogradouro(logradouro);
        endereco.setComplemento(complemento);
        endereco.setNumero(numero);
        endereco.setBairro(bairro);
        endereco.setCidade(cidade);
        //UF sempre em maiúsculas, para que "rj" e "RJ" sejam o mesmo estado nas consultas
        endereco.setUf(uf.toUpperCase());
        endereco.setCep(cep);
    }

    /*
        O email de boas vindas só pode sair se o cadastro realmente foi
        gravado. Enviar antes do COMMIT poderia dar as boas vindas a um
        cliente cujo cadastro foi desfeito (rollback). Por isso a
        mensagem é gravada na fila logo após o COMMIT da transação.
     */
    private void enviarMensagemBoasVindasAposGravar(Cliente cliente) {
        var mensagem = new EmailMessageDto(
                cliente.getEmail(),
                "Cadastro realizado com sucesso",
                """
                Olá, %s!

                Seu registro foi efetuado com sucesso em nosso sistema.

                Atenciosamente,
                Equipe Clientes API
                """.formatted(cliente.getNome())
        );

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                enviarMensagemBoasVindas(mensagem);
            }
        });
    }

    private void enviarMensagemBoasVindas(EmailMessageDto mensagem) {
        try {
            mensagemProducer.enviar(mensagem);
        } catch (Exception e) {
            //O cadastro do cliente não deve falhar caso a mensageria esteja indisponível
            log.error("Falha ao gravar a mensagem de boas vindas na fila: {}", e.getMessage());
        }
    }

    /*
        A API nunca devolve as entidades diretamente, e sim os DTOs de
        resposta. Assim o JSON não entra em laço (cliente -> endereço ->
        cliente -> ...) e a API mostra só o que interessa a quem consome.
     */
    private ClienteResponse converterParaResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getEmail(),
                cliente.getCpf(),
                cliente.getDataNascimento(),
                cliente.getEnderecos().stream()
                        .map(e -> new EnderecoResponse(
                                e.getId(),
                                e.getLogradouro(),
                                e.getComplemento(),
                                e.getNumero(),
                                e.getBairro(),
                                e.getCidade(),
                                e.getUf(),
                                e.getCep()
                        ))
                        .toList()
        );
    }
}
