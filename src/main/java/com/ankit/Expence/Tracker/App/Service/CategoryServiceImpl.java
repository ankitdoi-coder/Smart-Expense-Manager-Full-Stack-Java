package com.ankit.Expence.Tracker.App.Service;

import com.ankit.Expence.Tracker.App.Model.Expence;
import com.ankit.Expence.Tracker.App.Model.User;
import com.ankit.Expence.Tracker.App.Repository.ExpenceRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private ExpenceRepository expenceRepository;

    @Override
    public Map<String, Double> getCategoryTotals(User user) {
        List<Expence> expenses = expenceRepository.findByUser(user);

        // ✅ Group by category and sum amounts
        return expenses.stream()
                .collect(Collectors.groupingBy(
                        Expence::getCategory,
                        LinkedHashMap::new,
                        Collectors.summingDouble(Expence::getAmount)
                ));
    }

    @Override
    public Map<String, List<Expence>> getExpensesGroupedByCategory(User user) {
        List<Expence> expenses = expenceRepository.findByUser(user);

        // ✅ Group by category
        return expenses.stream()
                .collect(Collectors.groupingBy(
                        Expence::getCategory,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }
}
