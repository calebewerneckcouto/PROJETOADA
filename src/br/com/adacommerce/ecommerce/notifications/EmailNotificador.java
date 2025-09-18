package br.com.adacommerce.ecommerce.notifications;

import br.com.adacommerce.ecommerce.model.Cliente;
import br.com.adacommerce.ecommerce.model.Pedido;
import br.com.adacommerce.ecommerce.service.EmailService;

public class EmailNotificador implements Notificador {

    @Override
    public void notificarCliente(Cliente cliente, String mensagem) {
        try {
            EmailService emailService = new EmailService(
                    cliente.getEmail(),                 
                    "AdaCommerce",                      
                    "Notificação do Sistema",           
                    mensagem                            
            );
            emailService.enviarEmail(); 
            System.out.println("E-mail enviado com sucesso para " + cliente.getEmail());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Falha ao enviar e-mail para " + cliente.getEmail());
        }
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
        String mensagem = "⚠ ALERTA DE ESTOQUE BAIXO ⚠\n" +
                          "Produto: " + produtoNome + "\n" +
                          "Quantidade em estoque: " + quantidade + "\n" +
                          "Por favor, reabastecer o estoque.";
        try {
            EmailService emailService = new EmailService(
                    "seu-email-admin@gmail.com",
                    "AdaCommerce",
                    "Alerta de Estoque Baixo",
                    mensagem
            );
            emailService.enviarEmail();
            System.out.println("E-mail de alerta enviado para administrador.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void notificarPedidoAguardandoPagamento(Cliente cliente, Pedido pedido) {
        String mensagem = "Olá " + cliente.getNome() + ",\n" +
                "Seu pedido de ID " + pedido.getId() +
                " foi finalizado e está aguardando pagamento.\n" +
                "Valor total: R$" + pedido.getValorTotal() + "\n" +
                "Data: " + pedido.getDataCriacao() + "\n" +
                "Obrigado por comprar conosco!";
        notificarCliente(cliente, mensagem);
    }
}
