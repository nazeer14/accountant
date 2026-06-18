package com.pack.controller;


import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
@Tag(name = "Expense", description = "Expense Controller")
public class ExpenseController {

    @GetMapping
    public ResponseEntity<?> getExpense(String id){
        return ResponseEntity.ok("Success");
    }

    @PostMapping
    public ResponseEntity<?> createExpense(){
        return ResponseEntity.ok("Created");
    }

}
