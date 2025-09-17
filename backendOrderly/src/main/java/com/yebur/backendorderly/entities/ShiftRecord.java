package com.yebur.backendorderly.entities;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "shift_records")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShiftRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_employee", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;
    
    @Column
    private String notes;

    @OneToMany(mappedBy = "shiftRecord")
    private List<ShiftRecordStory> shiftRecordStories;
}
