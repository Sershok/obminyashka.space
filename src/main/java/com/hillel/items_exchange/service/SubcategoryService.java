package com.hillel.items_exchange.service;

import com.hillel.items_exchange.model.Subcategory;
import java.util.List;
import java.util.Optional;

public interface SubcategoryService {

    /**
     * Returns all subcategory names by given Category ID.
     *
     * @param categoryId is Category ID.
     * @return list of subcategory names from DB that are represented as {@link String}
     */
    List<String> findSubcategoryNamesByCategoryId(long categoryId);

    /**
     * Removes the subcategory with the given ID from DB.
     *
     * @param subcategoryId is Subcategory ID to remove.
     */
    void removeSubcategoryById(long subcategoryId);

    /**
     * Retrieves a category by its ID.
     *
     * @param id is Subcategory ID
     * @return {@link Subcategory} entity from DB
     */
    Optional<Subcategory> findById(long id);

    /**
     * If a subcategory exists, returns {@code true}, otherwise {@code false}.
     *
     * @param id is Subcategory ID.
     * @return true if a subcategory with the given id exists, false otherwise.
     */
    boolean isSubcategoryExistsById(long id);

    /**
     * Checks if a subcategory with the given ID exists in DB and has not products.
     *
     * @param id is Subcategory ID.
     * @return {@code true} if a subcategory with the given ID can be deleted, {@code false} otherwise.
     */
    boolean isSubcategoryDeletable(long id);

    /**
     * Returns all Subcategory identifiers.
     *
     * @return list of all subcategory identifiers from DB
     */
    List<Long> findAllSubcategoryIds();
}
