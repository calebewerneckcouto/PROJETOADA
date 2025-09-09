package br.com.adacommerce.ecommerce.service;

import java.util.List;
import java.util.Optional;

import br.com.adacommerce.ecommerce.exceptions.ValidationException;
import br.com.adacommerce.ecommerce.model.ItemPedido;
import br.com.adacommerce.ecommerce.model.Pedido;
import br.com.adacommerce.ecommerce.model.Produto;
import br.com.adacommerce.ecommerce.notifications.Notificador;
import br.com.adacommerce.ecommerce.repository.PedidoRepositoryFile;
import br.com.adacommerce.ecommerce.repository.ProdutoRepositoryFile;
import br.com.adacommerce.ecommerce.validators.EstoqueValidator;

public class ProdutoService {
    private final ProdutoRepositoryFile produtoRepository;
    private final Notificador notificador;
    private final PedidoRepositoryFile pedidoRepository;
    private final int limiteAlertaEstoque = 5;

    public ProdutoService(ProdutoRepositoryFile produtoRepository,
                          Notificador notificador,
                          PedidoRepositoryFile pedidoRepositoryFile) {
        this.produtoRepository = produtoRepository;
        this.notificador = notificador;
        this.pedidoRepository = pedidoRepositoryFile;
    }

    public void adicionarItem(Pedido pedido, Produto produto, int quantidade) {
        EstoqueValidator.validarEstoqueSuficiente(produto, quantidade);

        ItemPedido item = new ItemPedido(null, produto, quantidade, quantidade);
        pedido.adicionarItem(item);

        pedidoRepository.save(pedido);
    }

    public Produto cadastrarProduto(String nome, String descricao, double preco, int quantidadeEstoque) {
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
            if (quantidadeEstoque <= limiteAlertaEstoque) {
                notificador.notificarEstoqueBaixo(produto.getNome(), quantidadeEstoque);
            }
        }

        return produtoRepository.save(produto);
    }

    public Optional<Produto> buscarProdutoPorId(Long id) {
        return produtoRepository.findById(id);
    }
}
