package dev.zux13.filestatistics.output.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.NoArgsConstructor;

import java.util.List;

@XmlRootElement(name = "statistics")
@NoArgsConstructor
public class OutputStatistics {

    private List<ExtensionStatDto> extensions;

    public OutputStatistics(List<ExtensionStatDto> extensions) {
        this.extensions = extensions;
    }

    @XmlElement(name = "extension")
    public List<ExtensionStatDto> getExtensions() {
        return extensions;
    }

}