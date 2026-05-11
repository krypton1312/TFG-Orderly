package com.yebur.backendorderly.cashsessions;

import com.yebur.backendorderly.cashcount.CashCountRepository;
import com.yebur.backendorderly.cashcount.CashCountService;
import com.yebur.backendorderly.cashoperations.CashOperationRepository;
import com.yebur.backendorderly.orderdetail.OrderDetailRepository;
import com.yebur.backendorderly.websocket.WsEvent;
import com.yebur.backendorderly.websocket.WsEventType;
import com.yebur.backendorderly.websocket.WsNotifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CashSessionServiceWsNotifyTest {

    @Mock private CashSessionRepository cashSessionRepository;
    @Mock private CashCountRepository cashCountRepository;
    @Mock private CashCountService cashCountService;
    @Mock private CashOperationRepository cashOperationRepository;
    @Mock private OrderDetailRepository orderDetailRepository;
    @Mock private WsNotifier wsNotifier;

    @InjectMocks private CashSessionService cashSessionService;

    @Test
    void testReopen_BroadcastsSessionOpened() {
        CashSession cs = new CashSession();
        cs.setId(42L);
        cs.setBusinessDate(LocalDate.now().minusDays(1));
        cs.setShiftNo(1);
        cs.setOpenedAt(LocalDateTime.now().minusHours(5));
        cs.setClosedAt(LocalDateTime.now().minusHours(1));
        cs.setStatus(CashSessionStatus.CLOSED);

        when(cashSessionRepository.existsCashSessionByStatus(CashSessionStatus.OPEN)).thenReturn(false);
        when(cashSessionRepository.findFirstByStatusOrderByIdDesc(CashSessionStatus.CLOSED))
                .thenReturn(Optional.of(cs));
        when(cashSessionRepository.saveAndFlush(any(CashSession.class))).thenReturn(cs);

        cashSessionService.reopen();

        ArgumentCaptor<WsEvent> captor = ArgumentCaptor.forClass(WsEvent.class);
        verify(wsNotifier).send(captor.capture());
        WsEvent event = captor.getValue();
        assertEquals(WsEventType.SESSION_OPENED, event.getType());
        assertEquals(42L, event.getSessionId());
    }

    @Test
    void testOpen_BroadcastsSessionOpened() {
        // open() reads cashCountService.getLastCashCountTotal() (wrapped in try/catch)
        // then calls existsByBusinessDateAndStatus(businessDate, OPEN) and maxShiftNoByBusinessDate
        when(cashSessionRepository.existsByBusinessDateAndStatus(any(LocalDate.class),
                org.mockito.ArgumentMatchers.eq(CashSessionStatus.OPEN))).thenReturn(false);
        when(cashSessionRepository.maxShiftNoByBusinessDate(any(LocalDate.class))).thenReturn(0);
        when(cashSessionRepository.saveAndFlush(any(CashSession.class)))
                .thenAnswer(inv -> {
                    CashSession saved = inv.getArgument(0);
                    saved.setId(7L);
                    return saved;
                });

        cashSessionService.open();

        ArgumentCaptor<WsEvent> captor = ArgumentCaptor.forClass(WsEvent.class);
        verify(wsNotifier).send(captor.capture());
        WsEvent event = captor.getValue();
        assertEquals(WsEventType.SESSION_OPENED, event.getType());
        assertNotNull(event.getSessionId());
    }
}
