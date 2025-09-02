package com.ankit.Expence.Tracker.App.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ankit.Expence.Tracker.App.Model.Expence;
import com.ankit.Expence.Tracker.App.Model.User;

public interface ExpenceService {

   Optional<Expence> findById(Long id);

   double totalMonthlyExpense(Long userId);

   double totalExpence(Long userId);

   void saveNewExpence(Expence expence);

   List<Expence> viewTransactions();

   long numberOfTransaction(User user);

   String topSpendingCategory(User user);

   double lastTransaction(User user);

   Expence lastTransactionDetails(User user);

   Map<String, Double> getCategoryWiseSpending(User user);

   List<Expence> getExpensesByUser(User user);

   public List<Expence> viewTransactionsByUser(Long userId);
}
