package com.example.employee.controller;

import com.example.employee.dto.ApiResponse;
import com.example.employee.dto.EmployeeRequest;
import com.example.employee.dto.EmployeeResponse;
import com.example.employee.dto.PagedResponse;
import com.example.employee.exception.ResourceNotFoundException;
import com.example.employee.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice test for {@link EmployeeController}.
 * Only loads the Web MVC layer; the service is mocked.
 */
@WebMvcTest(EmployeeController.class)
@ActiveProfiles("test")
class EmployeeControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean EmployeeService employeeService;

    private EmployeeResponse sampleResponse;
    private EmployeeRequest validRequest;

    @BeforeEach
    void setUp() {
        sampleResponse = EmployeeResponse.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Doe")
                .fullName("Jane Doe")
                .email("jane.doe@example.com")
                .jobTitle("Senior Engineer")
                .salary(new BigDecimal("95000"))
                .build();

        validRequest = EmployeeRequest.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .jobTitle("Senior Engineer")
                .salary(new BigDecimal("95000"))
                .hireDate(LocalDate.of(2022, 1, 10))
                .build();
    }

    // -----------------------------------------------------------------------
    // GET /api/v1/employees
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/v1/employees")
    class ListEmployeesTests {

        @Test
        @WithMockUser
        @DisplayName("returns 200 with paginated list")
        void returns200WithList() throws Exception {
            PagedResponse<EmployeeResponse> paged = PagedResponse.<EmployeeResponse>builder()
                    .content(List.of(sampleResponse))
                    .page(0).size(20).totalElements(1).totalPages(1)
                    .first(true).last(true)
                    .build();

            when(employeeService.getEmployees(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(paged);

            mockMvc.perform(get("/api/v1/employees").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content", hasSize(1)))
                    .andExpect(jsonPath("$.data.content[0].email").value("jane.doe@example.com"))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("returns 401 when not authenticated")
        void returns401WhenUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/employees"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // -----------------------------------------------------------------------
    // GET /api/v1/employees/{id}
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/v1/employees/{id}")
    class GetByIdTests {

        @Test
        @WithMockUser
        @DisplayName("returns 200 when employee found")
        void returns200WhenFound() throws Exception {
            when(employeeService.getEmployeeById(1L)).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/v1/employees/1").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.fullName").value("Jane Doe"));
        }

        @Test
        @WithMockUser
        @DisplayName("returns 404 when employee not found")
        void returns404WhenNotFound() throws Exception {
            when(employeeService.getEmployeeById(99L))
                    .thenThrow(new ResourceNotFoundException("Employee not found with id=99"));

            mockMvc.perform(get("/api/v1/employees/99").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("99")));
        }
    }

    // -----------------------------------------------------------------------
    // POST /api/v1/employees
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("POST /api/v1/employees")
    class CreateEmployeeTests {

        @Test
        @WithMockUser
        @DisplayName("returns 201 with created employee")
        void returns201OnCreate() throws Exception {
            when(employeeService.createEmployee(any(EmployeeRequest.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(post("/api/v1/employees")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value("jane.doe@example.com"));
        }

        @Test
        @WithMockUser
        @DisplayName("returns 400 when required fields are missing")
        void returns400OnValidationFailure() throws Exception {
            EmployeeRequest badRequest = EmployeeRequest.builder()
                    .email("not-an-email")   // invalid
                    // firstName, lastName, jobTitle, salary, hireDate all missing
                    .build();

            mockMvc.perform(post("/api/v1/employees")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(badRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors").isArray());
        }
    }

    // -----------------------------------------------------------------------
    // DELETE /api/v1/employees/{id}
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("DELETE /api/v1/employees/{id}")
    class DeleteEmployeeTests {

        @Test
        @WithMockUser
        @DisplayName("returns 200 on successful soft-delete")
        void returns200OnDelete() throws Exception {
            doNothing().when(employeeService).deleteEmployee(1L);

            mockMvc.perform(delete("/api/v1/employees/1").with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser
        @DisplayName("returns 404 when employee not found")
        void returns404WhenNotFound() throws Exception {
            doThrow(new ResourceNotFoundException("Employee not found with id=99"))
                    .when(employeeService).deleteEmployee(99L);

            mockMvc.perform(delete("/api/v1/employees/99").with(csrf()))
                    .andExpect(status().isNotFound());
        }
    }
}
