package com.cjconfecciones.back.dao;

import com.cjconfecciones.back.entities.EstadoPedido;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;

import java.util.List;

public class EstadoPedidoDAO {

    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("unitPersistence");

    public List<EstadoPedido> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            Query query = em.createQuery("SELECT e FROM EstadoPedido e ORDER BY e.id");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public EstadoPedido findById(Integer id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(EstadoPedido.class, id);
        } finally {
            em.close();
        }
    }

    public void persist(EstadoPedido estadoPedido) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(estadoPedido);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void update(EstadoPedido estadoPedido) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(estadoPedido);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void delete(Integer id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            EstadoPedido estadoPedido = em.find(EstadoPedido.class, id);
            if (estadoPedido != null) {
                em.remove(estadoPedido);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}
