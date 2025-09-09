package br.com.adacommerce.ecommerce.notifications;

import br.com.adacommerce.ecommerce.model.Cliente;
import br.com.adacommerce.ecommerce.model.Pedido;

public interface Notificador {
    void notificarCliente(Cliente cliente, String mensagem);
    void notificarPedidoCriado(Cliente cliente, Pedido pedido);
    void notificarPagamentoAprovado(Cliente cliente, Pedido pedido);
    void notificarPedidoEntregue(Cliente cliente, Pedido pedido);
    void notificarEstoqueBaixo(String produtoNome, int quantidade);
}