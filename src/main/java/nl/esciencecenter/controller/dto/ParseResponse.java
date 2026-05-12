package nl.esciencecenter.controller.dto;

import java.util.List;

public class ParseResponse {
    private final List<GraphNode> nodes;
    private final List<GraphEdge> edges;
    private final List<ApeTaxTuple> inputs;
    private final List<ApeTaxTuple> outputs;

    public ParseResponse(List<GraphNode> nodes, List<GraphEdge> edges,
            List<ApeTaxTuple> inputs, List<ApeTaxTuple> outputs) {
        this.nodes = nodes;
        this.edges = edges;
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public List<GraphNode> getNodes() { return nodes; }
    public List<GraphEdge> getEdges() { return edges; }
    public List<ApeTaxTuple> getInputs() { return inputs; }
    public List<ApeTaxTuple> getOutputs() { return outputs; }
}
