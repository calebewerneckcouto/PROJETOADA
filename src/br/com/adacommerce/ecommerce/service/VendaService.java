package br.com.adacommerce.ecommerce.service;

import java.util.List;
import java.util.stream.Collectors;

import br.com.adacommerce.ecommerce.exceptions.ValidationException;
import br.com.adacommerce.ecommerce.model.Cliente;
import br.com.adacommerce.ecommerce.model.ItemPedido;
import br.com.adacommerce.ecommerce.model.Pedido;
import br.com.adacommerce.ecommerce.model.Produto;
import br.com.adacommerce.ecommerce.model.StatusPedido;
import br.com.adacommerce.ecommerce.notifications.Notificador;
import br.com.adacommerce.ecommerce.repository.Repository;
import br.com.adacommerce.ecommerce.validators.EstoqueValidator;

public class VendaService {

    private final Repository<Pedido, Long> pedidoRepository;
    private final Repository<Produto, Long> produtoRepository;
    private final Notificador notificador;

    public VendaService(Repository<Pedido, Long> pedidoRepository,
                        Repository<Produto, Long> produtoRepository,
                        Notificador notificador) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.notificador = notificador;
    }

    public Pedido criarPedido(Cliente cliente) {
        if (cliente == null) throw new ValidationException("Cliente não pode ser nulo.");
        return pedidoRepository.save(new Pedido(null, cliente));
    }

    public List<Pedido> listarPedidosAbertos() {
        return pedidoRepository.findAll().stream()
                .filter(p -> p.getStatus() == StatusPedido.ABERTO)
                .collect(Collectors.toList());
    }

    public List<Pedido> listarPedidosFinalizados() {
        return pedidoRepository.findAll().stream()
                .filter(p -> p.getStatus() == StatusPedido.FINALIZADO)
                .collect(Collectors.toList());
    }

    public void adicionarItem(Pedido pedido, Produto produto, int quantidade, double precoVenda) {
        validarPedidoAberto(pedido);
        EstoqueValidator.validarEstoqueSuficiente(produto, quantidade);

        if (precoVenda <= 0) throw new ValidationException("Preço de venda inválido.");

        ItemPedido item = new ItemPedido(produto.getId(), produto, quantidade, precoVenda);
        pedido.adicionarItem(item);

        produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - quantidade);
        produtoRepository.save(produto);
        pedidoRepository.save(pedido);

        EstoqueValidator.verificarEstoqueBaixo(produto, 5);
    }

    public void removerItem(Pedido pedido, Long itemId) {
        validarPedidoAberto(pedido);

        ItemPedido item = pedido.getItens().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Item não encontrado no pedido."));

        Produto produto = item.getProduto();
        produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() + item.getQuantidade());
        produtoRepository.save(produto);

        pedido.removerItem(itemId);
        pedidoRepository.save(pedido);
    }

    public void alterarQuantidadeItem(Pedido pedido, Long itemId, int novaQuantidade) {
        validarPedidoAberto(pedido);

        ItemPedido item = pedido.getItens().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Item não encontrado no pedido."));

        int delta = novaQuantidade - item.getQuantidade();

        if (delta > 0) EstoqueValidator.validarEstoqueSuficiente(item.getProduto(), delta);

        item.getProduto().setQuantidadeEstoque(item.getProduto().getQuantidadeEstoque() - delta);
        item.setQuantidade(novaQuantidade);

        produtoRepository.save(item.getProduto());
        pedidoRepository.save(pedido);

        EstoqueValidator.verificarEstoqueBaixo(item.getProduto(), 5);
    }

    public void finalizarPedido(Pedido pedido) {
        validarPedidoAberto(pedido);
        if (!pedido.podeFinalizar()) {
            throw new ValidationException("Não é possível finalizar pedido sem itens ou valor maior que zero.");
        }
        pedido.finalizarPedido();
        pedidoRepository.save(pedido);
        notificador.notificarPedidoCriado(pedido.getCliente(), pedido);
    }

    public void pagarPedido(Pedido pedido) {
        if (pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO) {
            throw new ValidationException("Pedido deve estar 'AGUARDANDO PAGAMENTO' antes de pagar.");
        }
        pedido.pagar();
        pedidoRepository.save(pedido);
        notificador.notificarPagamentoAprovado(pedido.getCliente(), pedido);
    }

    public void entregarPedido(Pedido pedido) {
        if (pedido.getStatus() != StatusPedido.PAGO) {
            throw new ValidationException("Pedido deve estar 'PAGO' antes de entregar.");
        }
        pedido.entregar();
        pedidoRepository.save(pedido);
        notificador.notificarPedidoEntregue(pedido.getCliente(), pedido);
    }

    private void validarPedidoAberto(Pedido pedido) {
        if (pedido == null) throw new ValidationException("Pedido não pode ser nulo.");
        if (pedido.getStatus() != StatusPedido.ABERTO)
            throw new ValidationException("Só é possível alterar pedidos com status 'ABERTO'.");
    }
}
