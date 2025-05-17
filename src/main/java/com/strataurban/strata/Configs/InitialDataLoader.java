package com.strataurban.strata.Configs;

import com.strataurban.strata.Entities.User;
import com.strataurban.strata.Enums.EnumRoles;
import com.strataurban.strata.Repositories.v2.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class InitialDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public InitialDataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByRoles(EnumRoles.ADMIN).isEmpty()) {
            User admin = new User();
            admin.setFirstName("Admin");
            admin.setLastName("Admin");
            admin.setEmail("admin@strataurban.com");
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("cassillas1nengi!"));
//            admin.setPassword("cassillas1nengi!");
            admin.setPhone("+1234567890");
            admin.setRoles(EnumRoles.ADMIN);
            admin.setEmailVerified(true);

            User admin2 = new User();
            admin2.setFirstName("Admin");
            admin2.setLastName("Admin");
            admin2.setEmail("neuroleri");
            admin2.setUsername("neuroleri");
            admin2.setPassword(passwordEncoder.encode("password"));
//            admin.setPassword("cassillas1nengi!");
            admin2.setPhone("+1234567890");
            admin2.setRoles(EnumRoles.ADMIN);
            admin2.setEmailVerified(true);

            userRepository.save(admin);
            userRepository.save(admin2);

            System.out.println("Initial Admin user created");
        }
    }
}