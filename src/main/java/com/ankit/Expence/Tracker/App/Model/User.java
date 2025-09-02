package com.ankit.Expence.Tracker.App.Model;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Date;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "app_user") // <- avoid reserved keyword
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String password;
    private String pfpUrl;
    private Date createdAt;

    @OneToMany(mappedBy = "user")
    private List<Expence> expenses;

    // ✅ Custom toString to avoid recursion & hide password
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", pfpUrl='" + pfpUrl + '\'' +
                ", createdAt=" + createdAt +
                // ❌ DO NOT include password or expenses to avoid recursion
                '}';
    }
}
