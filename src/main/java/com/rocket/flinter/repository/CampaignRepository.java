package com.rocket.flinter.repository;

import com.rocket.flinter.dto.CampaignSummary;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class CampaignRepository {

    @Value("${campaign.csv.path:classpath:static/ad_data.csv}")
    private String csvPathConfig;

    private String csvPath;

    private Connection connection;

    @PostConstruct
    public void init() throws SQLException, IOException {
        connection = DriverManager.getConnection("jdbc:duckdb::memory:");
        csvPath = resolvePath(csvPathConfig);
    }

    public void overrideCsvPath(String path) {
        log.info("CSV path overridden to: {}", path);
        this.csvPath = path;
    }

    private String resolvePath(String path) throws IOException {
        // plain absolute/relative path — use directly
        if (!path.startsWith("classpath:")) {
            log.info("Using file path directly: {}", path);
            return path;
        }

        // always copy classpath resource to temp file
        // works in local, exploded jar, AND fat/nested jar
        String resourcePath = path.replace("classpath:", "");
        ClassPathResource resource = new ClassPathResource(resourcePath);

        if (!resource.exists()) {
            throw new IOException("CSV resource not found: " + path);
        }

        Path tmp = Files.createTempFile("ad_data", ".csv");
        try (InputStream in = resource.getInputStream()) {
            Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
        }
        tmp.toFile().deleteOnExit();
        log.info("Copied classpath resource to temp: {}", tmp);
        return tmp.toAbsolutePath().toString();
    }

    @PreDestroy
    public void close() throws SQLException {
        if (connection != null) connection.close();
    }

    public List<CampaignSummary> top10Cpa(boolean asc) {
        return top10Cpa(asc, csvPath);
    }


    public List<CampaignSummary> top10Cpa(boolean asc, String filePath) {
        String order = asc ? "ASC" : "DESC";
        String sql = """
                SELECT campaign_id       AS campaignId,
                       SUM(impressions)  AS totalImpressions,
                       SUM(clicks)       AS totalClicks,
                       SUM(spend)        AS totalSpend,
                       SUM(conversions)  AS totalConversions,
                       SUM(spend) / NULLIF(SUM(conversions), 0) AS cpa
                FROM read_csv_auto(?)        -- ? = your csv file path
                GROUP BY campaign_id
                ORDER BY cpa %s NULLS LAST
                LIMIT 10
                """.formatted(order);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, filePath);        // tells DuckDB WHERE the csv is
            return executeList(ps);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public List<CampaignSummary> top10Ctr(boolean asc) {
        return top10Ctr(asc, csvPath);
    }

    public List<CampaignSummary> top10Ctr(boolean asc, String filePath) {
        String order = asc ? "ASC" : "DESC";
        String sql = """
                SELECT campaign_id       AS campaignId,
                       SUM(impressions)  AS totalImpressions,
                       SUM(clicks)       AS totalClicks,
                       SUM(spend)        AS totalSpend,
                       SUM(conversions)  AS totalConversions,
                       SUM(clicks) / NULLIF(SUM(impressions), 0) AS ctr
                FROM read_csv_auto(?)
                GROUP BY campaign_id
                ORDER BY ctr %s NULLS LAST
                LIMIT 10
                """.formatted(order);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, filePath);        // tells DuckDB WHERE the csv is
            return executeList(ps);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private List<CampaignSummary> executeList(PreparedStatement ps) {
        List<CampaignSummary> list = new ArrayList<>();
        try {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private CampaignSummary mapRow(ResultSet rs) throws SQLException {
        return CampaignSummary.builder()
                .campaignId(rs.getString("campaignId"))
                .totalImpressions(rs.getInt("totalImpressions"))
                .totalClicks(rs.getInt("totalClicks"))
                .totalSpend(rs.getFloat("totalSpend"))
                .totalConversions(rs.getInt("totalConversions"))
                .build();
    }
}
