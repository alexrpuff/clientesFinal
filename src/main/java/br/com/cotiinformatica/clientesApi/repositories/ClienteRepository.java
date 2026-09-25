package br.com.cotiinformatica.clientesApi.repositories;

import br.com.cotiinformatica.clientesApi.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    /*
        Consulta todos os clientes com os seus endereços,
        em ordem alfabética (nome), usando JPQL
     */
    @Query("""
        SELECT DISTINCT c FROM Cliente c
        LEFT JOIN FETCH c.enderecos
        ORDER BY c.nome ASC
    """)
    List<Cliente> findAllComEnderecos();

    /*
        Consulta 1 cliente com os seus endereços
        através do ID, usando JPQL
     */
    @Query("""
        SELECT c FROM Cliente c
        LEFT JOIN FETCH c.enderecos
        WHERE c.id = :id
    """)
    Optional<Cliente> findByIdComEnderecos(@Param("id") Integer id);

    /*
        Consulta de cliente através do CPF usando JPQL
        (utilizado para garantir que o CPF seja único)
     */
    @Query("""
        SELECT c FROM Cliente c
        WHERE c.cpf = :cpf
    """)
    Cliente findByCpf(@Param("cpf") String cpf);
}
