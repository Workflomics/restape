package nl.esciencecenter.externalAPIs;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.Response;

/**
 * The {@code BioToolsRestClient} class provides methods to retrieve and process tool metrics
 * provided by bio.tools API.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BioToolsRestClient {

    private static final okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
 
    /**
    * Retrieve a JSON object corresponding to the tool from bio.tools for the given
    * tool ID.
    * The method uses the bio.tools API to fetch the annotations.
    * 
    * @param toolID - tool ID, not case sensitive. IDs are transformed into lower
    *               case as used in bio.tools, e.g.,
    *               "comet", "blast", etc.
    * @return JSONObject containing the metrics for the tool.
    * @throws IOException   In case the tool is not found in bio.tools.
    * @throws JSONException In case the JSON object returned by bio.tools cannot be
    *                       parsed.
    */
    public static JSONObject fetchToolFromBioTools(String toolID) throws JSONException, IOException {
       JSONObject bioToolAnnotation;
       toolID = toolID.toLowerCase();
       String urlToBioTools = "https://bio.tools/api/" + toolID +
             "?format=json";
       Request request = new Request.Builder().url(urlToBioTools).build();
       Response response = client.newCall(request).execute();

       if (!response.isSuccessful()) {
          throw new IOException("Tool " + toolID + " not found in bio.tools.");
       }

       bioToolAnnotation = new JSONObject(response.body().string());
       response.close();

       log.debug("The list of tools successfully fetched from bio.tools.");
       return bioToolAnnotation;
    }
   

}
