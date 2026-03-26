package com.rocket.flinter.service;

import com.rocket.flinter.dto.CampaignStatsSortFields;
import com.rocket.flinter.dto.CampaignSummary;
import com.rocket.flinter.dto.SortDirection;
import com.rocket.flinter.repository.CampaignLoadRepository;
import com.rocket.flinter.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final CampaignLoadRepository campaignLoadRepository;

    public List<CampaignSummary> searchCampaignStats(CampaignStatsSortFields sortFields, SortDirection sortDirection) {
        List<CampaignSummary> campaignSummaries;
        boolean asc = sortDirection == SortDirection.ASC;
        switch (sortFields) {
            case CPA -> campaignSummaries = campaignRepository.top10Cpa(asc);
            case CTR -> campaignSummaries = campaignRepository.top10Ctr(asc);
            default -> campaignSummaries = null;
        }
        return campaignSummaries;
    }

    public List<CampaignSummary> searchCampaignStatsV2(CampaignStatsSortFields sortFields, SortDirection sortDirection) {
        var campaignSumm = campaignLoadRepository.aggregate(Path.of("src/main/resources/static/ad_data.csv"));
        List<CampaignSummary> campaignSummaries;
        boolean asc = sortDirection == SortDirection.ASC;
        switch (sortFields) {
            case CPA -> {
                var compare = Comparator.comparing(CampaignSummary::getCpa);
                campaignSummaries = campaignSumm.values().stream()
                    .filter(c -> c.getCpa() != null)
                    .sorted(asc ? compare : compare.reversed())
                    .limit(10)
                    .toList();
            }
            case CTR ->{
                var compare = Comparator.comparing(CampaignSummary::getCtr);
                campaignSummaries = campaignSumm.values().stream()
                    .filter(c -> c.getCtr() != null)
                    .sorted(asc ? compare : compare.reversed())
                    .limit(10)
                    .toList();
            }
            default -> campaignSummaries = null;
        }
        return campaignSummaries;
    }


}
