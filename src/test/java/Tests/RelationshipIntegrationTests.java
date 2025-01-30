package Tests;

import dao.UserDaoImpl;
import dao.BorrowingDaoImpl;
import dao.BookDaoImpl;
import dao.CopyDaoImpl;
import dao.LibrarianDaoImpl;
import dao.PublisherDaoImpl;

import LibraryEntities.User;
import LibraryEntities.Borrowing;
import LibraryEntities.Book;
import LibraryEntities.Copy;
import LibraryEntities.Librarian;
import LibraryEntities.Publisher;

import org.junit.jupiter.api.*;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author tuxqeq
 * Integration tests for verifying relationships among entities:
 * <ul>
 *     <li>One-to-Many: Users ↔ Borrowings</li>
 *     <li>One-to-Many: Books ↔ Copies</li>
 *     <li>One-to-One: Users ↔ Librarians</li>
 *     <li>Many-to-One: Publishers ↔ Books</li>
 * </ul>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RelationshipIntegrationTests {

    private static EntityManagerFactory emf;

    private static UserDaoImpl userDao;
    private static BorrowingDaoImpl borrowingDao;
    private static BookDaoImpl bookDao;
    private static CopyDaoImpl copyDao;
    private static LibrarianDaoImpl librarianDao;
    private static PublisherDaoImpl publisherDao;

    /**
     * Sets up the EntityManagerFactory and all DAOs before all tests.
     */
    @BeforeAll
    static void setup() {
        emf = Persistence.createEntityManagerFactory("LibraryPU");
        userDao = new UserDaoImpl();
        borrowingDao = new BorrowingDaoImpl();
        bookDao = new BookDaoImpl();
        copyDao = new CopyDaoImpl();
        librarianDao = new LibrarianDaoImpl();
        publisherDao = new PublisherDaoImpl();
    }

    /**
     * Closes the EntityManagerFactory after all tests complete.
     */
    @AfterAll
    static void tearDown() {
        if (emf != null) {
            emf.close();
        }
    }

    /**
     * Tests the One-to-Many relationship between User and Borrowings.
     * Verifies a User can have multiple Borrowing records.
     */
    @Test
    @Order(1)
    void testOneToMany_Users_Borrowings() {
        User user = new User();
        user.setName("UserForBorrowings");
        user.setEmail("borrower@example.com");
        user.setPhoneNumber("555-1111");
        user.setAddress("123 Borrow St");
        user = userDao.create(user);
        Long userId = user.getId().longValue();

        Book bookA = new Book("TitleA", "AuthorA", "PublisherA", 2020, "123");
        bookA = bookDao.create(bookA);
        Copy copyA = new Copy(bookA, "Available");
        copyA = copyDao.create(copyA);

        Book bookB = new Book("TitleB", "AuthorB", "PublisherB", 2021, "1234");
        bookB = bookDao.create(bookB);
        Copy copyB = new Copy(bookB, "Available");
        copyB = copyDao.create(copyB);

        Borrowing b1 = new Borrowing(user, copyA, LocalDate.now());
        Borrowing b2 = new Borrowing(user, copyB, LocalDate.now());
        borrowingDao.create(b1);
        borrowingDao.create(b2);

        User updatedUser = userDao.findById(userId);
        assertNotNull(updatedUser, "User should still exist");
        assertEquals(2, updatedUser.getBorrowings().size(), "User should have exactly 2 borrowings");

        for (Borrowing br : updatedUser.getBorrowings()) {
            assertEquals(userId.intValue(), br.getUser().getId(),
                    "Borrowing should reference the same user");
        }
    }

    /**
     * Tests the One-to-Many relationship between Book and Copies.
     * Verifies a single Book can have multiple Copies.
     */
    @Test
    @Order(2)
    void testOneToMany_Books_Copies() {
        Book book = new Book("OneToManyBook", "TestAuthor", "TestPub", 2022, "12");
        book = bookDao.create(book);
        Long bookId = book.getId();

        Copy copy1 = new Copy(book, "Available");
        Copy copy2 = new Copy(book, "Available");
        copy1 = copyDao.create(copy1);
        copy2 = copyDao.create(copy2);

        Book updatedBook = bookDao.findById(bookId);
        assertNotNull(updatedBook, "Book should exist");
        assertEquals(2, updatedBook.getCopies().size(),
                "There should be 2 copies for this Book");
    }

    /**
     * Tests the One-to-One relationship between User and Librarian.
     * Verifies that creating a User plus a Librarian record links them 1-to-1.
     */
    @Test
    @Order(3)
    void testOneToOne_Users_Librarians() {
        User user = new User();
        user.setName("LibrarianUser");
        user.setEmail("librarian@example.com");
        user.setPhoneNumber("777-1234");
        user.setAddress("Lib St 7");
        user = userDao.create(user);
        Long userId = user.getId().longValue();

        Librarian librarian = new Librarian(user, LocalDate.now(), "Head Librarian");
        librarian = librarianDao.create(librarian);
        Integer librarianId = librarian.getId();

        Librarian foundLibrarian = librarianDao.findById(librarianId.longValue());
        assertNotNull(foundLibrarian, "Librarian should exist");
        assertEquals("Head Librarian", foundLibrarian.getPosition(), "Position mismatch");
        assertNotNull(foundLibrarian.getUser(), "Should reference a user");
        assertEquals(userId.intValue(), foundLibrarian.getUser().getId(),
                "User ID mismatch in One-to-One relationship");
    }

    /**
     * Tests the Many-to-One relationship between Publisher and Book.
     * Verifies each Book can only reference a single Publisher,
     * and that changing a Book's publisher is reflected correctly.
     */
    @Test
    @Order(4)
    void testManyToOne_Publishers_Books() {
        Publisher publisher = new Publisher("RelTestPublisher", "123 Main", "555-5678");
        publisher = publisherDao.create(publisher);
        Integer pubId = publisher.getId();

        Book bk1 = new Book("PublisherTest1", "PubAuthor1", null, 2000, "001");
        bk1.setPublisher(publisher);
        bk1 = bookDao.create(bk1);

        Book bk2 = new Book("PublisherTest2", "PubAuthor2", null, 2001, "002");
        bk2.setPublisher(publisher);
        bk2 = bookDao.create(bk2);

        Publisher updatedPub = publisherDao.findById(pubId.longValue());
        assertNotNull(updatedPub, "Publisher should exist");
        assertEquals(2, updatedPub.getBooks().size(),
                "Publisher should have 2 books referencing it");

        updatedPub.getBooks().forEach(book -> {
            assertEquals(pubId.intValue(), book.getPublisher().getId(),
                    "Book should reference the correct publisher");
        });

        Publisher newPub = new Publisher("NewPub", "987 Another St", "000-1111");
        newPub = publisherDao.create(newPub);

        bk2.setPublisher(newPub);
        bookDao.update(bk2);

        Book reloadedBk2 = bookDao.findById(bk2.getId());
        assertEquals("NewPub", reloadedBk2.getPublisher().getName(),
                "Book’s publisher should have changed to 'NewPub'");

        Publisher reloadedOldPub = publisherDao.findById(pubId.longValue());
        assertEquals(1, reloadedOldPub.getBooks().size(),
                "Old publisher should now only have 1 book associated");
    }
}