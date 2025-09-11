package br.com.adacommerce.ecommerce.repository;

import br.com.adacommerce.ecommerce.model.Cliente;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClienteRepositoryMemory implements Repository<Cliente, Long> {
    private Map<Long, Cliente> clientes = new ConcurrentHashMap<>();
    private Long nextId = 1L;

    @Override
    public Cliente save(Cliente cliente) {
        if (cliente.getId() == null) {
            cliente.setId(nextId++);
        }
        clientes.put(cliente.getId(), cliente);
        return cliente;
    }

    @Override
    public Optional<Cliente> findById(Long id) {
        return Optional.ofNullable(clientes.get(id));
    }

    @Override
    public List<Cliente> findAll() {
        return new ArrayList<>(clientes.values());
    }

    @Override
    public void delete(Long id) {
        clientes.remove(id);
    }

    @Override
    public Long getNextId() {
        return nextId;
    }

    public Optional<Cliente> findByEmail(String email) {
        return clientes.values().stream()
                .filter(c -> c.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    public Optional<Cliente> findByDocumento(String documento) {
        return clientes.values().stream()
                .filter(c -> c.getDocumento().equals(documento))
                .findFirst();
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    public boolean existsByDocumento(String documento) {
        return findByDocumento(documento).isPresent();
    }
}
