package nl.esciencecenter.models.benchmarks;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Abstract class {@link Benchmark} containing general information about a
 * benchmark, however, it must be implemented (e.g., as an bio.tools benchmark)
 * to be able to compute the benchmark value.
 */

@RequiredArgsConstructor
public class Benchmark {

    /**
     * General information about the benchmark that is being computed.
     */
    @NonNull
    @Getter
    @Setter
    private BenchmarkBase benchmarkInfo;
    /**
     * Value of the benchmark for the workflow.
     */
    @Getter
    @Setter
    private String value;
    /**
     * Desirability value (from 0 to 1.0) of the benchmark for the workflow.
     */
    @Getter
    @Setter
    private double desirabilityValue;
    /**
     * Benchmark for each tool/step in the workflow.
     */
    @Getter
    @Setter
    private List<WorkflowStepBenchmark> workflow;

    /**
     * Generate a JSON object containing the benchmark information. The content can
     * be visualized using the Wokrkflomics web platform.
     * 
     * @return JSON object containing the benchmark information.
     */
    public JSONObject toJSON() {
        JSONObject benchmarkJson = this.benchmarkInfo.getTitleJson();

        JSONObject aggregateValue = new JSONObject();
        aggregateValue.put("value", value);
        aggregateValue.put("desirability", desirabilityValue);
        benchmarkJson.put("aggregate_value", aggregateValue);

        JSONArray workflowJson = new JSONArray();
        for (WorkflowStepBenchmark step : workflow) {
            workflowJson.put(step.toJSON());
        }
        benchmarkJson.put("steps", workflowJson);
        return benchmarkJson;
    }
}
