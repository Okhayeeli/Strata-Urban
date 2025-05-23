package com.strataurban.strata.Security;

import com.strataurban.strata.Enums.EnumRoles;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityUserDetails {

    private Long id;
    private String firstName;
    private String middleName;
    private String lastName;
    private EnumRoles role;
    private String phoneNumber;
    private String emailAddress;
    private String username;
    private String city;
    private String state;
    private String country;

}
