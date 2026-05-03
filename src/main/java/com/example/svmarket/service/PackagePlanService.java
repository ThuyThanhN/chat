package com.example.svmarket.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.svmarket.dto.PackagePlanResponse;
import com.example.svmarket.entity.PackagePlan;
import com.example.svmarket.entity.PackageStatus;
import com.example.svmarket.repository.PackagePlanRepository;

@Service
public class PackagePlanService {

    @Autowired
    private PackagePlanRepository packagePlanRepository;

    // Lấy danh sách tất cả các gói hiện tại
    public List<PackagePlanResponse> getAllPlans() {
        return packagePlanRepository.findAll()
                .stream()
                .map(p -> new PackagePlanResponse(
                p.getId(),
                p.getName(),
                p.getPrice(),
                p.getPostLimit(),
                p.getPushLimit(),
                p.getPushHours(),
                p.getDurationDays(),
                p.getPriorityLevel(),
                p.getStatus() != null ? p.getStatus() : PackageStatus.ACTIVE
        ))
                .toList();
    }

    // them goi tin
    public PackagePlan create(PackagePlan p) {
        if (p.getStatus() == null) {
            p.setStatus(PackageStatus.ACTIVE);
        }
        if (p.getPostLimit() == null) {
            p.setPostLimit(0);
        }
        if (p.getPushLimit() == null) {
            p.setPushLimit(0);
        }
        if (p.getPushHours() == null) {
            p.setPushHours(0);
        }
        if (p.getPriorityLevel() == null) {
            p.setPriorityLevel(1);
        }
        if (p.getIsFeatured() == null) {
            p.setIsFeatured(false);
        }

        return packagePlanRepository.save(p);
    }

