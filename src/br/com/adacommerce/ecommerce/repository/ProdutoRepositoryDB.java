package br.com.adacommerce.ecommerce.repository;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import br.com.adacommerce.ecommerce.model.Produto;

public class ProdutoRepositoryDB implements Repository<Produto, Long> {

    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("ecommercePU");
    private EntityManager em = emf.createEntityManager();

    @Override
    public Produto save(Produto entity) {
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
    public Optional<Produto> findById(Long id) {
        Produto produto = em.find(Produto.class, id);
        return Optional.ofNullable(produto);
    }

    @Override
    public List<Produto> findAll() {
        return em.createQuery("FROM Produto", Produto.class).getResultList();
    }

    @Override
    public void delete(Long id) {
        em.getTransaction().begin();
        Produto produto = em.find(Produto.class, id);
        if (produto != null) {
            em.remove(produto);
        }
        em.getTransaction().commit();
    }

    @Override
    public Long getNextId() {
        return null; // Auto-increment no banco
    }
}
