package nl.esciencecenter.restape;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.annotation.PostConstruct;
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
import org.springframework.stereotype.Service;

/**
 * Loads the EDAM ontology once at startup and exposes O(1) URI → label resolution
 * (concept §3.3: statisches Invertieren der Taxonomie).
 *
 * Label resolution falls back to the short-form EDAM ID while the ontology is
 * still loading and whenever a URI is not found.
 */
@Service
public class EDAMTaxonomyService {

    private static final Logger log = LoggerFactory.getLogger(EDAMTaxonomyService.class);

    private static final String EDAM_OWL_IRI =
            "https://raw.githubusercontent.com/Workflomics/tools-and-domains/main/domains/edam.owl";

    private final AtomicReference<Map<String, String>> labelMap = new AtomicReference<>(null);

    @PostConstruct
    public void init() {
        CompletableFuture.runAsync(this::loadOntology);
    }

    public String resolveLabel(String uri) {
        Map<String, String> map = labelMap.get();
        if (map == null) return shortForm(uri);
        return map.getOrDefault(uri, shortForm(uri));
    }

    public boolean isReady() {
        return labelMap.get() != null;
    }

    private void loadOntology() {
        try {
            log.info("Loading EDAM ontology for label resolution from {}", EDAM_OWL_IRI);
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

            labelMap.set(Collections.unmodifiableMap(map));
            log.info("EDAM ontology loaded: {} labels indexed.", map.size());
        } catch (Exception e) {
            log.warn("Could not load EDAM ontology ({}). Falling back to short-form labels.", e.getMessage());
            labelMap.set(Collections.emptyMap());
        }
    }

    static String shortForm(String uri) {
        if (uri == null || uri.isBlank()) return "";
        int slash = uri.lastIndexOf('/');
        int hash = uri.lastIndexOf('#');
        return uri.substring(Math.max(slash, hash) + 1);
    }
}
