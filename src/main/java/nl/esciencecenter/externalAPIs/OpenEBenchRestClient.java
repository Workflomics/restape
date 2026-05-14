package nl.esciencecenter.externalAPIs;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.uu.cs.ape.utils.APEFiles;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * The {@code ToolBenchmarkingAPIs} class provides methods to retrieve and
 * process tool metrics provided by OpenEBench API.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OpenEBenchRestClient {

    private static final OkHttpClient client = new OkHttpClient();

    /**
     * Retrieve a list of JSON objects containing the metrics for each tool version
     * corresponding to the given tool ID from OpenEBench API. All version from
     * BioConda will be included, as well as all copies of bio.tools entry, each
     * representing a platform ("cdd", "cmd", "app", etc.).
     * 
     * @param toolID - tool ID, not case sensitive, (as used in
     *               bio.tools), e.g., "comet", "blast", etc.
     * @return List of JSONObjects, each containing the metrics for a tool
     *         version.
     * @throws IOException   - In case the tool is not found in OpenEBench.
     * @throws JSONException - In case the JSON object returned by bio.tools or
     *                       OpenEBench API cannot be parsed.
     */
    static List<JSONObject> fetchToolMetricsPerVersion(String toolID)
            throws JSONException, IOException {
        toolID = toolID.toLowerCase();
        JSONArray openEBenchAggregateAnnotation = fetchToolAggregate(toolID);
        List<String> toolOEBVersionsURLs = getToolVersionsURLs(openEBenchAggregateAnnotation);

        /*
         * Correct the URLs to point to the metrics rather than general tool
         * information. The OpenEBench API does not provide a more direct way to
         * retrieve the metrics.
         */
        toolOEBVersionsURLs = replaceTool2Metric(toolOEBVersionsURLs);

        List<JSONObject> openEBenchToolVersions = new ArrayList<>();

        // retrieve the JSON metrics for each tool version
        toolOEBVersionsURLs.forEach(metricOEBenchURL -> {
            try {
                File file = APEFiles.readPathToFile(metricOEBenchURL);
                String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                openEBenchToolVersions.add(new JSONObject(content));
            } catch (JSONException e) {
                log.error("Tool version metrics JSON provided by OEB could not be parsed.");
                e.printStackTrace();
            } catch (IOException e) {
                log.error("Tool version metrics file could not be created.");
                e.printStackTrace();
            }
        });

        log.debug("The list of tool versions was successfully fetched from OpenEBench.");
        return openEBenchToolVersions;

    }

    /**
     * Retrieve a JSON objects containing the metrics for the bio.tools entry for
     * the given tool ID from OpenEBench API.
     * 
     * @param toolID - tool ID, not case sensitive, (as used in
     *               bio.tools), e.g., "comet", "blast", etc.
     * @return JSONObject, containing the metrics for the tool version.
     * @throws IOException   - In case the tool is not found in bio.tools.
     * @throws JSONException - In case the JSON object returned by bio.tools or
     *                       OpenEBench API cannot be parsed.
     */
    public static JSONObject fetchToolMetricsBiotoolsVersion(String toolID)
            throws JSONException, IOException {
        toolID = toolID.toLowerCase();
        JSONArray openEBenchAggregateAnnotation = fetchToolAggregate(toolID);

        String biotoolsVersionURL = getToolVersionsURLs(openEBenchAggregateAnnotation).stream()
                .filter(url -> url.contains("biotools:"))
                .findFirst().orElse(null);

        if (biotoolsVersionURL == null) {
            return new JSONObject();
        }

        /*
         * Correct the URL to point to the metrics rather than general tool
         * information. The OpenEBench API does not provide a more direct way to
         * retrieve the metrics.
         */
        biotoolsVersionURL = replaceTool2MetricInOEBCall(biotoolsVersionURL);

        // retrieve the JSON metrics for each tool version
        String metricsJson = "";
        try {
            File metricsFile = APEFiles.readPathToFile(biotoolsVersionURL);
            metricsJson = FileUtils.readFileToString(metricsFile, StandardCharsets.UTF_8);
        } catch (JSONException e) {
            log.error("Tool version metrics JSON provided by OEB could not be parsed.");
            e.printStackTrace();
        } catch (IOException e) {
            log.error("Tool version metrics file could not be created.");
            e.printStackTrace();
        }

        log.debug("The list of tool versions was successfully fetched from OpenEBench.");
        return new JSONObject(metricsJson);

    }

    /**
     * Use the given URL to fetch the JSON object, using the OkHttp client. In case
     * of a failure, the method will return empty JSON object.
     * 
     * @param url - URL to be used to fetch the JSON object
     * @return JSON object
     */
    public static JSONObject getJSONfromURL(String url) {
        JSONObject responseJSON = null;
        try {
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code when trying to fetch" + response);
            }

            responseJSON = new JSONObject(response.body().string());
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return responseJSON;

    }

    /**
     * Parse OpenEBench aggregated annotations to get the list of tool versions and
     * their URLs.
     * 
     * @param openEBenchAggregateAnnotation - JSONArray of JSONObjects, each of
     *                                      which contains a aggregated tool
     *                                      annotation.
     * @return List of tool version URLs.
     */
    static List<String> getToolVersionsURLs(JSONArray openEBenchAggregateAnnotation) throws JSONException {

        List<String> openEBenchToolVersions = new ArrayList<>();
        // Retrieve a list of tool versions

        openEBenchAggregateAnnotation.forEach(tool -> ((JSONObject) tool).getJSONArray("entities").forEach(
                toolType -> {
                    if (((JSONObject) toolType).get("type") != null) {
                        ((JSONObject) toolType).getJSONArray("tools")
                                .forEach(
                                        toolVersion -> openEBenchToolVersions
                                                .add(((JSONObject) toolVersion).getString("@id")));
                    }
                }));
        return openEBenchToolVersions;
    }

    /**
     * Filter out non-bio.tools URLs from the list of tool versions, by removing all
     * URLs that do not contain "biotools:" string.
     * 
     * @param openEBenchToolVersionURLs
     * @return true if the list was modified, false otherwise
     */
    static boolean filterOutNonBioTools(List<String> openEBenchToolVersionURLs) {
        return openEBenchToolVersionURLs.removeIf(url -> !url.contains("biotools:"));
    }

    /**
     * Create a new list of URLs that references tool metrics rather than general
     * tool
     * information. In practice, the method replaces `/tool/` with `/metric/` in the
     * URL.
     * 
     * @param openEBenchToolVersionURLs
     * @return List of URLs that reference tool metrics.
     */
    static List<String> replaceTool2Metric(List<String> openEBenchToolVersionURLs) {
        return openEBenchToolVersionURLs.stream().map(url -> replaceTool2MetricInOEBCall(url)).toList();
    }

    /**
     * Create a new URL that references tool metrics rather than general tool
     * information. In practice, the method replaces `/tool/` with `/metric/` in the
     * URL.
     * 
     * @param openEBenchToolVersionURL
     * @return URL that references tool metrics.
     */
    static String replaceTool2MetricInOEBCall(String openEBenchToolVersionURL) {
        return openEBenchToolVersionURL.replaceFirst("/tool/", "/metrics/");
    }

    /**
     * Get a list of tool versions from OpenEBench based on the tool ID. Each entry
     * contains general information about the tool version and a link (under "@id")
     * to
     * the detailed information. The same link, when `/tool/` is replaced with
     * `/metrics/` can be used to retrieve the metrics for the tool version.<br>
     * <br>
     * <strong>Important:</strong>
     * The OpenEBench API does not provide a more direct way to
     * retrieve the metrics, and thus, the URLs must be corrected to point to the
     * metrics rather than general tool
     * information.
     * 
     * @param toolID
     * @return
     * @throws JSONException In case the JSON object returned by OpenEBench API
     *                       cannot be parsed.
     * @throws IOException   In case a local file cannot be created.
     */
    static JSONArray fetchToolAggregate(String toolID) throws JSONException, IOException {
        JSONArray openEBenchAnnotation;
        String urlToAggregateOEB = "https://openebench.bsc.es/monitor/rest/aggregate?id=" + toolID;

        File file = APEFiles.readPathToFile(urlToAggregateOEB);
        openEBenchAnnotation = APEFiles.readFileToJSONArray(file);

        log.debug("The list of tool aggregations was successfully fetched from OpenEBench.");
        return openEBenchAnnotation;
    }
}
