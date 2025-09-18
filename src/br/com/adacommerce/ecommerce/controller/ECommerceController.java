package br.com.adacommerce.ecommerce.controller;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import br.com.adacommerce.ecommerce.exceptions.ValidationException;
import br.com.adacommerce.ecommerce.model.Cliente;
import br.com.adacommerce.ecommerce.model.Pedido;
import br.com.adacommerce.ecommerce.model.Produto;
import br.com.adacommerce.ecommerce.notifications.EmailNotificador;
import br.com.adacommerce.ecommerce.notifications.Notificador;
import br.com.adacommerce.ecommerce.service.ClienteService;
import br.com.adacommerce.ecommerce.service.GptService;
import br.com.adacommerce.ecommerce.service.ProdutoService;
import br.com.adacommerce.ecommerce.service.VendaService;
import br.com.adacommerce.ecommerce.validators.DocumentoValidator;
import br.com.adacommerce.ecommerce.validators.EmailValidator;

public class ECommerceController {

	private final ClienteService clienteService;
	private final ProdutoService produtoService;
	private final VendaService vendaService;
	private final Scanner scanner;
	private final Notificador notificador;
	private final String SENHA_GERENTE = System.getenv("GERENTE_SENHA");

	private final GptService gptService;

	private Cliente clienteSelecionado;
	private Pedido pedidoAtual;

	public ECommerceController(ClienteService clienteService, ProdutoService produtoService, VendaService vendaService,
			Scanner scanner, GptService gptService) {
		this.clienteService = clienteService;
		this.produtoService = produtoService;
		this.vendaService = vendaService;
		this.scanner = scanner;
		this.gptService = gptService;
		this.notificador = new EmailNotificador();
	}

	public void iniciar() {
		boolean rodando = true;
		while (rodando) {
			mostrarMenu();
			String opcao = scanner.nextLine();
			switch (opcao) {
			case "1":
				selecionarOuCadastrarCliente();
				break;
			case "2":
				cadastrarOuAtualizarProduto();
				break;
			case "3":
				criarPedido();
				break;
			case "4":
				adicionarProdutoAoPedido();
				break;
			case "5":
				atualizarQuantidadeItem();
				break;
			case "6":
				finalizarPedido();
				break;
			case "7":
				pagarPedido();
				break;
			case "8":
				entregarPedido();
				break;
			case "9":
				mostrarPedidosFinalizados();
				break;
			case "10":
				removerItemDoPedido();
				break;
			case "11":
				atualizarCliente();
				break;
			case "12":
				buscarClientePorDocumento();
				break;
			case "13":
				buscarProdutoPorNome();
				break;

			case "14":
				relatorioGpt();
				break;

			case "0":
				rodando = false;
				System.out.println("Saindo do sistema...");
				break;
			default:
				System.out.println("Opção inválida!");
			}

		}
	}

	private void mostrarMenu() {
		System.out.println("\n=== MENU ===");
		System.out.println("1 - Cadastrar / Selecionar Cliente");
		System.out.println("2 - Cadastrar / Atualizar Produto");
		System.out.println("3 - Criar Pedido");
		System.out.println("4 - Adicionar Produto ao Pedido");
		System.out.println("5 - Atualizar quantidade de item do Pedido");
		System.out.println("6 - Finalizar Pedido");
		System.out.println("7 - Pagar Pedido");
		System.out.println("8 - Entregar Pedido");
		System.out.println("9 - Mostrar pedidos finalizados");
		System.out.println("10 - Remover item do Pedido");
		System.out.println("11 - Atualizar Cliente");
		System.out.println("12 - Buscar Cliente por Documento");
		System.out.println("13 - Buscar Produto por Nome");
		System.out.println("14 - Perguntar ao gerente ADA (ChatGPT) (Somente Diretoria)");

		System.out.println("0 - Sair");
		System.out.print("Escolha uma opção: ");
	}

