package com.rocket.flinter.service;

import com.rocket.flinter.dto.CampaignStatsSortFields;
import com.rocket.flinter.dto.CampaignSummary;
import com.rocket.flinter.dto.SortDirection;
import com.rocket.flinter.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CampaignCliRunner implements CommandLineRunner {


    private final CampaignRepository campaignRepository;

    @Override
    public void run(String... args) throws Exception {
        if (args.length == 0) return; // no args → normal web server mode

        Options options = buildOptions();
        CommandLineParser parser = new DefaultParser();
        HelpFormatter help = new HelpFormatter();

        try {
            CommandLine cmd = parser.parse(options, args);
            String inputFile = cmd.getOptionValue("file");

            // --file is optional — only override if explicitly passed
            if (cmd.hasOption("file")) {
                inputFile = cmd.getOptionValue("file");
                Path inputPath = Path.of(inputFile);

                if (!Files.exists(inputPath)) {
                    System.err.println("File not found: " + inputFile);
                    System.exit(1);
                    return;
                }

                campaignRepository.overrideCsvPath(inputFile);
                System.out.println("Input file : " + inputFile);
            } else {
                System.out.println("Input file : using default (static/ad_data.csv)");
            }


            if (cmd.hasOption("help")) {
                help.printHelp("campaign-cli", options);
                System.exit(0);
            }

            String output = cmd.getOptionValue("output", "result.csv");
            List<CampaignSummary> result = resolveCommand(cmd);

            if (result.isEmpty()) {
                System.out.println("No results found.");
                System.exit(0);
            }

            exportToCsv(result, output);
            System.out.printf("Exported %d rows → %s%n", result.size(), output);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            help.printHelp("campaign-cli", options);
            System.exit(1);
        }
    }

    private void exportToCsv(List<CampaignSummary> data, String outputPath) throws IOException {
        Path path = Path.of(outputPath);
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path))) {
            writer.println(CsvHelper.CSV_HEADER);
            data.forEach(c -> writer.printf(CsvHelper.CSV_ROW_FORMAT,
                    c.getCampaignId(),
                    c.getTotalImpressions(),
                    c.getTotalClicks(),
                    c.getTotalSpend(),
                    c.getTotalConversions(),
                    c.getCtr(),
                    c.getCpa()
            ));
        }
    }


    private List<CampaignSummary> resolveCommand(CommandLine cmd)  {

        if (cmd.hasOption("top-cpa")) {
            String order = cmd.getOptionValue("top-cpa", "asc");
            System.out.printf("Fetching top 10 CPA %s...%n", order);
            return campaignRepository.top10Cpa(true);
        } else if (cmd.hasOption("top-ctr")) {
            String order = cmd.getOptionValue("top-ctr", "desc");
            System.out.printf("Fetching top 10 CPA %s...%n", order);
            return campaignRepository.top10Ctr(false);
        }
        return new ArrayList<>();
    }

    private Options buildOptions() {
        Options options = new Options();

        options.addOption(Option.builder("f")
                .longOpt("file")
                .desc("Input CSV file path (optional, defaults to static/ad_data.csv)")
                .hasArg()
                .argName("path")
                .build());

        options.addOption(Option.builder()
                .longOpt("top-cpa")
                .desc("Top 10 campaigns by CPA (asc)")
                .hasArg()
                .argName("order")
                .optionalArg(true)
                .build());

        options.addOption(Option.builder()
                .longOpt("top-ctr")
                .desc("Top 10 campaigns by CTR (desc)")
                .hasArg()
                .argName("order")
                .optionalArg(true)
                .build());

        options.addOption(Option.builder("o")
                .longOpt("output")
                .desc("Output CSV file path (default: result.csv)")
                .hasArg()
                .argName("file")
                .build());

        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Show help")
                .build());

        return options;
    }
}
