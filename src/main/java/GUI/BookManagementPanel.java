package GUI;

import LibraryEntities.Book;
import LibraryEntities.Copy;
import dao.Dao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * A panel that displays and manages {@link Book} entities, along with options to add,
 * edit, or delete books. Also handles creation of related {@link Copy} entities.
 */
public class BookManagementPanel extends JPanel {

    /**
     * The DAO for managing Book entities.
     */
    private Dao<Book> bookDao;

    /**
     * The DAO for managing Copy entities.
     */
    private Dao<Copy> copyDao;

    /**
     * The table for showing all books.
     */
    private JTable bookTable;

    /**
     * Constructs a new panel for book management.
     *
     * @param bookDao A DAO for Book operations.
     * @param copyDao A DAO for Copy operations.
     */
    public BookManagementPanel(Dao<Book> bookDao, Dao<Copy> copyDao) {
        this.bookDao = bookDao;
        this.copyDao = copyDao;
        setLayout(new BorderLayout());
        initComponents();
    }

    /**
     * Initializes the table and action buttons.
     */
    private void initComponents() {
        bookTable = new JTable(new DefaultTableModel(
                new Object[]{"ID", "Title", "Author", "Publisher", "ISBN", "Year", "Availability"}, 0));
        add(new JScrollPane(bookTable), BorderLayout.CENTER);

        JPanel bookActions = new JPanel(new FlowLayout());
        JButton addBookButton = new JButton("Add Book");
        JButton editBookButton = new JButton("Edit Book");
        JButton deleteBookButton = new JButton("Delete Book");

        bookActions.add(addBookButton);
        bookActions.add(editBookButton);
        bookActions.add(deleteBookButton);
        add(bookActions, BorderLayout.SOUTH);

        addBookButton.addActionListener(e -> addBook());
        editBookButton.addActionListener(e -> editBook());
        deleteBookButton.addActionListener(e -> deleteBook());
    }

    /**
     * Loads all Book entities into the table, including availability counts.
     */
    public void loadBooks() {
        DefaultTableModel model = (DefaultTableModel) bookTable.getModel();
        model.setRowCount(0);
        List<Book> books = bookDao.findAll();

        for (Book book : books) {
            model.addRow(new Object[]{
                    book.getId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getPublisherName(),
                    book.getIsbn(),
                    book.getPublicationYear(),
                    book.getAvailableCopies() // Show "Availability"
            });
        }
    }

    /**
     * Opens a dialog to add a new Book and one or more Copies.
     */
    private void addBook() {
        JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();
        JTextField publisherField = new JTextField();
        JTextField isbnField = new JTextField();
        JTextField publicationYearField = new JTextField();
        JTextField copiesField = new JTextField();

        Object[] fields = {
                "Title:", titleField,
                "Author:", authorField,
                "Publisher:", publisherField,
                "ISBN:", isbnField,
                "Publication Year:", publicationYearField,
                "Number of Copies:", copiesField
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Add Book", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String title = titleField.getText();
                String author = authorField.getText();
                String publisher = publisherField.getText();
                Integer.parseInt(isbnField.getText());
                String isbn = isbnField.getText();
                int publicationYear = Integer.parseInt(publicationYearField.getText());
                int numberOfCopies = Integer.parseInt(copiesField.getText());

                if (numberOfCopies <= 0) {
                    JOptionPane.showMessageDialog(this,
                            "Number of copies must be greater than 0.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Book book = new Book(title, author, publisher, publicationYear, isbn);
                bookDao.create(book);

                // Create copies
                for (int i = 0; i < numberOfCopies; i++) {
                    Copy copy = new Copy(book, "Available");
                    copyDao.create(copy);
                }
                loadBooks();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Please enter valid numeric values for ISBN, publication year and number of copies.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "An error occurred while adding the book: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Opens a dialog to edit the selected Book.
     */
    private void editBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a book to edit.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Long bookId = Long.valueOf(bookTable.getValueAt(selectedRow, 0).toString());
        Book book = bookDao.findById(bookId);

        JTextField titleField = new JTextField(book.getTitle());
        JTextField authorField = new JTextField(book.getAuthor());
        JTextField publisherField = new JTextField(book.getPublisherName());
        JTextField isbnField = new JTextField(book.getIsbn());
        JTextField publicationYearField = new JTextField(book.getPublicationYear().toString());

        Object[] fields = {
                "Title:", titleField,
                "Author:", authorField,
                "Publisher:", publisherField,
                "ISBN:", isbnField,
                "Publication Year:", publicationYearField
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Edit Book", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                book.setTitle(titleField.getText());
                book.setAuthor(authorField.getText());
                book.setPublisherName(publisherField.getText());
                book.setIsbn(isbnField.getText());
                book.setPublicationYear(Integer.parseInt(publicationYearField.getText()));
                bookDao.update(book);
                loadBooks();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Invalid numeric field.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Deletes the selected Book from the database. If the book has active borrowings or more than one copy, it shwos an error message.
     */
    private void deleteBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a book to delete.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Long bookId = Long.valueOf(bookTable.getValueAt(selectedRow, 0).toString());
        try{
            bookDao.delete(bookId);
        }catch(Exception e){
            JOptionPane.showMessageDialog(this,
                    "Cannot delete book with active borrowings or with more than one copies.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        loadBooks();
    }
}