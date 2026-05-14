package nl.esciencecenter.models.benchmarks;


import org.json.JSONObject;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
/**
 * Class representing the design-time benchmarks for a workflow step (tool).
 */
public class WorkflowStepBenchmark {
    /**
     * Description of the benchmark value used in the visualization.
     */
    @NonNull
    private String description;
    /**
     * Value of the benchmark for the workflow step (tool).
     */
    @NonNull
    private String value;
    /**
     * Desirability value (from 0 to 1.0) of the benchmark for the workflow step
     * (tool).
     */
    private double desirabilityValue;

    @Override
    public String toString() {
        return "{ label:" + description + ", value:" + value + ", desirability:"
                + desirabilityValue + "}";
    }

    /**
     * Generate a JSON object containing the tool benchmark information. The content can
     * be visualized using the Wokrkflomics web platform.
     * 
     * @return JSON object containing the benchmark information.
     */
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("label", description);
        json.put("value", value);
        json.put("desirability", desirabilityValue);
        return json;
    }


}