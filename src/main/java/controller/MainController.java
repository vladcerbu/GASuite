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
        this.generateButton.setDisable(true);
        this.resultLabel.setVisible(false);
        this.stateLabel.setTextFill(Paint.valueOf("Orange"));
        this.stateLabel.setText("State: Generating...");
    }

    // If the algorithm ends unexpectedly then we print the error
    @FXML
    private void stateFailed(Exception exception) {
        this.generateButton.setDisable(false);
        this.resultLabel.setVisible(false);
        this.stateLabel.setTextFill(Paint.valueOf("Red"));
        this.stateLabel.setText("State: An error has occured!\n" + exception.getMessage());
        System.out.println(Arrays.toString(exception.getStackTrace()));
    }

    // If the algorithm ends successfully then we print the results
    @FXML
    private void stateDone() {
        this.generateButton.setDisable(false);
        this.stateLabel.setTextFill(Paint.valueOf("Green"));
        this.stateLabel.setText("State: Done!");
        double result = this.ga.getBestFitness() * 100;
        if (result <= 100.0)
            this.resultLabel.setTextFill(Paint.valueOf("Green"));
        if (result < 90.0)
            this.resultLabel.setTextFill(Paint.valueOf("Yellow"));
        if (result < 70.0)
            this.resultLabel.setTextFill(Paint.valueOf("Red"));
        this.resultLabel.setText("Result: The solution has a coverage of " + String.format("%.2f", result) + "%  Time: " + String.format("%.2f", this.ga.getExecutionTime()) + "s");
        this.resultLabel.setVisible(true);
    }

    // Handling of the File Chooser
    @FXML
    public void handleClassFileChoice() {
        FileChooser fileChooser = new FileChooser();
        File selected = fileChooser.showOpenDialog(this.stage);
        if (selected != null && selected.exists() && selected.isFile() && selected.toString().endsWith(".java"))
            this.classPathTextField.setText(selected.getAbsolutePath());
        else
            this.classPathTextField.setText("You need to specify a .java file.");
    }

    // Handling of the Directory Chooser
    @FXML
    public void handleResultFileChoice() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selected = directoryChooser.showDialog(this.stage);
        if (selected != null && selected.exists() && selected.isDirectory())
            this.resultPathTextField.setText(selected.getAbsolutePath());
        else
            this.resultPathTextField.setText("You need to specify a directory.");
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
            minNr = Integer.parseInt(this.minNrTextField.getText());
            maxNr = Integer.parseInt(this.maxNrTextField.getText());
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
            strLength = Integer.parseInt(this.strLengthTextField.getText());
            if (strLength < 0)
                strLength = Integer.parseInt(ApplicationContext.getProperties().getProperty("data.default.maxStringLength"));
            this.ga.setMaxStringLength(strLength);
        } catch (NumberFormatException ignored) {
            this.ga.setMaxStringLength(Integer.parseInt(ApplicationContext.getProperties().getProperty("data.default.maxStringLength")));
        }

        try {
            solLength = Integer.parseInt(this.solLengthTextField.getText());
            if (solLength < 1)
                solLength = Integer.parseInt(ApplicationContext.getProperties().getProperty("data.default.maxSuiteLength"));
            this.ga.setMaxSuiteLength(solLength);
        } catch (NumberFormatException e) {
            this.ga.setMaxSuiteLength(Integer.parseInt(ApplicationContext.getProperties().getProperty("data.default.maxSuiteLength")));
        }

        try {
            populationSize = Integer.parseInt(this.populationSizeTextField.getText());
            if (populationSize < 2)
                populationSize = Integer.parseInt(ApplicationContext.getProperties().getProperty("data.populationSize"));
            this.ga.setPopulationSize(populationSize);
        } catch (NumberFormatException e) {
            this.ga.setPopulationSize(Integer.parseInt(ApplicationContext.getProperties().getProperty("data.populationSize")));
        }

        try {
            generations = Integer.parseInt(this.generationsNumberTextField.getText());
            if (generations < 0)
                generations = Integer.parseInt(ApplicationContext.getProperties().getProperty("data.generations"));
            this.ga.setGenerations(generations);
        } catch (NumberFormatException e) {
            this.ga.setGenerations(Integer.parseInt(ApplicationContext.getProperties().getProperty("data.generations")));
        }

        try {
            crossoverProb = Double.parseDouble(this.crossoverProbabilityTextField.getText());
            if (crossoverProb <= 0 || crossoverProb >= 1)
                crossoverProb = Double.parseDouble(ApplicationContext.getProperties().getProperty("data.crossoverProb"));
            this.ga.setCrossoverProb(crossoverProb);
        } catch (NumberFormatException e) {
            this.ga.setCrossoverProb(Double.parseDouble(ApplicationContext.getProperties().getProperty("data.crossoverProb")));
        }

        try {
            initialAdditionProb = Double.parseDouble(this.additionProbabilityTextField.getText());
            if (initialAdditionProb <= 0 || initialAdditionProb >= 1)
                initialAdditionProb = Double.parseDouble(ApplicationContext.getProperties().getProperty("data.initialAdditionProb"));
            this.ga.setInitialAdditionProb(initialAdditionProb);
        } catch (NumberFormatException e) {
            this.ga.setInitialAdditionProb(Double.parseDouble(ApplicationContext.getProperties().getProperty("data.initialAdditionProb")));
        }

        ToggleGroup toggleGroup = this.small1.getToggleGroup();
        Toggle selected = toggleGroup.getSelectedToggle();
        if (selected == this.small1)
            this.ga.setStringType(1);
        if (selected == this.big2)
            this.ga.setStringType(2);
        if (selected == this.numbers3)
            this.ga.setStringType(3);
        if (selected == this.others4)
            this.ga.setStringType(4);

        this.ga.setOnlyFirst(this.firstCheckBox.isSelected());

        // A requirement of the algorithm is that the path to the class file is correct.
        // If not, then the execution can't take place, so we return false here
        File classFile = new File(this.classPathTextField.getText());
        if (!classFile.exists() || !classFile.isFile()) {
            stateFailed(new Exception("Given path of a class file is not a .java file!"));
            return false;
        }
        else
            this.ga.setFullPath(this.classPathTextField.getText());

        classFile = new File(this.resultPathTextField.getText());
        if (!classFile.exists() || !classFile.isDirectory())
            this.ga.setResultDirectory(ApplicationContext.getProperties().getProperty("data.resultsfile"));
        else
            this.ga.setResultDirectory(this.resultPathTextField.getText());

        return true; // If everything is in order we return true
    }
}
