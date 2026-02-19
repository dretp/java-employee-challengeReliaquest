package com.reliaquest.api.Interfaces;

import com.reliaquest.api.model.EmployeeModel;
import java.util.List;

public interface IEmployeeService {

    List<EmployeeModel> getAllEmployees();

    List<EmployeeModel> getEmployeesByNameSearch(String searchString);

    EmployeeModel getEmployeeById(String id);

    Integer getHighestSalaryOfEmployees();

    List<String> getTopTenHighestEarningEmployeeNames();

    String createEmployee(EmployeeModel employeeInput);

    String deleteEmployeeById(String id);
}
