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
        try {
            this.proximoId = clienteRepository.getNextId();
        } catch (UnsupportedOperationException e) {
            this.proximoId = null; 
        }
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

        
        Optional<Cliente> existente = buscarPorDocumento(documento);
        if (existente.isPresent()) {
            throw new ValidationException("Já existe cliente com este documento!");
        }

        Cliente cliente;
        if (proximoId != null) {
            cliente = new Cliente(proximoId++, nome, email, documento);
        } else {
            cliente = new Cliente(null, nome, email, documento);
        }

        return clienteRepository.save(cliente);
    }

    
    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    
    public Optional<Cliente> buscarPorId(Long id) {
        return clienteRepository.findById(id);
    }

   
    public Cliente atualizarCliente(Long id, String nome, String email, String documento) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Cliente não encontrado"));

        if (nome != null && !nome.trim().isEmpty()) {
            cliente.setNome(nome);
        }
        if (email != null && !email.trim().isEmpty()) {
            cliente.setEmail(email);
        }
        if (documento != null && !documento.trim().isEmpty()) {
            
            Optional<Cliente> existente = buscarPorDocumento(documento);
            if (existente.isPresent() && !existente.get().getId().equals(id)) {
                throw new ValidationException("Já existe outro cliente com este documento!");
            }
            cliente.setDocumento(documento);
        }

        return clienteRepository.save(cliente);
    }

    
    public Optional<Cliente> buscarPorDocumento(String documento) {
        return clienteRepository.findAll().stream()
                .filter(c -> c.getDocumento().equalsIgnoreCase(documento))
                .findFirst();
    }

    
    public void excluirCliente(Long id) {
        throw new UnsupportedOperationException("Exclusão de cliente não é permitida (histórico mantido).");
    }
}
