package GUI;

import LibraryEntities.Borrowing;
import LibraryUtil.JPAUtil;
import dao.Dao;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * A panel to display the borrowing history for a specific user.
 */
public class BorrowingHistoryPanel extends JPanel {

    /**
     * The DAO for managing {@link Borrowing} entities.
     */
    private final Dao<Borrowing> borrowingDao;

    /**
     * The user ID for which to display borrowing history.
     */
    private final Long userId;

    /**
     * The table that displays borrowing data.
     */
    private JTable borrowingHistoryTable;

    /**
     * Constructs a new panel for borrowing history.
     *
     * @param borrowingDao DAO for Borrowing entities.
     * @param userId       The user ID whose history is displayed.
     */
    public BorrowingHistoryPanel(Dao<Borrowing> borrowingDao, Long userId) {
        this.borrowingDao = borrowingDao;
        this.userId = userId;
        setLayout(new BorderLayout());
        initComponents();
    }

    /**
     * Initializes the table and its scroll pane.
     */
    private void initComponents() {
        borrowingHistoryTable = new JTable(new DefaultTableModel(
                new Object[]{"ID", "Book", "Borrow Date", "Return Date"}, 0));
        add(new JScrollPane(borrowingHistoryTable), BorderLayout.CENTER);
    }

    /**
     * Finds all {@link Borrowing} records for the given user ID.
     *
     * @param userId The user ID.
     * @return A list of Borrowing entities for that user.
     */
    public List<Borrowing> findBorrowingHistoryByUser(Long userId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Borrowing> query = em.createQuery(
                    "SELECT br FROM Borrowing br WHERE br.user.id = :uid", Borrowing.class);
            query.setParameter("uid", userId.intValue());
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Loads and displays the borrowing history for the given user.
     */
    public void loadBorrowingHistory() {
        DefaultTableModel model = (DefaultTableModel) borrowingHistoryTable.getModel();
        model.setRowCount(0);

        if (userId == null) {
            return;
        }

        List<Borrowing> borrowings = findBorrowingHistoryByUser(userId);
        for (Borrowing borrowing : borrowings) {
            model.addRow(new Object[]{
                    borrowing.getId(),
                    borrowing.getCopy().getBook().getTitle(),
                    borrowing.getBorrowDate(),
                    borrowing.getReturnDate()
            });
        }
    }
}