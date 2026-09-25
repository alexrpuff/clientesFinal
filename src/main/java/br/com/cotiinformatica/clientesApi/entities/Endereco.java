package br.com.cotiinformatica.clientesApi.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/*
    Endereço de um cliente. Ele fica em uma tabela separada porque
    um cliente pode ter vários endereços, e cada endereço pertence
    a um único cliente.
 */
@Entity
@Table(name = "enderecos")
@Getter
@Setter
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "logradouro", length = 150, nullable = false)
    private String logradouro;

    //Único campo opcional do endereço (nem todo endereço tem apartamento, bloco, sala...)
    @Column(name = "complemento", length = 100)
    private String complemento;

    //Texto, e não número, porque existem endereços como "S/N" ou "120-A"
    @Column(name = "numero", length = 20, nullable = false)
    private String numero;

    @Column(name = "bairro", length = 100, nullable = false)
    private String bairro;

    @Column(name = "cidade", length = 100, nullable = false)
    private String cidade;

    //Sigla do estado, sempre gravada em maiúsculas (ex.: RJ)
    @Column(name = "uf", length = 2, nullable = false)
    private String uf;

    //Somente os 8 dígitos, sem o hífen (a formatação fica por conta de quem exibe)
    @Column(name = "cep", length = 8, nullable = false)
    private String cep;

    /*
        Lado N do relacionamento (muitos endereços para um cliente).
        É aqui que fica a chave estrangeira cliente_id, que diz "este
        endereço é do cliente X". Ela é obrigatória (nullable = false)
        porque não existe endereço sem cliente.

        fetch = LAZY: o endereço só carrega o cliente se alguém pedir.
        Nas consultas o caminho é sempre do cliente para os seus
        endereços, então buscar o cliente de novo a partir de cada
        endereço seria trabalho desnecessário para o banco.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
}
