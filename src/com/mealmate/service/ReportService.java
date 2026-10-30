
package com.mealmate.service;

import com.mealmate.dao.ReportDao;
import com.mealmate.model.ReportData;

import java.sql.SQLException;

public class ReportService {

    private final ReportDao reportDao = new ReportDao();

    public String[] names() {
        return reportDao.getReportNames();
    }

    public ReportData run(int reportIndex) throws SQLException {
        if (reportIndex < 0
                || reportIndex >= reportDao.getReportNames().length) {
            throw new IllegalArgumentException(
                "Select a valid report."
            );
        }

        return reportDao.getReport(reportIndex);
    }
}
