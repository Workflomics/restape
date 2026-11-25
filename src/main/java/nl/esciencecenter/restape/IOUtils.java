package nl.esciencecenter.restape;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.nio.file.NoSuchFileException;

import org.apache.commons.io.FileUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import nl.esciencecenter.controller.dto.WorkflowsZip;

/**
 * The {@code IOUtils} class provides static methods to read the input files.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IOUtils {

        /**
         * URL to the README file containing instructions on how to run the workflows.
         */
        private static final String README_URL = "https://raw.githubusercontent.com/Workflomics/tools-and-domains/refs/heads/add_instructions/instructions.txt";

        /**
         * Get the CWL content of the file at the given path.
         * 
         * @param filePath - path to the CWL file
         * @return CWL content of the file representing a workflow
         * @throws IOException - if the file cannot be read
         */
        public static String getLocalCwlFile(Path filePath) throws IOException, NoSuchFileException {
                return FileUtils.readFileToString(filePath.toFile(), StandardCharsets.UTF_8);
        }

        /**
         * Get the Snakemake content of the file at the given path.
         *
         * @param filePath - path to the Snakemake file
         * @return Snakemake content of the file representing a workflow
         * @throws IOException - if the file cannot be read
         */
        public static String getLocalSnakemakeFile(Path filePath) throws IOException, NoSuchFileException {
            return FileUtils.readFileToString(filePath.toFile(), StandardCharsets.UTF_8);
        }

        /**
         * Get the JSON content of the file at the given path.
         * 
         * @param filePath - path to the benchmarking JSON file
         * @return CWL content of the file representing a workflow
         * @throws IOException - if the file cannot be read
         */
        public static String getLocalBenchmarkFile(Path filePath) throws IOException {
                return FileUtils.readFileToString(filePath.toFile(), StandardCharsets.UTF_8);
        }

        /**
         * Zip the provided CWL files as well as the CWL input file (`inputs.yml`) into
         * a single zip file. In addition, a `readme.txt` file with instructions on how
         * to run the workflows is added to the zip.
         * 
         * @param workflowsZipInfo - the CWL zip information, containing the runID and the list of workflow file names.
         * 
         * @return Path to the created zip file.
         * @throws IOException Error is thrown if the zip file cannot be created or
         *                     written to.
         */
        public static Path zipFilesForLocalExecution(WorkflowsZip workflowsZipInfo) throws IOException {
                
                List<Path> filePaths = workflowsZipInfo.getAllPaths();
                
                // Add the CWL input file to the zip
                Path cwlInputPath = RestApeUtils.calculatePath(workflowsZipInfo.getRunID(), "CWL", "input.yml");
                filePaths.add(cwlInputPath);
                
                Path zipPath = cwlInputPath.getParent().resolve("workflows.zip");
                try (FileOutputStream fos = new FileOutputStream(zipPath.toFile());
                                ZipOutputStream zipOut = new ZipOutputStream(fos)) {
                        for (Path file : filePaths) {
                                zipOut.putNextEntry(new ZipEntry(file.getFileName().toString()));
                                Files.copy(file, zipOut);
                                zipOut.closeEntry();
                        }
                        addReadmeToZip(zipOut);
                }
                return zipPath;
        }

        /**
         * Add the `readme.txt` file to the zip from the given URL. The file contains
         * instructions on how to run the workflows.
         * 
         * @param zipOut - the zip output stream
         * @throws IOException - if the README file cannot be read
         */
        private static void addReadmeToZip(ZipOutputStream zipOut) throws IOException {
                // Download readme.txt and add to the zip
                URL readmeUrl = new URL(README_URL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) readmeUrl.openConnection();
                httpURLConnection.setRequestMethod("GET");
                // Ensure the connection timeout is set to a reasonable value
                httpURLConnection.setConnectTimeout(5000); // 5 seconds
                httpURLConnection.setReadTimeout(5000); // 5 seconds

                try (InputStream in = new BufferedInputStream(httpURLConnection.getInputStream())) {
                        zipOut.putNextEntry(new ZipEntry("readme.txt"));

                        byte[] buffer = new byte[1024];
                        int count;
                        while ((count = in.read(buffer)) != -1) {
                                zipOut.write(buffer, 0, count);
                        }

                        zipOut.closeEntry();
                } finally {
                        httpURLConnection.disconnect();
                }
        }
}
