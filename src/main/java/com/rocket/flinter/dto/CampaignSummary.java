package com.rocket.flinter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CampaignSummary {

    private String campaignId;
    private Integer totalImpressions;
    private Integer totalClicks;
    private Float totalSpend;
    private Integer totalConversions;

    public Double getCtr() {
        return totalImpressions == 0 ? null : (double) totalClicks / totalImpressions;
    }

    public Double getCpa() {
        return totalConversions == 0 ? null : (double) totalSpend / totalConversions;
    }
}
