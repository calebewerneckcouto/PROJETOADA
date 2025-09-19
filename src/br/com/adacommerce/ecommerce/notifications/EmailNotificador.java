package br.com.adacommerce.ecommerce.notifications;

import br.com.adacommerce.ecommerce.model.Cliente;
import br.com.adacommerce.ecommerce.model.Pedido;
import br.com.adacommerce.ecommerce.model.ItemPedido;
import br.com.adacommerce.ecommerce.service.EmailService;

public class EmailNotificador implements Notificador {

    @Override
    public void notificarCliente(Cliente cliente, String mensagem) {
        String html = gerarTemplateBase("Notificação do Sistema", mensagem);
        notificarClienteHtml(cliente, "Notificação do Sistema", html);
    }

    private void notificarClienteHtml(Cliente cliente, String assunto, String corpoHtml) {
        try {
            EmailService emailService = new EmailService(
                cliente.getEmail(),
                "AdaCommerce",
                assunto,
                corpoHtml
            );
            emailService.enviarEmail();
            System.out.println("E-mail HTML enviado para " + cliente.getEmail());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Falha ao enviar e-mail HTML para " + cliente.getEmail());
        }
    }

    // ------- GERA TABELA DE PRODUTOS -------
    private String gerarTabelaProdutos(Pedido pedido) {
        StringBuilder produtosHtml = new StringBuilder("<table style='width:100%; border-collapse: collapse; margin-top:15px;'>");
        produtosHtml.append("<tr style='background:#f4f4f4; text-align:left;'>")
                    .append("<th style='padding:8px; border:1px solid #ddd;'>Produto</th>")
                    .append("<th style='padding:8px; border:1px solid #ddd;'>Qtd</th>")
                    .append("<th style='padding:8px; border:1px solid #ddd;'>Preço Unit.</th>")
                    .append("<th style='padding:8px; border:1px solid #ddd;'>Subtotal</th>")
                    .append("</tr>");

        for (ItemPedido item : pedido.getItens()) {
            produtosHtml.append("<tr>")
                .append("<td style='padding:8px; border:1px solid #ddd;'>").append(item.getProduto().getNome()).append("</td>")
                .append("<td style='padding:8px; border:1px solid #ddd;'>").append(item.getQuantidade()).append("</td>")
                .append("<td style='padding:8px; border:1px solid #ddd;'>R$ ").append(String.format("%.2f", item.getProduto().getPreco())).append("</td>")
                .append("<td style='padding:8px; border:1px solid #ddd;'>R$ ").append(String.format("%.2f", item.getSubtotal())).append("</td>")
                .append("</tr>");
        }
        produtosHtml.append("</table>");
        return produtosHtml.toString();
    }

    // ------- TEMPLATE BASE -------
    private String gerarTemplateBase(String titulo, String corpo) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                    <div style="max-width: 600px; margin: auto; background: #ffffff; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); padding: 20px;">
                        <div style="text-align:center;">
                            <h2 style="color:#4CAF50;">%s</h2>
                        </div>
                        <div style="margin-top:15px; font-size:14px; color:#333;">%s</div>
                        <hr style="margin-top:30px;">
                        <p style="font-size:12px; color:#777; text-align:center;">Obrigado por comprar na <strong>AdaCommerce</strong> 💚</p>
                    </div>
                </body>
                </html>
                """
                .formatted(titulo, corpo);
    }

    // ------- PEDIDO CRIADO -------
    @Override
    public void notificarPedidoCriado(Cliente cliente, Pedido pedido) {
        String corpo = """
            Olá <strong>%s</strong>,<br>
            Seu pedido <strong>#%d</strong> foi criado com sucesso!<br><br>
            <strong>Dados do Cliente:</strong><br>
            Nome: %s<br>
            Email: %s<br><br>

            <strong>Resumo do Pedido:</strong><br>
            Valor Total: <strong>R$ %.2f</strong><br>
            Status: <strong>%s</strong><br>
            Data: %s<br><br>

