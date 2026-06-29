package nl.esciencecenter.controller.dto;

import java.util.List;

public class ParseResponse {
    private final List<GraphNode> nodes;
    private final List<GraphEdge> edges;
    private final List<TaxonomyElem> inputs;
    private final List<TaxonomyElem> outputs;

    public ParseResponse(List<GraphNode> nodes, List<GraphEdge> edges,
            List<TaxonomyElem> inputs, List<TaxonomyElem> outputs) {
        this.nodes = nodes;
        this.edges = edges;
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public List<GraphNode> getNodes() { return nodes; }
    public List<GraphEdge> getEdges() { return edges; }
    public List<TaxonomyElem> getInputs() { return inputs; }
    public List<TaxonomyElem> getOutputs() { return outputs; }
}
