package harvestLog.service;


import harvestLog.model.Category;

import java.util.List;

public interface ICategoryService {

    /**
     * Finds a Category by its name, ignoring case. If no category is found,
     * a new one is created and persisted with the given name.
     *
     * @param name The name of the category to find or create.
     * @return The existing or newly created Category.
     */
    Category findByNameOrCreate(String name);

    /**
     * Retrieves all categories, sorted alphabetically by name.
     *
     * @return A List of all Category objects.
     */
    List<Category> getAll();

    /**
     * Retrieves a category by its unique identifier.
     *
     * @param id The ID of the category to retrieve.
     * @return The found Category.
     * @throws java.util.NoSuchElementException if no category is found with the given ID.
     */
    Category getById(Long id);
}
