package br.com.adacommerce.ecommerce.controller;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import br.com.adacommerce.ecommerce.exceptions.ValidationException;
import br.com.adacommerce.ecommerce.model.Cliente;
import br.com.adacommerce.ecommerce.model.Pedido;
import br.com.adacommerce.ecommerce.model.Produto;
import br.com.adacommerce.ecommerce.service.ClienteService;
import br.com.adacommerce.ecommerce.service.ProdutoService;
import br.com.adacommerce.ecommerce.service.VendaService;
import br.com.adacommerce.ecommerce.notifications.Notificador;
import br.com.adacommerce.ecommerce.notifications.EmailNotificador;

public class ECommerceController {

    private final ClienteService clienteService;
    private final ProdutoService produtoService;
    private final VendaService vendaService;
    private final Scanner scanner;
    private final Notificador notificador;

    private Cliente clienteSelecionado;
    private Pedido pedidoAtual;

    public ECommerceController(ClienteService clienteService, ProdutoService produtoService,
                               VendaService vendaService, Scanner scanner) {
        this.clienteService = clienteService;
        this.produtoService = produtoService;
        this.vendaService = vendaService;
        this.scanner = scanner;
        this.notificador = new EmailNotificador();
    }

    public void iniciar() {
        boolean rodando = true;
        while (rodando) {
            mostrarMenu();
            String opcao = scanner.nextLine();
            switch (opcao) {
                case "1": selecionarOuCadastrarCliente(); break;
                case "2": cadastrarOuAtualizarProduto(); break;
                case "3": criarPedido(); break;
                case "4": adicionarProdutoAoPedido(); break;
                case "5": atualizarQuantidadeItem(); break;
                case "6": finalizarPedido(); break;
                case "7": pagarPedido(); break;
                case "8": entregarPedido(); break;
                case "9": mostrarPedidosFinalizados(); break;
                case "10": removerItemDoPedido(); break;
                case "11": mostrarPedidosAbertos(); break;
                case "0": rodando = false; System.out.println("Saindo do sistema..."); break;
                default: System.out.println("Opção inválida!");
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
        System.out.println("11 - Mostrar Pedidos em Aberto");
        System.out.println("0 - Sair");
        System.out.print("Escolha uma opção: ");
    }

    private void selecionarOuCadastrarCliente() {
        List<Cliente> clientes = clienteService.listarClientes();
        System.out.println("Clientes cadastrados:");
        clientes.forEach(c -> System.out.println(c.getId() + " - " + c.getNome() + " (" + c.getEmail() + ")"));

        System.out.print("Digite ID para selecionar ou 0 para cadastrar novo: ");
        long idCliente;
        try { idCliente = Long.parseLong(scanner.nextLine()); } 
        catch (NumberFormatException e) { System.out.println("ID inválido!"); return; }

        if (idCliente == 0) {
            try {
                System.out.print("Nome: "); String nome = scanner.nextLine();
                System.out.print("Email: "); String email = scanner.nextLine();
                System.out.print("Documento: "); String doc = scanner.nextLine();
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

    private void cadastrarOuAtualizarProduto() {
        List<Produto> produtos = produtoService.listarProdutos();
        System.out.println("Produtos cadastrados:");
        produtos.forEach(p -> System.out.println(p.getId() + " - " + p.getNome() + " (Qtd: " + p.getQuantidadeEstoque() + ")"));

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
                System.out.print("Nome: "); String nome = scanner.nextLine();
                System.out.print("Descrição: "); String desc = scanner.nextLine();
                System.out.print("Preço: "); double preco = Double.parseDouble(scanner.nextLine());
                System.out.print("Quantidade em estoque: "); int qtd = Integer.parseInt(scanner.nextLine());
                produtoService.cadastrarProduto(nome, desc, preco, qtd);
                System.out.println("Produto cadastrado!");
            } catch (ValidationException | NumberFormatException ve) {
                System.out.println("Erro: " + ve.getMessage());
            }
        } else {
            try {
                System.out.print("Novo nome (Enter para manter): "); String nome = scanner.nextLine();
                System.out.print("Nova descrição (Enter para manter): "); String desc = scanner.nextLine();
                System.out.print("Novo preço (0 para manter): "); String precoStr = scanner.nextLine();
                double preco = precoStr.isEmpty() ? 0 : Double.parseDouble(precoStr);

                System.out.print("Nova quantidade (Enter para manter): "); 
                String qtdStr = scanner.nextLine();
                Integer qtd = null;
                if (!qtdStr.isEmpty()) {
                    try { qtd = Integer.parseInt(qtdStr); } 
                    catch (NumberFormatException e) { qtd = null; }
                }

                produtoService.atualizarProduto(idProduto, 
                                                nome.isEmpty() ? null : nome, 
                                                desc.isEmpty() ? null : desc, 
                                                preco, 
                                                qtd);
                System.out.println("Produto atualizado!");
            } catch (ValidationException | NumberFormatException ve) {
                System.out.println("Erro: " + ve.getMessage());
            }
        }
    }

    private void criarPedido() {
        if (clienteSelecionado == null) { System.out.println("Selecione um cliente primeiro!"); return; }
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
        if (produtos.isEmpty()) { System.out.println("Nenhum produto cadastrado!"); return; }

        System.out.println("Produtos disponíveis:");
        produtos.forEach(p -> System.out.println(
            p.getId() + " - " + p.getNome() + " (Qtd: " + p.getQuantidadeEstoque() + ", Preço: " + p.getPreco() + ")"
        ));

        try {
            System.out.print("Digite o ID do produto que deseja adicionar: "); 
            Long prodId = Long.parseLong(scanner.nextLine());
            System.out.print("Quantidade: "); 
            int qtd = Integer.parseInt(scanner.nextLine());

            Produto produto = produtoService.buscarProdutoPorId(prodId)
                    .orElseThrow(() -> new ValidationException("Produto não encontrado"));

            if (produto.getQuantidadeEstoque() < qtd) {
                System.out.println("Estoque insuficiente! Disponível: " + produto.getQuantidadeEstoque());
                return;
            }

            vendaService.adicionarItem(pedidoAtual, produto, qtd, produto.getPreco());
            System.out.println("Item adicionado!");

            if (produto.getQuantidadeEstoque() - qtd <= 5) { 
                notificador.notificarEstoqueBaixo(produto.getNome(), produto.getQuantidadeEstoque() - qtd);
            }

        } catch (ValidationException | NumberFormatException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void atualizarQuantidadeItem() {
        if (pedidoAtual == null) { System.out.println("Crie um pedido primeiro!"); return; }
        try {
            System.out.print("ID do item: "); Long itemId = Long.parseLong(scanner.nextLine());
            System.out.print("Nova quantidade: "); int qtd = Integer.parseInt(scanner.nextLine());
            vendaService.alterarQuantidadeItem(pedidoAtual, itemId, qtd);
            System.out.println("Quantidade atualizada!");

            // Verifica estoque do produto atualizado
            pedidoAtual.getItens().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .ifPresent(i -> {
                    if (i.getProduto().getQuantidadeEstoque() <= 5) {
                        notificador.notificarEstoqueBaixo(i.getProduto().getNome(), i.getProduto().getQuantidadeEstoque());
                    }
                });

        } catch (ValidationException | NumberFormatException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void finalizarPedido() {
        if (pedidoAtual == null) { System.out.println("Crie um pedido primeiro!"); return; }
        try {
            vendaService.finalizarPedido(pedidoAtual);
            System.out.println("Pedido finalizado com sucesso!");
            mostrarResumoPedido(pedidoAtual);
        } catch (ValidationException ve) {
            System.out.println("Não foi possível finalizar o pedido: " + ve.getMessage());
        }
    }

    private void pagarPedido() {
        if (pedidoAtual == null) { System.out.println("Crie um pedido primeiro!"); return; }
        try {
            vendaService.pagarPedido(pedidoAtual);
            System.out.println("Pedido pago com sucesso!");
            mostrarResumoPedido(pedidoAtual);
            notificador.notificarPagamentoAprovado(clienteSelecionado, pedidoAtual);
        } catch (ValidationException ve) {
            System.out.println("Não foi possível pagar o pedido: " + ve.getMessage());
        }
    }

    private void entregarPedido() {
        if (pedidoAtual == null) { System.out.println("Crie um pedido primeiro!"); return; }
        try {
            vendaService.entregarPedido(pedidoAtual);
            System.out.println("Pedido entregue com sucesso!");
            mostrarResumoPedido(pedidoAtual);
            notificador.notificarPedidoEntregue(clienteSelecionado, pedidoAtual);
        } catch (ValidationException ve) {
            System.out.println("Não foi possível entregar o pedido: " + ve.getMessage());
        }
    }

    private void removerItemDoPedido() {
        if (pedidoAtual == null) { System.out.println("Crie um pedido primeiro!"); return; }
        try {
            System.out.print("ID do item: "); Long itemId = Long.parseLong(scanner.nextLine());
            vendaService.removerItem(pedidoAtual, itemId);
            System.out.println("Item removido!");
        } catch (ValidationException | NumberFormatException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void mostrarPedidosFinalizados() {
        List<Pedido> pedidos = vendaService.listarPedidosFinalizados();
        if (pedidos.isEmpty()) System.out.println("Nenhum pedido finalizado!");
        else pedidos.forEach(this::mostrarResumoPedido);
    }

    private void mostrarPedidosAbertos() {
        List<Pedido> pedidos = vendaService.listarPedidosAbertos();
        if (pedidos.isEmpty()) System.out.println("Nenhum pedido em aberto!");
        else pedidos.forEach(this::mostrarResumoPedido);
    }

    private void mostrarResumoPedido(Pedido pedido) {
        System.out.println("\n=== RESUMO PEDIDO ID " + pedido.getId() + " ===");
        System.out.println("Cliente: " + pedido.getCliente().getNome());
        pedido.getItens().forEach(i -> System.out.println(
                i.getId() + " - " + i.getProduto().getNome() + " x" + i.getQuantidade() +
                " = " + i.getPrecoVenda() * i.getQuantidade()
        ));
        System.out.println("Status: " + pedido.getStatus());
        System.out.println("Total: " + pedido.getValorTotal());
    }
}
