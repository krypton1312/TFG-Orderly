package com.yebur.backendorderly.entities;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.yebur.backendorderly.enums.EmployeeStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Name is required")
    private String name;

    @Column(nullable = false)
    @NotBlank(message = "Last name is required")
    private String lastname;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "employee_rol",
        joinColumns = @JoinColumn(name = "id_employee"),
        inverseJoinColumns = @JoinColumn(name = "id_role")
    )
    private Set<Role> roles;

    @Column
    private String phoneNumber;

    @Column(unique = true)
    @Email(message = "Email should be valid")
    private String email;

    @Column(nullable = false)
    private LocalDate hireDate;

    @Column
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be between 3 and 20 characters")
    private String password;

    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;

    @OneToMany(mappedBy = "employee")
    private List<Order> orders;

    @OneToMany(mappedBy = "employee")
    private List<ShiftRecord> shiftRecords;

}
