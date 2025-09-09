package br.com.adacommerce.ecommerce.notifications;

import br.com.adacommerce.ecommerce.model.Cliente;
import br.com.adacommerce.ecommerce.model.Pedido;

public class EmailNotificador implements Notificador {
    @Override
    public void notificarCliente(Cliente cliente, String mensagem) {
        System.out.println("=== EMAIL ENVIADO ===");
        System.out.println("Para: " + cliente.getEmail());
        System.out.println("Assunto: Notificação do Sistema");
        System.out.println("Mensagem: " + mensagem);
        System.out.println("=====================");
    }
    
    @Override
    public void notificarPedidoCriado(Cliente cliente, Pedido pedido) {
        String mensagem = "Seu pedido #" + pedido.getId() + " foi criado com sucesso!\n" +
                         "Valor total: R$" + pedido.getValorTotal() + "\n" +
                         "Status: " + pedido.getStatus() + "\n" +
                         "Aguardando pagamento.";
        
        notificarCliente(cliente, mensagem);
    }
    
    @Override
    public void notificarPagamentoAprovado(Cliente cliente, Pedido pedido) {
        String mensagem = "Pagamento do pedido #" + pedido.getId() + " aprovado!\n" +
                         "Valor: R$" + pedido.getValorTotal() + "\n" +
                         "Seu pedido será preparado para entrega.";
        
        notificarCliente(cliente, mensagem);
    }
    
    @Override
    public void notificarPedidoEntregue(Cliente cliente, Pedido pedido) {
        String mensagem = "Seu pedido #" + pedido.getId() + " foi entregue!\n" +
                         "Agradecemos pela preferência.";
        
        notificarCliente(cliente, mensagem);
    }
    
    @Override
    public void notificarEstoqueBaixo(String produtoNome, int quantidade) {
        System.out.println("=== ALERTA DE ESTOQUE BAIXO ===");
        System.out.println("Produto: " + produtoNome);
        System.out.println("Quantidade em estoque: " + quantidade);
        System.out.println("Por favor, reabastecer o estoque.");
        System.out.println("===============================");
    }
}