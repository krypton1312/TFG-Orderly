package com.yebur.backendorderly.shiftrecordstory;


import java.time.LocalDateTime;

import com.yebur.backendorderly.employee.Employee;
import com.yebur.backendorderly.shiftrecord.ShiftRecord;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "shift_record_stories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class ShiftRecordStory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_shift_record", nullable = false)
    private ShiftRecord shiftRecord;

    @Column
    @ToString.Include
    private LocalDateTime oldEntryRecord;

    @Column
    @ToString.Include
    private LocalDateTime newEntryRecord;

    @Column
    @ToString.Include
    private LocalDateTime oldExitRecord;

    @Column
    @ToString.Include
    private LocalDateTime newExitRecord;

    @Column
    @ToString.Include
    private String notes;

    @Column
    @ToString.Include
    private LocalDateTime modificationDate;

    @OneToOne
    @JoinColumn(name = "id_employee", nullable = false)
    private Employee employee;

}
