package com.example.svmarket.service;

import com.example.svmarket.entity.Category;
import com.example.svmarket.repository.CategoryRepository;
import com.example.svmarket.service.CloudinaryService.UploadedImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Service
public class AdminCategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category createCategory(String name, MultipartFile file) throws IOException {
        Category category = new Category();
        category.setName(name);

        if (file != null && !file.isEmpty()) {
            UploadedImage uploadedImage = cloudinaryService.uploadCategoryImage(file);
            category.setImage(uploadedImage.secureUrl());
            category.setImagePublicId(uploadedImage.publicId());
        }

        return categoryRepository.save(category);
    }

    public Category updateCategory(Integer id, String name, MultipartFile file) throws IOException {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
        category.setName(name);

        if (file != null && !file.isEmpty()) {
            if (category.getImagePublicId() != null && !category.getImagePublicId().isBlank()) {
                cloudinaryService.deleteImage(category.getImagePublicId());
            }
            UploadedImage uploadedImage = cloudinaryService.uploadCategoryImage(file);
            category.setImage(uploadedImage.secureUrl());
            category.setImagePublicId(uploadedImage.publicId());
        }

        return categoryRepository.save(category);
    }

    public void deleteCategory(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
        if (category.getListings() != null && !category.getListings().isEmpty()) {
            throw new RuntimeException("Không thể xóa danh mục đang có sản phẩm");
        }

        if (category.getImagePublicId() != null && !category.getImagePublicId().isBlank()) {
            cloudinaryService.deleteImage(category.getImagePublicId());
        }

        categoryRepository.deleteById(id);
    }

    // Hàm xuất Excel
    public ByteArrayInputStream exportCategoriesExcel() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Categories");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("Name");
            headerRow.createCell(2).setCellValue("Image");

            List<Category> categories = categoryRepository.findAll();
            int rowIdx = 1;
            for (Category cat : categories) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(cat.getId());
                row.createCell(1).setCellValue(cat.getName());
                row.createCell(2).setCellValue(cat.getImage() != null ? cat.getImage() : "");
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi xuất file Excel", e);
        }
    }

    // Hàm nhập Excel
    public void importCategoriesExcel(MultipartFile file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean isFirstRow = true;
            DataFormatter formatter = new DataFormatter(); // Định dạng an toàn cho mọi kiểu dữ liệu của Excel
            for (Row row : sheet) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                } // Bỏ qua dòng tiêu đề

                String name = formatter.formatCellValue(row.getCell(1)).trim();
                String image = formatter.formatCellValue(row.getCell(2)).trim();

                if (!name.isEmpty()) {
                    Category category = new Category();
                    category.setName(name);
                    category.setImage(!image.isEmpty() ? image : null);
                    categoryRepository.save(category);
                }
            }
        }
    }
}