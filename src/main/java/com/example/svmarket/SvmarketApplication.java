package com.example.svmarket;

import com.example.svmarket.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;

@SpringBootApplication
public class SvmarketApplication implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    public static void main(String[] args) {
        SpringApplication.run(SvmarketApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        long count = userRepository.count();
        System.out.println("Ket noi database thanh cong: " + count);
    }
}