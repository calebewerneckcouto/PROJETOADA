package br.com.adacommerce.ecommerce.repository;

import br.com.adacommerce.ecommerce.model.Cliente;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClienteRepositoryFile implements Repository<Cliente, Long> {
    private final String arquivo = "clientes.dat";
    private Map<Long, Cliente> clientes;
    private Long nextId = 1L;

    public ClienteRepositoryFile() {
        clientes = load();
        if (!clientes.isEmpty()) {
            nextId = Collections.max(clientes.keySet()) + 1;
        }
    }

    @Override
    public Cliente save(Cliente cliente) {
        if (cliente.getId() == null) {
            cliente.setId(nextId++);
        }
        clientes.put(cliente.getId(), cliente);
        saveToFile();
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
        saveToFile();
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

    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(arquivo))) {
            oos.writeObject(clientes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Cliente> load() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arquivo))) {
            return (Map<Long, Cliente>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new ConcurrentHashMap<>();
        }
    }
}
