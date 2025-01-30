package LibraryEntities;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * Represents a record of a user borrowing a copy of a book.
 */
@Entity
@Table(name = "Borrowings")
public class Borrowing {

    /**
     * Primary key for Borrowing, generated automatically.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * The user who made the borrowing (many-to-one).
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The physical copy being borrowed (many-to-one).
     */
    @ManyToOne
    @JoinColumn(name = "copy_id", nullable = false)
    private Copy copy;

    /**
     * The date the user borrowed the copy.
     */
    private LocalDate borrowDate;

    /**
     * The date the user returned the copy, or null if not yet returned.
     */
    private LocalDate returnDate;

    /**
     * Default constructor for JPA.
     */
    public Borrowing() {}

    /**
     * Constructs a Borrowing with an existing ID, user, copy, and borrow date.
     *
     * @param id         The unique ID for this Borrowing.
     * @param user       The user who borrows the copy.
     * @param copy       The specific copy that was borrowed.
     * @param borrowDate The date the copy was borrowed.
     */
    public Borrowing(Integer id, User user, Copy copy, LocalDate borrowDate) {
        this.id = id;
        this.user = user;
        this.copy = copy;
        this.borrowDate = borrowDate;
    }

    /**
     * Constructs a Borrowing without specifying the ID.
     *
     * @param user       The user who borrows the copy.
     * @param copy       The specific copy that was borrowed.
     * @param borrowDate The date the copy was borrowed.
     */
    public Borrowing(User user, Copy copy, LocalDate borrowDate) {
        this.user = user;
        this.copy = copy;
        this.borrowDate = borrowDate;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Copy getCopy() { return copy; }
    public void setCopy(Copy copy) { this.copy = copy; }

    public LocalDate getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
}