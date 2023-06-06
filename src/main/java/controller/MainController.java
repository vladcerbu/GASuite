package controller;

import application.appconfig.ApplicationContext;
import ga.GA;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Paint;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Arrays;

public class MainController {

    private Stage stage;
    private GA ga;

    @FXML
    private TextField classPathTextField;
    @FXML
    private TextField minNrTextField;
    @FXML
    private TextField maxNrTextField;
    @FXML
    private TextField strLengthTextField;
    @FXML
    private TextField solLengthTextField;
    @FXML
    private TextField resultPathTextField;
    @FXML
    private TextField populationSizeTextField;
    @FXML
    private TextField generationsNumberTextField;
    @FXML
    private TextField crossoverProbabilityTextField;
    @FXML
    private TextField additionProbabilityTextField;

    @FXML
    private RadioButton small1;
    @FXML
    private RadioButton big2;
    @FXML
    private RadioButton numbers3;
    @FXML
    private RadioButton others4;
    @FXML
    private Button generateButton;
    @FXML
    private CheckBox firstCheckBox;
    @FXML
    private Label stateLabel;
    @FXML
    private Label resultLabel;

    @FXML
    public void initialize() { }

    public void init(Stage stage) {
        this.ga = new GA();
        this.stage = stage;
    }

    // While the algorithm is running
    @FXML
    private void stateGenerating() {
        generateButton.setDisable(true);
        resultLabel.setVisible(false);
        stateLabel.setTextFill(Paint.valueOf("Orange"));
        stateLabel.setText("State: Generating...");
    }

    // If the algorithm ends unexpectedly then we print the error
    @FXML
    private void stateFailed(Exception exception) {
        generateButton.setDisable(false);
        resultLabel.setVisible(false);
        stateLabel.setTextFill(Paint.valueOf("Red"));
        stateLabel.setText("State: An error has occured! " + exception.getMessage());
        System.out.println(Arrays.toString(exception.getStackTrace()));
    }

    // If the algorithm ends successfully then we print the results
    @FXML
    private void stateDone() {
        generateButton.setDisable(false);
        stateLabel.setTextFill(Paint.valueOf("Green"));
        stateLabel.setText("State: Done!");
        double result = this.ga.getBestFitness() * 100;
        if (result == 100.0)
            resultLabel.setTextFill(Paint.valueOf("Green"));
        if (result < 100.0)
            resultLabel.setTextFill(Paint.valueOf("Yellow"));
        if (result < 80.0)
            resultLabel.setTextFill(Paint.valueOf("Red"));
        resultLabel.setText("Result: The solution has a coverage of " + String.format("%.2f", result) + "%  Time: " + String.format("%.2f", this.ga.getExecutionTime()) + "s");
        resultLabel.setVisible(true);
    }

    // Handling of the File Chooser
    @FXML
    public void handleClassFileChoice() {
        FileChooser fileChooser = new FileChooser();
        File selected = fileChooser.showOpenDialog(stage);
        if (selected != null && selected.exists() && selected.isFile() && selected.toString().endsWith(".java"))
            classPathTextField.setText(selected.getAbsolutePath());
        else
            classPathTextField.setText("You need to specify a .java file.");
    }

