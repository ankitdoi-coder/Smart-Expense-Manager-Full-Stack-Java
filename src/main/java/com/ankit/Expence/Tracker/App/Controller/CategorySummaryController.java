package com.ankit.Expence.Tracker.App.Controller;

import com.ankit.Expence.Tracker.App.Model.Expence;
import com.ankit.Expence.Tracker.App.Model.User;
import com.ankit.Expence.Tracker.App.Repository.ExpenceRepository;
import com.ankit.Expence.Tracker.App.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class CategorySummaryController {

    @Autowired
    private ExpenceRepository expenceRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/categories")
    public String showCategorySummary(Model model, Principal principal) {

        // ✅ 1. Get current logged-in user
        User loggedInUser = userService.findByEmail(principal.getName());

        // ✅ 2. Fetch only this user's expenses
        List<Expence> expenses = expenceRepository.findByUserId(loggedInUser.getId());

        // ✅ 3. Group expenses by category
        Map<String, List<Expence>> grouped = expenses.stream()
                .collect(Collectors.groupingBy(Expence::getCategory));

        // ✅ 4. Build summary list
        List<Map<String, Object>> summary = new ArrayList<>();

        for (String category : grouped.keySet()) {
            List<Expence> list = grouped.get(category);

            // Sort list by date DESC to get last expense
            list.sort(Comparator.comparing(Expence::getDate).reversed());
            Expence last = list.get(0);

            // Calculate total amount in this category
            double total = list.stream().mapToDouble(Expence::getAmount).sum();

            Map<String, Object> row = new HashMap<>();
            row.put("category", category);
            row.put("lastExpenseTitle", last.getTitle());
            row.put("lastExpenseDate", last.getDate());
            row.put("total", total);
            summary.add(row);
        }

        model.addAttribute("summary", summary);
        return "category";
    }
}
