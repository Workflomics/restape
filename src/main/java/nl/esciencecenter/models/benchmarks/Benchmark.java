package nl.esciencecenter.models.benchmarks;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Abstract class {@link Benchmark} containing general information about a
 * benchmark, however, it must be implemented (e.g., as an bio.tools benchmark)
 * to be able to compute the benchmark value.
 */
public class Benchmark {

    private BenchmarkBase benchmarkInfo;
    private String value;
    private double desirabilityValue;
    private List<WorkflowStepBenchmark> workflow;

    public Benchmark(BenchmarkBase benchmarkInfo) {
        this.benchmarkInfo = benchmarkInfo;
    }

    public BenchmarkBase getBenchmarkInfo() { return benchmarkInfo; }
    public void setBenchmarkInfo(BenchmarkBase benchmarkInfo) { this.benchmarkInfo = benchmarkInfo; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public double getDesirabilityValue() { return desirabilityValue; }
    public void setDesirabilityValue(double desirabilityValue) { this.desirabilityValue = desirabilityValue; }

    public List<WorkflowStepBenchmark> getWorkflow() { return workflow; }
    public void setWorkflow(List<WorkflowStepBenchmark> workflow) { this.workflow = workflow; }

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
