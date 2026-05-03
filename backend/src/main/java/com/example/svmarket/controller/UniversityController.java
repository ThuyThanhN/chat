package com.example.svmarket.controller;

import com.example.svmarket.dto.UniversityJson;
import com.example.svmarket.service.UniversityService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/universities")
@CrossOrigin(origins = "http://localhost:5174")
public class UniversityController {

    private final UniversityService universityService;

    public UniversityController(UniversityService universityService) {
        this.universityService = universityService;
    }

    @GetMapping
    public List<UniversityJson> getAll() {
        return universityService.getAllUniversities();
    }
}