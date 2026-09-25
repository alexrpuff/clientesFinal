package br.com.cotiinformatica.clientesApi.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/*
    Cliente é o centro do sistema: é a pessoa que a empresa atende.
    Aqui ficam só os dados que identificam a pessoa (nome, email, CPF e
    data de nascimento). Onde ela mora fica na entidade Endereco,
    porque uma mesma pessoa pode ter mais de um endereço
    (casa, trabalho, casa de praia...).
 */
@Entity
@Table(name = "clientes")
@Getter
@Setter
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "nome", length = 100, nullable = false)
    private String nome;

    //Email usado para avisar o cliente de que o cadastro foi feito (email de boas vindas)
    @Column(name = "email", length = 100, nullable = false)
    private String email;

    /*
        O CPF identifica a pessoa de forma única no país, por isso ele
        também é único aqui: a mesma pessoa não pode aparecer duas vezes
        na carteira de clientes. O ClienteService confere isso antes de
        salvar, e o banco garante com a restrição UNIQUE caso duas
        requisições tentem cadastrar o mesmo CPF ao mesmo tempo.
     */
    @Column(name = "cpf", length = 11, nullable = false, unique = true)
    private String cpf;

    @Column(name = "datanascimento", nullable = false)
    private LocalDate dataNascimento;

    /*
        Relacionamento 1:N (um cliente possui muitos endereços).

        - mappedBy = "cliente": quem guarda a ligação no banco é o endereço
          (coluna cliente_id na tabela enderecos). Assim a tabela clientes
          não precisa de uma coluna para cada endereço, e o cliente pode
          ter quantos endereços precisar.
        - cascade = ALL: o endereço não existe sozinho, ele é sempre "o
          endereço de alguém". Por isso o que acontece com o cliente
          acontece com os seus endereços: ao cadastrar o cliente, o endereço
          é gravado junto; ao excluir o cliente, os endereços vão junto.
        - orphanRemoval = true: se um endereço for retirado da lista do
          cliente, ele é apagado do banco em vez de ficar "órfão", sem dono.
        - @OrderBy("id ASC"): os endereços aparecem na ordem em que foram
          cadastrados, então o primeiro endereço informado é sempre o primeiro da lista.
     */
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Endereco> enderecos = new ArrayList<>();

    /*
        Ao adicionar um endereço, os dois lados da relação precisam saber
        um do outro: a lista do cliente recebe o endereço e o endereço
        passa a apontar para o cliente. Sem isso o endereço seria gravado
        sem dono (cliente_id nulo) e o banco recusaria.
     */
    public void adicionarEndereco(Endereco endereco) {
        endereco.setCliente(this);
        enderecos.add(endereco);
    }
}
