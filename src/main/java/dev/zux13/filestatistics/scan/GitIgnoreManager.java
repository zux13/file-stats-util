package dev.zux13.filestatistics.scan;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.ignore.IgnoreNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GitIgnoreManager {

    private final Path basePath;
    private final Map<Path, IgnoreNode> ignoreNodeCache = new HashMap<>();

    public GitIgnoreManager(Path basePath) {
        this.basePath = basePath.toAbsolutePath().normalize();
    }

    private IgnoreNode getIgnoreNode(Path dir) {
        return ignoreNodeCache.computeIfAbsent(dir, d -> {
            Path gitIgnoreFile = d.resolve(".gitignore");
            if (Files.exists(gitIgnoreFile)) {
                try {
                    IgnoreNode node = new IgnoreNode();
                    try (var inputStream = Files.newInputStream(gitIgnoreFile)) {
                        node.parse(inputStream);
                    }
                    return node;
                } catch (IOException e) {
                    log.error("Cannot read .gitignore file {}: {}", gitIgnoreFile, e.getMessage());
                }
            }
            return null;
        });
    }

    public boolean shouldBeIgnored(Path path) {
        Path absolutePath = path.toAbsolutePath().normalize();

        if (!absolutePath.startsWith(basePath)) {
            // Path is outside basePath, cannot be ignored
            return false;
        }

        Path current = absolutePath.getParent();
        while (current != null && current.startsWith(basePath)) {
            IgnoreNode node = getIgnoreNode(current);
            if (node != null) {
                Path relativePath = current.relativize(absolutePath);
                String relativePathStr = relativePath.toString().replace('\\', '/');

                IgnoreNode.MatchResult result = node.isIgnored(relativePathStr, Files.isDirectory(absolutePath));

                switch (result) {
                    case IGNORED:
                        return true;
                    case NOT_IGNORED:
                        return false;
                    case CHECK_PARENT, CHECK_PARENT_NEGATE_FIRST_MATCH:
                        // Continue to the parent directory's .gitignore
                        break;
                }
            }
            if (current.equals(basePath)) {
                break;
            }
            current = current.getParent();
        }

        return false;
    }
}
