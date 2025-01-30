package Tests;

import dao.BookDaoImpl;
import dao.CopyDaoImpl;
import LibraryEntities.Book;
import LibraryEntities.Copy;
import org.junit.jupiter.api.*;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link Book} entity CRUD operations and associated copies.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookTests {

    private static EntityManagerFactory emf;
    private static BookDaoImpl bookDao;
    private static CopyDaoImpl copyDao;
    private static Long testBookId;
    private static Long copy1Id;
    private static Long copy2Id;

    /**
     * Initializes the EntityManagerFactory and DAOs before all tests.
     */
    @BeforeAll
    static void setup() {
        emf = Persistence.createEntityManagerFactory("LibraryPU");
        bookDao = new BookDaoImpl();
        copyDao = new CopyDaoImpl();
    }

    /**
     * Closes the EntityManagerFactory after all tests are run.
     */
    @AfterAll
    static void tearDown() {
        if (emf != null) {
            emf.close();
        }
    }

    /**
     * Creates a new Book and associated Copies, then verifies they are persisted.
     */
    @Test
    @Order(1)
    void testCreateBookWithCopies() {
        Book book = new Book("Test Title", "Test Author", "Test Publisher", 2021, "123");
        Book createdBook = bookDao.create(book);

        assertNotNull(createdBook);
        assertNotNull(createdBook.getId());
        testBookId = createdBook.getId();

        // Create and persist copies
        Copy copy1 = new Copy(createdBook, "Available");
        Copy copy2 = new Copy(createdBook, "Available");
        copy1 = copyDao.create(copy1);
        copy2 = copyDao.create(copy2);

        copy1Id = copy1.getId();
        copy2Id = copy2.getId();

        Book fetchedBook = bookDao.findById(testBookId);
        assertNotNull(fetchedBook);
        assertEquals(2, fetchedBook.getCopies().size(), "Copies count mismatch");
    }

    /**
     * Reads the previously created Book, verifying fields match expectation.
     */
    @Test
    @Order(2)
    void testReadBook() {
        Book foundBook = bookDao.findById(testBookId);
        assertNotNull(foundBook, "Book not found");
        assertEquals("Test Title", foundBook.getTitle(), "Book title mismatch");
        assertEquals(2, foundBook.getCopies().size(), "Copies count mismatch");
    }

    /**
     * Updates the Book's title and verifies the change is persisted.
     */
    @Test
    @Order(3)
    void testUpdateBook() {
        Book foundBook = bookDao.findById(testBookId);
        assertNotNull(foundBook, "Book not found");

        foundBook.setTitle("Updated Title");
        Book updatedBook = bookDao.update(foundBook);
        assertEquals("Updated Title", updatedBook.getTitle(), "Book title not updated");
    }

    /**
     * Tests the custom query to find all Books with an available copy.
     */
    @Test
    @Order(4)
    void testFindAvailableTitles() {
        var availableBooks = bookDao.findAvailableTitles();
        assertNotNull(availableBooks, "Available books list should not be null");
        assertTrue(availableBooks.stream().anyMatch(b -> b.getId().equals(testBookId)),
                "Test book should be in available titles");
    }

    /**
     * Tests the method to retrieve all Books with eagerly fetched copies.
     */
    @Test
    @Order(5)
    void testFindAllWithCopies() {
        var booksWithCopies = bookDao.findAllWithCopies();
        assertNotNull(booksWithCopies, "Books with copies list should not be null");
        assertTrue(booksWithCopies.stream().anyMatch(b -> b.getId().equals(testBookId)),
                "Test book should be in books with copies");
    }

    /**
     * Attempts to delete a Book that still has multiple copies or borrowings,
     * expecting an exception.
     */
    @Test
    @Order(6)
    void testDeleteBookWithExistingCopiesShouldFail() {
        Long bookIdWithBorrowingsAndMultipleCopies = 1L; // Example

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            bookDao.delete(bookIdWithBorrowingsAndMultipleCopies);
        });
        assertEquals("Cannot delete book with borrowings or more than one copy.", exception.getMessage());
    }

    /**
     * Properly deletes the Book by first removing its copies, then deleting the Book.
     */
    @Test
    @Order(7)
    void testDeleteBook() {
        copyDao.delete(copy1Id);
        copyDao.delete(copy2Id);

        assertDoesNotThrow(() -> bookDao.delete(testBookId),
                "Book should be deleted successfully when no copies exist");

        Book foundBook = bookDao.findById(testBookId);
        assertNull(foundBook, "Book should be null after deletion");
    }
}