package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.EmployeeRequestDto;
import com.example.employeemanagement.dto.EmployeeResponseDto;
import com.example.employeemanagement.dto.PagedResponseDto;
import com.example.employeemanagement.exception.EmployeeNotFoundException;
import com.example.employeemanagement.exception.GlobalExceptionHandler;
import com.example.employeemanagement.model.Employee.EmploymentStatus;
import com.example.employeemanagement.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("EmployeeController MVC Tests")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeResponseDto sampleResponse;
    private EmployeeRequestDto validRequest;

    @BeforeEach
    void setUp() {
        validRequest = EmployeeRequestDto.builder()
            .firstName("Jane")
            .lastName("Doe")
            .email("jane.doe@example.com")
            .department("Engineering")
            .jobTitle("Software Engineer")
            .employmentStatus(EmploymentStatus.ACTIVE)
            .hireDate(LocalDate.of(2023, 6, 15))
            .salary(new BigDecimal("90000.00"))
            .build();

        sampleResponse = EmployeeResponseDto.builder()
            .id(1L)
            .employeeNumber("EMP-000001")
            .firstName("Jane")
            .lastName("Doe")
            .fullName("Jane Doe")
            .email("jane.doe@example.com")
            .department("Engineering")
            .jobTitle("Software Engineer")
            .employmentStatus(EmploymentStatus.ACTIVE)
            .hireDate(LocalDate.of(2023, 6, 15))
            .salary(new BigDecimal("90000.00"))
            .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/employees — 201 Created")
    void createEmployee_returns201() throws Exception {
        when(employeeService.createEmployee(any())).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.employeeNumber").value("EMP-000001"))
            .andExpect(jsonPath("$.fullName").value("Jane Doe"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/employees — 400 on invalid payload")
    void createEmployee_returns400OnInvalidPayload() throws Exception {
        EmployeeRequestDto badRequest = EmployeeRequestDto.builder()
            .email("not-an-email")  // invalid email
            // missing required fields
            .build();

        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(badRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("GET /api/v1/employees/{id} — 200 OK")
    void getEmployeeById_returns200() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/employees/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.email").value("jane.doe@example.com"));
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("GET /api/v1/employees/{id} — 404 when not found")
    void getEmployeeById_returns404() throws Exception {
        when(employeeService.getEmployeeById(99L))
            .thenThrow(new EmployeeNotFoundException("Employee not found with id: 99"));

        mockMvc.perform(get("/api/v1/employees/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("Employee not found with id: 99"));
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("GET /api/v1/employees — 200 with paged results")
    void getAllEmployees_returns200() throws Exception {
        PagedResponseDto<EmployeeResponseDto> page = PagedResponseDto.<EmployeeResponseDto>builder()
            .content(List.of(sampleResponse))
            .page(0)
            .size(20)
            .totalElements(1)
            .totalPages(1)
            .last(true)
            .build();

        when(employeeService.getAllEmployees(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/employees"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/v1/employees/{id} — 200 on update")
    void updateEmployee_returns200() throws Exception {
        when(employeeService.updateEmployee(eq(1L), any())).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/v1/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/v1/employees/{id} — 204 No Content")
    void deleteEmployee_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/employees/1").with(csrf()))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/v1/employees — 401 when unauthenticated")
    void getAllEmployees_returns401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
            .andExpect(status().isUnauthorized());
    }
}
