package br.com.adacommerce.ecommerce.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Pedido implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Cliente cliente;
    private LocalDateTime dataCriacao;
    private StatusPedido status;
    private List<ItemPedido> itens;
    
    public Pedido(Long id, Cliente cliente) {
        this.id = id;
        this.cliente = cliente;
        this.dataCriacao = LocalDateTime.now();
        this.status = StatusPedido.ABERTO;
        this.itens = new ArrayList<>();
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; } // Corrigido: não retorna Long, apenas seta
    public Cliente getCliente() { return cliente; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public StatusPedido getStatus() { return status; }
    public List<ItemPedido> getItens() { return itens; }
    
    // Calcula o total do pedido
    public double getValorTotal() {
        return itens.stream().mapToDouble(ItemPedido::getSubtotal).sum();
    }
    
    // Adiciona item ao pedido
    public void adicionarItem(ItemPedido item) {
        if (status != StatusPedido.ABERTO) {
            throw new IllegalStateException("Não é possível adicionar itens a um pedido " + status);
        }
        itens.add(item);
    }
    
    // Remove item do pedido
    public void removerItem(Long itemId) {
        if (status != StatusPedido.ABERTO) {
            throw new IllegalStateException("Não é possível remover itens de um pedido " + status);
        }
        itens.removeIf(item -> item.getId().equals(itemId));
    }
    
    // Altera quantidade de um item
    public void alterarQuantidadeItem(Long itemId, int novaQuantidade) {
        if (status != StatusPedido.ABERTO) {
            throw new IllegalStateException("Não é possível alterar a quantidade de itens de um pedido " + status);
        }
        
        for (ItemPedido item : itens) {
            if (item.getId().equals(itemId)) {
                item.setQuantidade(novaQuantidade);
                return;
            }
        }
        throw new IllegalArgumentException("Item não encontrado no pedido");
    }
    
    // Finaliza pedido (aguardando pagamento)
    public void finalizarPedido() {
        if (status != StatusPedido.ABERTO) {
            throw new IllegalStateException("Só é possível finalizar pedidos com status ABERTO");
        }
        
        if (itens.isEmpty() || getValorTotal() <= 0) {
            throw new IllegalStateException("Pedido deve ter pelo menos um item e valor maior que zero");
        }
        
        this.status = StatusPedido.AGUARDANDO_PAGAMENTO;
    }
    
    // Pagar pedido
    public void pagar() {
        if (status != StatusPedido.AGUARDANDO_PAGAMENTO) {
            throw new IllegalStateException("Só é possível pagar pedidos com status AGUARDANDO_PAGAMENTO");
        }
        this.status = StatusPedido.PAGO;
    }
    
    // Entregar pedido
    public void entregar() {
        if (status != StatusPedido.PAGO) {
            throw new IllegalStateException("Só é possível entregar pedidos com status PAGO");
        }
        this.status = StatusPedido.FINALIZADO;
    }
    
    @Override
    public String toString() {
        return "Pedido [ID: " + id + ", Cliente: " + cliente.getNome() + 
               ", Data: " + dataCriacao + ", Status: " + status + 
               ", Valor Total: R$" + getValorTotal() + "]";
    }
}
