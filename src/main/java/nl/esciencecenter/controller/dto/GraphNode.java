package nl.esciencecenter.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GraphNode {
    private final String id;
    private final String label;
    private final NodeType type;

    /**
     * Kind of node in the workflow graph. Constants are lowercase so Jackson
     * serialises them as {@code "input"}/{@code "tool"}/{@code "output"} for the
     * frontend (matching the existing {@code ImageFormat} enum convention).
     */
    public enum NodeType {
        input, tool, output
    }
}
