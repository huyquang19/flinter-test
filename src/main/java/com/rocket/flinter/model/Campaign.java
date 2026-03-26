package com.rocket.flinter.model;

import lombok.Data;

@Data
public class Campaign {
    private String campaignId;
    private String date;
    private Integer impressions;
    private Integer clicks;
    private Float spend;
    private Integer conversions;
}
