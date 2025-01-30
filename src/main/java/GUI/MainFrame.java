package GUI;

import LibraryEntities.Book;
import LibraryEntities.Borrowing;
import LibraryEntities.Copy;
import LibraryEntities.User;
import LibraryEntities.Publisher;

import dao.BookDaoImpl;
import dao.BorrowingDaoImpl;
import dao.CopyDaoImpl;
import dao.Dao;
import dao.PublisherDaoImpl;
import dao.UserDaoImpl;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

/**
 * The main application frame for the Library System, which can be shown either
 * in librarian mode or in regular user mode.
 */
public class MainFrame extends JFrame {

    /**
     * DAO for managing Users.
     */
    private final Dao<User> userDao;

    /**
     * DAO for managing Books.
     */
    private final Dao<Book> bookDao;

    /**
     * DAO for managing Borrowings.
     */
    private final Dao<Borrowing> borrowingDao;

    /**
     * DAO for managing Copies.
     */
    private final Dao<Copy> copyDao;

    /**
     * DAO for managing Publishers.
     */
    private final Dao<Publisher> publisherDao;

    /**
     * The user ID of the logged in user, if not a librarian.
     */
    private Long loggedInUserId;

    /**
     * Constructs the MainFrame, specifying if the user is a librarian and optionally a user ID.
     *
     * @param isLibrarian True if the user is a librarian.
     * @param userId      The ID of the user if not a librarian (can be null otherwise).
     */
    public MainFrame(boolean isLibrarian, Long userId) {
        super(isLibrarian ? "Library System - Librarian" : "Library System - User");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);

        // Initialize DAOs
        this.userDao = new UserDaoImpl();
        this.bookDao = new BookDaoImpl();
        this.borrowingDao = new BorrowingDaoImpl();
        this.copyDao = new CopyDaoImpl();
        this.publisherDao = new PublisherDaoImpl();

        if (!isLibrarian && userId != null) {
            this.loggedInUserId = userId;
        }

        // Populate default data if needed
        initializeDefaultData();

        // Build the UI
        setupUI(isLibrarian);

