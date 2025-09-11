package br.com.adacommerce.ecommerce.repository;

import br.com.adacommerce.ecommerce.model.Pedido;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PedidoRepositoryMemory implements Repository<Pedido, Long> {
    private Map<Long, Pedido> pedidos = new ConcurrentHashMap<>();
    private Long nextId = 1L;

    @Override
    public Pedido save(Pedido pedido) {
        if (pedido.getId() == null) {
            pedido.setId(nextId++);
        }
        pedidos.put(pedido.getId(), pedido);
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
    }

    @Override
    public Long getNextId() {
        return nextId;
    }
}
