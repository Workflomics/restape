package nl.esciencecenter.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import nl.esciencecenter.controller.dto.APEConfig;
import nl.esciencecenter.controller.dto.CWLFileInfo;
import nl.esciencecenter.controller.dto.SnakeFileInfo;
import nl.esciencecenter.controller.dto.WorkflowsZip;
import nl.esciencecenter.controller.dto.ConstraintElem;
import nl.esciencecenter.controller.dto.ImgFileInfo;
import nl.esciencecenter.controller.dto.TaxonomyElem;
import nl.esciencecenter.restape.APEWorkflowMetadata;
import nl.esciencecenter.restape.ApeAPI;
import nl.esciencecenter.restape.IOUtils;
import nl.esciencecenter.restape.RestApeUtils;
import nl.uu.cs.ape.configuration.APEConfigException;

/**
 * This class represents the RESTful APE controller. It provides the RESTful API for the APE library.
 * 
 */
@RestController
public class RestApeController {

        private static final String invalidRunIDMsg = "The run ID is invalid.";
        private static final String invalidFileNameMsg = "The file name format is invalid.";

        /**
         * Index of the RESTful APE API. Welcome message.
         * 
         * @return Welcome message.
         */
        @GetMapping("/")
        @Operation(summary = "Index", description = "Index of the RESTful APE API", tags = { "Index" })
        public String index() {
                return "Welcome to the RESTful APE API!";
        }

