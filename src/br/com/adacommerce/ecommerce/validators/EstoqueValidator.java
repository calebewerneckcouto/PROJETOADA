package br.com.adacommerce.ecommerce.validators;

import br.com.adacommerce.ecommerce.exceptions.InsufficientStockException;
import br.com.adacommerce.ecommerce.model.Produto;

public class EstoqueValidator {
    public static void validarEstoqueSuficiente(Produto produto, int quantidade) {
        if (produto.getQuantidadeEstoque() < quantidade) {
            throw new InsufficientStockException(
                "Estoque insuficiente para o produto " + produto.getNome() + 
                ". Disponível: " + produto.getQuantidadeEstoque() + 
                ", Solicitado: " + quantidade
            );
        }
    }
    
    public static void verificarEstoqueBaixo(Produto produto, int limiteAlerta) {
        if (produto.getQuantidadeEstoque() <= limiteAlerta && produto.getQuantidadeEstoque() > 0) {
            System.out.println("ALERTA: Estoque baixo para o produto " + produto.getNome() + 
                             ". Quantidade: " + produto.getQuantidadeEstoque());
        }
    }
}