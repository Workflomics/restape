package nl.esciencecenter.controller.dto;

public class GraphNode {
    private final String id;
    private final String label;
    private final String type; // "tool" | "input" | "output"

    public GraphNode(String id, String label, String type) {
        this.id = id;
        this.label = label;
        this.type = type;
    }

    public String getId() { return id; }
    public String getLabel() { return label; }
    public String getType() { return type; }
}
