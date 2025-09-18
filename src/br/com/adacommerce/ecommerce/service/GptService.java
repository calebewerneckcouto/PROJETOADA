package br.com.adacommerce.ecommerce.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import br.com.adacommerce.ecommerce.model.Cliente;
import br.com.adacommerce.ecommerce.model.ItemPedido;
import br.com.adacommerce.ecommerce.model.Pedido;
import br.com.adacommerce.ecommerce.model.Produto;

public class GptService {

	private final String CHAT_GPT_API_KEY = System.getenv("OPENAI_KEY");
	private final String MODEL = "gpt-4-turbo";
	private final String SENHA_GERENTE = System.getenv("GERENTE_SENHA");

	// Tamanho máximo de cada chunk em caracteres (ajuste conforme necessário)
	private final int CHUNK_SIZE = 3000;

	private ClienteService clienteService;
	private ProdutoService produtoService;
	private VendaService vendaService;

	private Gson gson = new Gson();

	public GptService(ClienteService clienteService, ProdutoService produtoService, VendaService vendaService) {
		this.clienteService = clienteService;
		this.produtoService = produtoService;
		this.vendaService = vendaService;
	}

	/**
	 * Gera chunks dos dados do banco organizados por tipo
	 */
	private List<String> gerarChunksDoBanco() {
		List<String> chunks = new ArrayList<>();

		// Chunk 1: Clientes
		StringBuilder clientesChunk = new StringBuilder("=== CLIENTES ===\n");
		List<Cliente> clientes = clienteService.listarClientes();
		for (Cliente c : clientes) {
			clientesChunk.append(String.format("ID: %d | Nome: %s | Email: %s | Documento: %s\n", c.getId(),
					c.getNome(), c.getEmail(), c.getDocumento()));
		}
		chunks.add(clientesChunk.toString());

		// Chunk 2: Produtos
		StringBuilder produtosChunk = new StringBuilder("=== PRODUTOS ===\n");
		List<Produto> produtos = produtoService.listarProdutos();
		for (Produto p : produtos) {
			produtosChunk.append(String.format("ID: %d | Nome: %s | Descrição: %s | Preço: %.2f | Estoque: %d\n",
					p.getId(), p.getNome(), p.getDescricao(), p.getPreco(), p.getQuantidadeEstoque()));
		}
		chunks.add(produtosChunk.toString());

		// Chunks 3+: Pedidos (pode gerar múltiplos chunks se houver muitos pedidos)
		List<Pedido> pedidos = vendaService.listarPedidosFinalizados();
		StringBuilder pedidoChunk = new StringBuilder("=== PEDIDOS ===\n");

		for (Pedido ped : pedidos) {
			String pedidoInfo = String.format(
					"\nPEDIDO ID: %d\nCliente: %s\nStatus: %s\nValor Total: %.2f\nData: %s\nItens:\n", ped.getId(),
					ped.getCliente().getNome(), ped.getStatus().name(), ped.getValorTotal(), ped.getDataCriacao());

			// Se adicionar este pedido ultrapassar o limite, salva o chunk atual
			if (pedidoChunk.length() + pedidoInfo.length() > CHUNK_SIZE) {
				chunks.add(pedidoChunk.toString());
				pedidoChunk = new StringBuilder("=== PEDIDOS (continuação) ===\n");
			}

			pedidoChunk.append(pedidoInfo);

			// Adiciona itens do pedido
			for (ItemPedido item : ped.getItens()) {
				String itemInfo = String.format("  - %s | Qtd: %d | Preço Venda: %.2f | Subtotal: %.2f\n",
						item.getProduto().getNome(), item.getQuantidade(), item.getPrecoVenda(),
						item.getPrecoVenda() * item.getQuantidade());
				pedidoChunk.append(itemInfo);
			}
		}

		if (pedidoChunk.length() > 0) {
			chunks.add(pedidoChunk.toString());
		}

		return chunks;
	}

