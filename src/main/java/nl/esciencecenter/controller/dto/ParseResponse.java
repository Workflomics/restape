package nl.esciencecenter.controller.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ParseResponse {
    private final List<GraphNode> nodes;
    private final List<GraphEdge> edges;
    private final List<TaxonomyElem> inputs;
    private final List<TaxonomyElem> outputs;
}
