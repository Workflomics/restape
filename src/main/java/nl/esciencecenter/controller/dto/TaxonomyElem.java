package nl.esciencecenter.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * This class represents a single element of the taxonomy.
 * Used for taxonomy tree responses as well as for the flat input/output EDAM
 * terms of a parsed workflow, where only {@code id} and {@code label} are set.
 */
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaxonomyElem {
    public String id;
    public String label;
    public String root;
    public TaxonomyElem[] subsets;
}
