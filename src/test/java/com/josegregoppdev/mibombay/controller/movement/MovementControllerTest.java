package com.josegregoppdev.mibombay.controller.movement;

import com.josegregoppdev.mibombay.common.tenant.TenantContext;
import com.josegregoppdev.mibombay.dto.movement.MovementDTO;
import com.josegregoppdev.mibombay.model.movement.MovementType;
import com.josegregoppdev.mibombay.service.movement.MovementService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.josegregoppdev.mibombay.testdata.TestDataFactory.createMovementDTO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovementControllerTest {

    @Mock
    private MovementService movementService;

    @InjectMocks
    private MovementController controller;

    private static final String TENANT_ID = "tnt_test1234567890123456789012345678";

    @BeforeEach
    void setUp() {
        TenantContext.set(TENANT_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void list_noFilters_addsAttributesAndReturnsView() {
        Pageable pageable = PageRequest.of(0, 20);
        MovementDTO dto = createMovementDTO();
        Page<MovementDTO> page = new PageImpl<>(List.of(dto));
        when(movementService.getMovementsByFilters(eq(TENANT_ID), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        Model model = new ExtendedModelMap();
        String view = controller.list(null, null, null, pageable, model);

        assertEquals("movement/list", view);
        assertTrue(model.containsAttribute("page"));
        assertTrue(model.containsAttribute("types"));
        assertTrue(model.containsAttribute("selectedType"));
        assertTrue(model.containsAttribute("from"));
        assertTrue(model.containsAttribute("to"));
    }

    @Test
    void list_withFilters_convertsDatesAndPassesToService() {
        Pageable pageable = PageRequest.of(0, 20);
        when(movementService.getMovementsByFilters(
                eq(TENANT_ID), eq(MovementType.SALE),
                eq(LocalDateTime.of(2026, 8, 1, 0, 0)),
                eq(LocalDateTime.of(2026, 8, 31, 23, 59, 59)),
                any(Pageable.class)))
                .thenReturn(Page.empty());

        Model model = new ExtendedModelMap();
        String view = controller.list(MovementType.SALE,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31), pageable, model);

        assertEquals("movement/list", view);
        assertEquals(MovementType.SALE, model.getAttribute("selectedType"));
        assertEquals(LocalDate.of(2026, 8, 1), model.getAttribute("from"));
        assertEquals(LocalDate.of(2026, 8, 31), model.getAttribute("to"));
    }

    @Test
    void list_withoutDates_passesNullDateTimes() {
        Pageable pageable = PageRequest.of(0, 20);
        when(movementService.getMovementsByFilters(eq(TENANT_ID), eq(MovementType.PURCHASE), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(Page.empty());

        Model model = new ExtendedModelMap();
        String view = controller.list(MovementType.PURCHASE, null, null, pageable, model);

        assertEquals("movement/list", view);
        verify(movementService).getMovementsByFilters(TENANT_ID, MovementType.PURCHASE, null, null, pageable);
    }
}
