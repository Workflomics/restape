package nl.esciencecenter.models.benchmarks;

import org.json.JSONObject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
/**
 * Base information for a benchmark.
 */
public class BenchmarkBase {
    @NonNull
    private String benchmarkTitle;
    @NonNull
    private String benchmarkCategory;
    @NonNull
    private String benchmarkDescription;
    @NonNull
    private String unit;
    private String expectedField;
    private String expectedValue;

    public JSONObject getTitleJson() {
        JSONObject benchmarkJson = new JSONObject();
        benchmarkJson.put("title", benchmarkTitle);
        benchmarkJson.put("category", benchmarkCategory);
        benchmarkJson.put("description", benchmarkDescription);
        benchmarkJson.put("unit", unit);
        return benchmarkJson;
    }

}