package com.example.svmarket.service;

import com.example.svmarket.dto.UniversityJson;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Service
public class UniversityService {

    private List<UniversityJson> universities;

    @PostConstruct
    public void init() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        InputStream is = getClass()
                .getResourceAsStream("/data/universities_vn.json");

        universities = Arrays.asList(
                mapper.readValue(is, UniversityJson[].class)
        );
    }

    public String findUniversityByEmail(String email) {

        String domain
                = email.substring(email.indexOf("@") + 1)
                        .toLowerCase();

        return universities.stream()
                .filter(x -> x.getAbbr() != null)
                .filter(x -> {
                    String abbr
                            = x.getAbbr()
                                    .split("/")[0]
                                    .trim()
                                    .toLowerCase();

                    return domain.contains(abbr);
                })
                .map(UniversityJson::getName)
                .findFirst()
                .orElse(null);
    }

    //danh sach tat ca truong
    public List<UniversityJson> getAllUniversities() {
        return universities;
    }
}
