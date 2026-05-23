package com.cjconfecciones.back.dao;

import com.cjconfecciones.back.entities.HistorialEstadoPedido;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;

import java.util.List;

public class HistorialEstadoPedidoDAO {

    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("unitPersistence");

    public List<HistorialEstadoPedido> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            Query query = em.createQuery("SELECT h FROM HistorialEstadoPedido h ORDER BY h.id");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public HistorialEstadoPedido findById(Integer id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(HistorialEstadoPedido.class, id);
        } finally {
            em.close();
        }
    }

    public List<HistorialEstadoPedido> findByPedidoId(Integer cpedido) {
        EntityManager em = emf.createEntityManager();
        try {
            Query query = em.createQuery(
                "SELECT h FROM HistorialEstadoPedido h WHERE h.cpedido = :cpedido ORDER BY h.fecha DESC"
            );
            query.setParameter("cpedido", cpedido);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public void persist(HistorialEstadoPedido historial) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(historial);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void update(HistorialEstadoPedido historial) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(historial);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void delete(Integer id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            HistorialEstadoPedido historial = em.find(HistorialEstadoPedido.class, id);
            if (historial != null) {
                em.remove(historial);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}
