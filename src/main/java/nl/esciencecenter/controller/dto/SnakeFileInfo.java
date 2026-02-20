package nl.esciencecenter.controller.dto;

import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import nl.esciencecenter.restape.RestApeUtils;

@Getter
@NoArgsConstructor
public class SnakeFileInfo {
    @JsonProperty("run_id")
    private String runID;

    @JsonProperty("file_name")
    private String fileName;
    static final String snakemakeExtension = "smk";


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
     * Set the file name in case it is valid, i.e., corresponds to file name formatting defined within APE and includes the supported Snakemake extension.
     * If the file name is not valid, an exception is thrown.
     *
     * @throws IllegalArgumentException If the structure is not valid.
     */
    public void setFileName(String fileName) throws IllegalArgumentException {
        if (!RestApeUtils.isValidAPEFileName(fileName, snakemakeExtension)) {
            throw new IllegalArgumentException("The Snakemake file_name format '" + fileName + "' is invalid.");
        }
        this.fileName = fileName;
    }

    /**
     * Get the path to the Snakemake file. If the file does not exist or is not accessible, an exception is thrown.
     *
     * @return Path to the Snakemake file.
     */
    public Path calculatePath() {
        Path path = RestApeUtils.calculatePath(runID, "Snakemake", fileName);
        if(Files.notExists(path)) {
            throw new IllegalArgumentException("The specified Snakemake file does not exist: " + path.toString());
        } else if (!Files.isReadable(path)) {
            throw new IllegalArgumentException("The Snakemake file cannot be accessed: " + path.toString());
        }
        return path;
    }

}