        setLocationRelativeTo(null);
    }

    /**
     * Sets up the tabbed interface. Depending on whether the user is a librarian,
     * shows different panels (Users, Books, Borrowings) or
     * (Library Titles, Available Titles, Borrowing History).
     *
     * @param isLibrarian A boolean indicating librarian mode or user mode.
     */
    private void setupUI(boolean isLibrarian) {
        JTabbedPane tabbedPane = new JTabbedPane();

        if (isLibrarian) {
            // Librarian panels
            UserManagementPanel userPanel = new UserManagementPanel(userDao);
            BookManagementPanel bookPanel = new BookManagementPanel(bookDao, copyDao);
            BorrowingManagementPanel borrowingPanel = new BorrowingManagementPanel(
                    borrowingDao, userDao, bookDao, copyDao, bookPanel);

            tabbedPane.addTab("Users", userPanel);
            tabbedPane.addTab("Books", bookPanel);
            tabbedPane.addTab("Borrowings", borrowingPanel);

            userPanel.loadUsers();
            bookPanel.loadBooks();
            borrowingPanel.loadBorrowings();

        } else {
            // User panels
            LibraryTitlesPanel libraryTitlesPanel = new LibraryTitlesPanel(bookDao);
            AvailableTitlesPanel availableTitlesPanel = new AvailableTitlesPanel(bookDao);
            BorrowingHistoryPanel borrowingHistoryPanel = new BorrowingHistoryPanel(borrowingDao, loggedInUserId);

            tabbedPane.addTab("Library Titles", libraryTitlesPanel);
            tabbedPane.addTab("Available Titles", availableTitlesPanel);
            tabbedPane.addTab("Borrowing History", borrowingHistoryPanel);

            libraryTitlesPanel.loadLibraryTitles();
            availableTitlesPanel.loadAvailableTitles();
            borrowingHistoryPanel.loadBorrowingHistory();
        }

        add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * Inserts default data (Users, Books, Copies, Borrowings, Publishers)
     * if the database appears empty.
     */
    private void initializeDefaultData() {
        // Insert default publishers
        Publisher penguin = new Publisher("Penguin Books", "375 Hudson Street, New York, NY", "(212) 366-2000");
        Publisher harperCollins = new Publisher("HarperCollins", "195 Broadway, New York, NY", "(212) 207-7000");
        if (publisherDao.findAll().isEmpty()) {
            publisherDao.create(penguin);
            publisherDao.create(harperCollins);
        }

        // Add default users
        if (userDao.findAll().isEmpty()) {
            userDao.create(new User("Alice", "alice@example.com", "123-456", "123 Main St"));
            userDao.create(new User("Bob", "bob@example.com", "789-012", "456 Elm St"));
        }

        // Add default books & copies
        if (bookDao.findAll().isEmpty()) {
            Book book1 = new Book("1984", "George Orwell", "Secker & Warburg", 1949, "123456789");
            Book book2 = new Book("Brave New World", "Aldous Huxley", "Chatto & Windus", 1932, "987654321");
            book1.setPublisher(penguin);
            book2.setPublisher(harperCollins);
            bookDao.create(book1);
            bookDao.create(book2);

            copyDao.create(new Copy(book1, "Available"));
            copyDao.create(new Copy(book1, "Available"));
            copyDao.create(new Copy(book2, "Available"));
            copyDao.create(new Copy(book2, "Available"));
        }

        // Add default borrowings
        if (borrowingDao.findAll().isEmpty()) {
            User alice = userDao.findAll().stream()
                    .filter(user -> "Alice".equals(user.getName()))
                    .findFirst()
                    .orElse(null);
            User bob = userDao.findAll().stream()
                    .filter(user -> "Bob".equals(user.getName()))
                    .findFirst()
                    .orElse(null);

            if (alice != null) {
                Copy aliceCopy = copyDao.findAll().stream()
                        .filter(copy -> "1984".equals(copy.getBook().getTitle()) &&
                                "Available".equals(copy.getStatus()))
                        .findFirst()
                        .orElse(null);
                if (aliceCopy != null) {
                    aliceCopy.setStatus("Borrowed");
                    copyDao.update(aliceCopy);

                    Borrowing borrowing1 = new Borrowing(alice, aliceCopy, LocalDate.now().minusDays(5));
                    borrowingDao.create(borrowing1);
                }
            }

            if (bob != null) {
                Copy bobCopy = copyDao.findAll().stream()
                        .filter(copy -> "Brave New World".equals(copy.getBook().getTitle()) &&
                                "Available".equals(copy.getStatus()))
                        .findFirst()
                        .orElse(null);
                if (bobCopy != null) {
                    bobCopy.setStatus("Borrowed");
                    copyDao.update(bobCopy);

                    Borrowing borrowing2 = new Borrowing(bob, bobCopy, LocalDate.now().minusDays(3));
                    borrowingDao.create(borrowing2);
                }
            }
        }
    }

    /**
     * Main entry point to launch the application.
     * Prompts the user for librarian or user mode, and optionally for a User ID if not a librarian.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            boolean isLibrarian = (JOptionPane.showConfirmDialog(
                    null,
                    "Are you a librarian?",
                    "Role Selection",
                    JOptionPane.YES_NO_OPTION
            ) == JOptionPane.YES_OPTION);

            if (!isLibrarian) {
                Long userId = promptForUserId();
                if (userId != null) {
                    new MainFrame(false, userId).setVisible(true);
                }
            } else {
                new MainFrame(true, null).setVisible(true);
            }
        });
    }

    /**
     * Prompts the user for a User ID if the user is not a librarian.
     *
     * @return The parsed User ID or null if invalid or canceled.
     */
    private static Long promptForUserId() {
        JTextField userIdField = new JTextField();
        int option = JOptionPane.showConfirmDialog(
                null,
                new Object[]{"Enter User ID:", userIdField},
                "User Login",
                JOptionPane.OK_CANCEL_OPTION
        );
        if (option == JOptionPane.OK_OPTION) {
            try {
                return Long.parseLong(userIdField.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(
                        null,
                        "Invalid User ID. Enter a numeric value.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
        return null;
    }
}