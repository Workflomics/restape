package nl.esciencecenter.restape;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Resolves EDAM URIs to human-readable labels using a pre-built static mapping
 * bundled as a classpath resource (edam_labels.json).
 */
@Component
public class EdamLabels {

    private static final Logger log = LoggerFactory.getLogger(EdamLabels.class);
    private static final String RESOURCE = "/edam_labels.json";

    private final Map<String, String> labels;

    public EdamLabels() {
        Map<String, String> loaded = Collections.emptyMap();
        try (InputStream is = EdamLabels.class.getResourceAsStream(RESOURCE)) {
            if (is == null) {
                log.warn("edam_labels.json not found on classpath — URIs will be shown as short-form IDs");
            } else {
                loaded = new ObjectMapper().readValue(is, new TypeReference<>() {});
                log.info("EDAM labels loaded: {} entries", loaded.size());
            }
        } catch (IOException e) {
            log.warn("Failed to load edam_labels.json: {}", e.getMessage());
        }
        this.labels = Collections.unmodifiableMap(loaded);
    }

    public String resolve(String uri) {
        if (uri == null || uri.isBlank()) return "";
        return labels.getOrDefault(uri, shortForm(uri));
    }

    static String shortForm(String uri) {
        if (uri == null || uri.isBlank()) return "";
        int slash = uri.lastIndexOf('/');
        int hash  = uri.lastIndexOf('#');
        return uri.substring(Math.max(slash, hash) + 1);
    }
}
