package com.reliaquest.api.service;

import com.reliaquest.api.Interfaces.IEmployeeService;
import com.reliaquest.api.exception.EmployeeCreationException;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.model.EmployeeModel;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.dto.ServerResponse;

import java.io.IOException;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class EmployeeService implements IEmployeeService {

    private static final String BASE = "http://localhost:8112/api/v1/employee";
    private RestTemplate reliaQuestRest;
    private ObjectMapper mapper;
    private EmployeeService Instance;

    public EmployeeService() {
        init();
        mapper = new ObjectMapper();
    }

    private RestTemplate reliaQuestSvc() {

        if (reliaQuestRest == null) {
            reliaQuestRest = new RestTemplate();

        }
        return reliaQuestRest;
    }

    private void init() {
        if (Instance == null) {
            Instance = this;
        }

    }


    public EmployeeService getInstance() {
        return Instance;
    }


    ///
    private List<EmployeeModel> fetchAllInternal() {
        try {
            String json = reliaQuestSvc().getForObject(BASE, String.class);
            if (json == null) return List.of();

            ServerResponse<List<EmployeeModel>> resp = mapper.readValue(json, new TypeReference<ServerResponse<List<EmployeeModel>>>() {
            });

            return resp.getData() == null ? List.of() : resp.getData();
        } catch (RestClientException | IOException e) {
            throw new RuntimeException("Failed to fetch employees", e);
        }
    }

    @Override
    public List<EmployeeModel> getAllEmployees() {
        return fetchAllInternal();
    }

    @Override
    public List<EmployeeModel> getEmployeesByNameSearch(String searchString) {
        String lower = searchString == null ? "" : searchString.toLowerCase();
        return getAllEmployees().stream()
                .filter(e -> e.getName() != null && e.getName().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeModel getEmployeeById(String id) {
        try {
            String json = reliaQuestSvc().getForObject(BASE + "/" + id, String.class);

            if (json == null) throw new EmployeeNotFoundException("Employee not found: " + id);

            ServerResponse<EmployeeModel> resp = mapper.readValue(json, new TypeReference<ServerResponse<EmployeeModel>>() {
            });
            EmployeeModel emp = resp.getData();
            if (emp == null) throw new EmployeeNotFoundException("Employee not found: " + id);
            return emp;
        }
        catch (RestClientException | IOException e) {
            throw new RuntimeException("Failed to fetch employee by id", e);
        }
    }

    @Override
    public Integer getHighestSalaryOfEmployees() {
        return getAllEmployees().stream()
                .map(EmployeeModel::getSalary)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(0);
    }

    @Override
    public List<String> getTopTenHighestEarningEmployeeNames() {
        return getAllEmployees().stream()
                .filter(e -> e.getSalary() != null)
                // sort by salary desc, then by name asc to deterministically break ties
                .sorted(Comparator.comparing(EmployeeModel::getSalary, Comparator.nullsLast(Comparator.naturalOrder())).reversed()
                        .thenComparing(e -> e.getName() == null ? "" : e.getName()))
                .limit(10)
                .map(EmployeeModel::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     *
     * @param employeeInput
     * @return the employee id
     */


    //I'm not returning the whole object just the empId so we can do something with that later on
    // and save on memory resources
    @Override
    public String createEmployee(EmployeeModel employeeInput) {
        try {
            Map<String, Object> payload = buildCreatePayload(employeeInput);

            String json = reliaQuestSvc().postForObject(BASE, new org.springframework.http.HttpEntity<>(payload, createJsonHeaders()), String.class);

            if (json == null) throw new EmployeeCreationException("Failed to create employee");

            ServerResponse<EmployeeModel> resp = mapper.readValue(json, new TypeReference<ServerResponse<EmployeeModel>>() {
            });
            EmployeeModel createdEmp = resp.getData();

            if (createdEmp == null) throw new EmployeeCreationException("Failed to create employee");

            return createdEmp.getId().toString();
        }
        catch (RestClientException | IOException e) {
            throw new RuntimeException("Failed to create employee", e);
        }
    }

    // helper function used to create employee
    private Map<String, Object> buildCreatePayload(EmployeeModel employeeInput) {
        if (employeeInput == null || employeeInput.getName() == null || employeeInput.getName().isBlank()) {
            throw new EmployeeCreationException("Invalid employee input");
        }
        return Map.of(
                "name", employeeInput.getName(),
                "salary", employeeInput.getSalary(),
                "age", employeeInput.getAge(),
                "title", employeeInput.getTitle());
    }

    @Override
    public String deleteEmployeeById(String id) {
        EmployeeModel emp = getEmployeeById(id);
        try {
            String delEmpById = reliaQuestSvc().execute(BASE + "/" + emp.getName(), org.springframework.http.HttpMethod.DELETE, null, clientHttpResponse -> {
                return new String(clientHttpResponse.getBody().readAllBytes());
            });

            if (delEmpById == null) throw new RuntimeException("Failed to delete employee: " + emp.getName());
            ServerResponse<Boolean> delResp = mapper.readValue(delEmpById, new TypeReference<ServerResponse<Boolean>>() {
            });

            if (Boolean.TRUE.equals(delResp.getData())) return emp.getName();

            throw new RuntimeException("Failed to delete employee: " + emp.getName());

        }
        catch (RestClientException | IOException e) {
            throw new RuntimeException("Failed to delete employee", e);
        }
    }

    private org.springframework.http.HttpHeaders createJsonHeaders() {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();

        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        return headers;
    }

}
