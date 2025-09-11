package br.com.adacommerce.ecommerce.repository;

import br.com.adacommerce.ecommerce.model.Produto;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ProdutoRepositoryMemory implements Repository<Produto, Long> {
    private Map<Long, Produto> produtos = new ConcurrentHashMap<>();
    private Long nextId = 1L;

    @Override
    public Produto save(Produto produto) {
        if (produto.getId() == null) {
            produto.setId(nextId++);
        }
        produtos.put(produto.getId(), produto);
        return produto;
    }

    @Override
    public Optional<Produto> findById(Long id) {
        return Optional.ofNullable(produtos.get(id));
    }

    @Override
    public List<Produto> findAll() {
        return new ArrayList<>(produtos.values());
    }

    @Override
    public void delete(Long id) {
        produtos.remove(id);
    }

    @Override
    public Long getNextId() {
        return nextId;
    }

    public List<Produto> findProdutosComEstoqueBaixo(int limite) {
        List<Produto> baixo = new ArrayList<>();
        for (Produto p : produtos.values()) {
            if (p.getQuantidadeEstoque() <= limite) baixo.add(p);
        }
        return baixo;
    }

    public List<Produto> findProdutosSemEstoque() {
        List<Produto> sem = new ArrayList<>();
        for (Produto p : produtos.values()) {
            if (p.getQuantidadeEstoque() == 0) sem.add(p);
        }
        return sem;
    }
}
