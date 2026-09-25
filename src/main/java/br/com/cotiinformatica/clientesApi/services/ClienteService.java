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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private MensagemProducer mensagemProducer;

    /*
        Método para cadastrar um cliente com o seu endereço
     */
    @Transactional
    public ClienteResponse criarCliente(CriarClienteRequest request) {

        //Verificar se o CPF já está cadastrado
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
        clienteRepository.save(cliente);

        //Gravar na fila do RabbitMQ a mensagem de boas vindas para o cliente
        enviarMensagemBoasVindas(cliente);

        return converterParaResponse(cliente);
    }

    /*
        Método para editar os dados de um cliente e de um endereço.
        Se o id do endereço não for informado, o endereço é adicionado ao cliente.
     */
    @Transactional
    public ClienteResponse editarCliente(EditarClienteRequest request) {

        //Buscar o cliente no banco de dados através do ID
        var cliente = buscarClientePorId(request.id());

        //Verificar se o CPF pertence a outro cliente
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
            endereco = cliente.getEnderecos().stream()
                    .filter(e -> e.getId().equals(dados.id()))
                    .findFirst()
                    .orElseThrow(() -> new RegistroNaoEncontradoException(
                            "Endereço não encontrado para este cliente. Verifique o ID informado."));
        }

        preencherEndereco(endereco, dados.logradouro(), dados.complemento(), dados.numero(),
                dados.bairro(), dados.cidade(), dados.uf(), dados.cep());

        //Atualizar no banco de dados
        clienteRepository.saveAndFlush(cliente);

        return converterParaResponse(cliente);
    }

    /*
        Método para excluir um cliente e os seus endereços
     */
    @Transactional
    public ClienteResponse excluirCliente(Integer id) {

        var cliente = buscarClientePorId(id);
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

    private void preencherEndereco(Endereco endereco, String logradouro, String complemento, String numero,
                                   String bairro, String cidade, String uf, String cep) {
        endereco.setLogradouro(logradouro);
        endereco.setComplemento(complemento);
        endereco.setNumero(numero);
        endereco.setBairro(bairro);
        endereco.setCidade(cidade);
        endereco.setUf(uf.toUpperCase());
        endereco.setCep(cep);
    }

    private void enviarMensagemBoasVindas(Cliente cliente) {
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

        try {
            mensagemProducer.enviar(mensagem);
        } catch (Exception e) {
            //O cadastro do cliente não deve falhar caso a mensageria esteja indisponível
            log.error("Falha ao gravar a mensagem de boas vindas na fila: {}", e.getMessage());
        }
    }

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
