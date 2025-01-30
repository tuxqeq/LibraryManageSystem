package Tests;

import dao.BorrowingDaoImpl;
import dao.UserDaoImpl;
import dao.CopyDaoImpl;
import dao.BookDaoImpl;
import LibraryEntities.Borrowing;
import LibraryEntities.User;
import LibraryEntities.Copy;
import LibraryEntities.Book;

import org.junit.jupiter.api.*;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author tuxqeq
 * Test class for {@link Borrowing} entity CRUD operations
 * and checks around returning copies.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BorrowingTests {

    private static EntityManagerFactory emf;
    private static BorrowingDaoImpl borrowingDao;
    private static UserDaoImpl userDao;
    private static CopyDaoImpl copyDao;
    private static BookDaoImpl bookDao;

    private static Long testBorrowingId;
    private static Long testUserId;
    private static Long testCopyId;

    @BeforeAll
    static void setup() {
        emf = Persistence.createEntityManagerFactory("LibraryPU");
        borrowingDao = new BorrowingDaoImpl();
        userDao = new UserDaoImpl();
        copyDao = new CopyDaoImpl();
        bookDao = new BookDaoImpl();
    }

    @AfterAll
    static void tearDown() {
        if (emf != null) {
            emf.close();
        }
    }

    /**
     * Creates a Borrowing by first creating a User, Book, and Copy.
     */
    @Test
    @Order(1)
    void testCreateBorrowing() {
        User user = new User("Bob", "bob@example.com", "999", "999 Street");
        User createdUser = userDao.create(user);
        testUserId = createdUser.getId().longValue();

        Book dummyBook = new Book("DummyTitle", "DummyAuthor", "DummyPublisher", 2000, "12123");
        dummyBook = bookDao.create(dummyBook);

        Copy copy = new Copy(dummyBook, "Available");
        copy = copyDao.create(copy);
        testCopyId = copy.getId();

        Borrowing borrowing = new Borrowing(createdUser, copy, LocalDate.now());
        Borrowing createdBorrowing = borrowingDao.create(borrowing);
        assertNotNull(createdBorrowing, "Borrowing should be created");
        assertNotNull(createdBorrowing.getId(), "Borrowing ID should not be null");

        testBorrowingId = createdBorrowing.getId().longValue();
    }

    /**
     * Reads the previously created Borrowing and verifies it exists.
     */
    @Test
    @Order(2)
    void testReadBorrowing() {
        Borrowing found = borrowingDao.findById(testBorrowingId);
        assertNotNull(found, "Borrowing not found");
        assertEquals(testBorrowingId.intValue(), found.getId(), "Borrowing ID mismatch");
    }

    /**
     * Updates the borrow date and verifies the change is persisted.
     */
    @Test
    @Order(3)
    void testUpdateBorrowing() {
        Borrowing found = borrowingDao.findById(testBorrowingId);
        assertNotNull(found, "Borrowing not found for update");

        LocalDate newDate = LocalDate.now().plusDays(1);
        found.setBorrowDate(newDate);
        Borrowing updated = borrowingDao.update(found);
        assertEquals(newDate, updated.getBorrowDate(), "Borrow date was not updated");
    }

    /**
     * Uses {@code returnCopy} to mark the Borrowing as returned
     * and confirm the Copy is set to "Available".
     */
    @Test
    @Order(4)
    void testReturnCopy() {
        borrowingDao.returnCopy(testBorrowingId);

        Borrowing found = borrowingDao.findById(testBorrowingId);
        assertNotNull(found.getReturnDate(), "Return date should be set now");

        Copy associatedCopy = copyDao.findById(found.getCopy().getId());
        assertEquals("Available", associatedCopy.getStatus(),
                "Copy status should be 'Available' after return");
    }

    /**
     * Deletes the Borrowing and confirms it is removed from the DB.
     */
    @Test
    @Order(5)
    void testDeleteBorrowing() {
        assertDoesNotThrow(() -> borrowingDao.delete(testBorrowingId));
        Borrowing found = borrowingDao.findById(testBorrowingId);
        assertNull(found, "Borrowing should be null after deletion");
    }

    /**
     * Demonstrates that one User can have multiple Borrowings.
     */
    @Test
    @Order(6)
    void testUserBorrowingsIntegration() {
        User user = userDao.findById(testUserId);
        assertNotNull(user, "User should still exist");

        Book book = new Book("Title", "Author", "Publisher", 2000, "ISBN");
        book = bookDao.create(book);
        Book book2 = new Book("Title1", "Author1", "Publisher1", 2000, "ISBN1");
        book2 = bookDao.create(book2);

        Copy copy1 = new Copy(book, "Available");
        copy1 = copyDao.create(copy1);

        Copy copy2 = new Copy(book2, "Available");
        copy2 = copyDao.create(copy2);

        Borrowing b1 = new Borrowing(user, copy1, LocalDate.now());
        Borrowing b2 = new Borrowing(user, copy2, LocalDate.now());

        borrowingDao.create(b1);
        borrowingDao.create(b2);

        user = userDao.findById(testUserId);
        List<Borrowing> borrowings = user.getBorrowings();
        assertEquals(2, borrowings.size(), "User should have two borrowing records");
    }
}