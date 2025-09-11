package br.com.adacommerce.ecommerce.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "pedidos")
public class Pedido implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPedido status;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    public Pedido() {}

    public Pedido(Long id, Cliente cliente) {
        this.id = id;
        this.cliente = cliente;
        this.dataCriacao = LocalDateTime.now();
        this.status = StatusPedido.ABERTO;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public StatusPedido getStatus() { return status; }
    public void setStatus(StatusPedido status) { this.status = status; }

    public List<ItemPedido> getItens() { return itens; }
    public void setItens(List<ItemPedido> itens) { this.itens = itens; }

    public double getValorTotal() {
        return itens.stream().mapToDouble(ItemPedido::getSubtotal).sum();
    }

    public void adicionarItem(ItemPedido item) {
        itens.add(item);
        item.setPedido(this);
    }

    public void removerItem(Long itemId) {
        itens.removeIf(item -> item.getId().equals(itemId));
    }
    public boolean podeFinalizar() {
        return itens != null && !itens.isEmpty() && getValorTotal() > 0;
    }

    @Override
    public String toString() {
        return "Pedido [id=" + id + ", cliente=" + cliente.getNome() +
                ", data=" + dataCriacao + ", status=" + status +
                ", total=" + getValorTotal() + "]";
    }

    public void finalizarPedido() {
        if (!podeFinalizar()) {
            throw new IllegalStateException("Pedido não pode ser finalizado: precisa de itens e valor > 0");
        }
        this.status = StatusPedido.AGUARDANDO_PAGAMENTO;
    }

    public void pagar() {
        if (status != StatusPedido.AGUARDANDO_PAGAMENTO) {
            throw new IllegalStateException("Só é possível pagar pedidos que estão 'AGUARDANDO_PAGAMENTO'");
        }
        this.status = StatusPedido.PAGO;
    }

    public void entregar() {
        if (status != StatusPedido.PAGO) {
            throw new IllegalStateException("Só é possível entregar pedidos que estão 'PAGO'");
        }
        this.status = StatusPedido.FINALIZADO;
    }


}
