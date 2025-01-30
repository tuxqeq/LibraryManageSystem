package LibraryEntities;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * Represents a librarian, specialized from {@link User} (1-to-1 link).
 */
@Entity
@Table(name = "Librarians")
public class Librarian {

    /**
     * Primary key for Librarian, generated automatically (Integer).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * A one-to-one link to the underlying {@link User}.
     */
    @OneToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    /**
     * The date the Librarian started employment.
     */
    private LocalDate employmentDate;

    /**
     * The position/title of the Librarian (e.g., "Head Librarian").
     */
    private String position;

    /**
     * Default constructor for JPA.
     */
    public Librarian() {}

    /**
     * Constructs a new Librarian with the specified user, date, and position.
     *
     * @param user           The underlying User entity.
     * @param employmentDate The date this Librarian started employment.
     * @param position       The job title or position of the Librarian.
     */
    public Librarian(User user, LocalDate employmentDate, String position) {
        this.user = user;
        this.employmentDate = employmentDate;
        this.position = position;
    }

    public Integer getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDate getEmploymentDate() { return employmentDate; }
    public void setEmploymentDate(LocalDate employmentDate) { this.employmentDate = employmentDate; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
}