            <strong>Itens do Pedido:</strong><br>
            %s
        """.formatted(
            cliente.getNome(),
            pedido.getId(),
            cliente.getNome(),
            cliente.getEmail(),
            pedido.getValorTotal(),
            pedido.getStatus(),
            pedido.getDataCriacao(),
            gerarTabelaProdutos(pedido)
        );

        String html = gerarTemplateBase("Pedido Criado com Sucesso 🎉", corpo);
        notificarClienteHtml(cliente, "Pedido Criado", html);
    }

    // ------- PAGAMENTO APROVADO -------
    @Override
    public void notificarPagamentoAprovado(Cliente cliente, Pedido pedido) {
        String corpo = """
            Olá <strong>%s</strong>,<br>
            O pagamento do pedido <strong>#%d</strong> foi aprovado!<br><br>
            <strong>Resumo do Pedido:</strong><br>
            Valor Total: <strong>R$ %.2f</strong><br>
            Status: <strong>%s</strong><br>
            Data: %s<br><br>

            <strong>Itens do Pedido:</strong><br>
            %s
        """.formatted(
            cliente.getNome(),
            pedido.getId(),
            pedido.getValorTotal(),
            pedido.getStatus(),
            pedido.getDataCriacao(),
            gerarTabelaProdutos(pedido)
        );

        String html = gerarTemplateBase("Pagamento Aprovado ✅", corpo);
        notificarClienteHtml(cliente, "Pagamento Aprovado", html);
    }

    // ------- PEDIDO ENTREGUE -------
    @Override
    public void notificarPedidoEntregue(Cliente cliente, Pedido pedido) {
        String corpo = """
            Olá <strong>%s</strong>,<br>
            Seu pedido <strong>#%d</strong> foi entregue! 🎉<br><br>
            <strong>Resumo do Pedido:</strong><br>
            Valor Total: <strong>R$ %.2f</strong><br>
            Status: <strong>%s</strong><br>
            Data: %s<br><br>

            <strong>Itens do Pedido:</strong><br>
            %s
        """.formatted(
            cliente.getNome(),
            pedido.getId(),
            pedido.getValorTotal(),
            pedido.getStatus(),
            pedido.getDataCriacao(),
            gerarTabelaProdutos(pedido)
        );

        String html = gerarTemplateBase("Pedido Entregue 🚚", corpo);
        notificarClienteHtml(cliente, "Pedido Entregue", html);
    }

    // ------- ESTOQUE BAIXO -------
    @Override
    public void notificarEstoqueBaixo(String produtoNome, int quantidade) {
        String corpo = """
            ⚠ ALERTA DE ESTOQUE BAIXO ⚠<br>
            Produto: <strong>%s</strong><br>
            Quantidade em estoque: %d<br>
            Por favor, reabastecer o estoque.
        """.formatted(produtoNome, quantidade);

        String html = gerarTemplateBase("Alerta de Estoque Baixo ⚠", corpo);

        try {
            EmailService emailService = new EmailService(
                "seu-email-admin@gmail.com",
                "AdaCommerce",
                "Alerta de Estoque Baixo",
                html
            );
            emailService.enviarEmail();
            System.out.println("E-mail de alerta enviado para administrador.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ------- AGUARDANDO PAGAMENTO -------
    @Override
    public void notificarPedidoAguardandoPagamento(Cliente cliente, Pedido pedido) {
        String corpo = """
            Olá <strong>%s</strong>,<br>
            Seu pedido <strong>#%d</strong> está aguardando pagamento.<br><br>
            <strong>Resumo do Pedido:</strong><br>
            Valor Total: <strong>R$ %.2f</strong><br>
            Status: <strong>%s</strong><br>
            Data: %s<br><br>

            <strong>Itens do Pedido:</strong><br>
            %s
        """.formatted(
            cliente.getNome(),
            pedido.getId(),
            pedido.getValorTotal(),
            pedido.getStatus(),
            pedido.getDataCriacao(),
            gerarTabelaProdutos(pedido)
        );

        String html = gerarTemplateBase("Aguardando Pagamento ⏳", corpo);
        notificarClienteHtml(cliente, "Pedido Aguardando Pagamento", html);
    }
}
