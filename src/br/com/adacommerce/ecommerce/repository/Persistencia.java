package br.com.adacommerce.ecommerce.repository;

import java.util.List;
import java.util.Optional;

public interface Persistencia<T> {
    void salvar(T obj);                // Salva ou atualiza
    Optional<T> findById(long id);     // Busca por ID
    List<T> findAll();                 // Lista todos
    void deletar(long id);             // Deleta (opcional)
}
