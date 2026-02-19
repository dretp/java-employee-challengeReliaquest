package com.reliaquest.api.service;

import com.reliaquest.api.model.EmployeeModel;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    RestTemplate rest;

    private EmployeeService service;

    @BeforeEach
    void setUp() {
        service = new EmployeeService();
    }

    @Test
    void getTopTenHighestEarningEmployeeNames() throws Exception {
        // No mock response provided; service should handle null and return empty list
        List<String> result = service.getTopTenHighestEarningEmployeeNames();
        assertEquals(List.of(), result);
    }

    @Test
    void getHighestSalaryOfEmployees() throws Exception {
        // No mock response provided; service should return 0 when no data
        Integer max = service.getHighestSalaryOfEmployees();
        assertEquals(0, max);
    }

    @Test
    void getEmployeesByNameSearch() throws Exception {
        // No mock response provided; expect empty results
        List<EmployeeModel> matches = service.getEmployeesByNameSearch("and");
        assertEquals(0, matches.size());

        List<EmployeeModel> single = service.getEmployeesByNameSearch("sandra");
        assertEquals(0, single.size());
    }
}
