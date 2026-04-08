package com.pratik.bankingsystem.user.entity;

import com.pratik.bankingsystem.common.entity.BaseEntity;
import com.pratik.bankingsystem.common.enums.Role;
import com.pratik.bankingsystem.common.enums.UserStatus;
import com.pratik.bankingsystem.customer.entity.Customer;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Customer customer;
}