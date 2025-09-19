package br.com.adacommerce.ecommerce.repository;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import br.com.adacommerce.ecommerce.model.Cliente;

public class ClienteRepositoryDB implements Repository<Cliente, Long> {

    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("ecommercePU");
    private EntityManager em = emf.createEntityManager();

    @Override
    public Cliente save(Cliente entity) {
        em.getTransaction().begin();
        if (entity.getId() == null) {
            em.persist(entity);
        } else {
            entity = em.merge(entity);
        }
        em.getTransaction().commit();
        return entity;
    }

    @Override
    public Optional<Cliente> findById(Long id) {
        Cliente cliente = em.find(Cliente.class, id);
        return Optional.ofNullable(cliente);
    }

    @Override
    public List<Cliente> findAll() {
        return em.createQuery("FROM Cliente", Cliente.class).getResultList();
    }

    @Override
    public void delete(Long id) {
        em.getTransaction().begin();
        Cliente cliente = em.find(Cliente.class, id);
        if (cliente != null) {
            em.remove(cliente);
        }
        em.getTransaction().commit();
    }

    @Override
    public Long getNextId() {
       
        return null;
    }
}
