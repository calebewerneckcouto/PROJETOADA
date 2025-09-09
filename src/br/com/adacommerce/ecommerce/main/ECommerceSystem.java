package br.com.adacommerce.ecommerce.main;

import java.util.Scanner;

import br.com.adacommerce.ecommerce.notifications.EmailNotificador;
import br.com.adacommerce.ecommerce.repository.ClienteRepositoryFile;
import br.com.adacommerce.ecommerce.repository.PedidoRepositoryFile;
import br.com.adacommerce.ecommerce.repository.ProdutoRepositoryFile;
import br.com.adacommerce.ecommerce.service.ClienteService;
import br.com.adacommerce.ecommerce.service.ProdutoService;
import br.com.adacommerce.ecommerce.service.VendaService;
import br.com.adacommerce.ecommerce.controller.ECommerceController;

public class ECommerceSystem {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        ClienteRepositoryFile clienteRepo = new ClienteRepositoryFile();
        ProdutoRepositoryFile produtoRepo = new ProdutoRepositoryFile();
        PedidoRepositoryFile pedidoRepo = new PedidoRepositoryFile();
        EmailNotificador notificador = new EmailNotificador();

        ClienteService clienteService = new ClienteService(clienteRepo);
        ProdutoService produtoService = new ProdutoService(produtoRepo, notificador);
        VendaService vendaService = new VendaService(pedidoRepo, produtoRepo, notificador);

        ECommerceController controller = new ECommerceController(clienteService, produtoService, vendaService, scanner);
        controller.iniciar();

        scanner.close();
    }
}
