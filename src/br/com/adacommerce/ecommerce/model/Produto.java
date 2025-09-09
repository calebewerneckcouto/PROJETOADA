package br.com.adacommerce.ecommerce.model;

import java.io.Serializable;

public class Produto implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String nome;
    private String descricao;
    private double preco;
    private int quantidadeEstoque;
    
    public Produto(Long id, String nome, String descricao, double preco, int quantidadeEstoque) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.quantidadeEstoque = quantidadeEstoque;
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; } // ADICIONADO

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }
    public int getQuantidadeEstoque() { return quantidadeEstoque; }
    public void setQuantidadeEstoque(int quantidadeEstoque) { this.quantidadeEstoque = quantidadeEstoque; }
    
    public void reduzirEstoque(int quantidade) {
        if (quantidade > quantidadeEstoque) {
            throw new IllegalStateException("Quantidade em estoque insuficiente");
        }
        this.quantidadeEstoque -= quantidade;
    }
    
    public void aumentarEstoque(int quantidade) {
        this.quantidadeEstoque += quantidade;
    }
    
    @Override
    public String toString() {
        return "Produto [ID: " + id + ", Nome: " + nome + ", Descrição: " + descricao + 
               ", Preço: R$" + preco + ", Estoque: " + quantidadeEstoque + "]";
    }
}
