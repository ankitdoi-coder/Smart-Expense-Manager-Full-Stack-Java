package com.ankit.Expence.Tracker.App.Service;

import com.ankit.Expence.Tracker.App.dto.ReportView;
import com.ankit.Expence.Tracker.App.Model.Expence;
import com.ankit.Expence.Tracker.App.Model.User;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;

public interface ReportsService {
    ReportView buildReport(User user,
            LocalDate from,
            LocalDate to,
            String category,
            Double minAmt,
            Double maxAmt,
            String granularity);

    void generatePdf(List<Expence> expenses, OutputStream os);
    void generateExcel(List<Expence> expenses, OutputStream os) throws IOException;
}