	private void selecionarOuCadastrarCliente() {
		List<Cliente> clientes = clienteService.listarClientes();
		System.out.println("Clientes cadastrados:");
		clientes.forEach(c -> System.out.println(c.getId() + " - " + c.getNome() + " (" + c.getEmail() + ")"));

		System.out.print("Digite ID para selecionar ou 0 para cadastrar novo: ");
		long idCliente;
		try {
			idCliente = Long.parseLong(scanner.nextLine());
		} catch (NumberFormatException e) {
			System.out.println("ID inválido!");
			return;
		}

		if (idCliente == 0) {
			try {
				System.out.print("Nome: ");
				String nome = scanner.nextLine();
				System.out.print("Email: ");
				String email = scanner.nextLine();
				System.out.print("Documento: ");
				String doc = scanner.nextLine();

				DocumentoValidator.validar(doc);
				EmailValidator.validar(email);

				clienteSelecionado = clienteService.cadastrarCliente(nome, email, doc);
				System.out.println("Cliente cadastrado: " + clienteSelecionado.getNome());
			} catch (ValidationException ve) {
				System.out.println("Erro: " + ve.getMessage());
			}
		} else {
			Optional<Cliente> opt = clienteService.buscarPorId(idCliente);
			if (opt.isPresent()) {
				clienteSelecionado = opt.get();
				System.out.println("Cliente selecionado: " + clienteSelecionado.getNome());
			} else {
				System.out.println("Cliente não encontrado!");
			}
		}
	}

	private void atualizarCliente() {
		List<Cliente> clientes = clienteService.listarClientes();
		if (clientes.isEmpty()) {
			System.out.println("Nenhum cliente cadastrado!");
			return;
		}

		clientes.forEach(c -> System.out.println(c.getId() + " - " + c.getNome() + " (" + c.getEmail() + ")"));

		try {
			System.out.print("Digite o ID do cliente para atualizar: ");
			Long id = Long.parseLong(scanner.nextLine());

			System.out.print("Novo nome (Enter para manter): ");
			String nome = scanner.nextLine();

			System.out.print("Novo email (Enter para manter): ");
			String email = scanner.nextLine();

			System.out.print("Novo documento (Enter para manter): ");
			String documento = scanner.nextLine();

			Cliente atualizado = clienteService.atualizarCliente(id, nome.isEmpty() ? null : nome,
					email.isEmpty() ? null : email, documento.isEmpty() ? null : documento);

			System.out.println("Cliente atualizado: " + atualizado.getNome());

		} catch (ValidationException | NumberFormatException e) {
			System.out.println("Erro: " + e.getMessage());
		}
	}

	private void buscarClientePorDocumento() {
		System.out.print("Digite o documento do cliente: ");
		String documento = scanner.nextLine();

		clienteService.buscarPorDocumento(documento).ifPresentOrElse(
				c -> System.out.println("Encontrado: " + c.getNome() + " - " + c.getEmail()),
				() -> System.out.println("Nenhum cliente encontrado com este documento."));
	}

	private void buscarProdutoPorNome() {
		System.out.print("Digite o nome do produto: ");
		String nome = scanner.nextLine();

		produtoService.buscarProdutoPorNome(nome).ifPresentOrElse(
				p -> System.out.println("Encontrado: " + p.getNome() + " - Preço: " + p.getPreco() + " - Estoque: "
						+ p.getQuantidadeEstoque()),
				() -> System.out.println("Nenhum produto encontrado com este nome."));
	}

	private void cadastrarOuAtualizarProduto() {
		List<Produto> produtos = produtoService.listarProdutos();
		System.out.println("Produtos cadastrados:");
		produtos.forEach(
				p -> System.out.println(p.getId() + " - " + p.getNome() + " (Qtd: " + p.getQuantidadeEstoque() + ")"));

		System.out.print("Digite ID para atualizar ou 0 para cadastrar novo: ");
		long idProduto;
		try {
			idProduto = Long.parseLong(scanner.nextLine());
		} catch (NumberFormatException e) {
			System.out.println("ID inválido!");
			return;
		}

		if (idProduto == 0) {
			try {
				System.out.print("Nome: ");
				String nome = scanner.nextLine();
				System.out.print("Descrição: ");
				String desc = scanner.nextLine();
				System.out.print("Preço: ");
				double preco = Double.parseDouble(scanner.nextLine());
				System.out.print("Quantidade em estoque: ");
				int qtd = Integer.parseInt(scanner.nextLine());
				produtoService.cadastrarProduto(nome, desc, preco, qtd);
				System.out.println("Produto cadastrado!");
			} catch (ValidationException | NumberFormatException ve) {
				System.out.println("Erro: " + ve.getMessage());
			}
		} else {
			try {
				System.out.print("Novo nome (Enter para manter): ");
				String nome = scanner.nextLine();
				System.out.print("Nova descrição (Enter para manter): ");
				String desc = scanner.nextLine();
				System.out.print("Novo preço (0 para manter): ");
				String precoStr = scanner.nextLine();
				double preco = precoStr.isEmpty() ? 0 : Double.parseDouble(precoStr);

				System.out.print("Nova quantidade (Enter para manter): ");
				String qtdStr = scanner.nextLine();
				Integer qtd = null;
				if (!qtdStr.isEmpty()) {
					try {
						qtd = Integer.parseInt(qtdStr);
					} catch (NumberFormatException e) {
						qtd = null;
					}
				}

				produtoService.atualizarProduto(idProduto, nome.isEmpty() ? null : nome, desc.isEmpty() ? null : desc,
						preco, qtd);
				System.out.println("Produto atualizado!");
			} catch (ValidationException | NumberFormatException ve) {
				System.out.println("Erro: " + ve.getMessage());
			}
		}
	}

