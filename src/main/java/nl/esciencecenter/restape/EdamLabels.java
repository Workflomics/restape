package nl.esciencecenter.restape;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Resolves EDAM URIs to human-readable labels.
 * The EDAM ontology is fetched and indexed on the first call to {@link #ensureLoaded()},
 * which blocks until loading completes. Subsequent calls return immediately.
 */
@Component
public class EdamLabels {

    private static final Logger log = LoggerFactory.getLogger(EdamLabels.class);

    static final String EDAM_OWL_IRI =
            "https://raw.githubusercontent.com/Workflomics/tools-and-domains/main/domains/edam.owl";

    private volatile Map<String, String> labels = null;

    /**
     * Triggers loading if not already done and blocks until the index is ready.
     * Safe to call concurrently — only one thread performs the actual load.
     */
    public synchronized void ensureLoaded() {
        if (labels != null) return;
        log.info("Loading EDAM ontology from {}", EDAM_OWL_IRI);
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntologyLoaderConfiguration cfg = new OWLOntologyLoaderConfiguration()
                    .setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
            manager.setOntologyLoaderConfiguration(cfg);

            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(IRI.create(EDAM_OWL_IRI));
            OWLDataFactory factory = manager.getOWLDataFactory();

            Map<String, String> map = new HashMap<>();
            ontology.getClassesInSignature().forEach(cls -> {
                String iri = cls.getIRI().toString();
                EntitySearcher.getAnnotations(cls, ontology, factory.getRDFSLabel())
                        .findFirst()
                        .ifPresent(ann -> {
                            OWLAnnotationValue val = ann.getValue();
                            if (val instanceof OWLLiteral lit) {
                                map.put(iri, lit.getLiteral());
                            }
                        });
            });

            labels = Collections.unmodifiableMap(map);
            log.info("EDAM ontology loaded: {} labels indexed.", labels.size());
        } catch (Exception e) {
            log.error("Failed to load EDAM ontology: {}", e.getMessage());
            throw new IllegalStateException("EDAM ontology could not be loaded: " + e.getMessage(), e);
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
