package Tests;

import dao.CopyDaoImpl;
import dao.BookDaoImpl;
import LibraryEntities.Copy;
import LibraryEntities.Book;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author tuxqeq
 * Test class for {@link Copy} entity CRUD operations,
 * including the Book ↔ Copies relationship.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CopiesTests {

    private static EntityManagerFactory emf;
    private static CopyDaoImpl copyDao;
    private static BookDaoImpl bookDao;
    private static Long testCopyId;
    private static Long testBookId;

    @BeforeAll
    static void setup() {
        emf = Persistence.createEntityManagerFactory("LibraryPU");
        copyDao = new CopyDaoImpl();
        bookDao = new BookDaoImpl();
    }

    @AfterAll
    static void teardown() {
        if (emf != null) {
            emf.close();
        }
    }

    /**
     * Creates a new Book and a single Copy referencing it.
     */
    @Test
    @Order(1)
    void testCreateCopy() {
        Book book = new Book("CopyTestBook", "Some Author", "Test Pub", 2020, "121");
        Book createdBook = bookDao.create(book);
        testBookId = createdBook.getId();

        Copy copy = new Copy(createdBook, "Available");
        Copy createdCopy = copyDao.create(copy);

        assertNotNull(createdCopy.getId(), "Copy ID should be generated");
        testCopyId = createdCopy.getId();
    }

    /**
     * Reads the previously created Copy and verifies its fields.
     */
    @Test
    @Order(2)
    void testReadCopy() {
        Copy found = copyDao.findById(testCopyId);
        assertNotNull(found, "Copy not found");
        assertEquals("Available", found.getStatus(), "Copy status mismatch");
        assertNotNull(found.getBook(), "Copy should reference a Book");
    }

    /**
     * Updates the Copy's status and verifies persistence.
     */
    @Test
    @Order(3)
    void testUpdateCopy() {
        Copy found = copyDao.findById(testCopyId);
        found.setStatus("Borrowed");
        Copy updated = copyDao.update(found);
        assertEquals("Borrowed", updated.getStatus(), "Copy status was not updated");
    }

    /**
     * Deletes the Copy and ensures it is removed from the database.
     */
    @Test
    @Order(4)
    void testDeleteCopy() {
        assertDoesNotThrow(() -> copyDao.delete(testCopyId),
                "Copy should be deleted successfully if no Borrowing references it");

        Copy found = copyDao.findById(testCopyId);
        assertNull(found, "Copy should be null after deletion");
    }

    /**
     * Demonstrates that a single Book can have multiple Copies.
     */
    @Test
    @Order(5)
    void testBookCopiesIntegration() {
        Book foundBook = bookDao.findById(testBookId);
        assertNotNull(foundBook, "Book should still be in the DB");

        Copy c1 = new Copy(foundBook, "Available");
        Copy c2 = new Copy(foundBook, "Available");
        c1 = copyDao.create(c1);
        c2 = copyDao.create(c2);

        Book updatedBook = bookDao.findById(testBookId);
        assertEquals(2, updatedBook.getCopies().size(),
                "Book should have multiple copies in the relationship");
    }
}