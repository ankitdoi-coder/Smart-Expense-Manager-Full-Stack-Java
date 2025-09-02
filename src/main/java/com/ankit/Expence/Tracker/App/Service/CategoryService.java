package com.ankit.Expence.Tracker.App.Service;

import com.ankit.Expence.Tracker.App.Model.Expence;
import com.ankit.Expence.Tracker.App.Model.User;

import java.util.List;
import java.util.Map;

public interface CategoryService {

    /**
     * Returns the total expense amount for each category for a given user.
     * Example: { "Food" -> 2500.0, "Transport" -> 1500.0 }
     */
    Map<String, Double> getCategoryTotals(User user);

    /**
     * Returns a map where key = category name and value = list of expenses in that category.
     * Example: { "Food" -> [Expence1, Expence2], "Transport" -> [Expence3] }
     */
    Map<String, List<Expence>> getExpensesGroupedByCategory(User user);
}
