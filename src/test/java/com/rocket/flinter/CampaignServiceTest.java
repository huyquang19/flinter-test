package com.rocket.flinter;

import com.rocket.flinter.dto.CampaignStatsSortFields;
import com.rocket.flinter.dto.CampaignSummary;
import com.rocket.flinter.dto.SortDirection;
import com.rocket.flinter.repository.CampaignLoadRepository;
import com.rocket.flinter.repository.CampaignRepository;
import com.rocket.flinter.service.CampaignService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private CampaignLoadRepository campaignLoadRepository;

    @InjectMocks
    private CampaignService campaignService;


    private CampaignSummary buildSummary(String id, int impressions, int clicks, float spend, int conversions) {
        return CampaignSummary.builder()
                .campaignId(id)
                .totalImpressions(impressions)
                .totalClicks(clicks)
                .totalSpend(spend)
                .totalConversions(conversions)
                .build();
    }

    private Map<String, CampaignSummary> mockCsvData() {
        return Map.of(
                "A", buildSummary("A", 1000, 100, 500f,  10),  // ctr=0.10, cpa=50.0
                "B", buildSummary("B", 1000, 300, 300f,  5),   // ctr=0.30, cpa=60.0
                "C", buildSummary("C", 1000,  50, 800f,  3),   // ctr=0.05, cpa=266.6
                "D", buildSummary("D", 1000, 500, 100f,  20),  // ctr=0.50, cpa=5.0
                "E", buildSummary("E", 0,      0,   0f,  0)    // ctr=null, cpa=null (edge case)
        );
    }


    @Test
    void searchCampaignStats_CPA_ASC_shouldCallTop10CpaWithTrue() {
        List<CampaignSummary> expected = List.of(buildSummary("D", 1000, 500, 100f, 20));
        when(campaignRepository.top10Cpa(true)).thenReturn(expected);

        List<CampaignSummary> result = campaignService.searchCampaignStats(CampaignStatsSortFields.CPA, SortDirection.ASC);

        assertThat(result).isEqualTo(expected);
        verify(campaignRepository).top10Cpa(true);
        verifyNoMoreInteractions(campaignRepository);
    }

    @Test
    void searchCampaignStats_CPA_DESC_shouldCallTop10CpaWithFalse() {
        List<CampaignSummary> expected = List.of(buildSummary("C", 1000, 50, 800f, 3));
        when(campaignRepository.top10Cpa(false)).thenReturn(expected);

        List<CampaignSummary> result = campaignService.searchCampaignStats(CampaignStatsSortFields.CPA, SortDirection.DESC);

        assertThat(result).isEqualTo(expected);
        verify(campaignRepository).top10Cpa(false);
    }

    @Test
    void searchCampaignStats_CTR_ASC_shouldCallTop10CtrWithTrue() {
        List<CampaignSummary> expected = List.of(buildSummary("C", 1000, 50, 800f, 3));
        when(campaignRepository.top10Ctr(true)).thenReturn(expected);

        List<CampaignSummary> result = campaignService.searchCampaignStats(CampaignStatsSortFields.CTR, SortDirection.ASC);

        assertThat(result).isEqualTo(expected);
        verify(campaignRepository).top10Ctr(true);
    }

    @Test
    void searchCampaignStats_CTR_DESC_shouldCallTop10CtrWithFalse() {
        List<CampaignSummary> expected = List.of(buildSummary("D", 1000, 500, 100f, 20));
        when(campaignRepository.top10Ctr(false)).thenReturn(expected);

        List<CampaignSummary> result = campaignService.searchCampaignStats(CampaignStatsSortFields.CTR, SortDirection.DESC);

        assertThat(result).isEqualTo(expected);
        verify(campaignRepository).top10Ctr(false);
    }



    @Test
    void searchCampaignStatsV2_CPA_ASC_shouldReturnSortedAscendingByCpa() {
        when(campaignLoadRepository.aggregate(any(Path.class))).thenReturn(mockCsvData());

        List<CampaignSummary> result = campaignService.searchCampaignStatsV2(CampaignStatsSortFields.CPA, SortDirection.ASC);

        assertThat(result).isNotNull().isNotEmpty();
        assertThat(result).allMatch(c -> c.getCpa() != null);
        // verify first element has lowest CPA
        assertThat(result.get(0).getCpa()).isLessThanOrEqualTo(result.get(result.size() - 1).getCpa());
    }

    @Test
    void searchCampaignStatsV2_CPA_DESC_shouldReturnSortedDescendingByCpa() {
        when(campaignLoadRepository.aggregate(any(Path.class))).thenReturn(mockCsvData());

        List<CampaignSummary> result = campaignService.searchCampaignStatsV2(CampaignStatsSortFields.CPA, SortDirection.DESC);

        assertThat(result).isNotNull().isNotEmpty();
        assertThat(result).allMatch(c -> c.getCpa() != null);
        // verify first element has highest CPA
        assertThat(result.get(0).getCpa()).isGreaterThanOrEqualTo(result.get(result.size() - 1).getCpa());
    }

    @Test
    void searchCampaignStatsV2_CTR_ASC_shouldReturnSortedAscendingByCtr() {
        when(campaignLoadRepository.aggregate(any(Path.class))).thenReturn(mockCsvData());

        List<CampaignSummary> result = campaignService.searchCampaignStatsV2(CampaignStatsSortFields.CTR, SortDirection.ASC);

        assertThat(result).isNotNull().isNotEmpty();
        assertThat(result).allMatch(c -> c.getCtr() != null);
        assertThat(result.get(0).getCtr()).isLessThanOrEqualTo(result.get(result.size() - 1).getCtr());
    }

    @Test
    void searchCampaignStatsV2_CTR_DESC_shouldReturnSortedDescendingByCtr() {
        when(campaignLoadRepository.aggregate(any(Path.class))).thenReturn(mockCsvData());

        List<CampaignSummary> result = campaignService.searchCampaignStatsV2(CampaignStatsSortFields.CTR, SortDirection.DESC);

        assertThat(result).isNotNull().isNotEmpty();
        assertThat(result).allMatch(c -> c.getCtr() != null);
        assertThat(result.get(0).getCtr()).isGreaterThanOrEqualTo(result.get(result.size() - 1).getCtr());
    }

    @Test
    void searchCampaignStatsV2_CPA_shouldFilterOutZeroConversions() {
        when(campaignLoadRepository.aggregate(any(Path.class))).thenReturn(mockCsvData());

        List<CampaignSummary> result = campaignService.searchCampaignStatsV2(CampaignStatsSortFields.CPA, SortDirection.ASC);

        // "E" has totalConversions=0, getCpa() returns null → must be excluded
        assertThat(result).noneMatch(c -> c.getCpa() == null);
        assertThat(result).noneMatch(c -> "E".equals(c.getCampaignId()));
    }

    @Test
    void searchCampaignStatsV2_CTR_shouldFilterOutZeroImpressions() {
        when(campaignLoadRepository.aggregate(any(Path.class))).thenReturn(mockCsvData());

        List<CampaignSummary> result = campaignService.searchCampaignStatsV2(CampaignStatsSortFields.CTR, SortDirection.ASC);

        // "E" has totalImpressions=0, getCtr() returns null → must be excluded
        assertThat(result).noneMatch(c -> c.getCtr() == null);
        assertThat(result).noneMatch(c -> "E".equals(c.getCampaignId()));
    }

    @Test
    void searchCampaignStatsV2_shouldReturnAtMost10Results() {
        Map<String, CampaignSummary> largeData = new HashMap<>();
        for (int i = 1; i <= 15; i++) {
            largeData.put("C" + i, buildSummary("C" + i, 1000, i * 10, i * 50f, i));
        }
        when(campaignLoadRepository.aggregate(any(Path.class))).thenReturn(largeData);

        List<CampaignSummary> cpaResult = campaignService.searchCampaignStatsV2(CampaignStatsSortFields.CPA, SortDirection.ASC);
        List<CampaignSummary> ctrResult = campaignService.searchCampaignStatsV2(CampaignStatsSortFields.CTR, SortDirection.DESC);

        assertThat(cpaResult).hasSizeLessThanOrEqualTo(10);
        assertThat(ctrResult).hasSizeLessThanOrEqualTo(10);
    }



    @Test
    void searchCampaignStatsV2_emptyData_shouldReturnEmptyList() {
        when(campaignLoadRepository.aggregate(any(Path.class))).thenReturn(Map.of());

        List<CampaignSummary> cpResult = campaignService.searchCampaignStatsV2(CampaignStatsSortFields.CPA, SortDirection.ASC);
        List<CampaignSummary> ctrResult = campaignService.searchCampaignStatsV2(CampaignStatsSortFields.CTR, SortDirection.ASC);

        assertThat(cpResult).isEmpty();
        assertThat(ctrResult).isEmpty();
    }
}
