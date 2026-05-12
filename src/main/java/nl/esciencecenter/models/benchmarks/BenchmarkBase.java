package nl.esciencecenter.models.benchmarks;

import org.json.JSONObject;

/**
 * Base information for a benchmark.
 */
public class BenchmarkBase {
    private String benchmarkTitle;
    private String benchmarkCategory;
    private String benchmarkDescription;
    private String unit;
    private String expectedField;
    private String expectedValue;

    public BenchmarkBase() {}

    public BenchmarkBase(String benchmarkTitle, String benchmarkCategory, String benchmarkDescription,
            String unit, String expectedField, String expectedValue) {
        this.benchmarkTitle = benchmarkTitle;
        this.benchmarkCategory = benchmarkCategory;
        this.benchmarkDescription = benchmarkDescription;
        this.unit = unit;
        this.expectedField = expectedField;
        this.expectedValue = expectedValue;
    }

    public String getBenchmarkTitle() { return benchmarkTitle; }
    public void setBenchmarkTitle(String benchmarkTitle) { this.benchmarkTitle = benchmarkTitle; }

    public String getBenchmarkCategory() { return benchmarkCategory; }
    public void setBenchmarkCategory(String benchmarkCategory) { this.benchmarkCategory = benchmarkCategory; }

    public String getBenchmarkDescription() { return benchmarkDescription; }
    public void setBenchmarkDescription(String benchmarkDescription) { this.benchmarkDescription = benchmarkDescription; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getExpectedField() { return expectedField; }
    public void setExpectedField(String expectedField) { this.expectedField = expectedField; }

    public String getExpectedValue() { return expectedValue; }
    public void setExpectedValue(String expectedValue) { this.expectedValue = expectedValue; }

    public JSONObject getTitleJson() {
        JSONObject benchmarkJson = new JSONObject();
        benchmarkJson.put("title", benchmarkTitle);
        benchmarkJson.put("category", benchmarkCategory);
        benchmarkJson.put("description", benchmarkDescription);
        benchmarkJson.put("unit", unit);
        return benchmarkJson;
    }
}
