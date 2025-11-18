package dev.zux13.filestatistics;

import dev.zux13.filestatistics.analysis.model.AnalysisResult;
import dev.zux13.filestatistics.cli.ArgumentParser;
import dev.zux13.filestatistics.cli.Config;
import dev.zux13.filestatistics.output.ConsolePrinter;
import dev.zux13.filestatistics.output.FormatterFactory;
import dev.zux13.filestatistics.output.formatter.OutputFormatter;
import dev.zux13.filestatistics.processing.AnalysisService;
import dev.zux13.filestatistics.scan.FileScanner;
import dev.zux13.filestatistics.scan.GitIgnoreManager;
import dev.zux13.filestatistics.analysis.StatisticsAggregator;
import dev.zux13.filestatistics.scan.model.ScanResult;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class Main {

    public static void main(String[] args) {
        try {
            new Main().run(args);
        } catch (Exception e) {
            log.error("An error occurred: {}", e.getMessage());
            ConsolePrinter.printHelp();
            System.exit(1);
        }
    }

    private void run(String[] args) throws IOException {

        Config config = parseArguments(args);
        ScanResult scanResult = scanFiles(config);
        AnalysisResult analysisResult = analyzeFiles(config, scanResult);
        StatisticsAggregator aggregator = aggregate(scanResult, analysisResult);

        printResult(config, aggregator);
        printIgnoredStatsIfVerbose(config, aggregator);
    }

    private Config parseArguments(String[] args) {
        return new ArgumentParser().parse(args);
    }

    private ScanResult scanFiles(Config config) throws IOException {
        GitIgnoreManager ignoreManager = config.useGitIgnore()
                ? new GitIgnoreManager(config.path())
                : null;

        return new FileScanner(config, ignoreManager).scanFiles();
    }

    private AnalysisResult analyzeFiles(Config config, ScanResult scanResult) {
        return new AnalysisService(config.numThreads())
                .processFiles(scanResult.filesToAnalyze(), scanResult.ignoredFilesStats());
    }

    private StatisticsAggregator aggregate(ScanResult scanResult,
                                           AnalysisResult analysisResult) {

        StatisticsAggregator aggregator = new StatisticsAggregator();

        aggregator.setStatisticsByExtension(analysisResult.extensionStatistics());
        aggregator.setIgnoredByGitignoreCount(scanResult.ignoredFilesStats().getIgnoredByGitignore());
        aggregator.setIgnoredByExtensionCount(scanResult.ignoredFilesStats().getIgnoredByExtension());
        aggregator.setIgnoredBinaryOrEncodingCount(scanResult.ignoredFilesStats().getIgnoredBinaryOrEncoding());

        return aggregator;
    }

    private void printResult(Config config, StatisticsAggregator aggregator) {
        OutputFormatter formatter = FormatterFactory.createFormatter(config.outputFormat());
        ConsolePrinter.print(formatter.format(aggregator));
    }

    private void printIgnoredStatsIfVerbose(Config config, StatisticsAggregator aggregator) {
        if (!config.verbose()) {
            return;
        }
        String ignoredStats = String.format(
                """
                ---
                Статистика игнорированных файлов:
                  .gitignore: %d
                  по расширению: %d
                  бинарные/кодировка: %d
                """,
                aggregator.getIgnoredByGitignoreCount(),
                aggregator.getIgnoredByExtensionCount(),
                aggregator.getIgnoredBinaryOrEncodingCount());

        ConsolePrinter.print(ignoredStats);
    }

}