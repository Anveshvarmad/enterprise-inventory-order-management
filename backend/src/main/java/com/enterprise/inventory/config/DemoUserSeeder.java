package com.enterprise.inventory.config;

import com.enterprise.inventory.entity.AppUser;
import com.enterprise.inventory.enums.UserRole;
import com.enterprise.inventory.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DemoUserSeeder implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoUserSeeder(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        createUserIfMissing(
                "Admin User",
                "admin@inventory.com",
                "admin123",
                UserRole.ADMIN
        );

        createUserIfMissing(
                "Inventory Manager",
                "manager@inventory.com",
                "manager123",
                UserRole.MANAGER
        );

        createUserIfMissing(
                "Warehouse Staff",
                "warehouse@inventory.com",
                "staff123",
                UserRole.WAREHOUSE_STAFF
        );

        createUserIfMissing(
                "Sales User",
                "sales@inventory.com",
                "sales123",
                UserRole.SALES_USER
        );

        createUserIfMissing(
                "Support User",
                "support@inventory.com",
                "support123",
                UserRole.CUSTOMER_SUPPORT
        );
    }

    private void createUserIfMissing(
            String fullName,
            String email,
            String password,
            UserRole role
    ) {
        if (appUserRepository.findByEmailIgnoreCase(email).isPresent()) {
            return;
        }

        AppUser user = new AppUser();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setEnabled(true);

        appUserRepository.save(user);
    }
}
