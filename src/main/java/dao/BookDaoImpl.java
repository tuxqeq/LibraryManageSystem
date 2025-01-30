package dao;

import LibraryEntities.Book;
import LibraryUtil.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * DAO implementation for managing {@link Book} entities.
 * Provides methods for creating, finding, updating, and deleting books,
 * as well as queries related to availability and copies.
 */
public class BookDaoImpl implements Dao<Book> {

    /**
     * Persists a new {@link Book} entity in the database.
     *
     * @param entity The Book to be created (persisted).
     * @return The persisted Book with a generated ID.
     * @throws IllegalArgumentException if the Book entity is null.
     */
    @Override
    public Book create(Book entity) {
        if (entity == null) throw new IllegalArgumentException("Book cannot be null");
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
     * Finds a {@link Book} by its unique ID.
     *
     * @param id The ID of the Book to retrieve.
     * @return The Book with the specified ID, or null if not found.
     * @throws IllegalArgumentException if the ID is null.
     */
    @Override
    public Book findById(Long id) {
        if (id == null) throw new IllegalArgumentException("ID cannot be null");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Book.class, id);
        } finally {
            em.close();
        }
    }

    /**
     * Retrieves all {@link Book} entities from the database.
     *
     * @return A list of all persisted Books.
     */
    @Override
    public List<Book> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Book> q = em.createQuery("SELECT b FROM Book b", Book.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Updates an existing {@link Book} in the database.
     *
     * @param entity The Book entity with updated fields.
     * @return The merged (updated) Book entity.
     * @throws IllegalArgumentException if the Book or its ID is null.
     */
    @Override
    public Book update(Book entity) {
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException("Book or ID cannot be null");
        }
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Book merged = em.merge(entity);
            em.getTransaction().commit();
            return merged;
        } finally {
            em.close();
        }
    }

    /**
     * Deletes a {@link Book} from the database by its ID.
     * <p>This enforces the rule: "Cannot delete book with borrowings or more than one copy."</p>
     *
     * @param id The ID of the Book to delete.
     * @throws IllegalArgumentException if the ID is null.
     * @throws IllegalStateException    if the Book is currently borrowed or has copies.
     */
    @Override
    public void delete(Long id) {
        if (id == null) throw new IllegalArgumentException("ID cannot be null");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Book found = em.find(Book.class, id);
            if (found != null) {
                // Check if the book has any borrowings
                TypedQuery<Long> borrowingCountQuery = em.createQuery(
                        "SELECT COUNT(b) FROM Borrowing b WHERE b.copy.book.id = :bookId", Long.class);
                borrowingCountQuery.setParameter("bookId", id);
                Long borrowingCount = borrowingCountQuery.getSingleResult();

                // Check the number of copies
                int copyCount = found.getCopies().size();

                if (borrowingCount > 0 || copyCount > 0) {
                    throw new IllegalStateException("Cannot delete book with borrowings or more than one copy.");
                } else {
                    em.remove(found);
                }
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    /**
     * Retrieves a list of {@link Book} objects that have at least one "Available" copy.
     *
     * @return A list of Books with one or more available copies.
     */
    public List<Book> findAvailableTitles() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Book> query = em.createQuery(
                    "SELECT DISTINCT b FROM Book b JOIN b.copies c WHERE c.status = 'Available'",
                    Book.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Retrieves all Books and eagerly fetches their copies.
     *
     * @return A list of all Books with their associated copies loaded.
     */
    public List<Book> findAllWithCopies() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT DISTINCT b FROM Book b LEFT JOIN FETCH b.copies", Book.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}