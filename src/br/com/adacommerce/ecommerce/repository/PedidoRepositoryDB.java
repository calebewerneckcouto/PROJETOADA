package br.com.adacommerce.ecommerce.repository;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import br.com.adacommerce.ecommerce.model.Pedido;

public class PedidoRepositoryDB implements Repository<Pedido, Long> {

    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("ecommercePU");
    private EntityManager em = emf.createEntityManager();

    @Override
    public Pedido save(Pedido entity) {
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
    public Optional<Pedido> findById(Long id) {
        Pedido pedido = em.find(Pedido.class, id);
        return Optional.ofNullable(pedido);
    }

    @Override
    public List<Pedido> findAll() {
        return em.createQuery("FROM Pedido", Pedido.class).getResultList();
    }

    @Override
    public void delete(Long id) {
        em.getTransaction().begin();
        Pedido pedido = em.find(Pedido.class, id);
        if (pedido != null) {
            em.remove(pedido);
        }
        em.getTransaction().commit();
    }

    @Override
    public Long getNextId() {
        return null; // Auto-increment no banco
    }
}
