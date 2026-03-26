package com.rocket.flinter.service;

import com.rocket.flinter.dto.CampaignSummary;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.PrintWriter;
import java.util.List;

public class CsvHelper {
    public static final String CSV_HEADER =
            "campaign_id,total_impressions,total_clicks,total_spend,total_conversions,ctr,cpa";

    public static final String CSV_ROW_FORMAT =
            "%s,%d,%d,%.2f,%d,%.4f,%.2f%n";

    public static StreamingResponseBody toCsvStream(
            List<CampaignSummary> data) {

        return outputStream -> {
            try (PrintWriter writer = new PrintWriter(outputStream)) {
                writer.println(CSV_HEADER);
                data.forEach(c -> writer.printf(CSV_ROW_FORMAT,
                        c.getCampaignId(),
                        c.getTotalImpressions(),
                        c.getTotalClicks(),
                        c.getTotalSpend(),
                        c.getTotalConversions(),
                        c.getCtr(),
                        c.getCpa()
                ));
            }
        };
    }
}
