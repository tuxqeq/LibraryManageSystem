package LibraryEntities;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a general user in the library system.
 */
@Entity
@Table(name = "Users")
public class User {

    /**
     * Primary key for User, generated automatically (Integer).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * The user's name.
     */
    @Column(nullable = false)
    private String name;

    /**
     * The user's unique email address.
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * The user's phone number.
     */
    private String phoneNumber;

    /**
     * The user's home address.
     */
    private String address;

    /**
     * The list of Borrowings associated with this user (one-to-many).
     */
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private List<Borrowing> borrowings = new ArrayList<>();

    /**
     * Default constructor for JPA.
     */
    public User() {}

    /**
     * Constructs a user with specified fields.
     *
     * @param name        The user's name.
     * @param email       The user's email address.
     * @param phoneNumber The user's phone number.
     * @param address     The user's home address.
     */
    public User(String name, String email, String phoneNumber, String address) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public List<Borrowing> getBorrowings() { return borrowings; }
}