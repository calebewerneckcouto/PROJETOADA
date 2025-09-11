package br.com.adacommerce.ecommerce.repository;

import br.com.adacommerce.ecommerce.configure.Persistencia;
import br.com.adacommerce.ecommerce.model.Cliente;
import br.com.adacommerce.ecommerce.model.Produto;
import br.com.adacommerce.ecommerce.model.Pedido;

public class RepositoryFactory {

    private Persistencia tipo;

    public RepositoryFactory(Persistencia tipo) {
        this.tipo = tipo;
    }

    public Repository<Cliente, Long> getClienteRepository() {
        switch (tipo) {
            case ARQUIVO:
                return new ClienteRepositoryFile();
            case MEMORIA:
            default:
                return new ClienteRepositoryMemory();
        }
    }

    public Repository<Produto, Long> getProdutoRepository() {
        switch (tipo) {
            case ARQUIVO:
                return new ProdutoRepositoryFile();
            case MEMORIA:
            default:
                return new ProdutoRepositoryMemory();
        }
    }

    public Repository<Pedido, Long> getPedidoRepository() {
        switch (tipo) {
            case ARQUIVO:
                return new PedidoRepositoryFile();
            case MEMORIA:
            default:
                return new PedidoRepositoryMemory();
        }
    }
}
