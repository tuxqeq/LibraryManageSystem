package GUI;

import LibraryEntities.Book;
import LibraryEntities.Borrowing;
import LibraryEntities.Copy;
import LibraryEntities.User;
import dao.Dao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;

/**
 * A panel that manages the creation, editing, and deletion of {@link Borrowing} entities.
 * It allows the librarian to add a new borrowing by specifying user ID and book ID,
 * mark a borrowing as returned, or delete a borrowing.
 */
public class BorrowingManagementPanel extends JPanel {

    /**
     * DAO for Borrowing operations.
     */
    private final Dao<Borrowing> borrowingDao;

    /**
     * DAO for User operations.
     */
    private final Dao<User> userDao;

    /**
     * DAO for Book operations.
     */
    private final Dao<Book> bookDao;

    /**
     * DAO for Copy operations.
     */
    private final Dao<Copy> copyDao;

    /**
     * A reference to the BookManagementPanel to refresh availability.
     */
    private BookManagementPanel bookManagementPanel;

    /**
     * Table to display borrowing records.
     */
    private JTable borrowingTable;

    /**
     * Constructs a new BorrowingManagementPanel.
     *
     * @param borrowingDao        DAO for Borrowing entities.
     * @param userDao             DAO for User entities.
     * @param bookDao             DAO for Book entities.
     * @param copyDao             DAO for Copy entities.
     * @param bookManagementPanel A reference to the book panel to update availability.
     */
    public BorrowingManagementPanel(
            Dao<Borrowing> borrowingDao,
            Dao<User> userDao,
            Dao<Book> bookDao,
            Dao<Copy> copyDao,
            BookManagementPanel bookManagementPanel) {

        this.borrowingDao = borrowingDao;
        this.userDao = userDao;
        this.bookDao = bookDao;
        this.copyDao = copyDao;
        this.bookManagementPanel = bookManagementPanel;

        setLayout(new BorderLayout());
        initComponents();
    }

    /**
     * Initializes the borrowing table and the action buttons.
     */
    private void initComponents() {
        borrowingTable = new JTable(new DefaultTableModel(
                new Object[]{"ID", "User", "Book", "Borrow Date", "Return Date"}, 0));
        add(new JScrollPane(borrowingTable), BorderLayout.CENTER);

        JPanel borrowingActions = new JPanel(new FlowLayout());
        JButton addBorrowingButton = new JButton("Add Borrowing");
        JButton editBorrowingButton = new JButton("Edit Borrowing");
        JButton deleteBorrowingButton = new JButton("Delete Borrowing");

        borrowingActions.add(addBorrowingButton);
        borrowingActions.add(editBorrowingButton);
        borrowingActions.add(deleteBorrowingButton);
        add(borrowingActions, BorderLayout.SOUTH);

        addBorrowingButton.addActionListener(e -> addBorrowing());
        editBorrowingButton.addActionListener(e -> editBorrowing());
        deleteBorrowingButton.addActionListener(e -> deleteBorrowing());
    }

    /**
     * Loads all existing borrowings into the table.
     */
    public void loadBorrowings() {
        DefaultTableModel model = (DefaultTableModel) borrowingTable.getModel();
        model.setRowCount(0);
        for (Borrowing borrowing : borrowingDao.findAll()) {
            model.addRow(new Object[]{
                    borrowing.getId(),
                    borrowing.getUser().getName(),
                    borrowing.getCopy().getBook().getTitle(),
                    borrowing.getBorrowDate(),
                    borrowing.getReturnDate()
            });
        }
    }

    /**
     * Creates a new Borrowing record by prompting for user ID and book ID.
     */
    private void addBorrowing() {
        JTextField userIdField = new JTextField();
        JTextField bookIdField = new JTextField();
        JTextField borrowDateField = new JTextField(LocalDate.now().toString());

        Object[] fields = {
                "User ID:", userIdField,
                "Book ID:", bookIdField,
                "Borrow Date:", borrowDateField
        };

        int option = JOptionPane.showConfirmDialog(
                this,
                fields,
                "Add Borrowing",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {
            try {
                Long userId = Long.parseLong(userIdField.getText());
                Long bookId = Long.parseLong(bookIdField.getText());
                LocalDate borrowDate = LocalDate.parse(borrowDateField.getText());

                // Fetch the user and book
                User user = userDao.findById(userId);
                Book book = bookDao.findById(bookId);

                if (user == null) {
                    JOptionPane.showMessageDialog(this,
                            "User with ID " + userId + " not found.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (book == null) {
                    JOptionPane.showMessageDialog(this,
                            "Book with ID " + bookId + " not found.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Check availability
                if (book.getAvailableCopies() < 1) {
                    JOptionPane.showMessageDialog(this,
                            "No available copies for this book (" + book.getTitle() + ").",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Find an actual available copy
                Copy availableCopy = book.getCopies().stream()
                        .filter(c -> "Available".equals(c.getStatus()))
                        .findFirst()
                        .orElse(null);

                if (availableCopy == null) {
                    JOptionPane.showMessageDialog(this,
                            "No copy with status 'Available' found (unexpected).",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Mark that copy as Borrowed
                availableCopy.setStatus("Borrowed");
                copyDao.update(availableCopy);

                // Create the Borrowing
                Borrowing newBorrowing = new Borrowing(null, user, availableCopy, borrowDate);
                borrowingDao.create(newBorrowing);

                // Refresh tables
                loadBorrowings();
                if (bookManagementPanel != null) {
                    bookManagementPanel.loadBooks();
                }

                JOptionPane.showMessageDialog(this,
                        "Borrowing created successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error creating borrowing: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Allows editing the Return Date of the selected Borrowing.
     */
    private void editBorrowing() {
        int selectedRow = borrowingTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a borrowing record to edit.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Long borrowingId = Long.valueOf(borrowingTable.getValueAt(selectedRow, 0).toString());
        Borrowing borrowing = borrowingDao.findById(borrowingId);

        JTextField returnDateField = new JTextField(
                borrowing.getReturnDate() == null ? "" : borrowing.getReturnDate().toString()
        );

        Object[] fields = {
                "Return Date (yyyy-MM-dd):", returnDateField
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Edit Borrowing", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                LocalDate returnDate = LocalDate.parse(returnDateField.getText());
                borrowing.setReturnDate(returnDate);

                // Mark the copy as available again
                Copy copy = borrowing.getCopy();
                copy.setStatus("Available");
                copyDao.update(copy);

                borrowingDao.update(borrowing);
                loadBorrowings();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error updating borrowing: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Deletes the selected Borrowing from the database.
     */
    private void deleteBorrowing() {
        int selectedRow = borrowingTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a borrowing record to delete.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Long borrowingId = Long.valueOf(borrowingTable.getValueAt(selectedRow, 0).toString());
        borrowingDao.delete(borrowingId);
        loadBorrowings();
    }
}