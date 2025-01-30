package dao;

import LibraryEntities.Copy;
import LibraryUtil.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * DAO implementation for managing {@link Copy} entities.
 * Provides methods for create, read, update, and delete.
 */
public class CopyDaoImpl implements Dao<Copy> {

    /**
     * Persists a new {@link Copy} entity, ensuring the associated Book is managed.
     *
     * @param entity The Copy to create.
     * @return The persisted Copy with a generated ID.
     */
    @Override
    public Copy create(Copy entity) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            // Ensure the Book is managed
            entity.setBook(em.merge(entity.getBook()));
            em.persist(entity);
            em.getTransaction().commit();
            return entity;
        } finally {
            em.close();
        }
    }

    /**
     * Finds a {@link Copy} by its ID.
     *
     * @param id The ID of the Copy to find.
     * @return The Copy if found, or null if not found.
     * @throws IllegalArgumentException if the provided ID is null.
     */
    @Override
    public Copy findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Copy.class, id);
        } finally {
            em.close();
        }
    }

    /**
     * Retrieves all {@link Copy} entities.
     *
     * @return A list of all Copies in the database.
     */
    @Override
    public List<Copy> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Copy> q = em.createQuery("SELECT c FROM Copy c", Copy.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Updates an existing {@link Copy} entity in the database.
     *
     * @param entity The Copy to update.
     * @return The merged (updated) Copy.
     * @throws IllegalArgumentException if the Copy or its ID is null.
     */
    @Override
    public Copy update(Copy entity) {
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException("Copy or ID cannot be null");
        }
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Copy merged = em.merge(entity);
            em.getTransaction().commit();
            return merged;
        } finally {
            em.close();
        }
    }

    /**
     * Deletes a {@link Copy} by its ID.
     * Removes the Copy from its parent Book's list of copies.
     *
     * @param id The ID of the Copy to delete.
     * @throws IllegalArgumentException if the provided ID is null.
     */
    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Copy found = em.find(Copy.class, id);
            if (found != null) {
                found.getBook().getCopies().remove(found);
                em.remove(found);
                em.flush();
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}