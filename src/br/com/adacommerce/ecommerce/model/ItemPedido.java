package br.com.adacommerce.ecommerce.model;

import java.io.Serializable;

public class ItemPedido implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long id;
    private Produto produto;
    private int quantidade;
    private double precoVenda;

    public ItemPedido() {
    }

    // ✅ Construtor que está faltando
    public ItemPedido(Long id, Produto produto, int quantidade, double precoVenda) {
        this.id = id;
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoVenda = precoVenda;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public double getPrecoVenda() {
        return precoVenda;
    }

    public void setPrecoVenda(double precoVenda) {
        this.precoVenda = precoVenda;
    }

    // Método utilitário
    public double getSubtotal() {
        return quantidade * precoVenda;
    }
}
