
package com.mealmate.model;

import java.util.List;

public record ReportData(
    List<String> columns,
    List<List<Object>> rows
) {}
