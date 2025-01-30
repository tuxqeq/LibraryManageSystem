package dao;

import LibraryEntities.Publisher;
import LibraryUtil.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * DAO implementation for managing {@link Publisher} entities.
 * Offers create, read, update, and delete operations.
 */
public class PublisherDaoImpl implements Dao<Publisher> {

    /**
     * Persists a new {@link Publisher} entity.
     *
     * @param entity The Publisher to create.
     * @return The persisted Publisher with a generated ID.
     */
    @Override
    public Publisher create(Publisher entity) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(entity);
            em.getTransaction().commit();
            return entity;
        } finally {
            em.close();
        }
    }

    /**
     * Finds a {@link Publisher} by its integer ID (cast from Long).
     *
     * @param id The Long ID to search for.
     * @return The Publisher if found, or null otherwise.
     */
    @Override
    public Publisher findById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Publisher.class, id.intValue());
        } finally {
            em.close();
        }
    }

    /**
     * Retrieves all {@link Publisher} entities from the database.
     *
     * @return A list of all Publishers.
     */
    @Override
    public List<Publisher> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Publisher> q = em.createQuery("SELECT p FROM Publisher p", Publisher.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Updates an existing {@link Publisher} in the database.
     *
     * @param entity The Publisher with updated fields.
     * @return The merged Publisher entity.
     */
    @Override
    public Publisher update(Publisher entity) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Publisher merged = em.merge(entity);
            em.getTransaction().commit();
            return merged;
        } finally {
            em.close();
        }
    }

    /**
     * Deletes a {@link Publisher} by its ID, enforcing the rule:
     * "Delete only if no books reference this publisher."
     *
     * @param id The Long ID (converted to int) of the Publisher to delete.
     * @throws IllegalStateException if the Publisher is referenced by existing Books.
     */
    @Override
    public void delete(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Publisher found = em.find(Publisher.class, id.intValue());
            if (found != null) {
                if (!found.getBooks().isEmpty()) {
                    em.getTransaction().rollback();
                    throw new IllegalStateException("Cannot delete Publisher with existing Books.");
                }
                em.remove(found);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}