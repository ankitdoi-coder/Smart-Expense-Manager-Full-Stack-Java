package com.ankit.Expence.Tracker.App.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;

import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import com.ankit.Expence.Tracker.App.dto.ReportView;
import com.ankit.Expence.Tracker.App.dto.TopCategoryRow;
import com.ankit.Expence.Tracker.App.Model.Expence;
import com.ankit.Expence.Tracker.App.Model.User;
import com.ankit.Expence.Tracker.App.Repository.ExpenceRepository;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ReportsServiceImpl implements ReportsService {

    private final ExpenceRepository repo;

    public ReportsServiceImpl(ExpenceRepository repo) {
        this.repo = repo;
    }

    @Override
    public ReportView buildReport(User user,
            LocalDate from,
            LocalDate to,
            String category,
            Double minAmt,
            Double maxAmt,
            String granularity) {

        // --- sensible defaults ---
        LocalDate today = LocalDate.now();
        if (to == null)
            to = today;
        if (from == null) {
            YearMonth ym = YearMonth.from(to);
            from = ym.atDay(1); // first day of the month
        }
        if (from.isAfter(to)) {
            // swap if needed
            LocalDate tmp = from;
            from = to;
            to = tmp;
        }
        if (granularity == null || granularity.isBlank())
            granularity = "daily";

        // --- dropdown categories ---
        List<String> categories = repo.findDistinctCategories(user);

        // Convert LocalDate → java.sql.Date for repository calls
        Date fromDateSql = Date.valueOf(from);
        Date toDateSql = Date.valueOf(to);

        // --- aggregates ---
        Double totalInRange = repo.sumTotal(user, fromDateSql, toDateSql, category, minAmt, maxAmt);
        if (totalInRange == null)
            totalInRange = 0.0;

        // Category sums
        List<Object[]> catRows = repo.sumByCategory(user, fromDateSql, toDateSql, category, minAmt, maxAmt);

        // Top 5 (with % of total)
        double denom = totalInRange <= 0 ? 1.0 : totalInRange;
        List<TopCategoryRow> top5 = new ArrayList<>();
        for (int i = 0; i < Math.min(5, catRows.size()); i++) {
            Object[] r = catRows.get(i);
            String cat = (String) r[0];
            double amt = ((Number) r[1]).doubleValue();
            double pct = (amt * 100.0) / denom;
            top5.add(new TopCategoryRow(i + 1, cat, amt, pct));
        }

        // Daily trend (fill missing dates with 0)
        List<Object[]> dayRows = repo.sumByDate(user, fromDateSql, toDateSql, category, minAmt, maxAmt);
        Map<LocalDate, Double> byDate = new HashMap<>();
        for (Object[] r : dayRows) {
            LocalDate d = ((Date) r[0]).toLocalDate(); // convert SQL Date → LocalDate
            double amt = ((Number) r[1]).doubleValue();
            byDate.put(d, amt);
        }
        List<String> dailyTrendLabels = new ArrayList<>();
        List<Double> dailyTrendData = new ArrayList<>();

        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("dd MMM");
        long days = ChronoUnit.DAYS.between(from, to) + 1;

        for (int i = 0; i < days; i++) {
            LocalDate d = from.plusDays(i);
            dailyTrendLabels.add(dayFmt.format(d));
            dailyTrendData.add(byDate.getOrDefault(d, 0.0));
        }

        // Avg per day
        double avgPerDay = days > 0 ? (totalInRange / days) : 0.0;

        // Peak weekday (sum all Mondays, all Tuesdays, ...)
        double[] weekdaySums = new double[7]; // 0=Monday ... 6=Sunday
        for (int i = 0; i < days; i++) {
            LocalDate d = from.plusDays(i);
            DayOfWeek dow = d.getDayOfWeek();
            double amt = byDate.getOrDefault(d, 0.0);
            weekdaySums[dow.getValue() - 1] += amt;
        }
        int peakIdx = 0;
        for (int i = 1; i < 7; i++)
            if (weekdaySums[i] > weekdaySums[peakIdx])
                peakIdx = i;
        String peakDay = DayOfWeek.of(peakIdx + 1).name().substring(0, 1).toUpperCase()
                + DayOfWeek.of(peakIdx + 1).name().substring(1).toLowerCase();
        double peakDayAmount = weekdaySums[peakIdx];

        // --- Change vs previous equal-length period ---
        LocalDate prevTo = from.minusDays(1);
        LocalDate prevFrom = from.minusDays(days);

        Date prevFromSql = Date.valueOf(prevFrom);
        Date prevToSql = Date.valueOf(prevTo);

        Double prevTotal = repo.sumTotal(user, prevFromSql, prevToSql, category, minAmt, maxAmt);
        if (prevTotal == null)
            prevTotal = 0.0;

        double changePctNum;
        if (prevTotal == 0.0 && totalInRange == 0.0) {
            changePctNum = 0.0;
        } else if (prevTotal == 0.0) {
            changePctNum = 100.0; // went from 0 to something
        } else {
            changePctNum = ((totalInRange - prevTotal) / prevTotal) * 100.0;
        }
        String monthChangePercent = (changePctNum >= 0 ? "+" : "") +
                String.format(Locale.US, "%.0f%%", changePctNum);

        // Labels
        String currentRangeLabel = from.getMonth().name().substring(0, 1)
                + from.getMonth().name().substring(1).toLowerCase()
                + " " + from.getYear();
        if (!YearMonth.from(from).equals(YearMonth.from(to))) {
            currentRangeLabel = String.format("%s %d – %s %d",
                    from.getMonth().name().substring(0, 1) + from.getMonth().name().substring(1).toLowerCase(),
                    from.getYear(),
                    to.getMonth().name().substring(0, 1) + to.getMonth().name().substring(1).toLowerCase(),
                    to.getYear());
        }
        String totalInRangeLabel = "Total: ₹" + formatAmount(totalInRange);

        return new ReportView(
                round2(avgPerDay),
                monthChangePercent,
                changePctNum,
                peakDay,
                round2(peakDayAmount),
                currentRangeLabel,
                totalInRangeLabel,
                dailyTrendLabels,
                dailyTrendData,
                dailyTrendLabels,
                dailyTrendData,
                top5,
                categories);
    }

    private static String formatAmount(double v) {
        return String.format(Locale.US, "%,.0f", v);
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    public void generatePdf(List<Expence> expenses, OutputStream os) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, os);
            document.open();

            document.add(new Paragraph("Expense Report"));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4); // adjust columns
            table.addCell("Date");
            table.addCell("Category");
            table.addCell("Description");
            table.addCell("Amount");

            for (Expence e : expenses) {
                table.addCell(e.getDate().toString());
                table.addCell(e.getCategory());
                table.addCell(e.getDescription());
                table.addCell(String.valueOf(e.getAmount()));
            }

            document.add(table);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            document.close();
        }
    }

    public void generateExcel(List<Expence> expenses, OutputStream os) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Expenses");

        // Header row
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Date");
        header.createCell(1).setCellValue("Category");
        header.createCell(2).setCellValue("Description");
        header.createCell(3).setCellValue("Amount");

        // Data rows
        int rowNum = 1;
        for (Expence e : expenses) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(e.getDate().toString());
            row.createCell(1).setCellValue(e.getCategory());
            row.createCell(2).setCellValue(e.getDescription());
            row.createCell(3).setCellValue(e.getAmount());
        }

        workbook.write(os);
        workbook.close();
    }

}
