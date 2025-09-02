package com.ankit.Expence.Tracker.App.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Expence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private double amount;
    private String category;

    @Temporal(TemporalType.DATE)
    private java.sql.Date date;

    private String description;

    @ManyToOne
    @JoinColumn(name = "user_Id")
    private User user;

    // ✅ Safe toString (no recursion)
    @Override
    public String toString() {
        return "Expence{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                ", date=" + date +
                ", description='" + description + '\'' +
                ", userId=" + (user != null ? user.getId() : null) +
                '}';
    }
}
