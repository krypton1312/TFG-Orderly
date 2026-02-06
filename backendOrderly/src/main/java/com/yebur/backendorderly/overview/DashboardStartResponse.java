package com.yebur.backendorderly.overview;

import com.yebur.backendorderly.employee.EmployeeResponse;
import com.yebur.backendorderly.shiftrecord.ShiftRecordResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStartResponse {
    private EmployeeResponse employee;
    private int availableTables;
    private int ocuppatedTables;
    private ShiftRecordResponse shiftRecord;
}
