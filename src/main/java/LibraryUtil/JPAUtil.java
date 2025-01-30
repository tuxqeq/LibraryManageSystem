package LibraryUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Utility class to provide a shared {@link EntityManagerFactory} for the "LibraryPU",
 * and a method to obtain an {@link EntityManager}.
 */
public class JPAUtil {

    /**
     * A singleton EntityManagerFactory initialized for "LibraryPU".
     */
    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("LibraryPU");

    /**
     * Creates a new {@link EntityManager} from the shared factory.
     *
     * @return A new EntityManager instance.
     */
    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    /**
     * Closes the singleton EntityManagerFactory.
     * Generally called when the application shuts down.
     */
    public static void close() {
        emf.close();
    }
}