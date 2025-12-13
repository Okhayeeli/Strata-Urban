package com.strataurban.strata.DTOs.v2;

import com.strataurban.strata.Enums.EnumRoles;
import com.strataurban.strata.Enums.ProviderRole;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserProfileResponse {

    private Long id;
    private String title;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private boolean emailVerified;
    private String username;
    private String phone;
    private String phone2;
    private String address;
    private String preferredLanguage;
    private String city;
    private String state;
    private String country;
    private EnumRoles roles;
    private String providerId;
    private String imageUrl;
    private ProviderRole providerRole;
    private Boolean selfCreated;
    private LocalDateTime lastActivity;

    private boolean canReceiveEmail;
    private boolean canReceiveSms;
    private boolean canReceivePush;
}
