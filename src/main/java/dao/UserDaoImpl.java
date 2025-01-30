package dao;

import LibraryEntities.User;
import LibraryUtil.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * DAO implementation for managing {@link User} entities.
 * Provides basic create, read, update, and delete operations.
 */
public class UserDaoImpl implements Dao<User> {

    /**
     * Persists a new {@link User} in the database.
     *
     * @param entity The User to create.
     * @return The persisted User with a generated ID.
     */
    @Override
    public User create(User entity) {
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
     * Finds a {@link User} by its integer ID, cast from Long.
     *
     * @param id The Long ID to search for.
     * @return The User if found, or null otherwise.
     */
    @Override
    public User findById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(User.class, id.intValue());
        } finally {
            em.close();
        }
    }

    /**
     * Retrieves all {@link User} entities.
     *
     * @return A list of all Users in the database.
     */
    @Override
    public List<User> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<User> q = em.createQuery("SELECT u FROM User u", User.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Updates an existing {@link User} in the database.
     *
     * @param entity The User with updated fields.
     * @return The merged User entity.
     */
    @Override
    public User update(User entity) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            User merged = em.merge(entity);
            em.getTransaction().commit();
            return merged;
        } finally {
            em.close();
        }
    }

    /**
     * Deletes a {@link User} by its ID, ensuring
     * "Cannot delete user with existing Borrowings."
     *
     * @param id The Long ID (converted to int) of the User to delete.
     * @throws IllegalStateException if the User has any Borrowings.
     */
    @Override
    public void delete(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            User found = em.find(User.class, id.intValue());
            if (found != null) {
                if (!found.getBorrowings().isEmpty()) {
                    em.getTransaction().rollback();
                    throw new IllegalStateException("Cannot delete user with existing Borrowings.");
                }
                em.remove(found);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}