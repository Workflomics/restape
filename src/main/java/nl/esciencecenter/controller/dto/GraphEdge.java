package nl.esciencecenter.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GraphEdge {
    private final String source;
    private final String target;
}