	private void criarPedido() {
		if (clienteSelecionado == null) {
			System.out.println("Selecione um cliente primeiro!");
			return;
		}
		pedidoAtual = vendaService.criarPedido(clienteSelecionado);
		System.out.println("Pedido criado com ID: " + pedidoAtual.getId());
		notificador.notificarPedidoCriado(clienteSelecionado, pedidoAtual);
	}

	private void adicionarProdutoAoPedido() {
		if (pedidoAtual == null) {
			System.out.println("Crie um pedido primeiro!");
			return;
		}

		List<Produto> produtos = produtoService.listarProdutos();
		if (produtos.isEmpty()) {
			System.out.println("Nenhum produto cadastrado!");
			return;
		}

		System.out.println("Produtos disponíveis:");
		produtos.forEach(p -> System.out.println(p.getId() + " - " + p.getNome() + " (Qtd: " + p.getQuantidadeEstoque()
				+ ", Preço: " + p.getPreco() + ")"));

		try {
			System.out.print("Digite o ID do produto que deseja adicionar: ");
			Long prodId = Long.parseLong(scanner.nextLine());
			System.out.print("Quantidade: ");
			int qtd = Integer.parseInt(scanner.nextLine());

			Produto produto = produtoService.buscarProdutoPorId(prodId)
					.orElseThrow(() -> new ValidationException("Produto não encontrado"));

			vendaService.adicionarItem(pedidoAtual, produto, qtd, produto.getPreco());
			System.out.println("Item adicionado!");

		} catch (ValidationException | NumberFormatException e) {
			System.out.println("Erro: " + e.getMessage());
		}
	}

	private void atualizarQuantidadeItem() {
		if (pedidoAtual == null) {
			System.out.println("Crie um pedido primeiro!");
			return;
		}
		try {
			System.out.print("ID do item: ");
			Long itemId = Long.parseLong(scanner.nextLine());
			System.out.print("Nova quantidade: ");
			int novaQtd = Integer.parseInt(scanner.nextLine());

			vendaService.alterarQuantidadeItem(pedidoAtual, itemId, novaQtd);
			System.out.println("Quantidade atualizada!");

		} catch (ValidationException | NumberFormatException e) {
			System.out.println("Erro: " + e.getMessage());
		}
	}

	

	// Método atualizado para o ECommerceController

	// Método atualizado para o ECommerceController

	// Método atualizado para o ECommerceController

