package com.ankit.Expence.Tracker.App.Service;

import java.util.Optional;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List; // ✅ Correct List
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ankit.Expence.Tracker.App.Model.Expence;
import com.ankit.Expence.Tracker.App.Model.User;
import com.ankit.Expence.Tracker.App.Repository.ExpenceRepository;
import com.ankit.Expence.Tracker.App.Repository.UserRepository;

@Service
public class ExpenceServiceImpl implements ExpenceService {
    @Autowired
    ExpenceRepository repo;
    @Autowired
    UserRepository userRepo;

    @Override
    public Optional<Expence> findById(Long id) {
        Optional<Expence> ExpenceId = repo.findById(id);
        return ExpenceId;
    }

    @Override
    public double totalMonthlyExpense(Long userId) {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();

        Double total = repo.findMonthlyTotalByUser(userId, month, year);
        return total != null ? total : 0.0;
    }

    @Override
    public void saveNewExpence(Expence expence) {
        repo.save(expence);
    }

    @Override
    public List<Expence> viewTransactions() {
        return repo.findAll();
    }

    @Override
    public long numberOfTransaction(User loggedInUser) {
        return repo.countByUser(loggedInUser);
    }

    @Override
    public String topSpendingCategory(User user) {
        List<Object[]> result = repo.findCategorySpending(user);
        if (!result.isEmpty()) {
            return result.get(0)[0].toString(); // category name
        }
        return "No Expense Yet";
    }

    @Override
    public double lastTransaction(User user) {
        Expence lastExp = repo.findTopByUserOrderByIdDesc(user); // to get the last expence row
        if (lastExp != null) {
            return lastExp.getAmount();
        }
        return 0.0;
    }

    @Override
    public Expence lastTransactionDetails(User user) {
        Expence lastTransactionDetails = repo.findTopByUserOrderByIdDesc(user); // to get the last expence row
        if (lastTransactionDetails != null) {
            return lastTransactionDetails;
        }
        return null;

    }

    @Override
    public Map<String, Double> getCategoryWiseSpending(User user) {
        List<Object[]> data = repo.getCategoryWiseSpending(user);
        Map<String, Double> result = new HashMap<>();
        for (Object[] row : data) {
            String category = (String) row[0];
            Double total = (Double) row[1];
            result.put(category, total);
        }
        return result;
    }

    @Override
    public List<Expence> getExpensesByUser(User user) {
        return repo.findByUser(user);
    }

    public List<Expence> viewTransactionsByUser(Long userId) {
        return repo.findByUserId(userId);
    }

    @Override
    public double totalExpence(Long userId) {
        return repo.totalExpence(userId);
    }

}
