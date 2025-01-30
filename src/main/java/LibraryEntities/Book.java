package LibraryEntities;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a book entity in the library system.
 */
@Entity
@Table(name = "Books")
public class Book {

    /**
     * The primary key identifier for Book, generated automatically.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The title of the Book.
     */
    private String title;

    /**
     * The author of the Book.
     */
    private String author;

    /**
     * The Publisher entity this Book references (many-to-one).
     */
    @ManyToOne
    @JoinColumn(name = "publisherId")
    private Publisher publisher;

    /**
     * The publisher's name (also stored separately).
     */
    private String publisherName;

    /**
     * The publication year of the Book.
     */
    @Column(nullable = false)
    private Integer publicationYear;

    /**
     * The unique ISBN of the Book.
     */
    @Column(unique = true, nullable = false)
    private String isbn;

    /**
     * The list of physical copies associated with this Book (one-to-many).
     */
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Copy> copies = new ArrayList<>();

    /**
     * Default constructor for JPA.
     */
    public Book() {}

    /**
     * Constructs a Book with basic fields.
     *
     * @param title           The title of the Book.
     * @param author          The author of the Book.
     * @param publisherName   The name of the publisher (string form).
     * @param publicationYear The year the Book was published.
     * @param isbn            The unique ISBN number.
     */
    public Book(String title, String author, String publisherName, int publicationYear, String isbn) {
        this.title = title;
        this.author = author;
        this.publisherName = publisherName;
        this.publicationYear = publicationYear;
        this.isbn = isbn;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public Publisher getPublisher() { return publisher; }

    /**
     * Sets the publisher entity and also updates {@code publisherName} with {@link Publisher#getName()}.
     *
     * @param publisher The Publisher entity to link to this Book.
     */
    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
        if (publisher != null) {
            this.publisherName = publisher.getName();
        }
    }

    public String getPublisherName() { return publisherName; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }

    public Integer getPublicationYear() { return publicationYear; }
    public void setPublicationYear(Integer publicationYear) { this.publicationYear = publicationYear; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public List<Copy> getCopies() { return copies; }

    /**
     * Counts how many copies of this book are currently marked as "Available".
     *
     * @return The number of available copies.
     */
    public int getAvailableCopies() {
        return (int) copies.stream()
                .filter(copy -> "Available".equals(copy.getStatus()))
                .count();
    }
}