        /**
         * Retrieve data taxonomy based on the provided domain configuration file.
         * 
         * @param configPath URL to the APE configuration file.
         * @return Taxonomy of data terms.
         * @throws IOException
         * @throws OWLOntologyCreationException
         */
        @GetMapping("/data_taxonomy")
        @Operation(summary = "Retrieve data taxonomy",
                description = "Retrieve data (taxonomy) within the domain.",
                tags = {"Domain"},
                parameters = {
                        @Parameter(name = "config_path", 
                                description = "URL to the APE configuration file.",
                                example = "https://raw.githubusercontent.com/Workflomics/tools-and-domains/refs/heads/main/domains/proteomics/config.json")
                },
                externalDocs = @ExternalDocumentation(description = "More information about the APE configuration file can be found here.",
                                                        url = "https://ape-framework.readthedocs.io/en/latest/docs/specifications/setup.html#configuration-file"),
                responses = {
                        @ApiResponse(responseCode = "200", 
                                description = "Successful operation. Taxonomy of data terms is provided.",
                                content = @Content(schema = @Schema(implementation = TaxonomyElem.class), 
                                                mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "400", description = "Invalid input"),
                        @ApiResponse(responseCode = "404", description = "Not found")
                })
        public ResponseEntity<String> getData(
                        @RequestParam("config_path") String configPath)
                        throws OWLOntologyCreationException, IOException, IllegalArgumentException {
                RestApeUtils.validateURL(configPath);
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                                .body(ApeAPI.getData(configPath).toString());
        }

        /**
         * Retrieve tool taxonomy based on the provided domain configuration file.
         * 
         * @param configPath URL to the APE configuration file.
         * @return Taxonomy of tool terms.
         * @throws IOException
         * @throws OWLOntologyCreationException
         */
        @GetMapping("/tools_taxonomy")
        @Operation(summary = "Retrieve tool taxonomy",
                description = "Retrieve tools (taxonomy) within the domain.",
                tags = {"Domain"},
                parameters = {
                        @Parameter(name = "config_path", 
                                description = "URL to the APE configuration file.",
                                example = "https://raw.githubusercontent.com/Workflomics/tools-and-domains/refs/heads/main/domains/proteomics/config.json")
                },
                externalDocs = @ExternalDocumentation(description = "More information about the APE configuration file can be found here.",
                                                        url = "https://ape-framework.readthedocs.io/en/latest/docs/specifications/setup.html#configuration-file"),
                responses = {
                        @ApiResponse(responseCode = "200", 
                                        description = "Successful operation. Taxonomy of tool terms is provided.",
                                        content = @Content(schema = @Schema(implementation = TaxonomyElem.class), 
                                                        mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "400", description = "Invalid input"),
                        @ApiResponse(responseCode = "404", description = "Not found")
                })
        public ResponseEntity<String> getTools(
                        @RequestParam("config_path") String configPath)
                        throws OWLOntologyCreationException, IOException {
                RestApeUtils.validateURL(configPath);
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                                .body(ApeAPI.getTools(configPath).toString());
        }

        /**
         * Retrieve constraint templates based on the provided domain configuration
         * file.
         * 
         * @param configPath URL to the APE configuration file.
         * @return Constraint templates.
         */
        @GetMapping("/constraint_templates")
        @Operation(summary = "Retrieve constraint templates",
                description = "Retrieve constraint templates used to specify the synthesis problem.",
                tags = {"Domain"},
                parameters = {
                        @Parameter(name = "config_path", 
                                description = "URL to the APE configuration file.",
                                example = "https://raw.githubusercontent.com/Workflomics/tools-and-domains/refs/heads/main/domains/proteomics/config.json")
                },
                externalDocs = @ExternalDocumentation(description = "More information about the APE configuration file can be found here.",
                                                        url = "https://ape-framework.readthedocs.io/en/latest/docs/specifications/setup.html#configuration-file"),
                responses = {
                        @ApiResponse(responseCode = "200", 
                                        description = "Successful operation. Constraint templates are provided.",
                                        content = @Content(schema = @Schema(implementation = ConstraintElem.class), 
                                                        mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "400", description = "Invalid input"),
                        @ApiResponse(responseCode = "404", description = "Not found")
                })
        public ResponseEntity<String> getConstraints(@RequestParam("config_path") String configPath)
                        throws JSONException, OWLOntologyCreationException, IOException {

                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                                .body(ApeAPI.getConstraints(configPath).toString());
        }

        /**
         * Retrieve domain constraint specified in the provided domain configuration
         * file.
         * 
         * @param configPath URL to the APE configuration file.
         * @return Domain constraints.
         */
        @GetMapping("/domain_constraints")
        @Operation(summary = "Retrieve a fixed set of domain constraints",
                description = "Retrieve a fixed set of domain constraints specified in the domain configuration file, which should always be used when synthesizing workflows within the domain.",
                tags = {"Domain"},
                parameters = {
                        @Parameter(name = "config_path", 
                                description = "URL to the APE configuration file.",
                                example = "https://raw.githubusercontent.com/Workflomics/tools-and-domains/refs/heads/main/domains/proteomics/config.json")
                },
                externalDocs = @ExternalDocumentation(description = "More information about the APE configuration file can be found here.",
                                                        url = "https://ape-framework.readthedocs.io/en/latest/docs/specifications/setup.html#configuration-file"),
                responses = {
                        @ApiResponse(responseCode = "200", 
                                        description = "Successful operation. Domain specific constraints are provided.",
                                        content = @Content(schema = @Schema(implementation = ConstraintElem.class), 
                                                        mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "400", description = "Invalid input"),
                        @ApiResponse(responseCode = "404", description = "Not found")
                })
        public ResponseEntity<String> getDomainConstraints(@RequestParam("config_path") String configPath)
                        throws JSONException, OWLOntologyCreationException, IOException {

                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                                .body(ApeAPI.getDomainConstraints(configPath).toString());
        }

        /**
         * Retrieve default input and output data terms based on the provided domain configuration file.
         * 
         * @param configPath URL to the APE configuration file.
         * @return Taxonomy of data terms.
         * @throws IOException - if the URL is invalid
         * @throws OWLOntologyCreationException - if the ontology cannot be created
         */
        @GetMapping("/domain_io")
        @Operation(summary = "Retrieve default domain input and output data",
                description = "Retrieve default input and output data terms within the domain.",
                tags = {"Domain"},
                parameters = {
                        @Parameter(name = "config_path", 
                                description = "URL to the APE configuration file.",
                                example = "https://raw.githubusercontent.com/Workflomics/tools-and-domains/refs/heads/main/domains/proteomics/config.json")
                },
                externalDocs = @ExternalDocumentation(description = "More information about the APE configuration file can be found here.",
                                                        url = "https://ape-framework.readthedocs.io/en/latest/docs/specifications/setup.html#configuration-file"),
                responses = {
                        @ApiResponse(responseCode = "200", 
                                description = "Successful operation. Default domain input and output data terms are provided.",
                                content = @Content(schema = @Schema(implementation = TaxonomyElem.class), 
                                                mediaType = MediaType.APPLICATION_JSON_VALUE)),
                        @ApiResponse(responseCode = "400", description = "Invalid input"),
                        @ApiResponse(responseCode = "404", description = "Not found")
                })
        public ResponseEntity<String> getDomainIO(
                        @RequestParam("config_path") String configPath)
                        throws OWLOntologyCreationException, IOException, IllegalArgumentException {
                RestApeUtils.validateURL(configPath);
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                                .body(ApeAPI.getDomainIO(configPath).toString());
        }

        /**
         * Synthesize workflow based on the provided run configuration file.
         * 
         * @param configJson JSON object containing the configuration for the synthesis.
         * @return List of resulting solutions, where each element describes a workflow
         *         (name,length, run_id, etc.)
         * @throws OWLOntologyCreationException
         */
        @PostMapping("/run_synthesis")
        @Operation(summary = "Run workflow synthesis",
                description = "Run workflow synthesis using the APE library. Returns the list of resulting solutions, where each element describes a workflow (name, length, run_id, etc.).",
                tags = {"APE"},
                requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                        description = "JSON object containing the configuration for the synthesis.",
                        required = true,
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = APEConfig.class))
                ),
                externalDocs = @ExternalDocumentation(
                        description = "More information about the APE configuration file can be found here.",
                        url = "https://ape-framework.readthedocs.io/en/latest/docs/specifications/setup.html#configuration-file"),
                responses = {
                        @ApiResponse(responseCode = "200", description = "Successful operation. A list of synthesized workflow solutions is returned."
                                        ),
                        @ApiResponse(responseCode = "400", description = "Invalid input"),
                        @ApiResponse(responseCode = "404", description = "Not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
                })
        public ResponseEntity<List<APEWorkflowMetadata>> runSynthesis(
                        @RequestBody(required = true) Map<String, Object> configJson)
                        throws APEConfigException, JSONException, OWLOntologyCreationException, IOException {
                JSONObject config = new JSONObject(configJson);

                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                                .body(ApeAPI.runSynthesis(config, false));
        }

        /**
         * Synthesize workflow based on the provided run configuration file.
         * 
         * @param configJson JSON object containing the configuration for the synthesis.
         * @return List of resulting solutions, where each element describes a workflow
         *         (name,length, run_id, etc.)
         * @throws IOException
         */
        @PostMapping("/run_synthesis_and_bench")
        @Operation(summary = "Run workflow synthesis and provide design-time benchmarks", 
                description = "This endpoint triggers the synthesis of workflows using the APE library and evaluates design-time benchmarks for the generated workflows. It returns a list of APEWorkflowMetadata objects, each representing a synthesized workflow solution with detailed metadata.",
                tags = { "APE" },
                requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "JSON object containing the configuration for the synthesis.", 
                                                content = @Content(schema = @Schema(implementation = APEConfig.class))),
                responses = {
                        @ApiResponse(responseCode = "200", description = "Successful operation. A list of synthesized workflow solutions is returned."),
                        @ApiResponse(responseCode = "400", description = "Invalid input. The request body does not match the expected schema."),
                        @ApiResponse(responseCode = "404", description = "Not found. The specified resource could not be found."),
                        @ApiResponse(responseCode = "500", description = "Internal server error. An unexpected error occurred.")
                },
                externalDocs = @ExternalDocumentation(description = "More information about the APE configuration file.", 
                                                        url = "https://ape-framework.readthedocs.io/en/latest/docs/specifications/setup.html#configuration-file"))
        public ResponseEntity<List<APEWorkflowMetadata>>  runSynthesisAndBench(
                        @RequestBody(required = true) Map<String, Object> configJson)
                        throws APEConfigException, JSONException, OWLOntologyCreationException, IOException {
                JSONObject config = new JSONObject(configJson);

                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                                .body(ApeAPI.runSynthesis(config, true));
        }

        /**
         * Retrieve the solution workflow based on the provided run ID and a candidate
         * solution.
         * 
         * @param imgFileInfo JSON object containing the configuration for the synthesis.
         * @return ResponseEntity containing the image representing the workflow.
         * @throws IOException if there is an error reading the image file.
         */
        @PostMapping("/image")
        @Operation(summary = "Retrieve an image representing the workflow",
                description = "Retrieve an image from the file system representing the workflow generated.",
                tags = {"Download"},
                requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                        description = "JSON object containing the information about the image, including the name of the image file, format of the image ('png' or 'svg'), and ID of the corresponding synthesis run.",
                        required = true,
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ImgFileInfo.class))
                ),
                responses = {
                        @ApiResponse(responseCode = "200", description = "Successful operation. Image representing the workflow is provided.",
                                content = @Content(mediaType = MediaType.IMAGE_PNG_VALUE)),
                        @ApiResponse(responseCode = "400", description = "Invalid input"),
                        @ApiResponse(responseCode = "404", description = "Not found")
                })
        public ResponseEntity<?> postImage(
                        @RequestBody(required = true) ImgFileInfo imgFileInfo) throws IOException {

                Path path = imgFileInfo.calculatePath();
                return ResponseEntity.ok()
                                .contentType(MediaType.parseMediaType(Files.probeContentType(path)))
                                .body(new FileSystemResource(path));
        }

        /**
         * Retrieve the solution CWL workflow based on the provided run ID and a
         * candidate solution.
         * 
         * @param fileName Name of the CWL file (provided under 'name' after the
         *                 synthesis run).
         * @param runID    ID of the corresponding synthesis run (provided under
         *                 'run_id' after the synthesis run).
         * @return CWL file representing the workflow.
         */
        @PostMapping("/cwl")
        @Operation(summary = "Retrieve a cwl file",
                description = "Retrieve a cwl file from the file system, describing the workflow.",
                tags = {"Download"},
                requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                        description = "JSON object containing the information for retrieving the CWL file, including the name of the CWL file (as 'cwl_name') and the ID of the corresponding synthesis run.",
                        required = true,
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = CWLFileInfo.class))
                ),
                responses = {
                        @ApiResponse(responseCode = "200", description = "Successful operation. CWL file describing the workflow is provided.", content = @Content(mediaType = "application/x-yaml")),
                        @ApiResponse(responseCode = "400", description = "Invalid input"),
                        @ApiResponse(responseCode = "404", description = "Not found")
                })
        public ResponseEntity<String> postCwl(
                        @RequestBody(required = true) CWLFileInfo cwlInfoJson) throws IOException {

                Path path = cwlInfoJson.calculatePath();
                return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/x-yaml"))
                                .body(IOUtils.getLocalCwlFile(path));
        }

        /**
         * Retrieve the solution CWL workflow based on the provided run ID and a
         * candidate solution.
         *
         * @param fileName Name of the Snakemake file (provided under 'name' after the
         *                 synthesis run).
         * @param runID    ID of the corresponding synthesis run (provided under
         *                 'run_id' after the synthesis run).
         * @return Snakemake file representing the workflow.
         */
        @PostMapping("/snakemake")
        @Operation(summary = "Retrieve a Snakemake file",
                description = "Retrieve a Snakemake file from the file system, describing the workflow.",
                tags = {"Download"},
                requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                        description = "JSON object containing the information for retrieving the Snakemake file, including the name of the Snakemake file (as 'snakemake_name') and the ID of the corresponding synthesis run.",
                        required = true,
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = SnakeFileInfo.class))
                ),
                responses = {
                        @ApiResponse(responseCode = "200", description = "Successful operation. Snakemake file describing the workflow is provided.", content = @Content(mediaType = "application/x-yaml")),
                        @ApiResponse(responseCode = "400", description = "Invalid input"),
                        @ApiResponse(responseCode = "404", description = "Not found")
                })
        public ResponseEntity<String> postSnakemake(
                @RequestBody(required = true) SnakeFileInfo snakeInfoJson) throws IOException {

            Path path = snakeInfoJson.calculatePath();
            return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/x-yaml"))
                    .body(IOUtils.getLocalSnakemakeFile(path));
        }

        /**
         * Retrieve the CWL input file based on the provided run ID.
         * 
         * @param runID ID of the corresponding synthesis run (provided under 'run_id'
         *              after the synthesis run).
         * @return CWL input file (.yml) representing the workflow inputs.
         */
        @GetMapping("/cwl_input")
        @Operation(summary = "Retrieve a cwl input file",
                description = "Retrieve a cwl input file from the file system, allowing to execute the workflows in the run.",
                tags = {"Download"},
                parameters = {
                        @Parameter(name = "run_id", 
                                description = "ID of the corresponding synthesis run (provided under 'run_id' after the synthesis run).",
                                example = "04ce2ef00c1685150252568")
                },
                responses = {
                        @ApiResponse(responseCode = "200", 
                                        description = "Successful operation. CWL input file representing the workflow inputs is provided.",
                                        content = @Content(mediaType = "application/x-yaml")),
                        @ApiResponse(responseCode = "400", description = "Invalid input"),
                        @ApiResponse(responseCode = "404", description = "Not found")
                })
        public ResponseEntity<String> getCwlInput(
                        @RequestParam("run_id") String runID) {
                if (!RestApeUtils.isValidRunID(runID)) {
                        return ResponseEntity.badRequest().body(invalidRunIDMsg);
                }
                try {
                        Path path = RestApeUtils.calculatePath(runID, "CWL", "input.yml");
                        return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/x-yaml"))
                                        .body(IOUtils.getLocalCwlFile(path));
                } catch (IOException e) {
                        return ResponseEntity.badRequest().body("The CWL input file could not be found.");
                }
        }

        /**
         * Retrieve the design-time benchmark information based on the provided run ID
         * and a
         * candidate solution.
         * 
         * @param fileName Name of the CWL file (provided under 'name' after the
         *                 synthesis run).
         * @param runID    ID of the corresponding synthesis run (provided under
         *                 'run_id' after the synthesis run).
         * @return CWL file representing the workflow.
         */
        @GetMapping("/design_time_benchmarks")
        @Operation(summary = "Retrieve a design-time benchmark file",
                description = "Retrieve a design-time benchmark file from the file system, describing the workflow.",
                tags = {"Download"},
                parameters = {
                        @Parameter(name = "file_name", 
                                description = "Name of the benchmark file (provided under 'bench_name' after the synthesis run).",
                                example = "workflowSolution_0.json"),
                        @Parameter(name = "run_id", 
                                description = "ID of the corresponding synthesis run (provided under 'run_id' after the synthesis run).",
                                example = "04ce2ef00c1685150252568")
                },
                responses = {
                        @ApiResponse(responseCode = "200", 
                                description = "Successful operation. Design-time benchmark file describing the workflow is provided.",
                                content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "400", description = "Invalid input"),
                        @ApiResponse(responseCode = "404", description = "Not found")
                })
        public ResponseEntity<String> getBenchmarks(
                        @RequestParam("file_name") String fileName,
                        @RequestParam("run_id") String runID) {
                if (!RestApeUtils.isValidRunID(runID)) {
                        return ResponseEntity.badRequest().body(invalidRunIDMsg);
                } else if (!RestApeUtils.isValidAPEFileName(fileName, "json")) {
                        return ResponseEntity.badRequest().body(invalidFileNameMsg);
                }
                try {
                        Path path = RestApeUtils.calculatePath(runID, "CWL", fileName);
                        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                                        .body(IOUtils.getLocalBenchmarkFile(path));
                } catch (IOException e) {
                        return ResponseEntity.badRequest().body("The CWL file could not be found.");
                }
        }

        /**
         * Retrieve the CWL solution files based on the provided run ID and CWL file
         * names.
         * 
         * @param cwlFilesJson JSON object containing the run_id and the list of CWL
         *                     files.
         * @return CWL file representing the workflow.
         */
        @PostMapping("/cwl_zip")
        @Operation(summary = "Retrieve the zip of cwl and Snakemake files.",
                description = "Retrieve the zip comprising CWL and Snakemake files specified in the request body. The request body should be a JSON object with the following fields: 'run_id' and 'workflows'. The 'run_id' field specifies the ID of the synthesis run, while the 'workflows' field is a list of CWL and Snakemake file names.",
                tags = {"Download"},
                requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "JSON object containing the following fields: 'run_id' and 'workflows'.",
                required = true,
                content = @Content(schema = @Schema(implementation = WorkflowsZip.class))
                ),
                responses = {
                        @ApiResponse(responseCode = "200", 
                                description = "Successful operation. A zip file comprising specified CWL and Snakemake files is returned.",
                                content = @Content(mediaType = "application/zip")),
                        @ApiResponse(responseCode = "400", description = "Invalid input"),
                        @ApiResponse(responseCode = "404", description = "Not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
                })
        public ResponseEntity<?> postZipCWLs(
                        @RequestBody(required = true) WorkflowsZip workflowsZipInfo) {
                try {
                        Path zipPath = IOUtils.zipFilesForLocalExecution(workflowsZipInfo);

                        Resource zipResource = new UrlResource(zipPath.toUri());
                        String zipContentType = Files.probeContentType(zipPath);
                        return ResponseEntity.ok()
                                        .contentType(MediaType.parseMediaType(
                                                        zipContentType != null ? zipContentType : "application/zip"))
                                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                                        "attachment; filename=\"" + zipPath.getFileName().toString()
                                                                        + "\"")
                                        .body(zipResource);
                } catch (IOException e) {
                        return ResponseEntity.badRequest()
                                        .body("An error occurred while creating the zip file.");
                } catch (ClassCastException e) {
                        return ResponseEntity.badRequest().body("JSON structure is not not valid.");
                } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().body(e.getMessage());
                }
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<String> handleException(IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
        }

        @ExceptionHandler(OWLOntologyCreationException.class)
        public ResponseEntity<String> handleException(OWLOntologyCreationException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
        }

        @ExceptionHandler(IOException.class)
        public ResponseEntity<String> handleException(IOException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
        }

        @ExceptionHandler(JSONException.class)
        public ResponseEntity<String> handleException(JSONException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
        }

        @ExceptionHandler(APEConfigException.class)
        public ResponseEntity<String> handleException(APEConfigException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
        }

}