	/**
	 * Envia dados em chunks e processa a pergunta
	 */
	public String gerarRelatorioGPT(String pergunta, String senhaFuncionario) {
		if (!SENHA_GERENTE.equals(senhaFuncionario)) {
			return "Senha incorreta! Acesso negado.";
		}

		List<String> chunks = gerarChunksDoBanco();

		// Consolida todos os chunks em uma única mensagem
		StringBuilder dadosCompletos = new StringBuilder();
		dadosCompletos.append(criarPromptComRegras(pergunta));
		dadosCompletos.append("\n\n=== DADOS DO BANCO ADA COMMERCE ===\n\n");

		for (String chunk : chunks) {
			dadosCompletos.append(chunk).append("\n\n");
		}

		dadosCompletos.append("=== FIM DOS DADOS ===\n\n");
		dadosCompletos.append("Baseando-se EXCLUSIVAMENTE nos dados acima, responda: ").append(pergunta);

		// Verifica se os dados não estão vazios
		if (chunks.isEmpty()) {
			return "Nenhum dado encontrado no banco de dados para análise.";
		}

		try {
			return enviarParaGPT(dadosCompletos.toString());
		} catch (Exception e) {
			return "Erro ao processar dados com GPT: " + e.getMessage();
		}
	}

	/**
	 * Cria prompt com regras específicas para o sistema
	 */
	private String criarPromptComRegras(String pergunta) {
		return String.format(
				"""
						Você é o Gerente ADA, assistente de análise de dados do sistema ADA Commerce.

						INSTRUÇÕES CRÍTICAS:
						1. Os dados do banco de dados serão fornecidos logo abaixo
						2. Responda APENAS com base nos dados fornecidos
						3. Se a pergunta não estiver relacionada aos dados do banco (clientes, produtos, pedidos, vendas), responda:
						   "Estou aqui apenas para ajudar na gestão e análise dos dados do ADA Commerce. Para outras questões, consulte a administração."
						4. Seja preciso e use apenas informações reais dos dados
						5. Forneça insights úteis (estatísticas, análises, recomendações)
						6. Use formato claro e organizado

						PERGUNTA A RESPONDER: %s
						""",
				pergunta);
	}

