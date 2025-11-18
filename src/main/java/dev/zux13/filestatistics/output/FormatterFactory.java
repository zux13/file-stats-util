package dev.zux13.filestatistics.output;

import dev.zux13.filestatistics.output.formatter.JsonFormatter;
import dev.zux13.filestatistics.output.formatter.OutputFormatter;
import dev.zux13.filestatistics.output.formatter.PlaintextFormatter;
import dev.zux13.filestatistics.output.formatter.XmlFormatter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FormatterFactory {

    public static OutputFormatter createFormatter(String format) {
        return switch (format.toLowerCase()) {
            case "plain" -> new PlaintextFormatter();
            case "json" -> new JsonFormatter();
            case "xml" -> new XmlFormatter();
            default -> throw new IllegalArgumentException("Unsupported output format: " + format);
        };
    }
}