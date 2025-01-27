package space.obminyashka.items_exchange.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import space.obminyashka.items_exchange.dao.SubcategoryRepository;
import space.obminyashka.items_exchange.model.Subcategory;
import space.obminyashka.items_exchange.service.SubcategoryService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubcategoryServiceImpl implements SubcategoryService {

    private final SubcategoryRepository subcategoryRepository;

    @Override
    public List<String> findSubcategoryNamesByCategoryId(long categoryId) {
        return subcategoryRepository.findSubcategoriesNamesByCategory(categoryId);
    }

    @Override
    public void removeSubcategoryById(long subcategoryId) {
        subcategoryRepository.deleteById(subcategoryId);
    }

    @Override
    public Optional<Subcategory> findById(long id) {
        return subcategoryRepository.findById(id);
    }

    @Override
    public boolean isSubcategoryExistsById(long id) {
        return subcategoryRepository.existsById(id);
    }

    @Override
    public boolean isSubcategoryDeletable(long id) {
        return subcategoryRepository.findById(id)
                .map(subcategory -> subcategory.getAdvertisements().isEmpty())
                .orElse(false);
    }

    @Override
    public List<Long> findAllSubcategoryIds() {
        return subcategoryRepository.findAll().stream()
                .map(Subcategory::getId)
                .toList();
    }
}
