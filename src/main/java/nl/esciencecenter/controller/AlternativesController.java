package nl.esciencecenter.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import nl.esciencecenter.controller.dto.ParseResponse;
import nl.esciencecenter.restape.CwlParser;
import nl.esciencecenter.restape.EdamLabels;

@RestController
@RequestMapping("/alternatives")
public class AlternativesController {

    @Autowired
    private EdamLabels edamLabels;

    /**
     * Parses a CWL v1.2 workflow and returns its DAG representation plus
     * workflow-level I/O terms for use as APE synthesis constraints.
     */
    @PostMapping(value = "/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Parse a CWL workflow file",
            description = "Accepts a CWL v1.2 Workflow file as multipart/form-data and returns a " +
                    "graph-optimised representation: tool nodes, data-flow edges, and the " +
                    "workflow-level input/output EDAM terms.",
            tags = {"Alternatives"},
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Successful operation. Graph representation of the CWL workflow is returned.",
                            content = @Content(
                                    schema = @Schema(implementation = ParseResponse.class),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE)),
                    @ApiResponse(responseCode = "400", description = "Invalid or unsupported CWL file")
            })
    public ResponseEntity<ParseResponse> parseCwl(
            @RequestParam("cwl_file") MultipartFile cwlFile) throws IOException {
        edamLabels.ensureLoaded();
        ParseResponse response = CwlParser.parse(cwlFile.getInputStream(), edamLabels::resolve);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> handleIOException(IOException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAny(Exception e) {
        return ResponseEntity.internalServerError()
                .body(e.getClass().getSimpleName() + ": " + e.getMessage());
    }
}
