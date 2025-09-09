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
import br.com.adacommerce.ecommerce.repository.PedidoRepositoryFile;
import br.com.adacommerce.ecommerce.repository.ProdutoRepositoryFile;
import br.com.adacommerce.ecommerce.validators.EstoqueValidator;

public class VendaService {

    private final PedidoRepositoryFile pedidoRepository;
    private final ProdutoRepositoryFile produtoRepository;
    private final Notificador notificador;

    public VendaService(PedidoRepositoryFile pedidoRepository,
                        ProdutoRepositoryFile produtoRepository,
                        Notificador notificador) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.notificador = notificador;
    }

    // ==================== PEDIDOS ====================
    public Pedido criarPedido(Cliente cliente) {
        if (cliente == null) {
            throw new ValidationException("Cliente não pode ser nulo.");
        }
        return pedidoRepository.save(new Pedido(null, cliente));
    }

    public List<Pedido> listarPedidosAbertos() {
        return pedidoRepository.findAll().stream()
                .filter(p -> p.getStatus() == StatusPedido.ABERTO)
                .collect(Collectors.toList());
    }

    public List<Pedido> listarPedidosFinalizados() {
        return pedidoRepository.findAll().stream()
                .filter(p -> p.getStatus() != StatusPedido.ABERTO)
                .collect(Collectors.toList());
    }

    // ==================== ITENS ====================
    public void adicionarItem(Pedido pedido, Produto produto, int quantidade, double precoVenda) {
        validarPedidoAberto(pedido);
        EstoqueValidator.validarEstoqueSuficiente(produto, quantidade);

        ItemPedido item = new ItemPedido(produto.getId(), produto, quantidade, precoVenda);
        pedido.adicionarItem(item);

        produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - quantidade);
        produtoRepository.save(produto);
        pedidoRepository.save(pedido);
    }

    public void removerItem(Pedido pedido, Long itemId) {
        validarPedidoAberto(pedido);

        ItemPedido item = pedido.getItens().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Item não encontrado"));

        // Devolve a quantidade ao estoque do produto
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
                .orElseThrow(() -> new ValidationException("Item não encontrado"));

        int delta = novaQuantidade - item.getQuantidade(); // diferença de quantidade

        // Se delta positivo -> precisa de mais estoque
        // Se delta negativo -> devolve estoque
        if (delta > 0) {
            EstoqueValidator.validarEstoqueSuficiente(item.getProduto(), delta);
            item.getProduto().setQuantidadeEstoque(item.getProduto().getQuantidadeEstoque() - delta);
        } else if (delta < 0) {
            item.getProduto().setQuantidadeEstoque(item.getProduto().getQuantidadeEstoque() - delta); // delta é negativo, então adiciona
        }

        item.setQuantidade(novaQuantidade);
        produtoRepository.save(item.getProduto());
        pedidoRepository.save(pedido);
    }

    // ==================== STATUS ====================
    public void finalizarPedido(Pedido pedido) {
        validarPedidoAberto(pedido);
        pedido.finalizarPedido();
        pedidoRepository.save(pedido);
        notificador.notificarPedidoCriado(pedido.getCliente(), pedido);
    }

    public void pagarPedido(Pedido pedido) {
        if (pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO) {
            throw new ValidationException("Pedido deve estar aguardando pagamento antes de pagar.");
        }
        pedido.pagar(); // muda status para PAGO
        pedidoRepository.save(pedido);
        notificador.notificarPagamentoAprovado(pedido.getCliente(), pedido);
    }

    public void entregarPedido(Pedido pedido) {
        if (pedido.getStatus() != StatusPedido.PAGO) {
            throw new ValidationException("Pedido deve estar pago antes de entregar.");
        }
        pedido.entregar();
        pedidoRepository.save(pedido);
        notificador.notificarPedidoEntregue(pedido.getCliente(), pedido);
    }

    // ==================== UTIL ====================
    private void validarPedidoAberto(Pedido pedido) {
        if (pedido == null) throw new ValidationException("Pedido não pode ser nulo.");
        if (pedido.getStatus() != StatusPedido.ABERTO) {
            throw new ValidationException("Só é possível alterar pedidos ABERTOS.");
        }
    }
}
