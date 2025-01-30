package dao;

import LibraryEntities.Borrowing;
import LibraryEntities.Copy;
import LibraryUtil.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * @author tuxqeq
 * DAO implementation for managing {@link Borrowing} entities.
 * Supports create, read, update, delete, and returning copies.
 */
public class BorrowingDaoImpl implements Dao<Borrowing> {

    /**
     * Persists a new {@link Borrowing} entity in the database.
     *
     * @param entity The Borrowing to be created.
     * @return The persisted Borrowing with a generated ID.
     */
    @Override
    public Borrowing create(Borrowing entity) {
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
     * Finds a {@link Borrowing} by its ID (stored as an Integer).
     * The Long ID is cast to int.
     *
     * @param id The Long ID to find.
     * @return The Borrowing if found, otherwise null.
     */
    @Override
    public Borrowing findById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Borrowing borrowing = em.find(Borrowing.class, id.intValue());
            System.out.println("Retrieved Borrowing: " + borrowing);
            return borrowing;
        } finally {
            em.close();
        }
    }

    /**
     * Retrieves all {@link Borrowing} entities from the database.
     *
     * @return A list of all Borrowings.
     */
    @Override
    public List<Borrowing> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Borrowing> q = em.createQuery("SELECT b FROM Borrowing b", Borrowing.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Updates an existing {@link Borrowing} in the database.
     *
     * @param entity The Borrowing with updated fields.
     * @return The merged (updated) Borrowing entity.
     */
    @Override
    public Borrowing update(Borrowing entity) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Borrowing merged = em.merge(entity);
            em.getTransaction().commit();
            return merged;
        } finally {
            em.close();
        }
    }

    /**
     * Deletes a {@link Borrowing} by its ID. Also sets the associated Copy
     * status to "Available" before removal.
     *
     * @param borrowingId The ID of the Borrowing to delete.
     */
    @Override
    public void delete(Long borrowingId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Borrowing borrowing = em.find(Borrowing.class, borrowingId.intValue());
            if (borrowing != null) {
                Copy copy = borrowing.getCopy();
                if (copy != null) {
                    copy.setStatus("Available");
                    em.merge(copy);
                }
                em.remove(borrowing);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    /**
     * Marks a borrowed copy as returned by setting the return date to now
     * and changing its status to "Available".
     *
     * @param borrowingId The ID of the Borrowing whose Copy is being returned.
     */
    public void returnCopy(Long borrowingId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Borrowing borrowing = em.find(Borrowing.class, borrowingId.intValue());
            if (borrowing != null) {
                borrowing.setReturnDate(LocalDate.now());
                borrowing.getCopy().setStatus("Available");
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}