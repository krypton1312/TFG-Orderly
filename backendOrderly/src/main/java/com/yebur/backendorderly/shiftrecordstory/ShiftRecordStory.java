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
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "shift_record_stories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftRecordStory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_shift_record", nullable = false)
    private ShiftRecord shiftRecord;

    @Column
    private LocalDateTime oldEntryRecord;

    @Column
    private LocalDateTime newEntryRecord;

    @Column
    private LocalDateTime oldExitRecord;

    @Column
    private LocalDateTime newExitRecord;

    @Column
    private String notes;

    @Column
    private LocalDateTime modificationDate;

    @OneToOne
    @JoinColumn(name = "id_employee", nullable = false)
    private Employee employee;

}
