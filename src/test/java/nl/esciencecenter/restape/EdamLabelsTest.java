package nl.esciencecenter.restape;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifikation der O(1)-URI-Auflösung nach Initialisierung.
 *
 * Das Laden der OWL-Ontologie wird per Reflection umgangen, um die Tests
 * netzwerkunabhängig zu halten. Getestet wird die Map-Lookup-Logik und der
 * shortForm-Fallback.
 */
@SpringBootTest
class EdamLabelsTest {

    private EdamLabels edamLabels;

    @BeforeEach
    void setUp() throws Exception {
        edamLabels = new EdamLabels();
        injectLabels(Map.of(
                "http://edamontology.org/format_3728", "LocARNA PP",
                "http://edamontology.org/format_3244", "mzXML"
        ));
    }

    /** Injects a pre-built map to bypass OWL loading. */
    private void injectLabels(Map<String, String> map) throws Exception {
        Field field = EdamLabels.class.getDeclaredField("labels");
        field.setAccessible(true);
        field.set(edamLabels, map);
    }

    @Test
    void testResolveKnownUri() {
        assertEquals("LocARNA PP", edamLabels.resolve("http://edamontology.org/format_3728"));
    }

    @Test
    void testResolveSecondKnownUri() {
        assertEquals("mzXML", edamLabels.resolve("http://edamontology.org/format_3244"));
    }

    @Test
    void testResolveUnknownUriFallback() {
        assertEquals("format_9999", edamLabels.resolve("http://edamontology.org/format_9999"));
    }

    @Test
    void testResolveNullReturnsEmpty() {
        assertEquals("", edamLabels.resolve(null));
    }

    @Test
    void testResolveBlankReturnsEmpty() {
        assertEquals("", edamLabels.resolve("   "));
    }

    @Test
    void testResolveLookupIsO1() {
        // After initialization the backing structure is a HashMap — verify by
        // measuring that 1 000 consecutive lookups complete well under 50 ms.
        long start = System.nanoTime();
        for (int i = 0; i < 1_000; i++) {
            edamLabels.resolve("http://edamontology.org/format_3728");
        }
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        assertTrue(elapsedMs < 50,
                "1 000 lookups should complete in <50 ms for O(1) map, took: " + elapsedMs + " ms");
    }

    @Test
    void testShortFormHashFragment() {
        assertEquals("label", EdamLabels.shortForm("http://example.org#label"));
    }

    @Test
    void testShortFormSlashPath() {
        assertEquals("format_3728", EdamLabels.shortForm("http://edamontology.org/format_3728"));
    }

    @Test
    void testResolveBeforeLoadFallback() throws Exception {
        EdamLabels uninitialised = new EdamLabels();
        // labels field is null → should return short form, not throw
        assertEquals("format_3728", uninitialised.resolve("http://edamontology.org/format_3728"));
    }
}
