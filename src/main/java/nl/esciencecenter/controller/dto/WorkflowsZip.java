package nl.esciencecenter.controller.dto;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.esciencecenter.restape.RestApeUtils;

/**
 * The {@code CWLZip} class represents the structure of the request to zip CWL files. It contains the runID and the list of workflow file names.
 
 */
@Getter
@Setter
@NoArgsConstructor
public class WorkflowsZip {
    @JsonProperty("run_id")
    private String runID;
    private List<String> workflows;

    /**
     * Set the runID in case it is valid, i.e., corresponds to runID formatting defined within RESTful APE. If the runID is not valid, an exception is thrown.
     * 
     * @throws IllegalArgumentException If the structure is not valid.
     */
    public void setRunID(String runID) throws IllegalArgumentException {
        if (!RestApeUtils.isValidRunID(runID)) {
            throw new IllegalArgumentException("The runID format '" + runID + "' is invalid.");
        }
        this.runID = runID;
    }
    
    /**
     * Set the workflow file names in case they are valid, i.e., corresponds to file name formatting defined within APE and include the required cwl extension. 
     * If the file names are not valid, an exception is thrown.
     * 
     * @throws IllegalArgumentException If the structure is not valid.
     */
    public void setWorkflows(List<String> workflows) throws IllegalArgumentException {
        if (!verifyWorkflows(workflows)) {
            throw new IllegalArgumentException("The CWL file name formats (under workflows) are invalid.");
        }
        this.workflows = workflows;
    }

    /**
     * Check whether all the workflows are valid CWL file names.
     * @param workflows The workflows to verify
     * 
     * @return true if all the workflows are valid CWL file names, false otherwise.
     */
    private boolean verifyWorkflows(List<String> workflows) {
        return workflows.stream().allMatch(workflowName -> RestApeUtils.isValidAPEFileName(workflowName, CWLFileInfo.cwlExtension));
    }

    /**
     * Get the paths to the CWL files.
     * 
     * @return List of paths to the CWL files.
     */
    public List<Path> getCWLPaths() {
        return workflows.stream()
                .map(fileName -> RestApeUtils.calculatePath(runID, "CWL", fileName))
                .collect(Collectors.toList());
    }

    /**
     * Get the paths to the Snakemake files.
     *
     * @return List of paths to the Snakemake files.
     */
    public List<Path> getSnakemakePaths() {
        return workflows.stream()
                .map(fileName -> RestApeUtils.calculatePath(runID, "Snakemake", fileName))
                .collect(Collectors.toList());
    }

    /**
     * Get the paths to the CWL, Snakemake and the SVG files.
     * 
     * @return List of paths to the CWL, Snakemake and the SVG files.
     */
    public List<Path> getAllPaths() {
        List<Path> paths = new ArrayList<>();
        for (String fileName : workflows) {
            paths.add(RestApeUtils.calculatePath(runID, "CWL", fileName));
            paths.add(RestApeUtils.calculatePath(runID, "Snakemake", fileName.replace(".cwl", ".smk")));
            paths.add(RestApeUtils.calculatePath(runID, "Figures", fileName.replace(".cwl", ".svg")));
        }
        return paths;
    }

}
