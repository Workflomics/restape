package nl.esciencecenter.restape;

import nl.esciencecenter.controller.dto.ParseResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CwlParserTest {

    private static final UnaryOperator<String> IDENTITY = uri -> uri;

    private InputStream fixture(String name) {
        return getClass().getClassLoader().getResourceAsStream(name);
    }

    private InputStream cwl(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    // ── Parsing ──────────────────────────────────────────────────────────────

    @Test
    void testParseCorrectNodeCount() throws Exception {
        ParseResponse result = CwlParser.parse(fixture("test_workflow.cwl"), IDENTITY);
        // 1 input node + 3 tool nodes + 1 output node
        assertEquals(5, result.getNodes().size());
    }

    @Test
    void testParseToolLabelsNoSuffix() throws Exception {
        ParseResponse result = CwlParser.parse(fixture("test_workflow.cwl"), IDENTITY);
        long toolCount = result.getNodes().stream()
                .filter(n -> "tool".equals(n.getType()))
                .count();
        assertEquals(3, toolCount);
        result.getNodes().stream()
                .filter(n -> "tool".equals(n.getType()))
                .forEach(n -> assertFalse(n.getLabel().matches(".*_\\d+$"),
                        "Tool label should not contain APE suffix: " + n.getLabel()));
    }

    @Test
    void testParseCorrectEdgeCount() throws Exception {
        ParseResponse result = CwlParser.parse(fixture("test_workflow.cwl"), IDENTITY);
        // input→A, A→B, B→C, C→output = 4 edges
        assertEquals(4, result.getEdges().size());
    }

    @Test
    void testParseDataflowOrder() throws Exception {
        ParseResponse result = CwlParser.parse(fixture("test_workflow.cwl"), IDENTITY);
        assertTrue(result.getEdges().stream()
                .anyMatch(e -> "ToolA_01".equals(e.getSource()) && "ToolB_01".equals(e.getTarget())));
        assertTrue(result.getEdges().stream()
                .anyMatch(e -> "ToolB_01".equals(e.getSource()) && "ToolC_01".equals(e.getTarget())));
    }

    @Test
    void testParseInputOutputTuples() throws Exception {
        ParseResponse result = CwlParser.parse(fixture("test_workflow.cwl"), IDENTITY);
        assertEquals(1, result.getInputs().size());
        assertEquals(1, result.getOutputs().size());
        assertEquals("http://edamontology.org/format_3728", result.getInputs().get(0).getId());
        assertEquals("http://edamontology.org/format_3244", result.getOutputs().get(0).getId());
    }

    // ── Robustheit ───────────────────────────────────────────────────────────

    @Test
    void testParseWrongClassFail() {
        String doc = "class: CommandLineTool\ncwlVersion: v1.2\n";
        assertThrows(IllegalArgumentException.class,
                () -> CwlParser.parse(cwl(doc), IDENTITY));
    }

    @Test
    void testParseWrongVersionFail() {
        String doc = "class: Workflow\ncwlVersion: v1.0\nsteps:\n  ToolA: {}\n";
        assertThrows(IllegalArgumentException.class,
                () -> CwlParser.parse(cwl(doc), IDENTITY));
    }

    @Test
    void testParseEmptyStepsFail() {
        String doc = "class: Workflow\ncwlVersion: v1.2\nsteps: {}\n";
        assertThrows(IllegalArgumentException.class,
                () -> CwlParser.parse(cwl(doc), IDENTITY));
    }

    @Test
    void testParseMissingEdamAnnotations() throws Exception {
        String doc = """
                class: Workflow
                cwlVersion: v1.2
                inputs:
                  input_1:
                    type: File
                outputs:
                  output_1:
                    type: File
                    outputSource: ToolA_01/output_1
                steps:
                  ToolA_01:
                    run: ToolA.cwl
                    in:
                      input_1: input_1
                    out: [output_1]
                """;
        ParseResponse result = CwlParser.parse(cwl(doc), IDENTITY);
        assertEquals(1, result.getNodes().stream().filter(n -> "tool".equals(n.getType())).count());
        assertTrue(result.getInputs().isEmpty(), "No EDAM tuples without format annotations");
    }

    @Test
    void testParseEmptyFileFail() {
        assertThrows(IllegalArgumentException.class,
                () -> CwlParser.parse(cwl(""), IDENTITY));
    }

    @Test
    void testParseInvalidYamlFail() {
        assertThrows(IllegalArgumentException.class,
                () -> CwlParser.parse(cwl("{ not: valid: yaml: [}"), IDENTITY));
    }
}
