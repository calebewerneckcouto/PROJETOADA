package br.com.adacommerce.ecommerce.notifications;

import br.com.adacommerce.ecommerce.model.Cliente;
import br.com.adacommerce.ecommerce.model.Pedido;
import br.com.adacommerce.ecommerce.service.EmailService;

public class EmailNotificador implements Notificador {

	@Override
	public void notificarCliente(Cliente cliente, String mensagem) {
	    // Converte a mensagem simples em HTML usando um template
	    String html = gerarTemplateBase("Notificação do Sistema", mensagem);
	    notificarClienteHtml(cliente, "Notificação do Sistema", html);
	}

	// Método privado só interno
	private void notificarClienteHtml(Cliente cliente, String assunto, String corpoHtml) {
	    try {
	        EmailService emailService = new EmailService(
	            cliente.getEmail(),
	            "AdaCommerce",
	            assunto,
	            corpoHtml
	        );
	        emailService.enviarEmail(); // envia HTML
	        System.out.println("E-mail HTML enviado para " + cliente.getEmail());
	    } catch (Exception e) {
	        e.printStackTrace();
	        System.out.println("Falha ao enviar e-mail HTML para " + cliente.getEmail());
	    }
	}


	private String gerarTemplateBase(String titulo, String corpo) {
		return """
				<html>
				<body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
				    <div style="max-width: 600px; margin: auto; background: #ffffff; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); padding: 20px;">
				        <div style="text-align:center;">
				            <img src="data:image/jpeg;base64,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"
				                 alt="Logo Ada" style="width:120px;"/>
				        </div>
				        <h2 style="color:#4CAF50;">%s</h2>
				        <div style="margin-top:15px; font-size:14px; color:#333;">%s</div>
				        <hr style="margin-top:30px;">
				        <p style="font-size:12px; color:#777; text-align:center;">Obrigado por comprar na <strong>AdaCommerce</strong> 💚</p>
				    </div>
				</body>
				</html>
				"""
				.formatted(titulo, corpo);
	}

	@Override
	public void notificarPedidoCriado(Cliente cliente, Pedido pedido) {
		String corpo = """
				    Olá <strong>%s</strong>,<br>
				    Seu pedido <strong>#%d</strong> foi criado com sucesso!<br>
				    <strong>Valor Total:</strong> R$ %.2f<br>
				    <strong>Status:</strong> %s<br>
				    Aguardando pagamento.
				""".formatted(cliente.getNome(), pedido.getId(), pedido.getValorTotal(), pedido.getStatus());

		String html = gerarTemplateBase("Pedido Criado com Sucesso 🎉", corpo);
		notificarClienteHtml(cliente, "Pedido Criado", html);
	}

	@Override
	public void notificarPagamentoAprovado(Cliente cliente, Pedido pedido) {
	    String corpo = """
	        Olá <strong>%s</strong>,<br>
	        Pagamento do pedido <strong>#%d</strong> aprovado!<br>
	        Valor: R$ %.2f<br>
	        Seu pedido será preparado para entrega.
	    """.formatted(cliente.getNome(), pedido.getId(), pedido.getValorTotal());

	    String html = gerarTemplateBase("Pagamento Aprovado ✅", corpo);
	    notificarClienteHtml(cliente, "Pagamento Aprovado", html);
	}


	@Override
	public void notificarPedidoEntregue(Cliente cliente, Pedido pedido) {
	    String corpo = "Seu pedido <strong>#" + pedido.getId() + "</strong> foi entregue!<br>Agradecemos pela preferência.";
	    String html = gerarTemplateBase("Pedido Entregue 🚚", corpo);
	    notificarClienteHtml(cliente, "Pedido Entregue", html);
	}


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


	@Override
	public void notificarPedidoAguardandoPagamento(Cliente cliente, Pedido pedido) {
	    String corpo = """
	        Olá <strong>%s</strong>,<br>
	        Seu pedido de ID <strong>%d</strong> foi finalizado e está aguardando pagamento.<br>
	        Valor total: R$ %.2f<br>
	        Data: %s
	    """.formatted(cliente.getNome(), pedido.getId(), pedido.getValorTotal(), pedido.getDataCriacao());

	    String html = gerarTemplateBase("Aguardando Pagamento ⏳", corpo);
	    notificarClienteHtml(cliente, "Pedido Aguardando Pagamento", html);
	}

}
