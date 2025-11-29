package com.strataurban.strata.Security;

import com.strataurban.strata.Entities.User;
import com.strataurban.strata.Repositories.v2.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityUserDetailsService {

    private final UserRepository userRepository;

    public SecurityUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public SecurityUserDetails getSecurityUserDetails() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalStateException("User not found for username: " + username));

        return new SecurityUserDetails(user.getId(), user.getFirstName(), user.getMiddleName(), user.getLastName(), user.getRoles(), user.getPhone(), user.getEmail(), user.getUsername(), user.getCity(), user.getState(), user.getCountry(), user.getFirstName() + " " + user.getMiddleName() + " " + user.getLastName()
        );
    }
}
