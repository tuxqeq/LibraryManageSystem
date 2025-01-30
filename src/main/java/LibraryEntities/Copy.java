package LibraryEntities;

import javax.persistence.*;

/**
 * Represents a physical copy of a Book in the library.
 */
@Entity
public class Copy {

    /**
     * Primary key for Copy, generated automatically.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The Book this Copy references (many-to-one).
     */
    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    /**
     * The status of this copy (e.g., "Available", "Borrowed").
     */
    private String status;

    /**
     * Constructs a Copy with a specific Book and status.
     *
     * @param book   The Book this copy references.
     * @param status The status of this copy (e.g., "Available").
     */
    public Copy(Book book, String status) {
        this.book = book;
        this.status = status;
    }

    /**
     * Default constructor for JPA.
     */
    public Copy() {}


    public Long getId() {
        return id;
    }

    public Book getBook() {
        return book;
    }

    /**
     * Sets the Book reference for this copy.
     *
     * @param book The Book entity to link.
     */
    public void setBook(Book book) {
        this.book = book;
    }

    public String getStatus() {
        return status;
    }

    /**
     * Updates the status of this copy (e.g., from "Available" to "Borrowed").
     *
     * @param status The new status string.
     */
    public void setStatus(String status) {
        this.status = status;
    }
}