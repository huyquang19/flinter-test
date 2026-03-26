package com.rocket.flinter.repository;

import com.rocket.flinter.dto.CampaignSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class CampaignLoadRepository {

    @Cacheable(value = "campaignSummaryCache",  key = "#filePath.toString() + #filePath.toFile().lastModified()")
    public Map<String, CampaignSummary> aggregate(Path filePath)  {
        Map<String, long[]> acc = new HashMap<>();
        // long[] = { impressions, clicks, conversions }
        Map<String, double[]> spendAcc = new HashMap<>();

        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String header = br.readLine(); // skip header
            if (header == null) throw new IOException("Empty file: " + filePath);

            String line;
            long lineNum = 1;
            while ((line = br.readLine()) != null) {
                lineNum++;
                try {
                    String[] col = line.split(",", -1);
                    if (col.length < 6) continue;

                    String id          = col[0].trim();
                    long impressions   = parseLong(col[2]);
                    long clicks        = parseLong(col[3]);
                    double spend       = parseDouble(col[4]);
                    long conversions   = parseLong(col[5]);

                    acc.merge(id,
                            new long[]{ impressions, clicks, conversions },
                            (a, b) -> new long[]{ a[0]+b[0], a[1]+b[1], a[2]+b[2] }
                    );
                    spendAcc.merge(id,
                            new double[]{ spend },
                            (a, b) -> new double[]{ a[0]+b[0] }
                    );
                } catch (Exception e) {
                    log.warn("Skipping bad line {}: {}", lineNum, line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // build summary map
        Map<String, CampaignSummary> result = new HashMap<>();
        acc.forEach((id, nums) -> result.put(id,
                CampaignSummary.builder()
                        .campaignId(id)
                        .totalImpressions((int) nums[0])
                        .totalClicks((int) nums[1])
                        .totalSpend((float) spendAcc.get(id)[0])
                        .totalConversions((int) nums[2])
                        .build()
        ));

        log.info("Aggregated {} unique campaigns from {}", result.size(), filePath);
        return result;
    }

    private static long parseLong(String s) {
        try { return s == null || s.isBlank() ? 0L : Long.parseLong(s.trim()); }
        catch (NumberFormatException e) { return 0L; }
    }

    private static double parseDouble(String s) {
        try { return s == null || s.isBlank() ? 0d : Double.parseDouble(s.trim()); }
        catch (NumberFormatException e) { return 0d; }
    }

}
