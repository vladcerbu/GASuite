package ga;

import application.appconfig.ApplicationContext;
import chromosome.TestCase;
import chromosome.TestSuite;
import chromosome.action.Action;
import chromosome.action.ConstructorAction;
import chromosome.action.MethodAction;
import executor.TestSuiteExecutor;
import parser.SpoonParser;
import spoon.reflect.declaration.CtMethod;
import util.RandomHelper;
import writer.ResultWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class GA {
//    private final String origDirectory = "src\\main\\resources\\originals";
    private final String tempDirectory = System.getProperty("java.io.tmpdir") + "\\ga_suite";
//    private final String origDirectory = "src\\main\\resources\\originals";
    private final String origDirectory = System.getProperty("java.io.tmpdir") + "\\ga_suite\\originals";
//    private final String spoonDirectory = "src\\main\\resources\\spooned";
    private final String spoonDirectory = System.getProperty("java.io.tmpdir") + "\\ga_suite\\spooned";
//    private final String runDirectory = "src\\main\\resources\\run";
    private final String runDirectory = System.getProperty("java.io.tmpdir") + "\\ga_suite\\run";
    private String fullPath;
    private String resultDirectory;
    private ResultWriter resultWriter;
    private TestSuiteExecutor executor;
    private RandomHelper randomHelper;
    private SpoonParser spoonParser;
    private ArrayList<TestSuite> population;
    private int maxSuiteLength;
    private int maxStringLength;
    private int minNr;
    private int maxNr;
    private int stringType;
    private boolean onlyFirst;
    private int populationSize;
    private int generations;
    private double crossoverProb;
    private double initialAdditionProb;
    private final int varNameLength = Integer.parseInt(ApplicationContext.getProperties().getProperty("data.varNameLength"));
    private double executionTime = 0.0;

    // Comparator for sorting the chromosomes. Biggest fitness first,
    // if equal then we choose the smallest one in terms of test case number
    private final Comparator<TestSuite> comp = (o1, o2) -> {
        if (o1.getFitness() > o2.getFitness())
            return -1;
        else if (o1.getFitness() < o2.getFitness())
            return 1;
        else
            return Integer.compare(o1.getTestCases().size(), o2.getTestCases().size());
    };

    public GA() { }

    public double getWorstFitness() {
        return population.get(population.size() - 1).getFitness();
    }

    public double getAverageFitness() {
        double total = 0.0;
        for (TestSuite chromosome : population)
            total += chromosome.getFitness();
        return total / population.size();
    }

    public double getExecutionTime() {
        return executionTime;
    }

    public double getBestFitness() {
        return population.get(0).getFitness();
    }

    public void setStringType(int stringType) {
        if (stringType > 4 || stringType < 1)
            this.stringType = 1;
        else
            this.stringType = stringType;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public void setResultDirectory(String resultDirectory) {
        this.resultDirectory = resultDirectory;
    }

    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    public void setGenerations(int generations) {
        this.generations = generations;
    }

    public void setCrossoverProb(double crossoverProb) {
        this.crossoverProb = crossoverProb;
    }

    public void setInitialAdditionProb(double initialAdditionProb) {
        this.initialAdditionProb = initialAdditionProb;
    }

    public void setOnlyFirst(boolean onlyFirst) {
        this.onlyFirst = onlyFirst;
    }

    public void setMaxNr(int maxNr) {
        this.maxNr = maxNr;
    }

    public void setMinNr(int minNr) {
        this.minNr = minNr;
    }

    public void setMaxStringLength(int maxStringLength) {
        this.maxStringLength = maxStringLength;
    }

    public void setMaxSuiteLength(int maxSuiteLength) {
        this.maxSuiteLength = maxSuiteLength;
    }

    // Checking maximum suite size and adjusting it if it is too small
    private void checkMaxSuiteSize() {
        if (this.maxSuiteLength < 4 * this.spoonParser.getTestableMethods().size())
            this.maxSuiteLength = 4 * this.spoonParser.getTestableMethods().size();
    }

    // Initializing the algorithm. Create necessary classes and generate random population
    private void initialize() throws IOException {
        if (Files.exists(Paths.get(this.runDirectory)))
            this.deleteFolder(this.runDirectory);
        if (Files.exists(Paths.get(this.origDirectory)))
            this.deleteFolder(this.origDirectory);
        if (Files.exists(Paths.get(this.spoonDirectory)))
            this.deleteFolder(this.spoonDirectory);
        if (Files.exists(Paths.get(this.tempDirectory)))
            this.deleteFolder(this.tempDirectory);
        Files.createDirectory(Paths.get(this.tempDirectory));
        Files.createDirectory(Paths.get(this.spoonDirectory));
        Files.createDirectory(Paths.get(this.origDirectory));

        StringBuilder rootPathBuilder = new StringBuilder();
        ArrayList<String> splitedPath = new ArrayList<>(List.of(this.fullPath.split("\\\\")));
        for (String s : splitedPath) {
            rootPathBuilder.append(s);
            if (s.equals("java"))
                break;
            rootPathBuilder.append("\\");
        }
        String rootPath = rootPathBuilder.toString();
        this.spoonParser = new SpoonParser(rootPath, this.fullPath, this.origDirectory, this.spoonDirectory);
        this.executor = new TestSuiteExecutor(this.spoonParser.getNestedClasses(), this.spoonDirectory, this.runDirectory);
        this.resultWriter = new ResultWriter(this.resultDirectory);
        this.randomHelper = new RandomHelper(this.spoonParser, this.minNr, this.maxNr, this.maxStringLength, this.stringType);
        // Checking the maximum suite size and adjusting it accordingly
        this.checkMaxSuiteSize();
        // Generating random population to start with
        this.generateRandomPopulation();
        // Evaluating the population
        this.evaluatePopulation();
        // Sorting the population to have the best chromosome first
        this.population.sort(this.comp);
    }

    // Genetic Algorithm main function
    public void startAlgorithm() throws Exception {
        long start = System.nanoTime();
        this.initialize();
        for (int gen = 0; gen < this.generations; gen++) {
            // If the user checked the CheckBox for getting only the first found perfect solution, then we return it.
            if (this.onlyFirst && this.population.get(0).getFitness() == 1.0)
                break;
            ArrayList<TestSuite> newPop = new ArrayList<>();
            // Elitism: The best suite from the previous generation is kept in the new population
            newPop.add(new TestSuite(this.population.get(0)));
            while (newPop.size() < this.populationSize) {
                ArrayList<TestSuite> offsprings = new ArrayList<>();
                ArrayList<TestSuite> parents = this.select(); // Selecting parents
                if (this.randomHelper.generateRandomDouble(0, 1) < this.crossoverProb) // Crossover if probability is met
                    offsprings = this.crossover(new TestSuite(parents.get(0)), new TestSuite(parents.get(1)));
                else // Else the offsprings will be the parents
                {
                    offsprings.add(new TestSuite(parents.get(0)));
                    offsprings.add(new TestSuite(parents.get(1)));
                }
                // Mutating the offsprings
                offsprings.set(0, this.mutate(offsprings.get(0)));
                offsprings.set(1, this.mutate(offsprings.get(1)));
                // Evaluating the offsprings
                offsprings.get(0).setFitness(this.evaluateChromosome(offsprings.get(0)));
                offsprings.get(1).setFitness(this.evaluateChromosome(offsprings.get(1)));

                double bestParentFitness = parents.get(0).getFitness() >= parents.get(1).getFitness() ? parents.get(0).getFitness() : parents.get(1).getFitness();
                double bestOffspringFitness = offsprings.get(0).getFitness() >= offsprings.get(1).getFitness() ? offsprings.get(0).getFitness() : offsprings.get(1).getFitness();
                int lengthParents = parents.get(0).getTestCases().size() + parents.get(1).getTestCases().size();
                int lengthOffsprings = offsprings.get(0).getTestCases().size() + offsprings.get(1).getTestCases().size();
                int bestLength = this.population.get(0).getTestCases().size();
                // Checking to see if the new chromosomes are better than the parents and then decide which ones to add to the new population
                if (bestOffspringFitness > bestParentFitness || (bestOffspringFitness == bestParentFitness && lengthOffsprings <= lengthParents))
                    for (int i = 0; i < offsprings.size(); ++i)
                        if (offsprings.get(i).getTestCases().size() <= 2 * bestLength)
                            newPop.add(offsprings.get(i));
                        else
                            newPop.add(parents.get(i));
                else
                    newPop.addAll(parents);
            }
            this.population = newPop;
            this.population.sort(comp);
            // If population is too big, we reduce it to the maximum size by eliminating the worst chromosomes
            if (this.population.size() > this.populationSize) {
                this.population.subList(this.populationSize, this.population.size()).clear();
            }
        }

        // Write the resulted test suite (the best one)
        try {
            this.resultWriter.writeSuite(this.population.get(0));
        } catch (IOException e) {
            throw new Exception("Unable to write the result");
        }

        // Delete folders
        deleteFolder(this.origDirectory);
        deleteFolder(this.spoonDirectory);
        deleteFolder(this.tempDirectory);

        // Calculate execution time
        long end = System.nanoTime();
        this.executionTime = (double) (end - start) / 1E9;
    }

    // Selection operator - Rank-Based Selection
    private ArrayList<TestSuite> select() {
        ArrayList<TestSuite> selected = new ArrayList<>();
        double sumRanks = this.populationSize * (this.populationSize + 1) / 2.0;
        double randomRank = randomHelper.generateRandomDouble(0, 1);
        double cumulativeSum = 0.0;

        for (int i = 0; i < this.populationSize; ++i) {
            cumulativeSum += (this.populationSize - i) / sumRanks;
            if (cumulativeSum >= randomRank) {
                selected.add(this.population.get(i));
                break;
            }
        }

        randomRank = this.randomHelper.generateRandomDouble(0, 1);
        cumulativeSum = 0.0;
        for (int i = 0; i < this.populationSize; ++i) {
            cumulativeSum += (this.populationSize - i) / sumRanks;
            if (cumulativeSum >= randomRank) {
                selected.add(this.population.get(i));
                break;
            }
        }

        return selected;
    }

    // Crossover operator - One-Point Crossover
    private ArrayList<TestSuite> crossover(TestSuite c1, TestSuite c2) {
        // Cutting point
        double p = this.randomHelper.generateRandomDouble(0, 1);
        ArrayList<TestCase> tc1 = c1.getTestCases();
        ArrayList<TestCase> tc2 = c2.getTestCases();
        int mid1 = (int) (p * tc1.size());
        int mid2 = (int) (p * tc2.size());
        ArrayList<TestCase> tco1 = new ArrayList<>();
        ArrayList<TestCase> tco2 = new ArrayList<>();
        for (int i = 0; i < mid1; ++i)
            tco1.add(tc1.get(i));
        for (int i = 0; i < mid2; ++i)
            tco2.add(tc2.get(i));
        for (int i = mid1; i < tc1.size(); ++i)
            tco2.add(tc1.get(i));
        for (int i = mid2; i < tc2.size(); ++i)
            tco1.add(tc2.get(i));
        ArrayList<TestSuite> offsprings = new ArrayList<>();
        offsprings.add(new TestSuite(c1.getClassName(), tco1));
        offsprings.add(new TestSuite(c2.getClassName(), tco2));
        return offsprings;
    }

    // Mutation operator - Custom Mutation
    private TestSuite mutate(TestSuite testSuite) {
        ArrayList<TestCase> testCases = testSuite.getTestCases();
        ArrayList<TestCase> toDelete = new ArrayList<>();
        // Mutation probability is always 1/n
        double mutationProb = 1.0 / testCases.size();
        double prob, mutationType;
        for (TestCase currentTestCase : testCases) {
            // Checking which test cases to mutate based on the probability
            prob = this.randomHelper.generateRandomDouble(0, 1);
            if (prob <= mutationProb) {
                // Checking which type of mutation to apply to the test case
                mutationType = this.randomHelper.generateRandomDouble(0, 1);
                if (mutationType <= 1.0 / 3.0) // Delete test case
                    toDelete.add(currentTestCase); // We delete all of them at the end of the loop
                else if (mutationType > 1.0 / 3.0 && mutationType <= 2.0 / 3.0) // New Values
                    for (Action action : currentTestCase.getActions())
                        this.randomHelper.generateRandomValues(action);
                else { // New method
                    ArrayList<Action> actions = currentTestCase.getActions();
                    // Choose a new method different from the current one
                    MethodAction oldMethod = (MethodAction) actions.remove(1); // Remove the old method
                    CtMethod<?> method = this.spoonParser.getRandomMethod();
                    if (this.spoonParser.getTestableMethods().size() > 1)
                        while (oldMethod.getMethodName().equals(method.getSimpleName()))
                            method = this.spoonParser.getRandomMethod();

                    // Make sure that the variable name in the method is different from the one in the constructor
                    String varn = this.randomHelper.generateRandomVarName(this.varNameLength);
                    String calln = actions.get(0).getVarName();
                    while (varn.equals(calln))
                        varn = this.randomHelper.generateRandomVarName(this.varNameLength);

                    // Initialize the new method and generate new values
                    MethodAction newMethod = new MethodAction(method, calln, varn);
                    this.randomHelper.generateRandomValues(newMethod);
                    actions.add(newMethod); // Add the new method to the test case actions
                    currentTestCase.setTestedMethodName(method.getSimpleName());
                    currentTestCase.setActions(actions);
                }
            }
        }
        // Delete the test cases that were mutated to be deleted
        for (TestCase testCase : toDelete)
            testCases.remove(testCase);

        // Add new test cases to the suite
        double additionProb = this.initialAdditionProb;
        double additionRandom = this.randomHelper.generateRandomDouble(0, 1);
        while (additionProb >= additionRandom && testCases.size() < this.maxSuiteLength) {
            testCases.add(this.generateRandomTestCase());
            additionProb = additionProb * additionProb;
            additionRandom = this.randomHelper.generateRandomDouble(0, 1);
        }
        testSuite.setTestCases(testCases);
        // Validate the outcome and return the mutated chromosome
        return this.validateChromosome(testSuite);
    }

    // Validation of a test suite (it needs to have different variable names)
    private TestSuite validateChromosome(TestSuite testSuite) {
        Set<TestCase> setTC = new LinkedHashSet<>(testSuite.getTestCases());
        testSuite.setTestCases(new ArrayList<>(setTC));
        ArrayList<TestCase> testCases = testSuite.getTestCases();
        for (TestCase testCase : testCases) {
            for (TestCase other : testCases) {
                if (!testCase.equals(other)) {
                    // Checking for the constructor's variable name to be different from the other's variable names
                    if (testCase.getActions().get(0).getVarName().equals(other.getActions().get(0).getVarName()) ||
                            testCase.getActions().get(0).getVarName().equals(other.getActions().get(1).getVarName())) {
                        String varn = this.randomHelper.generateRandomVarName(this.varNameLength);
                        while (varn.equals(other.getActions().get(0).getVarName()) || varn.equals(other.getActions().get(1).getVarName()))
                            varn = this.randomHelper.generateRandomVarName(this.varNameLength);
                        testCase.getActions().get(0).setVarName(varn);
                        MethodAction methodAction = (MethodAction) testCase.getActions().get(1);
                        methodAction.setCallName(varn);
                        testCase.getActions().set(1, methodAction);
                    }
                    // Checking for the method's variable name to be different from the other's variable names
                    if (testCase.getActions().get(1).getVarName().equals(other.getActions().get(0).getVarName()) ||
                            testCase.getActions().get(1).getVarName().equals(other.getActions().get(1).getVarName())) {
                        String varn = this.randomHelper.generateRandomVarName(this.varNameLength);
                        while (varn.equals(other.getActions().get(0).getVarName()) || varn.equals(other.getActions().get(1).getVarName()))
                            varn = this.randomHelper.generateRandomVarName(this.varNameLength);
                        testCase.getActions().get(1).setVarName(varn);
                    }
                }
            }
        }
        testSuite.setTestCases(testCases);
        return testSuite;
    }

    // Generating a random population of the given size
    private void generateRandomPopulation() {
        this.population = new ArrayList<>();
        for (int i = 0; i < this.populationSize; ++i) {
            TestSuite testSuite = new TestSuite(this.spoonParser.getClassName());
            ArrayList<TestCase> testCases = new ArrayList<>();
            // Generate a random suite with random length
            int max = this.randomHelper.generateRandomInteger(this.maxSuiteLength / 2, this.maxSuiteLength);
            for (int j = 0; j < max; ++j) {
                TestCase testCase = this.generateRandomTestCase();
                testCases.add(testCase);
            }
            testSuite.setTestCases(testCases);
            this.validateChromosome(testSuite);
            this.population.add(testSuite);
        }
    }

    // Generating a random test case
    private TestCase generateRandomTestCase() {
        ArrayList<Action> actions = new ArrayList<>();
        String calln = this.randomHelper.generateRandomVarName(this.varNameLength);
        ConstructorAction constructorAction = new ConstructorAction(this.spoonParser.getTestedClassConstructor(), calln);
        this.randomHelper.generateRandomValues(constructorAction);

        String varn = this.randomHelper.generateRandomVarName(this.varNameLength);
        while (varn.equals(calln))
            varn = this.randomHelper.generateRandomVarName(this.varNameLength);
        MethodAction methodAction = new MethodAction(this.spoonParser.getRandomMethod(), calln, varn);
        this.randomHelper.generateRandomValues(methodAction);

        actions.add(constructorAction);
        actions.add(methodAction);
        return new TestCase(methodAction.getMethodName(), actions);
    }

    // Evaluating the population
    private void evaluatePopulation() {
        for (TestSuite testSuite : this.population)
            testSuite.setFitness(this.evaluateChromosome(testSuite));
    }

    // Evaluating a chromosome
    private double evaluateChromosome(TestSuite testSuite) {
        try {
            return this.executor.calculateFitness(testSuite);
        } catch (Exception e) {
            return 0.0;
        }
    }

    // Delete the folder from the given path
    private void deleteFolder(String path) {
        File folder = new File(path);
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        }
        //noinspection ResultOfMethodCallIgnored
        folder.delete();
    }
}
