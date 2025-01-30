package LibraryEntities;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a publisher of books.
 */
@Entity
@Table(name = "Publishers")
public class Publisher {

    /**
     * Primary key for Publisher, generated automatically (Integer).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * The name of the publisher (e.g., "Penguin Books").
     */
    private String name;

    /**
     * The address of the publisher's office or headquarters.
     */
    private String address;

    /**
     * The phone number for contacting the publisher.
     */
    private String phoneNumber;

    /**
     * One publisher can have many books referencing it (one-to-many).
     */
    @OneToMany(mappedBy = "publisher", fetch = FetchType.EAGER)
    private List<Book> books = new ArrayList<>();

    /**
     * Default constructor for JPA.
     */
    public Publisher() {}

    /**
     * Constructs a Publisher with basic info.
     *
     * @param name        The name of the publisher.
     * @param address     The address of the publisher.
     * @param phoneNumber The publisher's contact phone number.
     */
    public Publisher(String name, String address, String phoneNumber) {
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    public Integer getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public List<Book> getBooks() { return books; }

    /**
     * Adds a {@link Book} to this publisher's list and sets the Book's publisher to this.
     *
     * @param book The Book to associate with this publisher.
     */
    public void addBook(Book book) {
        books.add(book);
        book.setPublisher(this);
    }

    /**
     * Removes a {@link Book} from this publisher's list and sets its publisher to null.
     *
     * @param book The Book to remove from this publisher.
     */
    public void removeBook(Book book) {
        books.remove(book);
        book.setPublisher(null);
    }
}