	/**
	 * Envia mensagem completa para o GPT
	 */
	private String enviarParaGPT(String conteudo) {
		try {
			URL url = new URL("https://api.openai.com/v1/chat/completions");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Authorization", "Bearer " + CHAT_GPT_API_KEY);
			con.setRequestProperty("Content-Type", "application/json");
			con.setDoOutput(true);

			String body = gson.toJson(Map.of("model", MODEL, "messages",
					List.of(Map.of("role", "user", "content", conteudo)), "max_tokens", 2000, "temperature", 0.2 // Resposta
																													// mais
																													// precisa
																													// mas
																													// com
																													// alguma
																													// criatividade
																													// para
																													// relatórios
			));

			try (OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream())) {
				writer.write(body);
				writer.flush();
			}

			try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
				StringBuilder responseData = new StringBuilder();
				String line;
				while ((line = in.readLine()) != null) {
					responseData.append(line);
				}

				JsonObject jsonResponse = JsonParser.parseString(responseData.toString()).getAsJsonObject();
				JsonArray choices = jsonResponse.getAsJsonArray("choices");

				if (choices.size() > 0) {
					JsonObject choice = choices.get(0).getAsJsonObject();
					JsonObject messageObj = choice.getAsJsonObject("message");
					if (messageObj != null && messageObj.has("content")) {
						return messageObj.get("content").getAsString();
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
			return "Erro ao conectar com GPT: " + e.getMessage();
		}

		return "Resposta vazia do GPT";
	}

	/**
	 * Método para debug - mostra quantos chunks foram gerados e preview dos dados
	 */
	public void mostrarEstatisticasChunks() {
		List<String> chunks = gerarChunksDoBanco();
		System.out.println("=== ESTATÍSTICAS DOS CHUNKS ===");
		System.out.println("Total de chunks: " + chunks.size());

		for (int i = 0; i < chunks.size(); i++) {
			System.out.printf("Chunk %d: %d caracteres\n", i + 1, chunks.get(i).length());
			// Preview das primeiras linhas
			String[] linhas = chunks.get(i).split("\n");
			System.out.println("Preview:");
			for (int j = 0; j < Math.min(3, linhas.length); j++) {
				System.out.println("  " + linhas[j]);
			}
			if (linhas.length > 3) {
				System.out.println("  ... (+" + (linhas.length - 3) + " linhas)");
			}
			System.out.println();
		}
	}

	/**
	 * Método para debug - mostra os dados que serão enviados ao GPT
	 */
	public void debugDados(String pergunta) {
		List<String> chunks = gerarChunksDoBanco();
		StringBuilder dadosCompletos = new StringBuilder();
		dadosCompletos.append(criarPromptComRegras(pergunta));
		dadosCompletos.append("\n\n=== DADOS DO BANCO ADA COMMERCE ===\n\n");

		for (String chunk : chunks) {
			dadosCompletos.append(chunk).append("\n\n");
		}

		System.out.println("=== DEBUG: DADOS QUE SERÃO ENVIADOS AO GPT ===");
		System.out.println(dadosCompletos.toString());
		System.out.println("=== FIM DO DEBUG ===");
	}

	/**
	 * Envia relatório por email
	 */
	public boolean enviarRelatorioPorEmail(String destinatario, String pergunta, String relatorio) {
		try {
			String assunto = "Relatório ADA Commerce - " + pergunta;

			String corpoHtml = formatarRelatorioParaEmail(pergunta, relatorio);

			EmailService emailService = new EmailService(destinatario, "Sistema ADA Commerce", assunto, corpoHtml);

			emailService.enviarEmail();
			return true;

		} catch (Exception e) {
			System.err.println("Erro ao enviar email: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Formata o relatório para HTML email
	 */
	/**
	 * Formata o relatório para HTML email
	 */
	private String formatarRelatorioParaEmail(String pergunta, String relatorio) {
		String dataHora = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date());

		return String.format(
				"""
						<!DOCTYPE html>
						<html>
						<head>
						    <meta charset="UTF-8">
						    <style>
						        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }
						        .container { background-color: white; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
						        .header {
						            background: linear-gradient(135deg, #007bff, #0056b3);
						            color: white;
						            padding: 20px;
						            border-radius: 8px 8px 0 0;
						            text-align: center;
						        }
						        .logo {
						            width: 60px;
						            height: 60px;
						            background-color: rgba(255,255,255,0.2);
						            border-radius: 50%%;
						            display: inline-flex;
						            align-items: center;
						            justify-content: center;
						            font-size: 24px;
						            margin-bottom: 10px;
						        }
						        .content { margin: 20px; line-height: 1.6; }
						        .footer {
						            margin: 20px;
						            padding-top: 20px;
						            border-top: 1px solid #eee;
						            font-size: 12px;
						            color: #666;
						        }
						        .pergunta {
						            font-weight: bold;
						            color: #007bff;
						            background-color: #f8f9fa;
						            padding: 10px;
						            border-left: 4px solid #007bff;
						            margin: 15px 0;
						        }
						        .resposta {
						            background-color: #f9f9f9;
						            padding: 20px;
						            border-radius: 5px;
						            border: 1px solid #e9ecef;
						            margin: 15px 0;
						        }
						        pre {
						            white-space: pre-wrap;
						            font-family: Arial, sans-serif;
						            margin: 0;
						        }
						        .data-hora {
						            font-size: 14px;
						            opacity: 0.9;
						        }
						    </style>
						</head>
						<body>
						    <div class="container">
						        <div class="header">
						           <img src="data:image/jpeg;base64,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"
				                 alt="Logo Ada" style="width:120px;"/>
						            <h2 style="margin: 0;">Relatório do Gerente ADA</h2>
						            <p class="data-hora">%s</p>
						        </div>

						        <div class="content">
						            <div class="pergunta">
						                <strong>Pergunta solicitada:</strong><br>
						                <em>"%s"</em>
						            </div>

						            <h3 style="color: #007bff;">Resposta do Gerente ADA:</h3>
						            <div class="resposta">
						                <pre>%s</pre>
						            </div>
						        </div>

						        <div class="footer">
						            <p><strong>Sistema ADA Commerce</strong></p>
						            <p>Este relatório foi gerado automaticamente pelo assistente de análise de dados.</p>
						            <p>Para dúvidas sobre os dados, consulte a administração do sistema.</p>
						            <hr style="border: none; border-top: 1px solid #eee; margin: 15px 0;">
						            <p style="text-align: center; color: #999;">
						                ADA Commerce - Gestão Inteligente de Dados
						            </p>
						        </div>
						    </div>
						</body>
						</html>
						""",
				dataHora, pergunta, relatorio.replace("<", "&lt;").replace(">", "&gt;"));
	}
}