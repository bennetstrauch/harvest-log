package harvestLog.controller;

import harvestLog.model.Category;
import harvestLog.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private ICategoryService categoryService;

    @GetMapping
    public List<Category> getAllCategories() {
        return categoryService.getAll();
    }



}

