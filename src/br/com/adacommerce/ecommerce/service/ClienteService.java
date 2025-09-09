package br.com.adacommerce.ecommerce.service;

import java.util.List;
import java.util.Optional;

import br.com.adacommerce.ecommerce.exceptions.ValidationException;
import br.com.adacommerce.ecommerce.model.Cliente;
import br.com.adacommerce.ecommerce.repository.ClienteRepositoryFile;
import br.com.adacommerce.ecommerce.validators.DocumentoValidator;
import br.com.adacommerce.ecommerce.validators.EmailValidator;

public class ClienteService {
    private final ClienteRepositoryFile clienteRepository;
    
    public ClienteService(ClienteRepositoryFile clienteRepository) {
        this.clienteRepository = clienteRepository;
    }
    
    public Cliente cadastrarCliente(String nome, String email, String documento) {
        // Validações
        if (nome == null || nome.trim().isEmpty()) {
            throw new ValidationException("Nome é obrigatório");
        }
        
        EmailValidator.validar(email);
        DocumentoValidator.validar(documento);
        
        // Verificar se email ou documento já existem
        if (clienteRepository.existsByEmail(email)) {
            throw new ValidationException("Já existe um cliente cadastrado com este email: " + email);
        }
        
        if (clienteRepository.existsByDocumento(documento)) {
            throw new ValidationException("Já existe um cliente cadastrado com este documento: " + documento);
        }
        
        Cliente cliente = new Cliente(null, nome, email, documento);
        return clienteRepository.save(cliente);
    }
    
    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }
    
    public Cliente atualizarCliente(Long id, String nome, String email, String documento) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Cliente não encontrado com ID: " + id));
        
        if (nome != null && !nome.trim().isEmpty()) {
            cliente.setNome(nome);
        }
        
        if (email != null && !email.trim().isEmpty()) {
            EmailValidator.validar(email);
            // Verificar se o novo email já existe (excluindo o próprio cliente)
            Optional<Cliente> clienteComEmail = clienteRepository.findByEmail(email);
            if (clienteComEmail.isPresent() && !clienteComEmail.get().getId().equals(id)) {
                throw new ValidationException("Já existe outro cliente com este email: " + email);
            }
            cliente.setEmail(email);
        }
        
        if (documento != null && !documento.trim().isEmpty()) {
            DocumentoValidator.validar(documento);
            // Verificar se o novo documento já existe (excluindo o próprio cliente)
            Optional<Cliente> clienteComDocumento = clienteRepository.findByDocumento(documento);
            if (clienteComDocumento.isPresent() && !clienteComDocumento.get().getId().equals(id)) {
                throw new ValidationException("Já existe outro cliente com este documento: " + documento);
            }
            cliente.setDocumento(documento);
        }
        
        return clienteRepository.save(cliente);
    }
    
    public Optional<Cliente> buscarClientePorId(Long id) {
        return clienteRepository.findById(id);
    }
    
    public Optional<Cliente> buscarClientePorEmail(String email) {
        return clienteRepository.findByEmail(email);
    }
}