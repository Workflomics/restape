package nl.esciencecenter.models.benchmarks;

import org.json.JSONObject;

/**
 * Class representing the design-time benchmarks for a workflow step (tool).
 */
public class WorkflowStepBenchmark {
    private String description;
    private String value;
    private double desirabilityValue;

    public WorkflowStepBenchmark() {}

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public double getDesirabilityValue() { return desirabilityValue; }
    public void setDesirabilityValue(double desirabilityValue) { this.desirabilityValue = desirabilityValue; }

    @Override
    public String toString() {
        return "{ label:" + description + ", value:" + value + ", desirability:" + desirabilityValue + "}";
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("label", description);
        json.put("value", value);
        json.put("desirability", desirabilityValue);
        return json;
    }
}
