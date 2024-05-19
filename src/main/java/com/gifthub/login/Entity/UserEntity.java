package com.gifthub.login.Entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@NoArgsConstructor
@DynamicInsert
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String identifier;
    private String name;
    private String email;

    @Builder
    public UserEntity(String identifier, String name, String email) {
        this.identifier = identifier;
        this.name = name;
        this.email = email;
    }

    public void update(String newName, String newEmail) {
        this.name = newName;
        this.email = newEmail;
    }

}
