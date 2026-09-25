package br.com.cotiinformatica.clientesApi.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "email", length = 100, nullable = false)
    private String email;

    @Column(name = "cpf", length = 11, nullable = false, unique = true)
    private String cpf;

    @Column(name = "datanascimento", nullable = false)
    private LocalDate dataNascimento;

    /*
        Um cliente possui muitos endereços.
        Os endereços são gravados/excluídos junto com o cliente (cascade).
     */
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Endereco> enderecos = new ArrayList<>();

    //Método auxiliar para manter os dois lados do relacionamento sincronizados
    public void adicionarEndereco(Endereco endereco) {
        endereco.setCliente(this);
        enderecos.add(endereco);
    }
}
