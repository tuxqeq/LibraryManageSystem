package GUI;

import LibraryEntities.Book;
import dao.BookDaoImpl;
import dao.Dao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * A panel that displays all {@link Book} entities that have at least one "Available" copy.
 */
public class AvailableTitlesPanel extends JPanel {

    /**
     * The data access object for Books.
     */
    private final Dao<Book> bookDao;

    /**
     * The table for displaying available titles.
     */
    private JTable availableTitlesTable;

    /**
     * Constructs the panel with the specified Book DAO.
     *
     * @param bookDao A {@link Dao} for managing Book entities.
     */
    public AvailableTitlesPanel(Dao<Book> bookDao) {
        this.bookDao = bookDao;
        setLayout(new BorderLayout());
        initComponents();
    }

    /**
     * Initializes the UI components (table and scroll pane).
     */
    private void initComponents() {
        availableTitlesTable = new JTable(new DefaultTableModel(
                new Object[]{"ID", "Title", "Author", "Publisher", "ISBN", "Year"}, 0));
        add(new JScrollPane(availableTitlesTable), BorderLayout.CENTER);
    }

    /**
     * Loads all books that have at least one available copy, using {@link BookDaoImpl#findAvailableTitles()}.
     */
    @SuppressWarnings("unchecked")
    public void loadAvailableTitles() {
        if (!(bookDao instanceof BookDaoImpl)) {
            return;
        }
        BookDaoImpl bookDaoImpl = (BookDaoImpl) bookDao;
        List<Book> availableBooks = bookDaoImpl.findAvailableTitles();

        DefaultTableModel model = (DefaultTableModel) availableTitlesTable.getModel();
        model.setRowCount(0);

        for (Book book : availableBooks) {
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