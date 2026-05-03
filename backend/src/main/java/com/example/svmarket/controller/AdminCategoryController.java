package com.example.svmarket.controller;

import com.example.svmarket.entity.Category;
import com.example.svmarket.service.AdminCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    @Autowired
    private AdminCategoryService adminCategoryService;

    public static class CategoryDTO {
        public Integer id;
        public String name;
        public String image;

        public CategoryDTO(Category category) {
            this.id = category.getId();
            this.name = category.getName();
            this.image = category.getImage();
        }
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> list = adminCategoryService.getAllCategories().stream()
                .map(CategoryDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PostMapping
    public ResponseEntity<?> createCategory(
            @RequestParam("name") String name,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            Category category = adminCategoryService.createCategory(name, image);
            return ResponseEntity.ok(new CategoryDTO(category));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(
            @PathVariable Integer id,
            @RequestParam("name") String name,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            Category category = adminCategoryService.updateCategory(id, name, image);
            return ResponseEntity.ok(new CategoryDTO(category));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id) {
        try {
            adminCategoryService.deleteCategory(id);
            return ResponseEntity.ok("Xóa danh mục thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportCategories() {
        InputStreamResource resource = new InputStreamResource(adminCategoryService.exportCategoriesExcel());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=categories.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    @PostMapping("/import")
    public ResponseEntity<?> importCategories(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) return ResponseEntity.badRequest().body("File rỗng");
            adminCategoryService.importCategoriesExcel(file);
            return ResponseEntity.ok("Nhập dữ liệu thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi nhập dữ liệu: " + e.getMessage());
        }
    }
}