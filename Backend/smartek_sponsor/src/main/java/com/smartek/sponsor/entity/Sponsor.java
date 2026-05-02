package com.smartek.sponsor.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sponsors", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email", name = "unique_sponsor_email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sponsor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;

    private String companyName;

    private String industry;

    private String website;

    private String logoUrl;

    @Enumerated(EnumType.STRING)
    private SponsorStatus status;

    @Transient
    private String password;

    @OneToMany(mappedBy = "sponsor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Contract> contracts = new ArrayList<>();
}
