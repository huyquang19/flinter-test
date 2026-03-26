package com.rocket.flinter.controller;

import com.rocket.flinter.dto.CampaignStatsSortFields;
import com.rocket.flinter.dto.SortDirection;
import com.rocket.flinter.service.CampaignService;
import com.rocket.flinter.service.CsvHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;


@RestController
@RequestMapping("campaign/")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;

    @GetMapping("v1/top-10")
    public ResponseEntity<?> getTop10Campaigns(@RequestParam CampaignStatsSortFields fields, @RequestParam(required = false, defaultValue = "ASC") SortDirection order) {
        var result = campaignService.searchCampaignStats(fields, order);
        return ResponseEntity.ok(result);
    }

    @GetMapping("v1/top-10/download")
    public ResponseEntity<StreamingResponseBody> downloadTop10Campaigns(@RequestParam CampaignStatsSortFields fields, @RequestParam(required = false, defaultValue = "ASC") SortDirection order) {
        var result = campaignService.searchCampaignStats(fields, order);
        String filename = "campaign_top10_by_" + fields.name().toLowerCase() + "_" + order.name().toLowerCase() + ".csv";
        var csvStream = CsvHelper.toCsvStream(result);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename= " + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvStream);
    }

    @GetMapping("v2/top-10")
    public ResponseEntity<?> getTop10CampaignsV2(@RequestParam CampaignStatsSortFields fields, @RequestParam(required = false, defaultValue = "ASC") SortDirection order) {
        var result = campaignService.searchCampaignStatsV2(fields, order);
        return ResponseEntity.ok(result);
    }

    @GetMapping("v2/top-10/download")
    public ResponseEntity<StreamingResponseBody> downloadTop10CampaignsV2(@RequestParam CampaignStatsSortFields fields, @RequestParam(required = false, defaultValue = "ASC") SortDirection order) {
        var result = campaignService.searchCampaignStatsV2(fields, order);
        String filename = "campaign_top10_by_" + fields.name().toLowerCase() + "_" + order.name().toLowerCase() + ".csv";
        var csvStream = CsvHelper.toCsvStream(result);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename= " + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvStream);
    }
}
