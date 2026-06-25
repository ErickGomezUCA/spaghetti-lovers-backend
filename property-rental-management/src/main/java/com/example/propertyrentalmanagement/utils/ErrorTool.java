package com.example.propertyrentalmanagement.utils;

import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Component
public class ErrorTool {
    public Map<String, List<String>> mapErrors(List<FieldError> fieldErrors) {
        Map<String, List<String>> errorsMap = new HashMap<>();

        fieldErrors.forEach(error -> {
            // Save the name of the field that has the error
            String field = error.getField();

            // Verify if the error list of the field exists, otherwise create it
            List<String> errors = errorsMap.getOrDefault(field, new ArrayList<>());

            // Add the error to the field error list
            errors.add(error.getDefaultMessage());

            // Update the field error list in the Map
            errorsMap.put(field, errors);
        });

        return errorsMap;
    }
}