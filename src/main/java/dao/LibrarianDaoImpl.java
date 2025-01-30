package dao;

import LibraryEntities.Librarian;
import LibraryUtil.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * DAO implementation for the {@link Librarian} entity.
 * Provides basic CRUD operations.
 */
public class LibrarianDaoImpl implements Dao<Librarian> {

    /**
     * Creates a new {@link Librarian} record.
     * Ensures the associated User is merged (managed) before persisting.
     *
     * @param entity The Librarian to create.
     * @return The persisted Librarian with a generated ID.
     */
    @Override
    public Librarian create(Librarian entity) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            entity.setUser(em.merge(entity.getUser()));
            em.persist(entity);
            em.getTransaction().commit();
            return entity;
        } finally {
            em.close();
        }
    }

    /**
     * Finds a {@link Librarian} by its ID (Integer), casting from Long.
     *
     * @param id The Long ID to cast and find.
     * @return The Librarian if found, or null otherwise.
     */
    @Override
    public Librarian findById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Librarian.class, id.intValue());
        } finally {
            em.close();
        }
    }

    /**
     * Retrieves all {@link Librarian} records.
     *
     * @return A list of all Librarians.
     */
    @Override
    public List<Librarian> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Librarian> query =
                    em.createQuery("SELECT l FROM Librarian l", Librarian.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Updates an existing {@link Librarian} entity in the database.
     *
     * @param entity The Librarian with updated fields.
     * @return The merged Librarian entity.
     */
    @Override
    public Librarian update(Librarian entity) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Librarian merged = em.merge(entity);
            em.getTransaction().commit();
            return merged;
        } finally {
            em.close();
        }
    }

    /**
     * Deletes a {@link Librarian} by its ID (converted to int).
     *
     * @param id The Long ID of the Librarian to delete.
     */
    @Override
    public void delete(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Librarian found = em.find(Librarian.class, id.intValue());
            if (found != null) {
                em.remove(found);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}