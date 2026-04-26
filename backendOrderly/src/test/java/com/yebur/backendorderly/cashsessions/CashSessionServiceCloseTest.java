package com.yebur.backendorderly.cashsessions;

import com.yebur.backendorderly.cashcount.CashCount;
import com.yebur.backendorderly.cashcount.CashCountRepository;
import com.yebur.backendorderly.cashcount.CashCountService;
import com.yebur.backendorderly.cashoperations.CashOperationRepository;
import com.yebur.backendorderly.orderdetail.OrderDetailRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CashSessionServiceCloseTest {

    @Mock
    private CashSessionRepository cashSessionRepository;

    @Mock
    private CashCountRepository cashCountRepository;

    @Mock
    private CashCountService cashCountService;

    @Mock
    private CashOperationRepository cashOperationRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @InjectMocks
    private CashSessionService cashSessionService;

    @Test
    void testClose_ThrowsWhenSessionNotFound() {
        when(cashSessionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> cashSessionService.close(99L, new CashCount()));
    }

    @Test
    void testClose_PersistsCashCount() {
        CashSession cs = new CashSession();
        cs.setId(1L);
        cs.setBusinessDate(LocalDate.now());
        cs.setShiftNo(1);
        cs.setOpenedAt(LocalDateTime.now().minusHours(1));
        cs.setCashStart(BigDecimal.ZERO);
        cs.setStatus(CashSessionStatus.OPEN);

        when(cashSessionRepository.findById(1L)).thenReturn(Optional.of(cs));
        when(cashCountService.getTotal(any(CashCount.class))).thenReturn(BigDecimal.ZERO);
        when(orderDetailRepository.getPaidSalesByCashSessionAndPaymentMethod(eq(1L), eq("CASH")))
                .thenReturn(BigDecimal.ZERO);
        when(orderDetailRepository.getPaidSalesByCashSessionAndPaymentMethod(eq(1L), eq("CARD")))
                .thenReturn(BigDecimal.ZERO);
        when(cashOperationRepository.getNetAmountByCashSession(1L)).thenReturn(BigDecimal.ZERO);
        when(cashSessionRepository.save(any(CashSession.class))).thenReturn(cs);

        cashSessionService.close(1L, new CashCount());

        verify(cashCountRepository).save(any(CashCount.class));
    }

    @Test
    void testClose_ComputesDifference() {
        // cashExpected = cashStart + cashSales + opsNet = 100 + 30 + 0 = 130
        // totalExpected = cashExpected + cardSales = 130 + 20 = 150
        // cashActual (stubbed) = 200
        // totalActual = cashActual + cardSales = 200 + 20 = 220
        // difference = totalActual - totalExpected = 220 - 150 = 70
        CashSession cs = new CashSession();
        cs.setId(2L);
        cs.setBusinessDate(LocalDate.now());
        cs.setShiftNo(1);
        cs.setOpenedAt(LocalDateTime.now().minusHours(2));
        cs.setCashStart(new BigDecimal("100.00"));
        cs.setStatus(CashSessionStatus.OPEN);

        when(cashSessionRepository.findById(2L)).thenReturn(Optional.of(cs));
        when(cashCountService.getTotal(any(CashCount.class))).thenReturn(new BigDecimal("200.00"));
        when(orderDetailRepository.getPaidSalesByCashSessionAndPaymentMethod(eq(2L), eq("CASH")))
                .thenReturn(new BigDecimal("30.00"));
        when(orderDetailRepository.getPaidSalesByCashSessionAndPaymentMethod(eq(2L), eq("CARD")))
                .thenReturn(new BigDecimal("20.00"));
        when(cashOperationRepository.getNetAmountByCashSession(2L)).thenReturn(BigDecimal.ZERO);
        when(cashSessionRepository.save(any(CashSession.class))).thenReturn(cs);

        CashSessionResponse result = cashSessionService.close(2L, new CashCount());

        assertEquals(0, new BigDecimal("70.00").compareTo(result.getDifference()),
                "Expected difference of 70.00 but got: " + result.getDifference());
    }
}
