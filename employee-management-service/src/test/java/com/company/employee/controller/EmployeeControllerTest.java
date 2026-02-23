package com.company.employee.controller;

import com.company.employee.dto.request.CreateEmployeeRequest;
import com.company.employee.dto.response.EmployeeResponse;
import com.company.employee.dto.response.PagedResponse;
import com.company.employee.exception.EmployeeNotFoundException;
import com.company.employee.exception.GlobalExceptionHandler;
import com.company.employee.model.entity.Employee.EmployeeStatus;
import com.company.employee.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
@Import(GlobalExceptionHandler.class)
@WithMockUser
@DisplayName("EmployeeController Integration Tests")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = new EmployeeResponse();
        sampleResponse.setId(1L);
        sampleResponse.setFirstName("Jane");
        sampleResponse.setLastName("Doe");
        sampleResponse.setFullName("Jane Doe");
        sampleResponse.setEmail("jane.doe@company.com");
        sampleResponse.setDepartment("Engineering");
        sampleResponse.setJobTitle("Software Engineer");
        sampleResponse.setSalary(new BigDecimal("90000.00"));
        sampleResponse.setHireDate(LocalDate.of(2022, 3, 15));
        sampleResponse.setStatus(EmployeeStatus.ACTIVE);
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/employees
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/v1/employees")
    class GetAllEmployees {

        @Test
        @DisplayName("should return 200 with paginated employees")
        void getAllEmployees_returns200() throws Exception {
            PagedResponse<EmployeeResponse> page = PagedResponse.<EmployeeResponse>builder()
                .content(List.of(sampleResponse))
                .page(0).size(20).totalElements(1).totalPages(1).last(true)
                .build();

            when(employeeService.getAllEmployees(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].email").value("jane.doe@company.com"));
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/employees/{id}
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/v1/employees/{id}")
    class GetEmployeeById {

        @Test
        @DisplayName("should return 200 when employee exists")
        void getById_found() throws Exception {
            when(employeeService.getEmployeeById(1L)).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/v1/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Jane Doe"));
        }

        @Test
        @DisplayName("should return 404 when employee not found")
        void getById_notFound() throws Exception {
            when(employeeService.getEmployeeById(99L))
                .thenThrow(new EmployeeNotFoundException(99L));

            mockMvc.perform(get("/api/v1/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
        }
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/employees
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /api/v1/employees")
    class CreateEmployee {

        @Test
        @DisplayName("should return 201 with created employee")
        void createEmployee_returns201() throws Exception {
            CreateEmployeeRequest request = new CreateEmployeeRequest();
            request.setFirstName("Jane");
            request.setLastName("Doe");
            request.setEmail("jane.doe@company.com");
            request.setDepartment("Engineering");
            request.setJobTitle("Software Engineer");

            when(employeeService.createEmployee(any(CreateEmployeeRequest.class)))
                .thenReturn(sampleResponse);

            mockMvc.perform(post("/api/v1/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("jane.doe@company.com"));
        }

        @Test
        @DisplayName("should return 400 when required fields are missing")
        void createEmployee_validationError() throws Exception {
            // Empty request — missing all required fields
            CreateEmployeeRequest request = new CreateEmployeeRequest();

            mockMvc.perform(post("/api/v1/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
        }

        @Test
        @DisplayName("should return 400 when email format is invalid")
        void createEmployee_invalidEmail() throws Exception {
            CreateEmployeeRequest request = new CreateEmployeeRequest();
            request.setFirstName("Jane");
            request.setLastName("Doe");
            request.setEmail("not-an-email");
            request.setDepartment("Engineering");
            request.setJobTitle("Software Engineer");

            mockMvc.perform(post("/api/v1/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'email')]").exists());
        }
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/employees/{id}
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("DELETE /api/v1/employees/{id}")
    class DeleteEmployee {

        @Test
        @DisplayName("should return 204 when employee deleted")
        void deleteEmployee_returns204() throws Exception {
            doNothing().when(employeeService).deleteEmployee(1L);

            mockMvc.perform(delete("/api/v1/employees/1").with(csrf()))
                .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should return 404 when employee not found")
        void deleteEmployee_notFound() throws Exception {
            doThrow(new EmployeeNotFoundException(99L)).when(employeeService).deleteEmployee(99L);

            mockMvc.perform(delete("/api/v1/employees/99").with(csrf()))
                .andExpect(status().isNotFound());
        }
    }
}
