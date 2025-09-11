package br.com.adacommerce.ecommerce.service;

import java.util.List;
import java.util.Optional;

import br.com.adacommerce.ecommerce.exceptions.ValidationException;
import br.com.adacommerce.ecommerce.model.Cliente;
import br.com.adacommerce.ecommerce.repository.Repository;

public class ClienteService {
    private final Repository<Cliente, Long> clienteRepository;
    private Long proximoId;

    
    public ClienteService(Repository<Cliente, Long> clienteRepository) {
        this.clienteRepository = clienteRepository;
        this.proximoId = clienteRepository.getNextId(); 
    }

    public Cliente cadastrarCliente(String nome, String email, String documento) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new ValidationException("Nome é obrigatório");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email é obrigatório");
        }
        if (documento == null || documento.trim().isEmpty()) {
            throw new ValidationException("Documento é obrigatório");
        }

        Cliente cliente = new Cliente(proximoId++, nome, email, documento);
        return clienteRepository.save(cliente);
    }

    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> buscarPorId(Long id) {
        return clienteRepository.findById(id);
    }
}
