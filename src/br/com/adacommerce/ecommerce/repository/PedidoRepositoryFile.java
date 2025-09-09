package br.com.adacommerce.ecommerce.repository;

import br.com.adacommerce.ecommerce.model.Pedido;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PedidoRepositoryFile implements Repository<Pedido, Long> {
    private final String arquivo = "pedidos.dat";
    private Map<Long, Pedido> pedidos;
    private Long nextId = 1L;

    public PedidoRepositoryFile() {
        pedidos = load();
        if (!pedidos.isEmpty()) {
            nextId = Collections.max(pedidos.keySet()) + 1;
        }
    }

    @Override
    public Pedido save(Pedido pedido) {
        if (pedido.getId() == null) {
            pedido.setId(nextId++);
        }
        pedidos.put(pedido.getId(), pedido);
        saveToFile();
        return pedido;
    }

    @Override
    public Optional<Pedido> findById(Long id) {
        return Optional.ofNullable(pedidos.get(id));
    }

    @Override
    public List<Pedido> findAll() {
        return new ArrayList<>(pedidos.values());
    }

    @Override
    public void delete(Long id) {
        pedidos.remove(id);
        saveToFile();
    }

    @Override
    public Long getNextId() {
        return nextId;
    }

    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(arquivo))) {
            oos.writeObject(pedidos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Pedido> load() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arquivo))) {
            return (Map<Long, Pedido>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new ConcurrentHashMap<>();
        }
    }
}
