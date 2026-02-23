package com.example.employeemanagement.integration;

import com.example.employeemanagement.dto.EmployeeRequestDto;
import com.example.employeemanagement.model.Employee.EmploymentStatus;
import com.example.employeemanagement.repository.EmployeeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full Spring context integration tests against an in-memory H2 database.
 *
 * <p>For Testcontainers-based tests (real PostgreSQL), see the /testcontainers
 * package (not included by default to keep CI fast without Docker).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Employee Integration Tests")
class EmployeeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void cleanUp() {
        employeeRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Full lifecycle: create → get → update → delete")
    void fullEmployeeLifecycle() throws Exception {
        // 1. Create
        EmployeeRequestDto createRequest = buildRequest("alice@example.com");

        String createResponse = mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("alice@example.com"))
            .andExpect(jsonPath("$.employeeNumber").exists())
            .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createResponse).get("id").asLong();

        // 2. Get by ID
        mockMvc.perform(get("/api/v1/employees/" + id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id));

        // 3. Update
        EmployeeRequestDto updateRequest = buildRequest("alice@example.com");
        updateRequest.setJobTitle("Principal Engineer");

        mockMvc.perform(put("/api/v1/employees/" + id)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.jobTitle").value("Principal Engineer"));

        // 4. Delete
        mockMvc.perform(delete("/api/v1/employees/" + id).with(csrf()))
            .andExpect(status().isNoContent());

        // 5. Verify it's gone
        assertThat(employeeRepository.findById(id)).isEmpty();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 409 on duplicate email")
    void shouldReturn409OnDuplicateEmail() throws Exception {
        EmployeeRequestDto request = buildRequest("duplicate@example.com");

        // First creation succeeds
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        // Second creation with same email fails
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("Search should return matching employees")
    void searchShouldReturnMatchingEmployees() throws Exception {
        EmployeeRequestDto emp = buildRequest("search.test@example.com");
        emp.setDepartment("DataScience");

        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emp)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/employees/search?query=DataScience"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].department").value("DataScience"));
    }

    // ---

    private EmployeeRequestDto buildRequest(String email) {
        return EmployeeRequestDto.builder()
            .firstName("Alice")
            .lastName("Example")
            .email(email)
            .department("Engineering")
            .jobTitle("Software Engineer")
            .employmentStatus(EmploymentStatus.ACTIVE)
            .hireDate(LocalDate.of(2023, 1, 10))
            .salary(new BigDecimal("88000.00"))
            .build();
    }
}
