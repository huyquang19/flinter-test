package com.rocket.flinter;

import com.rocket.flinter.dto.CampaignStatsSortFields;
import com.rocket.flinter.dto.CampaignSummary;
import com.rocket.flinter.dto.SortDirection;
import com.rocket.flinter.service.CampaignService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CampaignControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CampaignService campaignService;

    @Test
    void shouldReturnTop10Campaigns() throws Exception {

        List<CampaignSummary> mockResult = List.of(
                CampaignSummary.builder()
                        .campaignId("cmp-1")
                        .totalClicks(100)
                        .totalImpressions(1000)
                        .totalConversions(5)
                        .totalSpend(50f)
                        .build()
        );

        Mockito.when(
                campaignService.searchCampaignStats(
                        CampaignStatsSortFields.CTR,
                        SortDirection.ASC
                )
        ).thenReturn(mockResult);

        mockMvc.perform(
                        get("/campaign/v1/top-10")
                                .param("fields", "CTR")
                                .param("order", "ASC")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].campaignId").value("cmp-1"));
    }

    @Test
    void shouldDownloadCsvFile() throws Exception {

        List<CampaignSummary> mockResult = List.of(
                CampaignSummary.builder()
                        .campaignId("cmp-1")
                        .totalClicks(100)
                        .totalImpressions(1000)
                        .totalConversions(5)
                        .totalSpend(50f)
                        .build()
        );

        Mockito.when(
                campaignService.searchCampaignStats(
                        CampaignStatsSortFields.CTR,
                        SortDirection.ASC
                )
        ).thenReturn(mockResult);

        mockMvc.perform(
                        get("/campaign/v1/top-10/download")
                                .param("fields", "CTR")
                                .param("order", "ASC")
                )
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        Matchers.containsString(
                                "campaign_top10_by_ctr_asc.csv"
                        )
                ))
                .andExpect(content().contentType("text/csv"));
    }
}
