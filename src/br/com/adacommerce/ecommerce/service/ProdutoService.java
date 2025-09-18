package br.com.adacommerce.ecommerce.service;

import java.util.List;
import java.util.Optional;

import br.com.adacommerce.ecommerce.exceptions.ValidationException;
import br.com.adacommerce.ecommerce.model.Produto;
import br.com.adacommerce.ecommerce.notifications.EstoqueValidator;
import br.com.adacommerce.ecommerce.notifications.Notificador;
import br.com.adacommerce.ecommerce.repository.Repository;

public class ProdutoService {
    private final Repository<Produto, Long> produtoRepository;
    private final Notificador notificador;
    private final int limiteAlertaEstoque = 5;
    private Long proximoId;

    public ProdutoService(Repository<Produto, Long> produtoRepository, Notificador notificador) {
        this.produtoRepository = produtoRepository;
        this.notificador = notificador;

        try {
            this.proximoId = produtoRepository.getNextId();
        } catch (UnsupportedOperationException e) {
            this.proximoId = null; 
        }
    }

    
    public Produto cadastrarProduto(String nome, String descricao, double preco, int quantidadeEstoque) {
        if (nome == null || nome.trim().isEmpty())
            throw new ValidationException("Nome é obrigatório");
        if (preco <= 0)
            throw new ValidationException("Preço deve ser maior que zero");
        if (quantidadeEstoque < 0)
            throw new ValidationException("Quantidade em estoque não pode ser negativa");

        Produto produto;
        if (proximoId != null) {
            produto = new Produto(proximoId++, nome, descricao, preco, quantidadeEstoque);
        } else {
            produto = new Produto(null, nome, descricao, preco, quantidadeEstoque);
        }

        Produto salvo = produtoRepository.save(produto);
        EstoqueValidator.verificarEstoqueBaixo(salvo, limiteAlertaEstoque);
        return salvo;
    }

    
    public Produto atualizarProduto(Long id, String nome, String descricao, double preco, Integer quantidadeEstoque) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Produto não encontrado com ID: " + id));

        if (nome != null && !nome.trim().isEmpty())
            produto.setNome(nome);
        if (descricao != null)
            produto.setDescricao(descricao);
        if (preco > 0)
            produto.setPreco(preco);
        if (quantidadeEstoque != null && quantidadeEstoque >= 0) {
            produto.setQuantidadeEstoque(quantidadeEstoque);
            EstoqueValidator.verificarEstoqueBaixo(produto, limiteAlertaEstoque);
        }

        return produtoRepository.save(produto);
    }

    
    public List<Produto> listarProdutos() {
        return produtoRepository.findAll();
    }

    
    public Optional<Produto> buscarProdutoPorId(Long id) {
        return produtoRepository.findById(id);
    }

   
    public Optional<Produto> buscarProdutoPorNome(String nome) {
        String nomeNormalizado = nome.trim().toLowerCase();
        return produtoRepository.findAll().stream()
                .filter(p -> p.getNome() != null &&
                             p.getNome().trim().toLowerCase().equals(nomeNormalizado))
                .findFirst();
    }


    
    public void excluirProduto(Long id) {
        throw new UnsupportedOperationException("Exclusão de produto não é permitida (histórico mantido).");
    }
}