    //cap nhat goi tin
    public PackagePlan update(Integer id, PackagePlan newData) {
        PackagePlan p = packagePlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));

        p.setName(newData.getName());
        p.setPrice(newData.getPrice());
        p.setDurationDays(newData.getDurationDays());
        p.setStatus(newData.getStatus());

        // Cập nhật các trường mới thêm
        p.setPostLimit(newData.getPostLimit() != null ? newData.getPostLimit() : 0);
        p.setPushLimit(newData.getPushLimit() != null ? newData.getPushLimit() : 0);
        p.setPushHours(newData.getPushHours() != null ? newData.getPushHours() : 0);
        p.setPriorityLevel(newData.getPriorityLevel() != null ? newData.getPriorityLevel() : 1);
        p.setIsFeatured(newData.getIsFeatured() != null ? newData.getIsFeatured() : false);

        return packagePlanRepository.save(p);
    }

    //tim kiem va loc goi tin
    public Page<PackagePlan> search(
            String name,
            String status,
            Integer minPost,
            Integer maxPost,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        PackageStatus packageStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                packageStatus = PackageStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
            }
        }

        return packagePlanRepository.search(name, packageStatus, minPost, maxPost, pageable);
    }

    //template import goi tin
    public ByteArrayInputStream template() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Danh sách gói tin");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("Tên gói tin");
        header.createCell(2).setCellValue("Giá");
        header.createCell(3).setCellValue("Số bài đăng");
        header.createCell(4).setCellValue("Số lượt đẩy tin");
        header.createCell(5).setCellValue("Hiệu lực đẩy");
        header.createCell(6).setCellValue("Thời hạn gói");
        header.createCell(7).setCellValue("Mức độ ưu tiên");
        header.createCell(8).setCellValue("Mục đề xuất");
        header.createCell(9).setCellValue("Trạng thái");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return new ByteArrayInputStream(out.toByteArray());
    }

    //export goi tin
    public ByteArrayInputStream export() throws IOException {
        List<PackagePlan> plans = packagePlanRepository.findAll();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Danh sách gói tin");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("Tên gói tin");
        header.createCell(2).setCellValue("Giá");
        header.createCell(3).setCellValue("Số bài đăng");
        header.createCell(4).setCellValue("Số lượt đẩy tin");
        header.createCell(5).setCellValue("Hiệu lực đẩy");
        header.createCell(6).setCellValue("Thời hạn gói");
        header.createCell(7).setCellValue("Mức độ ưu tiên");
        header.createCell(8).setCellValue("Mục đề xuất");
        header.createCell(9).setCellValue("Trạng thái");

        int rowIdx = 1;
        for (PackagePlan p : plans) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(p.getId());
            row.createCell(1).setCellValue(p.getName());
            row.createCell(2).setCellValue(p.getPrice().doubleValue());
            row.createCell(3).setCellValue(p.getPostLimit());
            row.createCell(4).setCellValue(p.getPushLimit());
            row.createCell(5).setCellValue(p.getPushHours());
            row.createCell(6).setCellValue(p.getDurationDays());
            row.createCell(7).setCellValue(p.getPriorityLevel());
            row.createCell(8).setCellValue(p.getIsFeatured() ? "Có" : "Không");
            
            String statusVi = "Đang hoạt động";
            if (p.getStatus() != null) {
                if (p.getStatus() == PackageStatus.INACTIVE) {
                    statusVi = "Ngừng hoạt động";
                } else if (p.getStatus() == PackageStatus.EXPIRED) {
                    statusVi = "Hết hạn";
                }
            }
            row.createCell(9).setCellValue(statusVi);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return new ByteArrayInputStream(out.toByteArray());
    }

    //import goi tin tu file Excel
    public void importExcel(MultipartFile file) throws IOException {
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        DataFormatter formatter = new DataFormatter();
        
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            String name = formatter.formatCellValue(row.getCell(1)).trim();
            if (name.isEmpty()) continue;
            
            String priceStr = formatter.formatCellValue(row.getCell(2)).trim();
            BigDecimal price = priceStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(priceStr);
            
            String postStr = formatter.formatCellValue(row.getCell(3)).trim();
            int postLimit = postStr.isEmpty() ? 0 : Integer.parseInt(postStr);
            
            String pushStr = formatter.formatCellValue(row.getCell(4)).trim();
            int pushLimit = pushStr.isEmpty() ? 0 : Integer.parseInt(pushStr);
            
            String hoursStr = formatter.formatCellValue(row.getCell(5)).trim();
            int pushHours = hoursStr.isEmpty() ? 0 : Integer.parseInt(hoursStr);
            
            String durationStr = formatter.formatCellValue(row.getCell(6)).trim();
            int durationDays = durationStr.isEmpty() ? 0 : Integer.parseInt(durationStr);
            
            String priorityStr = formatter.formatCellValue(row.getCell(7)).trim();
            int priorityLevel = priorityStr.isEmpty() ? 1 : Integer.parseInt(priorityStr);
            
            boolean isFeatured = "Có".equalsIgnoreCase(formatter.formatCellValue(row.getCell(8)).trim());
            
            String statusStr = formatter.formatCellValue(row.getCell(9)).trim();
            PackageStatus status = PackageStatus.ACTIVE;
            if (statusStr.equalsIgnoreCase("Ngừng hoạt động") || statusStr.equalsIgnoreCase("INACTIVE")) {
                status = PackageStatus.INACTIVE;
            } else if (statusStr.equalsIgnoreCase("Hết hạn") || statusStr.equalsIgnoreCase("EXPIRED")) {
                status = PackageStatus.EXPIRED;
            }
            
            PackagePlan plan = new PackagePlan();
            plan.setName(name);
            plan.setPrice(price);
            plan.setPostLimit(postLimit);
            plan.setPushLimit(pushLimit);
            plan.setPushHours(pushHours);
            plan.setDurationDays(durationDays);
            plan.setPriorityLevel(priorityLevel);
            plan.setIsFeatured(isFeatured);
            plan.setStatus(status);
            
            packagePlanRepository.save(plan);
        }
        workbook.close();
    }
}
