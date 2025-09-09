package br.com.adacommerce.ecommerce.model;

import java.io.Serializable;

public class ItemPedido implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Produto produto;
    private int quantidade;
    private double precoVenda;
    
    public ItemPedido(Long id, Produto produto, int quantidade, double precoVenda) {
        this.id = id;
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoVenda = precoVenda;
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public Produto getProduto() { return produto; }
    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    public double getPrecoVenda() { return precoVenda; }
    public void setPrecoVenda(double precoVenda) { this.precoVenda = precoVenda; }
    
    public double getSubtotal() {
        return quantidade * precoVenda;
    }
    
    @Override
    public String toString() {
        return "ItemPedido [ID: " + id + ", Produto: " + produto.getNome() + ", Quantidade: " + quantidade + 
               ", Preço: R$" + precoVenda + ", Subtotal: R$" + getSubtotal() + "]";
    }
}