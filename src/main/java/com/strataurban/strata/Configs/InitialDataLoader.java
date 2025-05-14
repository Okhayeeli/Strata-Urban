package com.strataurban.strata.Configs;

import com.strataurban.strata.Entities.User;
import com.strataurban.strata.Enums.EnumRoles;
import com.strataurban.strata.Repositories.v2.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    public void run(String... args) throws Exception {
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
            admin.setFirstName("Admin");
            admin.setLastName("Admin");
            admin.setEmail("neuroleri");
            admin.setUsername("neuroleri");
            admin.setPassword(passwordEncoder.encode("password"));
//            admin.setPassword("cassillas1nengi!");
            admin.setPhone("+1234567890");
            admin.setRoles(EnumRoles.ADMIN);
            admin.setEmailVerified(true);
            userRepository.save(admin);

            System.out.println("Initial Admin user created");
        }
    }
}