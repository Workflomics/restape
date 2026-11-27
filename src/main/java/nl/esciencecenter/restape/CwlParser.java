package nl.esciencecenter.restape;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import nl.esciencecenter.controller.dto.GraphEdge;
import nl.esciencecenter.controller.dto.GraphNode;
import nl.esciencecenter.controller.dto.ParseResponse;
import nl.esciencecenter.controller.dto.TaxonomyElem;

/**
 * Transforms a CWL v1.2 Workflow document into the graph-optimised ParseResponse.
 */
public class CwlParser {

    private CwlParser() {}

    /**
     * Parses a CWL v1.2 Workflow from the given stream and returns the full DAG
     * including input and output data nodes.
     *
     * @throws IllegalArgumentException if the document fails validation or is structurally incomplete
     * @throws IOException              if the stream cannot be read
     */
    @SuppressWarnings("unchecked")
    public static ParseResponse parse(InputStream inputStream, UnaryOperator<String> labelResolver) throws IOException {
        Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions()));
        Map<String, Object> cwl;
        try {
            cwl = yaml.load(inputStream);
        } catch (Exception e) {
            throw new IllegalArgumentException("CWL file could not be parsed as YAML: " + e.getMessage());
        }
        if (cwl == null) {
            throw new IllegalArgumentException("CWL file is empty.");
        }

        // Step 1 – Validate
        validateMetadata(cwl);

        Map<String, Object> inputsSection  = (Map<String, Object>) cwl.get("inputs");
        Map<String, Object> stepsSection   = (Map<String, Object>) cwl.get("steps");
        Map<String, Object> outputsSection = (Map<String, Object>) cwl.get("outputs");

        if (stepsSection == null || stepsSection.isEmpty()) {
            throw new IllegalArgumentException("CWL Workflow must contain a non-empty 'steps' section.");
        }

        List<GraphNode> nodes = new ArrayList<>();
        List<GraphEdge> edges = new ArrayList<>();
        Set<String> seen     = new LinkedHashSet<>();

        // Step 2a – Input nodes (dagre places them at the top in TB layout)
        Set<String> inputIds = new LinkedHashSet<>();
        if (inputsSection != null) {
            for (Map.Entry<String, Object> entry : inputsSection.entrySet()) {
                String id    = entry.getKey();
                String label = formatLabel(entry.getValue(), id, labelResolver);
                nodes.add(new GraphNode(id, label, GraphNode.NodeType.input));
                inputIds.add(id);
            }
        }

        // Step 2b – Tool nodes
        Set<String> stepIds = stepsSection.keySet();
        for (String stepId : stepIds) {
            nodes.add(new GraphNode(stepId, toolLabel(stepId), GraphNode.NodeType.tool));
        }

        // Step 2c – Output nodes + remember which step feeds each output
        Map<String, String> outputSources = new LinkedHashMap<>();
        if (outputsSection != null) {
            for (Map.Entry<String, Object> entry : outputsSection.entrySet()) {
                String id    = entry.getKey();
                String label = formatLabel(entry.getValue(), id, labelResolver);
                nodes.add(new GraphNode(id, label, GraphNode.NodeType.output));

                if (entry.getValue() instanceof Map<?, ?> def) {
                    String src = (String) ((Map<String, Object>) def).get("outputSource");
                    if (src != null && src.contains("/")) {
                        String sourceStepId = src.substring(0, src.indexOf('/'));
                        if (stepIds.contains(sourceStepId)) {
                            outputSources.put(id, sourceStepId);
                        }
                    }
                }
            }
        }

        // Step 3 – Edges
        for (String targetId : stepIds) {
            Object inField = ((Map<String, Object>) stepsSection.get(targetId)).get("in");
            if (!(inField instanceof Map<?, ?> rawIn)) continue;

            for (Object sourceRef : ((Map<String, Object>) rawIn).values()) {
                if (!(sourceRef instanceof String ref)) continue;

                if (ref.contains("/")) {
                    // Tool → Tool
                    String sourceId = ref.substring(0, ref.indexOf('/'));
                    if (stepIds.contains(sourceId) && seen.add(sourceId + "->" + targetId)) {
                        edges.add(new GraphEdge(sourceId, targetId));
                    }
                } else if (inputIds.contains(ref) && seen.add(ref + "->" + targetId)) {
                    // Input → Tool
                    edges.add(new GraphEdge(ref, targetId));
                }
            }
        }

        // Tool → Output
        outputSources.forEach((outputId, sourceId) ->
            edges.add(new GraphEdge(sourceId, outputId)));

        // Step 4 – EDAM tuples for APE synthesis constraints
        List<TaxonomyElem> inputs  = extractTuples(inputsSection,  labelResolver);
        List<TaxonomyElem> outputs = extractTuples(outputsSection, labelResolver);

        return new ParseResponse(nodes, edges, inputs, outputs);
    }

    private static void validateMetadata(Map<String, Object> cwl) {
        String cwlClass   = (String) cwl.get("class");
        String cwlVersion = (String) cwl.get("cwlVersion");
        if (!"Workflow".equals(cwlClass)) {
            throw new IllegalArgumentException(
                    "CWL file must declare 'class: Workflow', found: " + cwlClass);
        }
        if (!"v1.2".equals(cwlVersion)) {
            throw new IllegalArgumentException(
                    "CWL file must declare 'cwlVersion: v1.2', found: " + cwlVersion);
        }
    }

    /** Strips the APE-generated numeric suffix (e.g. MSFragger_01 → MSFragger). */
    private static String toolLabel(String stepId) {
        return stepId.replaceAll("_\\d+$", "");
    }

    /**
     * Resolves the EDAM format URI from an inputs/outputs entry to a human-readable label.
     * Falls back to the port ID when no format is declared.
     */
    @SuppressWarnings("unchecked")
    private static String formatLabel(Object entry, String fallbackId, UnaryOperator<String> resolver) {
        if (entry instanceof Map<?, ?> def) {
            String uri = (String) ((Map<String, Object>) def).get("format");
            if (uri != null && !uri.isBlank()) {
                return resolver.apply(uri);
            }
        }
        return fallbackId;
    }

    @SuppressWarnings("unchecked")
    private static List<TaxonomyElem> extractTuples(Map<String, Object> section, UnaryOperator<String> resolver) {
        List<TaxonomyElem> tuples = new ArrayList<>();
        if (section == null) return tuples;
        for (Map.Entry<String, Object> entry : section.entrySet()) {
            if (!(entry.getValue() instanceof Map<?, ?> def)) continue;
            String uri = (String) ((Map<String, Object>) def).get("format");
            if (uri == null) continue;
            tuples.add(new TaxonomyElem(uri, resolver.apply(uri), null, null));
        }
        return tuples;
    }
}
