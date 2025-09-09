package br.com.adacommerce.ecommerce.service;

import java.util.List;
import java.util.Optional;

import br.com.adacommerce.ecommerce.exceptions.ValidationException;
import br.com.adacommerce.ecommerce.model.Produto;
import br.com.adacommerce.ecommerce.notifications.EstoqueValidator;
import br.com.adacommerce.ecommerce.notifications.Notificador;
import br.com.adacommerce.ecommerce.repository.ProdutoRepositoryFile;

public class ProdutoService {
    private final ProdutoRepositoryFile produtoRepository;
    private final Notificador notificador;
    private final int limiteAlertaEstoque = 5;
    
    public ProdutoService(ProdutoRepositoryFile produtoRepository, Notificador notificador) {
        this.produtoRepository = produtoRepository;
        this.notificador = notificador;
    }
    
    public Produto cadastrarProduto(String nome, String descricao, double preco, int quantidadeEstoque) {
        // Validações
        if (nome == null || nome.trim().isEmpty()) {
            throw new ValidationException("Nome é obrigatório");
        }
        
        if (preco <= 0) {
            throw new ValidationException("Preço deve ser maior que zero");
        }
        
        if (quantidadeEstoque < 0) {
            throw new ValidationException("Quantidade em estoque não pode ser negativa");
        }
        
        Produto produto = new Produto(null, nome, descricao, preco, quantidadeEstoque);
        Produto produtoSalvo = produtoRepository.save(produto);
        
        // Verificar se precisa alertar sobre estoque baixo
        if (quantidadeEstoque <= limiteAlertaEstoque) {
            notificador.notificarEstoqueBaixo(nome, quantidadeEstoque);
        }
        
        return produtoSalvo;
    }
    
    public List<Produto> listarProdutos() {
        return produtoRepository.findAll();
    }
    
    public Produto atualizarProduto(Long id, String nome, String descricao, double preco, Integer quantidadeEstoque) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Produto não encontrado com ID: " + id));
        
        if (nome != null && !nome.trim().isEmpty()) {
            produto.setNome(nome);
        }
        
        if (descricao != null) {
            produto.setDescricao(descricao);
        }
        
        if (preco > 0) {
            produto.setPreco(preco);
        }
        
        if (quantidadeEstoque != null && quantidadeEstoque >= 0) {
            produto.setQuantidadeEstoque(quantidadeEstoque);
            
            // Verificar se precisa alertar sobre estoque baixo
            if (quantidadeEstoque <= limiteAlertaEstoque) {
                notificador.notificarEstoqueBaixo(produto.getNome(), quantidadeEstoque);
            }
        }
        
        return produtoRepository.save(produto);
    }
    
    public Optional<Produto> buscarProdutoPorId(Long id) {
        return produtoRepository.findById(id);
    }
    
    public void reduzirEstoque(Long produtoId, int quantidade) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new ValidationException("Produto não encontrado com ID: " + produtoId));
        
        EstoqueValidator.validarEstoqueSuficiente(produto, quantidade);
        produto.reduzirEstoque(quantidade);
        produtoRepository.save(produto);
        
        // Verificar se precisa alertar sobre estoque baixo após a redução
        EstoqueValidator.verificarEstoqueBaixo(produto, limiteAlertaEstoque);
        if (produto.getQuantidadeEstoque() <= limiteAlertaEstoque) {
            notificador.notificarEstoqueBaixo(produto.getNome(), produto.getQuantidadeEstoque());
        }
    }
    
    public void aumentarEstoque(Long produtoId, int quantidade) {
        if (quantidade <= 0) {
            throw new ValidationException("Quantidade deve ser maior que zero");
        }
        
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new ValidationException("Produto não encontrado com ID: " + produtoId));
        
        produto.aumentarEstoque(quantidade);
        produtoRepository.save(produto);
    }
    
    public List<Produto> getProdutosComEstoqueBaixo() {
        return produtoRepository.findProdutosComEstoqueBaixo(limiteAlertaEstoque);
    }
    
    public List<Produto> getProdutosSemEstoque() {
        return produtoRepository.findProdutosSemEstoque();
    }
}