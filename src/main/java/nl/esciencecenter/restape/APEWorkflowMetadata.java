package nl.esciencecenter.restape;

import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import nl.uu.cs.ape.solver.solutionStructure.SolutionWorkflow;

/**
 * The {@link APEWorkflowMetadata} class represents metadata for a workflow solution from the APE solver.
 * This class encapsulates the details and metadata of a workflow solution,
 * including descriptive names, descriptions, the length of the solution, and benchmark information.
 * It provides functionality to convert these details into a JSONObject for serialization or further processing.
 */
@Getter
@NoArgsConstructor
public class APEWorkflowMetadata {
    
    @JsonProperty("workflow_name")
    private String workflowName;
    @JsonProperty("descriptive_name")
    private String descriptiveName;
    @JsonProperty("description")
    private String description;
    @JsonProperty("workflow_length")
    private int workflowLength;
    @JsonProperty("run_id")
    private String runId;
    @JsonProperty("cwl_name")
    private String cwlName;
    @JsonProperty("snakemake_name")
    private String snakemakeName;
    @JsonProperty("figure_name")
    private String figureName;
    @JsonProperty("benchmark_file")
    private String benchmarkFile; // Optional, indicates if benchmark data should be included.

    /**
     * Constructs a APEWorkflowMetadata instance from a given SolutionWorkflow and run configuration.
     * 
     * @param sol The SolutionWorkflow containing necessary details of the workflow solution.
     * @param runID The identifier for the run to which this solution belongs.
     * @param benchmark Indicates whether benchmark data is to be included for this solution.
     */
    public APEWorkflowMetadata(SolutionWorkflow sol, String runID, boolean benchmark) {
        this.workflowName = sol.getFileName();
        this.descriptiveName = sol.getDescriptiveName();
        this.description = sol.getDescription();
        this.workflowLength = sol.getSolutionLength();
        this.runId = runID;
        this.cwlName = sol.getFileName() + ".cwl";
        this.snakemakeName = sol.getFileName() + ".smk";
        this.figureName = sol.getFileName();
        if (benchmark) {
            this.benchmarkFile = sol.getFileName() + ".json";
        }
    }

    /**
     * Converts the APEWorkflowMetadata instance into a JSONObject representing its properties.
     * This facilitates the serialization of the workflow solution's metadata to a JSON format,
     * making it compatible with JSON-based data handling, APIs, and storage mechanisms.
     * 
     * @return A JSONObject representation of the APEWorkflowMetadata instance.
     */
    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        json.put("workflow_name", this.workflowName);
        json.put("descriptive_name", this.descriptiveName);
        json.put("description", this.description);
        json.put("workflow_length", this.workflowLength);
        json.put("run_id", this.runId);
        json.put("cwl_name", this.cwlName);
        json.put("snakemake_name", this.snakemakeName);
        json.put("figure_name", this.figureName);
        if (this.benchmarkFile != null) {
            json.put("benchmark_file", this.benchmarkFile);
        }
        return json;
    }

    /**
     * Converts the APEWorkflowMetadata instance into a JSON string representing its properties.
     * 
     * @return A JSON string representation of the APEWorkflowMetadata instance.
     */
    public String toString() {
        return toJSONObject().toString();
    }
}