    // Handling of the Directory Chooser
    @FXML
    public void handleResultFileChoice() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selected = directoryChooser.showDialog(stage);
        if (selected != null && selected.exists() && selected.isDirectory())
            resultPathTextField.setText(selected.getAbsolutePath());
        else
            resultPathTextField.setText("You need to specify a directory.");
    }

    // Pressing the Generate button
    @FXML
    public void handleGenerate() {
        if (prepareParameters()) {
                Thread thread = new Thread(() -> {
                    Platform.runLater(this::stateGenerating);
                    try {
                        this.ga.startAlgorithm();
                        Platform.runLater(this::stateDone);
                    } catch (Exception exception) {
                        Platform.runLater(() -> stateFailed(exception));
                    }
                });
                thread.setDaemon(true);
                thread.start();
        }
    }

    // Preparing parameters for the algorithm. If no parameter is specified then
    // we choose the default one from the resource file
    private boolean prepareParameters() {
        int minNr, maxNr, strLength, solLength, populationSize, generations;
        double crossoverProb, initialAdditionProb;

        try {
            minNr = Integer.parseInt(minNrTextField.getText());
            maxNr = Integer.parseInt(maxNrTextField.getText());
            if (minNr > maxNr) {
                int temp = minNr;
                minNr = maxNr;
                maxNr = temp;
            }
            this.ga.setMinNr(minNr);
            this.ga.setMaxNr(maxNr);
        } catch (NumberFormatException ignored) {
            this.ga.setMinNr(Integer.parseInt(ApplicationContext.getProperties().getProperty("data.default.minNumericalValue")));
            this.ga.setMaxNr(Integer.parseInt(ApplicationContext.getProperties().getProperty("data.default.maxNumericalValue")));
        }

        try {
            strLength = Integer.parseInt(strLengthTextField.getText());
            if (strLength < 0)
                strLength = Integer.parseInt(ApplicationContext.getProperties().getProperty("data.default.maxStringLength"));
            this.ga.setMaxStringLength(strLength);
        } catch (NumberFormatException ignored) {
            this.ga.setMaxStringLength(Integer.parseInt(ApplicationContext.getProperties().getProperty("data.default.maxStringLength")));
        }

        try {
            solLength = Integer.parseInt(solLengthTextField.getText());
            if (solLength < 1)
                solLength = Integer.parseInt(ApplicationContext.getProperties().getProperty("data.default.maxSuiteLength"));
            this.ga.setMaxSuiteLength(solLength);
        } catch (NumberFormatException e) {
            this.ga.setMaxSuiteLength(Integer.parseInt(ApplicationContext.getProperties().getProperty("data.default.maxSuiteLength")));
        }

        try {
            populationSize = Integer.parseInt(populationSizeTextField.getText());
            if (populationSize < 2)
                populationSize = Integer.parseInt(ApplicationContext.getProperties().getProperty("data.populationSize"));
            this.ga.setPopulationSize(populationSize);
        } catch (NumberFormatException e) {
            this.ga.setPopulationSize(Integer.parseInt(ApplicationContext.getProperties().getProperty("data.populationSize")));
        }

        try {
            generations = Integer.parseInt(generationsNumberTextField.getText());
            if (generations < 0)
                generations = Integer.parseInt(ApplicationContext.getProperties().getProperty("data.generations"));
            this.ga.setGenerations(generations);
        } catch (NumberFormatException e) {
            this.ga.setGenerations(Integer.parseInt(ApplicationContext.getProperties().getProperty("data.generations")));
        }

        try {
            crossoverProb = Double.parseDouble(crossoverProbabilityTextField.getText());
            if (crossoverProb <= 0 || crossoverProb >= 1)
                crossoverProb = Double.parseDouble(ApplicationContext.getProperties().getProperty("data.crossoverProb"));
            this.ga.setCrossoverProb(crossoverProb);
        } catch (NumberFormatException e) {
            this.ga.setCrossoverProb(Double.parseDouble(ApplicationContext.getProperties().getProperty("data.crossoverProb")));
        }

        try {
            initialAdditionProb = Double.parseDouble(additionProbabilityTextField.getText());
            if (initialAdditionProb <= 0 || initialAdditionProb >= 1)
                initialAdditionProb = Double.parseDouble(ApplicationContext.getProperties().getProperty("data.initialAdditionProb"));
            this.ga.setInitialAdditionProb(initialAdditionProb);
        } catch (NumberFormatException e) {
            this.ga.setInitialAdditionProb(Double.parseDouble(ApplicationContext.getProperties().getProperty("data.initialAdditionProb")));
        }

        ToggleGroup toggleGroup = small1.getToggleGroup();
        Toggle selected = toggleGroup.getSelectedToggle();
        if (selected == small1)
            this.ga.setStringType(1);
        if (selected == big2)
            this.ga.setStringType(2);
        if (selected == numbers3)
            this.ga.setStringType(3);
        if (selected == others4)
            this.ga.setStringType(4);

        this.ga.setOnlyFirst(firstCheckBox.isSelected());

        // A requirement of the algorithm is that the path to the class file is correct.
        // If not, then the execution can't take place, so we return false here
        File classFile = new File(classPathTextField.getText());
        if (!classFile.exists() || !classFile.isFile()) {
            stateFailed(new Exception("Given path of a class file is not a .java file!"));
            return false;
        }
        else
            this.ga.setClassPath(classPathTextField.getText());

        classFile = new File(resultPathTextField.getText());
        if (!classFile.exists() || !classFile.isDirectory())
            this.ga.setResultFile(ApplicationContext.getProperties().getProperty("data.resultsfile"));
        else
            this.ga.setResultFile(resultPathTextField.getText());

        return true; // If everything is in order we return true
    }
}
