package nl.esciencecenter.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Integrationstest für {@link AlternativesController}.
 *
 * Testet den vollständigen HTTP-Layer des {@code POST /alternatives/parse}
 * Endpunkts: Routing, Multipart-Handling, JSON-Serialisierung und
 * Fehlerbehandlung. Ergänzt die Unit-Tests in {@link nl.esciencecenter.restape.CwlParserTest},
 * die den Parser isoliert prüfen.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AlternativesControllerTest {

    @Autowired
    private MockMvc mvc;

    // ── Hilfsmethoden ────────────────────────────────────────────────────────

    /** Lädt die Test-Fixture aus dem Classpath. */
    private MockMultipartFile fixtureFile(String resourceName) throws Exception {
        byte[] bytes = getClass().getClassLoader()
                .getResourceAsStream(resourceName).readAllBytes();
        return new MockMultipartFile("cwl_file", resourceName,
                MediaType.TEXT_PLAIN_VALUE, bytes);
    }

    /** Erstellt eine In-Memory-CWL-Datei mit dem angegebenen Inhalt. */
    private MockMultipartFile inlineCwl(String content) {
        return new MockMultipartFile("cwl_file", "workflow.cwl",
                MediaType.TEXT_PLAIN_VALUE,
                content.getBytes(StandardCharsets.UTF_8));
    }

    // ── Happy Path ───────────────────────────────────────────────────────────

    @Test
    void testParseCwlPass() throws Exception {
        mvc.perform(MockMvcRequestBuilders.multipart("/alternatives/parse")
                        .file(fixtureFile("test_workflow.cwl"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nodes", hasSize(5)))
                .andExpect(jsonPath("$.edges", hasSize(4)))
                .andExpect(jsonPath("$.inputs", hasSize(1)))
                .andExpect(jsonPath("$.outputs", hasSize(1)));
    }

    @Test
    void testParseCwlToolLabelsNoSuffix() throws Exception {
        mvc.perform(MockMvcRequestBuilders.multipart("/alternatives/parse")
                        .file(fixtureFile("test_workflow.cwl"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodes[?(@.type=='tool')].label",
                        everyItem(not(matchesRegex(".*_\\d+$")))));
    }

    @Test
    void testParseCwlInputEdamUri() throws Exception {
        mvc.perform(MockMvcRequestBuilders.multipart("/alternatives/parse")
                        .file(fixtureFile("test_workflow.cwl"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inputs[0].id",
                        is("http://edamontology.org/format_3728")));
    }

    // ── Fehlerbehandlung (HTTP 400) ───────────────────────────────────────────

    @Test
    void testParseCwlWrongClassFail() throws Exception {
        String cwl = "class: CommandLineTool\ncwlVersion: v1.2\n";
        mvc.perform(MockMvcRequestBuilders.multipart("/alternatives/parse")
                        .file(inlineCwl(cwl))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testParseCwlWrongVersionFail() throws Exception {
        String cwl = "class: Workflow\ncwlVersion: v1.0\nsteps:\n  ToolA: {}\n";
        mvc.perform(MockMvcRequestBuilders.multipart("/alternatives/parse")
                        .file(inlineCwl(cwl))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testParseCwlEmptyFileFail() throws Exception {
        mvc.perform(MockMvcRequestBuilders.multipart("/alternatives/parse")
                        .file(inlineCwl(""))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testParseCwlInvalidYamlFail() throws Exception {
        mvc.perform(MockMvcRequestBuilders.multipart("/alternatives/parse")
                        .file(inlineCwl("{ not: valid: yaml: [}"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ── Falsche HTTP-Methode ──────────────────────────────────────────────────

    @Test
    void testParseCwlGetFail() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/alternatives/parse")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void testParseCwlNoContentTypeFail() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/alternatives/parse")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnsupportedMediaType());
    }
}
