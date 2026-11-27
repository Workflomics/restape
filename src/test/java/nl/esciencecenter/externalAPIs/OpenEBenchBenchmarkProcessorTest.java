package nl.esciencecenter.externalAPIs;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import nl.esciencecenter.restape.LicenseType;

@SpringBootTest
class OpenEBenchBenchmarkProcessorTest {
    
    /**
     * Test for isOSIFromOEBMetrics method.
     * It verifies that the method correctly identifies and returns the OSI status
     * from the provided JSON object representing tool metrics.
     */
    @Test
    void testIsOSIFromOEBMetrics() {
        // Constructing the JSON structure inline for the tool metrics
        JSONObject mockToolMetrics = new JSONObject("""
                    {
                        "project": {
                            "license": {
                                "osi": true
                            }
                        }
                    }
                """);

        // Testing the isOSIFromOEBMetrics method and asserting that the OSI status is
        assertSame(LicenseType.OSI_Approved, OpenEBenchBenchmarkProcessor.isOSIFromOEBMetrics(mockToolMetrics),
                "The method should return true for OSI license");
    }
}
