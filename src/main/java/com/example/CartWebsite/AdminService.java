package com.example.CartWebsite;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    public Admin findByEmail(String email) {
        return adminRepository.findByEmail(email);
    }

    public void save(Admin admin) {
        adminRepository.save(admin);
    }

    public long countAllAdmins() {
        return adminRepository.count();
    }
}
