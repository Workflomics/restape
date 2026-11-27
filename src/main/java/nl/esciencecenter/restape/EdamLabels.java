package nl.esciencecenter.restape;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Resolves EDAM URIs to human-readable labels via a pre-generated static index.
 *
 * The index (edam_labels.json) is bundled as a classpath resource and was generated
 * once from the EDAM OWL file. Lookups are O(1) after the first call to
 * {@link #ensureLoaded()}.
 */
@Component
public class EdamLabels {

    private volatile Map<String, String> labels = null;

    /**
     * Loads the static label index if not already done. Thread-safe; subsequent
     * calls return immediately.
     */
    public synchronized void ensureLoaded() {
        if (labels != null) return;
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("edam_labels.json")) {
            if (is == null) throw new IllegalStateException("edam_labels.json not found on classpath");
            Map<String, String> map = new ObjectMapper()
                    .readValue(is, new TypeReference<HashMap<String, String>>() {});
            labels = Collections.unmodifiableMap(map);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load edam_labels.json: " + e.getMessage(), e);
        }
    }

    public String resolve(String uri) {
        if (uri == null || uri.isBlank()) return "";
        Map<String, String> map = labels;
        if (map == null) return shortForm(uri);
        return map.getOrDefault(uri, shortForm(uri));
    }

    static String shortForm(String uri) {
        if (uri == null || uri.isBlank()) return "";
        int slash = uri.lastIndexOf('/');
        int hash  = uri.lastIndexOf('#');
        return uri.substring(Math.max(slash, hash) + 1);
    }
}
