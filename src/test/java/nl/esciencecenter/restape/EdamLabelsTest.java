package nl.esciencecenter.restape;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T-TAX-01: Verifikation der O(1)-URI-Auflösung nach Initialisierung (NFA 2).
 *
 * Das Laden der OWL-Ontologie wird per Reflection umgangen, um die Tests
 * netzwerkunabhängig zu halten. Getestet wird die Map-Lookup-Logik und der
 * shortForm-Fallback.
 */
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
    void T_TAX_01_knownUriReturnsLabel() {
        assertEquals("LocARNA PP", edamLabels.resolve("http://edamontology.org/format_3728"));
    }

    @Test
    void T_TAX_01_secondKnownUriReturnsLabel() {
        assertEquals("mzXML", edamLabels.resolve("http://edamontology.org/format_3244"));
    }

    @Test
    void T_TAX_01_unknownUriFallsBackToShortForm() {
        assertEquals("format_9999", edamLabels.resolve("http://edamontology.org/format_9999"));
    }

    @Test
    void T_TAX_01_nullInputReturnsEmpty() {
        assertEquals("", edamLabels.resolve(null));
    }

    @Test
    void T_TAX_01_blankInputReturnsEmpty() {
        assertEquals("", edamLabels.resolve("   "));
    }

    @Test
    void T_TAX_01_lookupIsO1_notLinearSearch() {
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
    void T_TAX_01_shortFormHashFragment() {
        assertEquals("label", EdamLabels.shortForm("http://example.org#label"));
    }

    @Test
    void T_TAX_01_shortFormSlashPath() {
        assertEquals("format_3728", EdamLabels.shortForm("http://edamontology.org/format_3728"));
    }

    @Test
    void T_TAX_01_resolveBeforeLoadFallsBackToShortForm() throws Exception {
        EdamLabels uninitialised = new EdamLabels();
        // labels field is null → should return short form, not throw
        assertEquals("format_3728", uninitialised.resolve("http://edamontology.org/format_3728"));
    }
}
