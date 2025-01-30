package Tests;

import LibraryEntities.Book;
import dao.BookDaoImpl;
import dao.CopyDaoImpl;
import dao.UserDaoImpl;
import dao.BorrowingDaoImpl;
import LibraryEntities.User;
import LibraryEntities.Borrowing;
import LibraryEntities.Copy;

import org.junit.jupiter.api.*;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author tuxqeq
 * Test class for performing CRUD operations and relationship checks
 * on the {@link User} entity. This class also ensures that a User
 * cannot be deleted if it has existing {@link Borrowing} records.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserTests {

    /**
     * The shared {@link EntityManagerFactory} used to manage test entities.
     */
    private static EntityManagerFactory emf;

    /**
     * The {@link UserDaoImpl} for performing User CRUD operations.
     */
    private static UserDaoImpl userDao;

    /**
     * The {@link BorrowingDaoImpl} for performing Borrowing CRUD operations.
     */
    private static BorrowingDaoImpl borrowingDao;

    /**
     * The {@link CopyDaoImpl} for handling Copy entities.
     */
    private static CopyDaoImpl copyDao;

    /**
     * The {@link BookDaoImpl} for handling Book entities.
     */
    private static BookDaoImpl bookDao;

    /**
     * Holds the ID of the test user created in {@link #testCreateUser()}.
     */
    private static Long testUserId;

    /**
     * Holds the ID of a test Borrowing, created to enforce "cannot delete user with existing borrowings."
     */
    private static Long testBorrowingId;

    /**
     * Sets up the test environment by creating the {@link EntityManagerFactory}
     * and instantiating DAO implementations before all tests run.
     */
    @BeforeAll
    static void setup() {
        emf = Persistence.createEntityManagerFactory("LibraryPU");
        userDao = new UserDaoImpl();
        borrowingDao = new BorrowingDaoImpl();
        copyDao = new CopyDaoImpl();
        bookDao = new BookDaoImpl();
    }

    /**
     * Closes the {@link EntityManagerFactory} after all tests have completed.
     */
    @AfterAll
    static void tearDown() {
        if (emf != null) {
            emf.close();
        }
    }

    /**
     * Creates a new {@link User} in the database and verifies the entity
     * is persisted successfully with a generated ID.
     */
    @Test
    @Order(1)
    void testCreateUser() {
        User user = new User();
        user.setName("Alice");
        user.setEmail("alice@example.com");
        user.setPhoneNumber("123456789");
        user.setAddress("123 Wonderland");

        User created = userDao.create(user);
        assertNotNull(created, "Created user should not be null");
        assertNotNull(created.getId(), "Created user ID should not be null");
        testUserId = created.getId().longValue();
    }

    /**
     * Reads the previously created {@link User} by ID and checks that the fields match.
     */
    @Test
    @Order(2)
    void testReadUser() {
        User found = userDao.findById(testUserId);
        assertNotNull(found, "User should exist in DB");
        assertEquals("Alice", found.getName(), "User name mismatch");
    }

    /**
     * Updates the {@link User}'s name field and verifies the change is persisted.
     */
    @Test
    @Order(3)
    void testUpdateUser() {
        User found = userDao.findById(testUserId);
        assertNotNull(found, "User not found for update");

        found.setName("Alice Updated");
        User updated = userDao.update(found);
        assertEquals("Alice Updated", updated.getName(), "User name was not updated");
    }

    /**
     * Creates a new {@link Borrowing} record for the user to demonstrate
     * that deleting a user with existing borrowings should fail.
     * Expects an {@link IllegalStateException} with a specific message.
     */
    @Test
    @Order(4)
    void testDeleteUserWithExistingBorrowingsShouldFail() {
        // 1) Make sure the User is actually in DB
        User found = userDao.findById(testUserId);
        assertNotNull(found, "User not found");

        // 2) Create/persist a Book so that the Copy can reference it
        Book dummyBook = new Book("DummyTitle", "DummyAuthor", "DummyPublisher", 2000, "1212");
        dummyBook = bookDao.create(dummyBook);

        // 3) Create/persist a Copy that references the saved Book
        Copy realCopy = new Copy(dummyBook, "Available");
        realCopy = copyDao.create(realCopy); // Now it has an ID & a valid book

        // 4) Create & persist Borrowing referencing the saved Copy
        Borrowing borrowing = new Borrowing(found, realCopy, LocalDate.now());
        Borrowing createdBorrowing = borrowingDao.create(borrowing);
        testBorrowingId = Long.valueOf(createdBorrowing.getId());

        // 5) Attempting to delete the User should fail because there's a Borrowing
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> userDao.delete(testUserId),
                "Deleting a user with borrowings should fail"
        );
        assertTrue(
                ex.getMessage().contains("Cannot delete user with existing Borrowings."),
                "Exception message should indicate relationship restriction"
        );
    }

    /**
     * Deletes the {@link Borrowing} to free up the user, then verifies
     * that deleting the user now succeeds.
     */
    @Test
    @Order(5)
    void testDeleteUser() {
        // Now remove the Borrowing so user is no longer in a relationship
        assertDoesNotThrow(() -> borrowingDao.delete(testBorrowingId));

        // Now we can safely delete the user
        assertDoesNotThrow(() -> userDao.delete(testUserId));

        User found = userDao.findById(testUserId);
        assertNull(found, "User should be null after deletion");
    }
}