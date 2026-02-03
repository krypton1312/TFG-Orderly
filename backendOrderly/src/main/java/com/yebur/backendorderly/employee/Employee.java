package com.yebur.backendorderly.employee;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.yebur.backendorderly.order.Order;
import com.yebur.backendorderly.role.Role;
import com.yebur.backendorderly.shiftrecord.ShiftRecord;

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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Name is required")
    @ToString.Include
    private String name;

    @Column(nullable = false)
    @NotBlank(message = "Last name is required")
    @ToString.Include
    private String lastname;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "employee_rol",
        joinColumns = @JoinColumn(name = "id_employee"),
        inverseJoinColumns = @JoinColumn(name = "id_role")
    )
    private Set<Role> roles;

    @Column
    @ToString.Include
    private String phoneNumber;

    @Column(unique = true)
    @Email(message = "Email should be valid")
    @ToString.Include
    private String email;

    @Column(nullable = false)
    @ToString.Include
    private LocalDate hireDate;

    @Column
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be between 3 and 20 characters")
    private String password;

    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    @ToString.Include
    private EmployeeStatus status;

    @OneToMany(mappedBy = "employee")
    private List<Order> orders;

    @OneToMany(mappedBy = "employee")
    private List<ShiftRecord> shiftRecords;

}
