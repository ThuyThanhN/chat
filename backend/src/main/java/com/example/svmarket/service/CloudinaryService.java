package com.example.svmarket.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    // Upload mot anh len Cloudinary va tra ve public_id + secure_url.
    public UploadedImage uploadListingImage(MultipartFile file) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "svmarket/listings",
                            "resource_type", "image"));

            String publicId = (String) result.get("public_id");
            String secureUrl = (String) result.get("secure_url");

            return new UploadedImage(publicId, secureUrl);
        } catch (IOException exception) {
            throw new RuntimeException("Khong the upload anh len Cloudinary", exception);
        }
    }

    // Upload avatar len Cloudinary va tra ve public_id + secure_url.
    public UploadedImage uploadAvatarImage(MultipartFile file) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "svmarket/avatars",
                            "resource_type", "image"));

            String publicId = (String) result.get("public_id");
            String secureUrl = (String) result.get("secure_url");

            return new UploadedImage(publicId, secureUrl);
        } catch (IOException exception) {
            throw new RuntimeException("Khong the upload avatar len Cloudinary", exception);
        }
    }

    // Upload category image to Cloudinary and return public_id + secure_url.
    public UploadedImage uploadCategoryImage(MultipartFile file) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "svmarket/categories",
                            "resource_type", "image"));

            String publicId = (String) result.get("public_id");
            String secureUrl = (String) result.get("secure_url");

            return new UploadedImage(publicId, secureUrl);
        } catch (IOException exception) {
            throw new RuntimeException("Không thể tải ảnh danh mục lên Cloudinary", exception);
        }
    }

    // Xoa mot anh tren Cloudinary theo public_id.
    public void deleteImage(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException exception) {
            throw new RuntimeException("Khong the xoa anh tren Cloudinary", exception);
        }
    }

    public record UploadedImage(String publicId, String secureUrl) {
    }

    // Upload ảnh thẻ sinh viên lên Cloudinary and trả về public_id + secure_url.
    public UploadedImage uploadStudentCard(MultipartFile file) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "svmarket/student-cards",
                            "resource_type", "image"));

            return new UploadedImage(
                    (String) result.get("public_id"),
                    (String) result.get("secure_url"));

        } catch (IOException e) {
            throw new RuntimeException("Không thể upload thẻ sinh viên lên Cloudinary", e);
        }
    }
}
