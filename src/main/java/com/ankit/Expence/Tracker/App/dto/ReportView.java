package com.ankit.Expence.Tracker.App.dto;

import java.util.List;

//dto=data transfer object
public record ReportView(
        // cards
        double avgPerDay,
        String monthChangePercent,   // e.g. "+18%"
        Double monthChangePercentNumeric, // e.g. 18.0
        String peakDay,              // e.g. "Saturday"
        double peakDayAmount,        // amount on that weekday (sum over the range)
        String currentRangeLabel,    // e.g. "This Month"
        String totalInRangeLabel,    // e.g. "Total: ₹12,300"

        // charts
        List<String> chartCategoryLabels,
        List<Double> chartCategoryData,
        List<String> dailyTrendLabels,
        List<Double> dailyTrendData,

        // lists
        List<TopCategoryRow> top5,

        // dropdown
        List<String> categories
) {}