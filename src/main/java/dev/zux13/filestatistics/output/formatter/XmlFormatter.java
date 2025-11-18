package dev.zux13.filestatistics.output.formatter;

import dev.zux13.filestatistics.analysis.StatisticsAggregator;
import dev.zux13.filestatistics.output.dto.OutputStatistics;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import lombok.extern.slf4j.Slf4j;

import java.io.StringWriter;

@Slf4j
public class XmlFormatter implements OutputFormatter {

    private static final JAXBContext jaxbContext;

    static {
        try {
            jaxbContext = JAXBContext.newInstance(OutputStatistics.class);
        } catch (JAXBException e) {
            throw new ExceptionInInitializerError("Error initializing JAXBContext: " + e.getMessage());
        }
    }

    @Override
    public String format(StatisticsAggregator aggregator) {
        try {
            OutputStatistics outputStats = aggregator.getOutputStatistics();

            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            StringWriter writer = new StringWriter();
            marshaller.marshal(outputStats, writer);

            return writer.toString();
        } catch (JAXBException e) {
            log.error("Error during XML serialization: {}", e.getMessage());
            return "<statistics></statistics>";
        }
    }
}