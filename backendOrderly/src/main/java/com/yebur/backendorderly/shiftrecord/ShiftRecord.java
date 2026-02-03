package com.yebur.backendorderly.shiftrecord;

import java.time.LocalDateTime;
import java.util.List;

import com.yebur.backendorderly.employee.Employee;
import com.yebur.backendorderly.shiftrecordstory.ShiftRecordStory;

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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "shift_records")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class ShiftRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_employee", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    @ToString.Include
    private LocalDateTime startTime;

    @Column
    @ToString.Include
    private LocalDateTime endTime;
    
    @Column
    @ToString.Include
    private String notes;

    @OneToMany(mappedBy = "shiftRecord")
    private List<ShiftRecordStory> shiftRecordStories;
}
