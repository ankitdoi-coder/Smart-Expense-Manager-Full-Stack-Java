package com.ankit.Expence.Tracker.App.Controller;

import com.ankit.Expence.Tracker.App.Model.User;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.ankit.Expence.Tracker.App.Model.Expence;
import com.ankit.Expence.Tracker.App.Service.ExpenceService;
import com.ankit.Expence.Tracker.App.Service.UserService;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

@Controller
public class controller {
    @Autowired
    ExpenceService expenceService;

    @Autowired
    UserService userService;

    // To show the register page
    @GetMapping({ "/register", "/" })
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // to submit registere detail
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, BindingResult result) {
        // Validate user input
        if (result.hasErrors()) {
            return "register"; // Return to registration page if there are errors
        }
        userService.saveNewUser(user); // Save user logic
        return "redirect:/login"; // Redirect to login after successful registration
    }

    // to show the login page
    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

    // to show the Hompage(DashBoard) & Data on Homepage
    @GetMapping("/home")
    public String home(Model model, Principal principal) {
        model.addAttribute("description", "Welcome to your home page!");
        if (principal != null) {
            String email = principal.getName(); // finds the Email of the current user
            User user = userService.findByEmail(email);// the using email it findas the user

            model.addAttribute("name", user.getName());

            // ✅ To get total expenses MONTLY
            double total = expenceService.totalMonthlyExpense(user.getId());
            model.addAttribute("totalMonthlyExpenses", total);

            // ✅ To get number of Transaction
            long numTransaction = expenceService.numberOfTransaction(user);
            model.addAttribute("transactionCount", numTransaction);

            // ✅ To get Top Spending Category
            String topCategory = expenceService.topSpendingCategory(user);
            model.addAttribute("topCategory", topCategory);

            // ✅ To Get the Last Transaction
            double lastTransaction = expenceService.lastTransaction(user);
            model.addAttribute("lastTransactionInfo", lastTransaction);

            // ✅ To Get the Last Transaction Details
            Expence lastTransactionDetails = expenceService.lastTransactionDetails(user);

            if (lastTransactionDetails != null) {
                model.addAttribute("tx", lastTransactionDetails);
            } else {
                model.addAttribute("tx", null);
            }

            Map<String, Double> categoryWise = expenceService.getCategoryWiseSpending(user);
            model.addAttribute("categoryWise", categoryWise);

        }
        return "home";

    }

    // to show the add new expence page
    @GetMapping("/transactions/new/show")
    public String showAddNewExpencePage(Model model, Principal principal) {
        if (principal != null) {
            User loggedInUser = userService.findByEmail(principal.getName());
            model.addAttribute("name", loggedInUser.getName());
        }
        model.addAttribute("expence", new Expence());
        return "newTransactions";
    }

    // to save add new Expence or Transaction
    @PostMapping("/transactions/new")
    public String newTransaction(@ModelAttribute Expence expence, Principal principal) {
        // 1️⃣ Get the logged-in user's email
        String email = principal.getName();
        // 2️⃣ Find the user by email
        User user = userService.findByEmail(email);
        // 3️⃣ Link expense to user
        expence.setUser(user);

        // 4️⃣ Save the expense
        expenceService.saveNewExpence(expence);

        return "redirect:/home";
        // Will trigger /home which sets name again
    }

    // To show the "View Transactions"
    @GetMapping("/transactions")
    public String viewTransactions(@ModelAttribute Expence expence, Model model, Principal principal) {
        // current user
        String email = principal.getName();
        User loggedInUser = userService.findByEmail(email);

        if (loggedInUser == null) {
            return "redirect:/login"; // Redirect if not logged in
        }
        List<Expence> expences = expenceService.viewTransactionsByUser(loggedInUser.getId());

        System.out.println("Total Expences Fetched for user " + loggedInUser.getName() + " :- " + expences.size());
        for (Expence e : expences) {
            System.out.println("Expence Title:" + e.getTitle() + ",Amount:" + e.getAmount() +
                    ",Categroies:" + e.getCategory() + ",Date:" + e.getDate() +
                    ",Description:" + e.getDescription());
        }

        model.addAttribute("expences", expences);
        return "viewTransactions";
    }

    // logout
    @GetMapping("/logout")
    public String logout() {
        return "redirect:/login?logout";
    }

    // to show the Profile page
    @GetMapping("/profile")
    public String showProfile(Principal principal, Model model) {
        User loggedInUser = userService.findByEmail(principal.getName()); // fetch user from DB
        model.addAttribute("name", loggedInUser.getName());
        model.addAttribute("email", principal.getName());
        model.addAttribute("createdAt", loggedInUser.getCreatedAt());
        if (principal != null) {
            String email = principal.getName();
            User user = userService.findByEmail(email); // ✅ Pass userId and get total expenses double total =
            //for the all total
            double totalExpence=expenceService.totalExpence(user.getId());
            model.addAttribute("totalExpenses",totalExpence);
            //For the Monthly total                                           
            double totalMonthlyExpense = expenceService.totalMonthlyExpense(user.getId());
            model.addAttribute("totalMonthlyExpenses", totalMonthlyExpense);
        }
        return "profile";
    }
}