package br.com.adacommerce.ecommerce.repository;

import br.com.adacommerce.ecommerce.model.Produto;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ProdutoRepositoryFile implements Repository<Produto, Long> {
    private final String arquivo = "produtos.dat";
    private Map<Long, Produto> produtos;
    private Long nextId = 1L;

    public ProdutoRepositoryFile() {
        produtos = load();
        if (!produtos.isEmpty()) {
            nextId = Collections.max(produtos.keySet()) + 1;
        }
    }

    @Override
    public Produto save(Produto produto) {
        if (produto.getId() == null) {
            produto.setId(nextId++);
        }
        produtos.put(produto.getId(), produto);
        saveToFile();
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
        saveToFile();
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

    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(arquivo))) {
            oos.writeObject(produtos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Produto> load() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arquivo))) {
            return (Map<Long, Produto>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new ConcurrentHashMap<>();
        }
    }
}
