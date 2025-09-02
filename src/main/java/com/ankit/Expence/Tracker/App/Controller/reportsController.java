package com.ankit.Expence.Tracker.App.Controller;

import com.ankit.Expence.Tracker.App.dto.ReportView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.ankit.Expence.Tracker.App.Model.Expence;
import com.ankit.Expence.Tracker.App.Model.User;
import com.ankit.Expence.Tracker.App.Service.ExpenceService;
import com.ankit.Expence.Tracker.App.Service.ReportsService;
import com.ankit.Expence.Tracker.App.Service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class reportsController {

    private final UserService userService;
    private final ReportsService reportsService;
    private final ExpenceService expenceService;

    public reportsController(UserService userService, ReportsService reportsService, ExpenceService expenceService) {
        this.userService = userService;
        this.reportsService = reportsService;
        this.expenceService = expenceService;

    }

    // Show the Reports Page
    @GetMapping("/reports")
    public String showReports(Principal principal, HttpServletRequest request,
            Model model,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String granularity, // daily/monthly/yearly (we use daily)
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double min,
            @RequestParam(required = false) Double max) {

        // current user
        String email = principal.getName();
        User user = userService.findByEmail(email);

        model.addAttribute("name", user.getName());

        // Build the report
        ReportView view = reportsService.buildReport(user, from, to, category, min, max, granularity);

        // Filters back to the form
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("granularity", granularity == null ? "daily" : granularity);
        model.addAttribute("category", category);
        model.addAttribute("min", min);
        model.addAttribute("max", max);
        model.addAttribute("categories", view.categories());

        // Cards & labels
        model.addAttribute("avgPerDay", view.avgPerDay());
        model.addAttribute("monthChangePercent", view.monthChangePercent());
        model.addAttribute("monthChangePercentNumeric", view.monthChangePercentNumeric());
        model.addAttribute("peakDay", view.peakDay());
        model.addAttribute("peakDayAmount", "₹" + view.peakDayAmount());
        model.addAttribute("currentRangeLabel", view.currentRangeLabel());
        model.addAttribute("totalInRange", view.totalInRangeLabel());

        // Charts
        model.addAttribute("chartCategoryLabels", view.chartCategoryLabels());
        model.addAttribute("chartCategoryData", view.chartCategoryData());
        model.addAttribute("dailyTrendLabels", view.dailyTrendLabels());
        model.addAttribute("dailyTrendData", view.dailyTrendData());

        // Top 5 list
        model.addAttribute("top5", view.top5());
        String query = request.getQueryString(); // Inject HttpServletRequest
        String pdfUrl = "/reports/export/pdf" + (query != null ? "?" + query : "");
        String excelUrl = "/reports/export/excel" + (query != null ? "?" + query : "");

        model.addAttribute("pdfUrl", pdfUrl);
        model.addAttribute("excelUrl", excelUrl);

        return "reports";
    }

    @GetMapping("/download/pdf")
    public void downloadPdf(HttpServletResponse response,
            @AuthenticationPrincipal User user) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=expenses.pdf");

        List<Expence> expenses = expenceService.getExpensesByUser(user);

       
        // Generate PDF
        reportsService.generatePdf(expenses, response.getOutputStream());
    }

    @GetMapping("/download/excel")
    public void downloadExcel(HttpServletResponse response,
            @AuthenticationPrincipal User user) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=expenses.xlsx");

        List<Expence> expenses = expenceService.getExpensesByUser(user);

        // Generate Excel
        reportsService.generateExcel(expenses, response.getOutputStream());
    }

}
