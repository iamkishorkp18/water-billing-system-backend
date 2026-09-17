package water_billing_platform.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import water_billing_platform.entity.SharedExpense;
import water_billing_platform.repository.SharedExpenseRepository;

@RestController
@RequestMapping("/shared-expenses")
@RequiredArgsConstructor
public class SharedExpenseController {

    private final SharedExpenseRepository sharedExpenseRepository;

    @PostMapping
    public SharedExpense createExpense(@RequestBody SharedExpense expense) {
        return sharedExpenseRepository.save(expense);
    }
}