	private void relatorioGpt() {
	    System.out.print("Digite a senha de acesso ao gerente ADA: ");
	    String senha = scanner.nextLine();

	    if (!SENHA_GERENTE.equals(senha)) {
	        System.out.println("Senha incorreta! Acesso negado.");
	        return;
	    }

	    // Opção para debug completo
	    System.out.print("Modo debug? (s para ver dados que serão enviados): ");
	    String debug = scanner.nextLine().toLowerCase();

	    System.out.println("=== GERENTE ADA - ASSISTENTE DE DADOS ===");
	    System.out.println("Você pode perguntar sobre:");
	    System.out.println("• Clientes cadastrados e seus dados");
	    System.out.println("• Produtos, preços e estoque");
	    System.out.println("• Pedidos, vendas e faturamento");
	    System.out.println("• Relatórios e análises dos dados");
	    System.out.println();
	    
	    System.out.print("Digite sua pergunta para o gerente ADA: ");
	    String pergunta = scanner.nextLine();

	    if (pergunta.trim().isEmpty()) {
	        System.out.println("Pergunta não pode estar vazia!");
	        return;
	    }

	    // Debug se solicitado
	    if ("s".equals(debug)) {
	        gptService.debugDados(pergunta);
	        System.out.print("Continuar com o envio? (s/n): ");
	        if (!"s".equals(scanner.nextLine().toLowerCase())) {
	            return;
	        }
	    }

	    System.out.println("\n🤖 Processando dados do banco e gerando resposta...");
	    
	    // Mostra estatísticas dos dados
	    gptService.mostrarEstatisticasChunks();
	    
	    // Chama o método corrigido
	    String resposta = gptService.gerarRelatorioGPT(pergunta, senha);

	    System.out.println("\n" + "=".repeat(50));
	    System.out.println("GERENTE ADA RESPONDE:");
	    System.out.println("=".repeat(50));
	    System.out.println(resposta);
	    System.out.println("=".repeat(50));
	    
	    // Opção de enviar por email
	    System.out.print("\nDeseja enviar este relatório por email? (s/n): ");
	    String enviarEmail = scanner.nextLine().toLowerCase();
	    
	    if ("s".equals(enviarEmail)) {
	        System.out.print("Digite o email de destino: ");
	        String emailDestino = scanner.nextLine().trim();
	        
	        if (!emailDestino.isEmpty() && emailDestino.contains("@")) {
	            System.out.println("📧 Enviando relatório por email...");
	            
	            boolean emailEnviado = gptService.enviarRelatorioPorEmail(emailDestino, pergunta, resposta);
	            
	            if (emailEnviado) {
	                System.out.println("✅ Relatório enviado com sucesso para: " + emailDestino);
	            } else {
	                System.out.println("❌ Erro ao enviar email. Verifique:");
	                System.out.println("  - Configurações EMAIL_USER e EMAIL_PASSWORD");
	                System.out.println("  - Conexão com internet");
	                System.out.println("  - Email de destino válido");
	            }
	        } else {
	            System.out.println("Email inválido! Deve conter @ e não estar vazio.");
	        }
	    }
	    
	    // Opção de fazer nova pergunta
	    System.out.print("\nDeseja fazer outra pergunta? (s/n): ");
	    String novaPergunta = scanner.nextLine().toLowerCase();
	    
	    if ("s".equals(novaPergunta)) {
	        relatorioGpt(); // Recursão para nova pergunta
	    }
	}
	private void removerItemDoPedido() {
		if (pedidoAtual == null) {
			System.out.println("Crie um pedido primeiro!");
			return;
		}
		try {
			System.out.print("ID do item: ");
			Long itemId = Long.parseLong(scanner.nextLine());
			vendaService.removerItem(pedidoAtual, itemId);
			System.out.println("Item removido!");
		} catch (ValidationException | NumberFormatException e) {
			System.out.println("Erro: " + e.getMessage());
		}
	}

	private void finalizarPedido() {
		if (pedidoAtual == null) {
			System.out.println("Crie um pedido primeiro!");
			return;
		}
		try {
			vendaService.finalizarPedido(pedidoAtual);
			System.out.println("Pedido finalizado com sucesso!");
			mostrarResumoPedido(pedidoAtual);
		} catch (ValidationException ve) {
			System.out.println("Não foi possível finalizar o pedido: " + ve.getMessage());
		}
	}

	private void pagarPedido() {
		if (pedidoAtual == null) {
			System.out.println("Crie um pedido primeiro!");
			return;
		}
		try {
			vendaService.pagarPedido(pedidoAtual);
			System.out.println("Pedido pago com sucesso!");
			mostrarResumoPedido(pedidoAtual);
		} catch (ValidationException ve) {
			System.out.println("Não foi possível pagar o pedido: " + ve.getMessage());
		}
	}

	private void entregarPedido() {
		if (pedidoAtual == null) {
			System.out.println("Crie um pedido primeiro!");
			return;
		}
		try {
			vendaService.entregarPedido(pedidoAtual);
			System.out.println("Pedido entregue com sucesso!");
			mostrarResumoPedido(pedidoAtual);

		} catch (ValidationException ve) {
			System.out.println("Não foi possível entregar o pedido: " + ve.getMessage());
		}
	}

	private void mostrarPedidosFinalizados() {
		List<Pedido> pedidos = vendaService.listarPedidosFinalizados();
		if (pedidos.isEmpty())
			System.out.println("Nenhum pedido finalizado!");
		else
			pedidos.forEach(this::mostrarResumoPedido);
	}

	private void mostrarResumoPedido(Pedido pedido) {
		System.out.println("\n=== RESUMO PEDIDO ID " + pedido.getId() + " ===");
		System.out.println("Cliente: " + pedido.getCliente().getNome());
		System.out.println("Data de criação: " + pedido.getDataCriacao());
		pedido.getItens().forEach(i -> System.out.println(i.getId() + " - " + i.getProduto().getNome() + " x"
				+ i.getQuantidade() + " = " + i.getPrecoVenda() * i.getQuantidade()));
		System.out.println("Status: " + pedido.getStatus());
		System.out.println("Total: " + pedido.getValorTotal());
	}

}
