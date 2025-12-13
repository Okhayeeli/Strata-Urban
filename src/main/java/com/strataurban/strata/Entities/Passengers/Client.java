package com.strataurban.strata.Entities.Passengers;

import com.strataurban.strata.Entities.User;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "client")
@Entity
public class Client extends User {
    @Transient
    private transient String testToken;
}
