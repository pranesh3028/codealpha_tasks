package web;

import java.util.*;

public class SvgChartBuilder {
    public static String buildSparkline(List<Double> history) {
        if (history == null || history.isEmpty()) return "";

        double min = Collections.min(history);
        double max = Collections.max(history);
        double range = max - min;
        if (range == 0) range = 1.0;

        // Color based on trend
        String color = history.get(history.size()-1) >= history.get(0) ? "#10b981" : "#ef4444";

        StringBuilder points = new StringBuilder();
        for (int i = 0; i < history.size(); i++) {
            double x = i * 5;
            double y = 20 - ((history.get(i) - min) / range * 20);
            points.append(x).append(",").append(y).append(" ");
        }

        return String.format("<svg width=\"100\" height=\"20\" style=\"vertical-align: middle\"><polyline fill=\"none\" stroke=\"%s\" stroke-width=\"1.5\" points=\"%s\"/></svg>", color, points);
    }

    public static String buildPerformanceChart(List<Double> values) {
        if (values == null || values.isEmpty()) return "<p>No data available</p>";

        double min = Collections.min(values);
        double max = Collections.max(values);
        double range = max - min;
        if (range == 0) range = 1.0;

        int width = 800;
        int height = 300;
        StringBuilder points = new StringBuilder();

        for (int i = 0; i < values.size(); i++) {
            double x = i * (double) width / (values.size() - 1 == 0 ? 1 : values.size() - 1);
            double y = height - ((values.get(i) - min) / range * height);
            points.append(x).append(",").append(y).append(" ");
        }

        // Gradient fill for the area under the chart
        String gradient = """
            <defs>
                <linearGradient id=\"grad\" x1=\"0%%\" y1=\"0%%\" x2=\"0%%\" y2=\"100%%\">
                    <stop offset=\"0%%\" style=\"stop-color:#2563eb;stop-opacity:0.3\" />
                    <stop offset=\"100%%\" style=\"stop-color:#2563eb;stop-opacity:0\" />
                </linearGradient>
            </defs>
            """;

        // Area polygon
        StringBuilder areaPoints = new StringBuilder("0,");
        areaPoints.append(height).append(" ");
        for (int i = 0; i < values.size(); i++) {
            double x = i * (double) width / (values.size() - 1 == 0 ? 1 : values.size() - 1);
            double y = height - ((values.get(i) - min) / range * height);
            areaPoints.append(x).append(",").append(y).append(" ");
        }
        areaPoints.append(width).append(",").append(height);

        return String.format(
            "<svg width=\"%d\" height=\"%d\" style=\"background: #ffffff; border: 1px solid #e2e8f0; border-radius: 8px; display: block; margin: 0 auto; box-shadow: 0 1px 2px rgba(0,0,0,0.05);\">" +
            "%s" +
            "<text x=\"10\" y=\"25\" font-family=\"Inter, Arial\" font-size=\"12\" fill=\"#64748b\">Peak Value: $%.2f</text>" +
            "<text x=\"10\" y=\"%d\" font-family=\"Inter, Arial\" font-size=\"12\" fill=\"#64748b\">Bottom: $%.2f</text>" +
            "<polygon fill=\"url(#grad)\" points=\"%s\" />" +
            "<polyline fill=\"none\" stroke=\"#2563eb\" stroke-width=\"3\" stroke-linejoin=\"round\" points=\"%s\"/>" +
            "</svg>",
            width, height, gradient, max, height - 20, min, areaPoints, points
        );
    }
}
