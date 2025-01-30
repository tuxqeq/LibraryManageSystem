package Tests;

import dao.UserDaoImpl;
import dao.LibrarianDaoImpl;
import LibraryEntities.Librarian;
import LibraryEntities.User;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author tuxqeq
 * Test class for {@link Librarian} entity and its 1-to-1 link with {@link User}.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LibrarianTests {

    private static EntityManagerFactory emf;
    private static UserDaoImpl userDao;
    private static LibrarianDaoImpl librarianDao;

    private static Long testUserId;
    private static Integer testLibrarianId;

    @BeforeAll
    static void setup() {
        emf = Persistence.createEntityManagerFactory("LibraryPU");
        userDao = new UserDaoImpl();
        librarianDao = new LibrarianDaoImpl();
    }

    @AfterAll
    static void teardown() {
        if (emf != null) {
            emf.close();
        }
    }

    /**
     * Creates a User and a Librarian referencing that user.
     */
    @Test
    @Order(1)
    void testCreateLibrarian() {
        User user = new User();
        user.setName("Librarian Lucy");
        user.setEmail("lucy@library.org");
        user.setPhoneNumber("1234");
        user.setAddress("Librarian Street");
        User createdUser = userDao.create(user);
        assertNotNull(createdUser.getId());
        testUserId = createdUser.getId().longValue();

        Librarian librarian = new Librarian(createdUser, LocalDate.now(), "Head Librarian");
        Librarian createdLibrarian = librarianDao.create(librarian);
        assertNotNull(createdLibrarian.getId(), "Librarian ID should not be null");
        testLibrarianId = createdLibrarian.getId();
    }

    /**
     * Reads the Librarian and verifies its fields and associated user.
     */
    @Test
    @Order(2)
    void testReadLibrarian() {
        Librarian found = librarianDao.findById(Long.valueOf(testLibrarianId));
        assertNotNull(found, "Librarian not found");
        assertEquals("Head Librarian", found.getPosition(), "Position mismatch");
        assertNotNull(found.getUser(), "Librarian should reference a User");
        assertEquals(testUserId.intValue(), found.getUser().getId(),
                "Referenced user ID mismatch");
    }

    /**
     * Updates the Librarian's position field.
     */
    @Test
    @Order(3)
    void testUpdateLibrarian() {
        Librarian found = librarianDao.findById(Long.valueOf(testLibrarianId));
        found.setPosition("Assistant Manager");
        Librarian updated = librarianDao.update(found);
        assertEquals("Assistant Manager", updated.getPosition(), "Position not updated");
    }

    /**
     * Deletes the Librarian, verifying removal of the 1-to-1 record.
     */
    @Test
    @Order(4)
    void testDeleteLibrarian() {
        assertDoesNotThrow(() -> librarianDao.delete(Long.valueOf(testLibrarianId)));
        Librarian found = librarianDao.findById(Long.valueOf(testLibrarianId));
        assertNull(found, "Librarian should be null after deletion");
    }

    /**
     * Checks that the underlying user still exists after the Librarian is removed.
     */
    @Test
    @Order(5)
    void testUserLibrarianOneToOneIntegration() {
        User user = userDao.findById(testUserId);
        assertNotNull(user, "User should still exist");
    }
}