package nl.esciencecenter.restape;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import nl.esciencecenter.controller.dto.WorkflowsZip;
import nl.uu.cs.ape.utils.APEFiles;

@SpringBootTest
class IOUtilsTest {
    

    @Test
    void testZipFilesForLocalExecution() throws Exception {

            String path = "https://raw.githubusercontent.com/Workflomics/tools-and-domains/refs/heads/main/domains/proteomics/config.json";
            String content = FileUtils.readFileToString(APEFiles.readPathToFile(path),
                            StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(content);
            jsonObject.put("solutions", "1");
            List<APEWorkflowMetadata> result = ApeAPI.runSynthesis(jsonObject, false);
            assertFalse(result.isEmpty(), "The encoding should be SAT.");
            String runID = result.get(0).getRunId();
            String cwlFile = result.get(0).getCwlName();

            WorkflowsZip workflowsZip = new WorkflowsZip();
            workflowsZip.setRunID(runID);
            workflowsZip.setWorkflows(List.of(cwlFile));

            IOUtils.zipFilesForLocalExecution(workflowsZip);
    }
}
