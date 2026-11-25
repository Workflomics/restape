package nl.esciencecenter.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import nl.uu.cs.ape.models.enums.ConfigEnum;

@Getter
@Setter
public class APEConfig {
    @JsonProperty("tool_annotations_path")
    private String toolAnnotationsPath;

    @JsonProperty("ontology_path")
    private String ontologyPath;

    @JsonProperty("ontologyPrefixIRI")
    private String ontologyPrefixIri;

    @JsonProperty("toolsTaxonomyRoot")
    private String toolsTaxonomyRoot;

    @JsonProperty("dataDimensionsTaxonomyRoots")
    private String[] dataDimensionsTaxonomyRoots;

    @JsonProperty("strict_tool_annotations")
    private boolean strictToolAnnotations;

    @JsonProperty("cwl_annotations_path")
    private String cwlAnnotationsPath;

    // @JsonProperty("solutions_dir_path") // Uncomment if this field should be included
    // private String solutionsDirPath;

    @JsonProperty("solution_length")
    private SolutionLength solutionLength;

    @JsonProperty("timeout_sec")
    private int timeoutSec;

    @JsonProperty("solutions")
    private int solutions;

    @JsonProperty("number_of_execution_scripts")
    private int numberOfExecutionScripts;

    @JsonProperty("number_of_generated_graphs")
    private int numberOfGeneratedGraphs;

    @JsonProperty("number_of_cwl_files")
    private int numberOfCwlFiles;

    @JsonProperty("number_of_snakemake_files")
    private int numberOfSnakemakeFiles;

    @JsonProperty("tool_seq_repeat")
    private boolean toolSeqRepeat;

    @JsonProperty("inputs")
    private APEDataInstance[] inputs;

    @JsonProperty("outputs")
    private APEDataInstance[] outputs;

    @JsonProperty("debug_mode")
    private boolean debugMode;

    @JsonProperty("use_workflow_input")
    private ConfigEnum useWorkflowInput = ConfigEnum.ALL;

    @JsonProperty("use_all_generated_data")
    private ConfigEnum useAllGeneratedData = ConfigEnum.ONE;

    /**
     * Represents the solution length configuration with minimum and maximum values.
     */
    public static class SolutionLength {
        @JsonProperty("min")
        public int min;

        @JsonProperty("max")
        public int max;
    }

    /**
     * Represents an APE taxonomy type with specified attributes designed for the life sciences domain and EDAM terminology.
     */
    public static class APEDataInstance {
        @JsonProperty("data_0006")
        public String[] data0006;

        @JsonProperty("format_1915")
        public String[] format1915;

        @JsonProperty("APE_label")
        public String[] apeLabel;
    }
    
}