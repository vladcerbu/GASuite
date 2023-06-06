package writer;

import chromosome.TestSuite;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class ResultWriter {

    private final String resultsFile;

    public ResultWriter(String resultsFile) {
        this.resultsFile = resultsFile;
    }

    /**
     * Writes the given test suite to a java file
     * @param testSuite The Test Suite to be written
     */
    public void writeSuite(TestSuite testSuite) throws IOException {
        String fileName = this.resultsFile + "/" + testSuite.getClassName() + "Test.java";
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false));
        writer.write(testSuite.toString());
        writer.close();
    }
}
