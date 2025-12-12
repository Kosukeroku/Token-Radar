package kosukeroku.token_radar.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    private String password;
    private LocalDateTime createdAt;


    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<TrackedCurrency> trackedCurrencies = new ArrayList<>();
}