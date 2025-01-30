package Tests;

import dao.PublisherDaoImpl;
import dao.BookDaoImpl;
import LibraryEntities.Publisher;
import LibraryEntities.Book;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author tuxqeq
 * Test class for {@link Publisher} entity operations and ensuring the
 * "delete only if no books reference this publisher" rule.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PublisherTests {

    private static EntityManagerFactory emf;
    private static PublisherDaoImpl publisherDao;
    private static BookDaoImpl bookDao;
    private static Long testBookId;
    private static Integer testPublisherId;

    @BeforeAll
    static void setup() {
        emf = Persistence.createEntityManagerFactory("LibraryPU");
        publisherDao = new PublisherDaoImpl();
        bookDao = new BookDaoImpl();
    }

    @AfterAll
    static void teardown() {
        if (emf != null) {
            emf.close();
        }
    }

    /**
     * Creates a Publisher and verifies it is persisted.
     */
    @Test
    @Order(1)
    void testCreatePublisher() {
        Publisher publisher = new Publisher("TestPublisher", "123 Main St", "555-1234");
        Publisher created = publisherDao.create(publisher);
        assertNotNull(created.getId(), "Publisher ID should be generated");
        testPublisherId = created.getId();
    }

    /**
     * Reads the Publisher and verifies its fields.
     */
    @Test
    @Order(2)
    void testReadPublisher() {
        Publisher found = publisherDao.findById(testPublisherId.longValue());
        assertNotNull(found, "Publisher not found");
        assertEquals("TestPublisher", found.getName(), "Publisher name mismatch");
    }

    /**
     * Updates the Publisher's name.
     */
    @Test
    @Order(3)
    void testUpdatePublisher() {
        Publisher found = publisherDao.findById(testPublisherId.longValue());
        found.setName("UpdatedPublisher");
        Publisher updated = publisherDao.update(found);
        assertEquals("UpdatedPublisher", updated.getName(), "Publisher name was not updated");
    }

    /**
     * Creates a Book referencing the Publisher, then verifies the association.
     */
    @Test
    @Order(4)
    void testLinkBookToPublisher() {
        Publisher publisher = publisherDao.findById(testPublisherId.longValue());
        Book book = new Book("PubTestBook", "Author X", publisher.getName(), 2022, "323");
        book.setPublisher(publisher);
        Book createdBook = bookDao.create(book);
        assertNotNull(createdBook.getId(), "Book ID should be generated");
        testBookId = createdBook.getId();

        Publisher updatedPub = publisherDao.findById(testPublisherId.longValue());
        assertFalse(updatedPub.getBooks().isEmpty(), "Publisher should have at least one Book");
    }

    /**
     * Attempts to delete a Publisher that is still referenced by at least one Book,
     * expecting an IllegalStateException.
     */
    @Test
    @Order(5)
    void testDeletePublisherWithExistingBooksShouldFail() {
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> publisherDao.delete(testPublisherId.longValue()),
                "Should throw if publisher is still referenced by a book"
        );
        assertTrue(ex.getMessage().contains("Cannot delete Publisher with existing Books."),
                "Exception message should indicate relationship restriction");
    }

    /**
     * After removing the referencing Book, the Publisher can be deleted successfully.
     */
    @Test
    @Order(6)
    void testDeletePublisherAfterRemovingBooks() {
        bookDao.delete(testBookId);

        assertDoesNotThrow(() -> publisherDao.delete(testPublisherId.longValue()),
                "Publisher should be deleted successfully when no books exist");

        Publisher found = publisherDao.findById(testPublisherId.longValue());
        assertNull(found, "Publisher should be null after deletion");
    }
}