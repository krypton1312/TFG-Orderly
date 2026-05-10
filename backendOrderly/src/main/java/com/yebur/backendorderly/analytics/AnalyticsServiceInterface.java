package com.yebur.backendorderly.analytics;

import java.time.YearMonth;

public interface AnalyticsServiceInterface {
    MonthlySummaryResponse getMonthlySummary(YearMonth yearMonth);
}
