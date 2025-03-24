package com.strataurban.strata.Entities.Passengers;

import com.strataurban.strata.Entities.User;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name = "client")
@Entity
public class Client extends User {
}
