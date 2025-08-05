package harvestLog.service.impl;

import harvestLog.model.Category;
import harvestLog.repository.CategoryRepository;
import harvestLog.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService implements ICategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

//    ##notgood
    public Category findByNameOrCreate(String name) {
        return categoryRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> categoryRepository.save(new Category(name.toUpperCase())));
    }

    public List<Category> getAll() {
        return categoryRepository.findAll(Sort.by("name"));
    }

    public Category getById(Long id) {
        return categoryRepository.findById(id).orElseThrow();
    }
}
