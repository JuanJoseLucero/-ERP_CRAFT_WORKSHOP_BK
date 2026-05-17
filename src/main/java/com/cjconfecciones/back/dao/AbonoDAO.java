package com.cjconfecciones.back.dao;

import com.cjconfecciones.back.entities.Abono;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;

import java.math.BigDecimal;
import java.util.List;

public class AbonoDAO {

    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("unitPersistence");

    public List<Abono> findByCabecera(Integer ccabecera) {
        EntityManager em = emf.createEntityManager();
        try {
            Query query = em.createQuery("SELECT a FROM Abono a WHERE a.ccabecera = :ccabecera");
            query.setParameter("ccabecera", ccabecera);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public BigDecimal getTotalAcumulado(Integer ccabecera) {
        EntityManager em = emf.createEntityManager();
        try {
            Query query = em.createQuery("SELECT SUM(a.valor) FROM Abono a WHERE a.ccabecera = :ccabecera");
            query.setParameter("ccabecera", ccabecera);
            BigDecimal result = (BigDecimal) query.getSingleResult();
            return result != null ? result : BigDecimal.ZERO;
        } finally {
            em.close();
        }
    }

    public void persist(Abono abono) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(abono);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}