package br.com.adacommerce.ecommerce.main;

import java.util.Scanner;

import br.com.adacommerce.ecommerce.configure.Persistencia;
import br.com.adacommerce.ecommerce.controller.ECommerceController;
import br.com.adacommerce.ecommerce.model.Cliente;
import br.com.adacommerce.ecommerce.model.Pedido;
import br.com.adacommerce.ecommerce.model.Produto;
import br.com.adacommerce.ecommerce.notifications.EmailNotificador;
import br.com.adacommerce.ecommerce.repository.Repository;
import br.com.adacommerce.ecommerce.repository.RepositoryFactory;
import br.com.adacommerce.ecommerce.service.ClienteService;
import br.com.adacommerce.ecommerce.service.GptService;
import br.com.adacommerce.ecommerce.service.ProdutoService;
import br.com.adacommerce.ecommerce.service.VendaService;

public class ECommerceSystem {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        
        Persistencia tipo = Persistencia.MEMORIA;
        RepositoryFactory factory = new RepositoryFactory(tipo);

      
        Repository<Cliente, Long> clienteRepo = factory.getClienteRepository();
        Repository<Produto, Long> produtoRepo = factory.getProdutoRepository();
        Repository<Pedido, Long> pedidoRepo = factory.getPedidoRepository();

      
        EmailNotificador notificador = new EmailNotificador();

      
        ClienteService clienteService = new ClienteService(clienteRepo);
        ProdutoService produtoService = new ProdutoService(produtoRepo, notificador);
        VendaService vendaService = new VendaService(pedidoRepo, produtoRepo, notificador);

       
        GptService gptService = new GptService(clienteService, produtoService, vendaService);

        
        ECommerceController controller = new ECommerceController(
                clienteService, produtoService, vendaService, scanner, gptService 
        );

       
        controller.iniciar();

        scanner.close();
    }
}
