package GUI;

import LibraryEntities.Book;
import dao.Dao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * A panel that displays all books in the library, regardless of their availability.
 */
public class LibraryTitlesPanel extends JPanel {

    /**
     * DAO for Book entities.
     */
    private final Dao<Book> bookDao;

    /**
     * Table for displaying all library titles.
     */
    private JTable libraryTitlesTable;

    /**
     * Constructs a new {@link LibraryTitlesPanel} using the given Book DAO.
     *
     * @param bookDao The DAO for Book entities.
     */
    public LibraryTitlesPanel(Dao<Book> bookDao) {
        this.bookDao = bookDao;
        setLayout(new BorderLayout());
        initComponents();
    }

    /**
     * Initializes the table and its containing scroll pane.
     */
    private void initComponents() {
        libraryTitlesTable = new JTable(new DefaultTableModel(
                new Object[]{"ID", "Title", "Author", "Publisher", "ISBN", "Year"}, 0));
        add(new JScrollPane(libraryTitlesTable), BorderLayout.CENTER);
    }

    /**
     * Loads all {@link Book} entities from the DAO into the table.
     */
    public void loadLibraryTitles() {
        DefaultTableModel model = (DefaultTableModel) libraryTitlesTable.getModel();
        model.setRowCount(0);

        List<Book> books = bookDao.findAll();
        for (Book book : books) {
            model.addRow(new Object[]{
                    book.getId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getPublisherName(),
                    book.getIsbn(),
                    book.getPublicationYear()
            });
        }
    }
}