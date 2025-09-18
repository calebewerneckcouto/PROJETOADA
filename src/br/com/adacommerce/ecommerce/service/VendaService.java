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

    private Long proximoPedidoId;
    private Long proximoItemId;

    public VendaService(Repository<Pedido, Long> pedidoRepository, Repository<Produto, Long> produtoRepository,
                        Notificador notificador) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.notificador = notificador;

        try {
            this.proximoPedidoId = pedidoRepository.getNextId();
        } catch (UnsupportedOperationException e) {
            this.proximoPedidoId = null;
        }

        try {
            this.proximoItemId = 1L;
        } catch (UnsupportedOperationException e) {
            this.proximoItemId = null;
        }
    }

    public Pedido criarPedido(Cliente cliente) {
        if (cliente == null)
            throw new ValidationException("Cliente não pode ser nulo.");

        Pedido pedido;
        if (proximoPedidoId != null) {
            pedido = new Pedido(proximoPedidoId++, cliente);
        } else {
            pedido = new Pedido(null, cliente);
        }

        pedidoRepository.save(pedido);

        System.out.println("🛒 Cliente " + cliente.getNome() + " criou o pedido ID " + pedido.getId());

        return pedido;
    }

    public List<Pedido> listarPedidosFinalizados() {
        return pedidoRepository.findAll().stream()
                .filter(p -> p.getStatus() == StatusPedido.FINALIZADO)
                .collect(Collectors.toList());
    }

    public void adicionarItem(Pedido pedido, Produto produto, int quantidade, double precoVenda) {
        validarPedidoAberto(pedido);

        Produto produtoAtualizado = produtoRepository.findById(produto.getId())
                .orElseThrow(() -> new ValidationException("Produto não encontrado no repositório."));

        EstoqueValidator.validarEstoqueSuficiente(produtoAtualizado, quantidade);

        if (precoVenda <= 0)
            throw new ValidationException("Preço de venda inválido.");

        ItemPedido item;
        if (proximoItemId != null) {
            item = new ItemPedido(proximoItemId++, produtoAtualizado, quantidade, precoVenda);
        } else {
            item = new ItemPedido(null, produtoAtualizado, quantidade, precoVenda);
        }

        pedido.adicionarItem(item);

        produtoAtualizado.setQuantidadeEstoque(produtoAtualizado.getQuantidadeEstoque() - quantidade);
        produtoRepository.save(produtoAtualizado);
        pedidoRepository.save(pedido);

        EstoqueValidator.verificarEstoqueBaixo(produtoAtualizado, 5);

        System.out.println("🛒 Cliente " + pedido.getCliente().getNome() + " adicionou " +
                quantidade + "x " + produtoAtualizado.getNome() + " ao pedido ID " + pedido.getId());
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

        System.out.println("❌ Cliente " + pedido.getCliente().getNome() + " removeu " +
                item.getQuantidade() + "x " + produto.getNome() + " do pedido ID " + pedido.getId());
    }

    public void alterarQuantidadeItem(Pedido pedido, Long itemId, int novaQuantidade) {
        validarPedidoAberto(pedido);

        ItemPedido item = pedido.getItens().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Item não encontrado no pedido."));

        int delta = novaQuantidade - item.getQuantidade();
        Produto produto = item.getProduto();

        if (delta > 0) {
            EstoqueValidator.validarEstoqueSuficiente(produto, delta);
            produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - delta);
        } else if (delta < 0) {
            produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() + Math.abs(delta));
        }

        item.setQuantidade(novaQuantidade);
        produtoRepository.save(produto);
        pedidoRepository.save(pedido);

        EstoqueValidator.verificarEstoqueBaixo(produto, 5);

        System.out.println("✏️ Cliente " + pedido.getCliente().getNome() + " alterou quantidade do produto " +
                produto.getNome() + " para " + novaQuantidade + " no pedido ID " + pedido.getId());
    }

    public void finalizarPedido(Pedido pedido) {
        validarPedidoAberto(pedido);
        if (!pedido.podeFinalizar()) {
            throw new ValidationException("Não é possível finalizar pedido sem itens ou valor maior que zero.");
        }

        pedido.finalizarPedido();
        pedidoRepository.save(pedido);

        notificador.notificarPedidoCriado(pedido.getCliente(), pedido);

        System.out.println("✔ Cliente " + pedido.getCliente().getNome() + " finalizou o pedido ID " + pedido.getId());
    }

    public void pagarPedido(Pedido pedido) {
        if (pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO) {
            throw new ValidationException("Pedido deve estar 'AGUARDANDO PAGAMENTO' antes de pagar.");
        }

        pedido.pagar();
        pedidoRepository.save(pedido);
        notificador.notificarPagamentoAprovado(pedido.getCliente(), pedido);

        System.out.println("💰 Cliente " + pedido.getCliente().getNome() + " efetuou pagamento do pedido ID " + pedido.getId());
    }

    public void entregarPedido(Pedido pedido) {
        if (pedido.getStatus() != StatusPedido.PAGO) {
            throw new ValidationException("Pedido deve estar 'PAGO' antes de entregar.");
        }

        pedido.entregar();
        pedidoRepository.save(pedido);
        notificador.notificarPedidoEntregue(pedido.getCliente(), pedido);

        System.out.println("📦 Pedido ID " + pedido.getId() + " entregue para o cliente " + pedido.getCliente().getNome());
    }

    private void validarPedidoAberto(Pedido pedido) {
        if (pedido == null)
            throw new ValidationException("Pedido não pode ser nulo.");
        if (pedido.getStatus() != StatusPedido.ABERTO)
            throw new ValidationException("Só é possível alterar pedidos com status 'ABERTO'.");
    }
}
