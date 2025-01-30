package dao;

import java.util.List;

/**
 *
 * A generic DAO (Data Access Object) interface providing basic CRUD operations.
 *
 * @param <T> The entity type this DAO manages.
 */
public interface Dao<T> {

    /**
     * Persists a new entity in the database.
     *
     * @param entity The entity to create.
     * @return The persisted entity with any generated fields (e.g., ID).
     */
    T create(T entity);

    /**
     * Finds an entity by its unique ID.
     *
     * @param id The ID of the entity to find.
     * @return The found entity, or null if not present.
     */
    T findById(Long id);

    /**
     * Retrieves all entities of type {@code T}.
     *
     * @return A list of all entities of type {@code T}.
     */
    List<T> findAll();

    /**
     * Updates an existing entity in the database.
     *
     * @param entity The entity with updated fields.
     * @return The merged (updated) entity.
     */
    T update(T entity);

    /**
     * Deletes an entity by its ID.
     *
     * @param id The ID of the entity to delete.
     */
    void delete(Long id);
}