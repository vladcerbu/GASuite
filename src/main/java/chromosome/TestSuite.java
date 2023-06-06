package chromosome;

import java.util.ArrayList;

public class TestSuite {
    private final String className;
    private ArrayList<TestCase> testCases;
    private double fitness;

    public TestSuite(TestSuite copied) {
        this.className = copied.getClassName();
        this.testCases = new ArrayList<>();
        this.fitness = copied.getFitness();
        for (TestCase copied_tc : copied.getTestCases())
            this.testCases.add(new TestCase(copied_tc));
    }

    public TestSuite(String className) {
        this.className = className;
    }

    public TestSuite(String className, ArrayList<TestCase> testCases) {
        this.className = className;
        this.testCases = testCases;
    }

    public ArrayList<TestCase> getTestCases() {
        return testCases;
    }

    public void setTestCases(ArrayList<TestCase> testCases) {
        this.testCases = testCases;
    }

    public Double getFitness() {
        return fitness;
    }

    public void setFitness(Double fitness) {
        this.fitness = fitness;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public String toString() {
        StringBuilder code = new StringBuilder("""
                import org.junit.jupiter.api.Test;

                import static org.junit.jupiter.api.Assertions.*;
                                
                class\040""" + this.className + "Test {\n\n");
        for (int i=0; i < testCases.size(); ++i) {
            testCases.get(i).setTestId(Integer.toString(i + 1));
            code.append(testCases.get(i).toString());
            code.append("\n\n");
        }
        code.append("}\n");
        return code.toString();
    }
}
