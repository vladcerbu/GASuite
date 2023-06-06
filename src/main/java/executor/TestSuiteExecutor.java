package executor;

import chromosome.TestCase;
import chromosome.TestSuite;
import chromosome.action.Action;
import org.apache.commons.io.FileUtils;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public final class TestSuiteExecutor {

    // Class that will help with loading the instrumented java code into memory
    public static class MemoryClassLoader extends ClassLoader {

        private final Map<String, byte[]> definitions = new HashMap<>();

        public void addDefinition(final String name, final byte[] bytes) {
            definitions.put(name, bytes);
        }

        @Override
        protected Class<?> loadClass(final String name, final boolean resolve)
                throws ClassNotFoundException {
            final byte[] bytes = definitions.get(name);
            if (bytes != null) {
                return defineClass(name, bytes, 0, bytes.length);
            }
            return super.loadClass(name, resolve);
        }

    }

    public TestSuiteExecutor() { }

    // Preparing the evaluation by modifying the tested class. We make it so that it implements
    // the Runnable interface and then add the test cases to the run() method
    private void prepareEvaluation(TestSuite testSuite) throws Exception {
        String fileName = System.getProperty("java.io.tmpdir") + "/ga_suite/spooned/" + testSuite.getClassName() + ".java";
        String text = Files.readString(Paths.get(fileName));
        for (int i = 0; i < text.length(); ++i) {
            if (text.charAt(i) == '{') {
                text = this.insertString(text, "implements Runnable ", i-1);
                break;
            }
        }
        StringBuilder newCode = new StringBuilder("\n\t@Override\n\tpublic void run() {\n");
        for (TestCase testCase : testSuite.getTestCases()) {
            Queue<String> valueQ = new LinkedList<>(testCase.getValues());
            for (Action action : testCase.getActions()) {
                newCode.append("\t\t");
                newCode.append(action.toString());
                int commas = action.getParamTypes().size() - 1;
                for (String parType : action.getParamTypes()) {
                    String value = valueQ.remove();
                    switch (parType) {
                        case "int", "Integer" -> newCode.append(Integer.valueOf(value));
                        case "double", "Double" -> newCode.append(Double.valueOf(value));
                        case "float", "Float" -> newCode.append(Float.valueOf(value)).append("f");
                        case "boolean", "Boolean" -> newCode.append(Boolean.valueOf(value));
                        case "String" -> newCode.append("\"").append(value).append("\"");
                        default -> newCode.append("null");
                    }
                    if (commas > 0) {
                        newCode.append(", ");
                        commas--;
                    }
                }
                newCode.append(");\n");
            }
        }
        newCode.append("\t}");
        for (int i = text.length() - 1; i >= 0; --i) {
            if (text.charAt(i) == '}') {
                text = this.insertString(text, newCode.toString(), i-2);
                break;
            }
        }
        File temp = new File(System.getProperty("java.io.tmpdir") + "/ga_suite/run/" + testSuite.getClassName() + ".java");
        temp.getParentFile().mkdirs();
        FileWriter myWriter = new FileWriter(temp);
        myWriter.write(text);
        myWriter.close();
    }

    // Helper function that inserts a String into another String after a certain index
    private String insertString(String originalString, String stringToBeInserted, int index) {
        StringBuilder newString = new StringBuilder();
        for (int i = 0; i < originalString.length(); i++) {
            newString.append(originalString.charAt(i));
            if (i == index) {
                newString.append(stringToBeInserted);
            }
        }
        return newString.toString();
    }

    // Main method of the executor. We receive a test suite and we return its fitness:
    // number of branches covered / total number of branches
    public double calculateFitness(TestSuite testSuite) throws Exception {
        this.prepareEvaluation(testSuite);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, System.getProperty("java.io.tmpdir") + "/ga_suite/run/" + testSuite.getClassName() + ".java");
        File resource = new File(System.getProperty("java.io.tmpdir") + "/ga_suite/run/" + testSuite.getClassName() + ".class");
        final String targetName = testSuite.getClassName();

        // For instrumentation and runtime we need an IRuntime instance to collect execution data
        final IRuntime runtime = new LoggerRuntime();

        // The Instrumenter creates a modified version of the test target class
        // that contains additional probes for execution data recording
        final Instrumenter instr = new Instrumenter(runtime);
        InputStream original = FileUtils.openInputStream(resource);
        final byte[] instrumented = instr.instrument(original, targetName);
        original.close();

        // Starting up the runtime
        final RuntimeData data = new RuntimeData();
        runtime.startup(data);

        // Using a special class loader to directly load the instrumented class definition from a byte array
        final MemoryClassLoader memoryClassLoader = new MemoryClassLoader();
        memoryClassLoader.addDefinition(targetName, instrumented);
        final Class<?> targetClass = memoryClassLoader.loadClass(targetName);

        // Here we execute our test target class through its Runnable interface
        final Constructor<?> constructor = targetClass.getConstructors()[0];
        final Runnable targetInstance = (Runnable) constructor.newInstance();
        targetInstance.run();

        // At the end of test execution we collect execution data and shutdown the runtime
        final ExecutionDataStore executionData = new ExecutionDataStore();
        final SessionInfoStore sessionInfos = new SessionInfoStore();
        data.collect(executionData, sessionInfos, false);
        runtime.shutdown();

        // Together with the original class definition we can calculate coverage information
        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
        original = FileUtils.openInputStream(resource);
        analyzer.analyzeClass(original, targetName);
        original.close();

        IClassCoverage cc = (IClassCoverage) coverageBuilder.getClasses().toArray()[0];
        ICounter branchCounter = cc.getBranchCounter();
        int coveredBranches = branchCounter.getCoveredCount(); // Number of covered branches
        int totalBranches = branchCounter.getTotalCount(); // Number of total branches
        //noinspection ResultOfMethodCallIgnored
        resource.delete();
        resource = new File(System.getProperty("java.io.tmpdir") + "/ga_suite/run/" + testSuite.getClassName() + ".java");
        //noinspection ResultOfMethodCallIgnored
        resource.delete();
        return (double) coveredBranches / totalBranches; // Resulting fitness
    }
}
