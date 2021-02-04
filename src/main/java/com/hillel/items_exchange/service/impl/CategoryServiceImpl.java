package com.hillel.items_exchange.service.impl;

import com.hillel.items_exchange.dao.CategoryRepository;
import com.hillel.items_exchange.dto.CategoryDto;
import com.hillel.items_exchange.dto.SubcategoryDto;
import com.hillel.items_exchange.model.Category;
import com.hillel.items_exchange.model.Subcategory;
import com.hillel.items_exchange.service.CategoryService;
import com.hillel.items_exchange.service.SubcategoryService;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.hillel.items_exchange.mapper.UtilMapper.*;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final SubcategoryService subcategoryService;

    @Override
    public List<String> findAllCategoryNames() {
        return categoryRepository.findAllCategoriesNames();
    }

    @Override
    public List<CategoryDto> findAllCategoryDtos() {
        return new ArrayList<>(convertAllTo(categoryRepository.findAll(),
                CategoryDto.class, ArrayList::new));
    }

    @Override
    public Optional<CategoryDto> findCategoryDtoById(long id) {
        return categoryRepository.findById(id)
                .map(category -> convertTo(category, CategoryDto.class));
    }

    @Override
    public CategoryDto saveCategoryWithSubcategories(CategoryDto categoryDto) {
        final Category category = saveCategory(categoryDto);
        return convertTo(category, CategoryDto.class);
    }

    @Override
    public void removeById(long categoryId) {
        categoryRepository.deleteById(categoryId);
    }

    @Override
    public boolean isCategoryDtoDeletable(long categoryId) {
        return categoryRepository.findById(categoryId)
                .map(category -> category.getSubcategories().stream()
                        .map(Subcategory::getAdvertisements)
                        .allMatch(Collection::isEmpty))
                .orElse(false);
    }

    @Override
    public boolean isCategoryDtoUpdatable(CategoryDto categoryDto) {
        return isCategoryExistsByIdAndNameOrNotExistsByName(categoryDto.getId(),
                categoryDto.getName()) && isSubcategoriesExist(categoryDto);
    }

    @Override
    public boolean isCategoryDtoValidForCreating(CategoryDto categoryDto) {
        return isCategoryNameHasNotDuplicate(categoryDto.getName())
                && categoryDto.getSubcategories().stream().allMatch(this::isSubcategoryIdEqualsZero);
    }

    private boolean isSubcategoryIdEqualsZero(SubcategoryDto subcategoryDto) {
        return subcategoryDto.getId() == 0L;
    }

    private boolean isCategoryNameHasNotDuplicate(String name) {
        return !categoryRepository.existsByNameIgnoreCase(name);
    }

    private boolean isSubcategoriesExist(CategoryDto categoryDto) {
        final List<Long> existingSubcategoriesIds = subcategoryService.findAllSubcategoryIds();

        return categoryDto.getSubcategories().stream()
                .filter(subcategoryDto -> !isSubcategoryIdEqualsZero(subcategoryDto))
                .allMatch(subcategoryDto -> existingSubcategoriesIds.contains(subcategoryDto.getId()));
    }

    private boolean isCategoryExistsByIdAndNameOrNotExistsByName(long categoryId, String categoryName) {
        return categoryRepository.existsByIdAndNameIgnoreCase(categoryId, categoryName)
                || (categoryRepository.existsById(categoryId) && isCategoryNameHasNotDuplicate(categoryName));
    }

    private Category saveCategory(CategoryDto categoryDto) {
        final Category category = convertTo(categoryDto, Category.class);
        category.getSubcategories().forEach(subcategory -> subcategory.setCategory(category));
        return categoryRepository.saveAndFlush(category);
    }
}
