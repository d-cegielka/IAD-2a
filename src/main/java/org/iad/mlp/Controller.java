package org.iad.mlp;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.univocity.parsers.common.processor.RowListProcessor;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import org.apache.commons.lang3.ArrayUtils;


public class Controller {
    Network network;
    Task training;
    StringBuilder globalErrorReport;

    @FXML
    private TextField neuronsOnLayers;

    @FXML
    private ChoiceBox<String> choiceBias;

    @FXML
    private TextArea textAreaPattern;

    @FXML
    private TextArea txtNetworkInfo;

    @FXML
    private TextArea txtTrainingInfo;

    @FXML
    private TextArea txtTestInfo;

    @FXML
    private TextField numOfEpochField;

    @FXML
    private ToggleGroup treningType;

    @FXML
    private RadioButton epoch;

    @FXML
    private RadioButton epochAndError;

    @FXML
    private TextField errorRateField;

    @FXML
    private TextField learningRateField;

    @FXML
    private TextField momentumRateField;

    @FXML
    private ChoiceBox<String> orderPattern;

    @FXML
    private ChoiceBox<String> patterns;

    @FXML
    private Label networkStatus;

    @FXML
    private Accordion infoSection;

    @FXML
    private ProgressBar progressTraining = new ProgressBar(0);

    @FXML
    private Button btnStartTrainingNetwork;

    @FXML
    private Button btnStopTrainingNetwork;

    ArrayList<Double[]> inputs;
    ArrayList<Double[]> outputs;

    public void initialize() throws IOException {
        choiceBias.getItems().addAll("tak","nie");
        orderPattern.getItems().addAll("losowa", "stała");
        choiceBias.getSelectionModel().select(0);
        orderPattern.getSelectionModel().select(0);
        readPatternFromTextArea();

        btnStartTrainingNetwork.setOnAction(trainingNetworkStartEvent);
        btnStopTrainingNetwork.setOnAction(trainingNetworkStopEvent);
    }


    @FXML
    public void trainingTypeChange() {
        if (epochAndError.isSelected()) {
            errorRateField.setDisable(false);
        } else {
            errorRateField.setDisable(true);
        }
    }

    @FXML
    public void loadNetwork() {
        try {
            File networkFile = openFile("Wybierz plik z siecią ");
            ObjectInputStream networkStream = new ObjectInputStream(new FileInputStream(networkFile));
            network = (Network) networkStream.readObject();
            networkStream.close();
            if(network.getNumOfNetworkLayers() > 0){
                networkStatus.setText("wczytano sieć");
                txtNetworkInfo.setText(network.toString());
                infoSection.setExpandedPane(infoSection.getPanes().get(0));
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            networkStatus.setText("nie udało się odczytać sieci! sprawdź plik i spróbuj ponownie");
        }
    }

    @FXML
    public void saveNetwork(){
        try {
            File networkFile = saveFile("Wybierz plik do zapisu sieci ");
            ObjectOutputStream outputObject = new ObjectOutputStream(new FileOutputStream(networkFile));
            outputObject.writeObject(network);
            outputObject.close();
            networkStatus.setText("zapisano sieć do pliku "+networkFile.getName());

        } catch (IOException e) {
            e.printStackTrace();
            networkStatus.setText("nie udało się zapisać pliku z siecią! spróbuj ponownie");
        }
    }


    @FXML
    public void createNetwork() {
        if(neuronsOnLayers.getText().isEmpty()) neuronsOnLayers.setText(neuronsOnLayers.getPromptText());
        String[] layersNeurons = neuronsOnLayers.getText().split(",");
        List<Integer> layersNeuronsList = new ArrayList<>();
        for (String obj : layersNeurons) {
            layersNeuronsList.add(Integer.parseInt(obj));
        }
        boolean isBias = false;
        if (choiceBias.getSelectionModel().isSelected(0)) {
            isBias = true;
        }
        network = new Network(layersNeuronsList, isBias);
        if(network.getNumOfNetworkLayers() == layersNeuronsList.size()){
            networkStatus.setText("utworzono sieć");
            txtNetworkInfo.setText(network.toString());
            infoSection.setExpandedPane(infoSection.getPanes().get(0));
        }
    }


    EventHandler<ActionEvent> trainingNetworkStartEvent = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent e) {
            if (numOfEpochField.getText().isEmpty()) {
                numOfEpochField.setText(numOfEpochField.getPromptText());
            }
            if(learningRateField.getText().isEmpty()) {
                learningRateField.setText(learningRateField.getPromptText());
            }
            if(momentumRateField.getText().isEmpty()) {
                momentumRateField.setText(momentumRateField.getPromptText());
            }
            infoSection.setExpandedPane(infoSection.getPanes().get(1));
            globalErrorReport = new StringBuilder();
            btnStartTrainingNetwork.setDisable(true);
            btnStopTrainingNetwork.setDisable(false);
            progressTraining.progressProperty().unbind();
            progressTraining.setProgress(0);
            training = trainingNetworkTask();

            //progressTraining.progressProperty().unbind();
            progressTraining.progressProperty().bind(training.progressProperty());

            networkStatus.setText("rozpoczęto trening sieci");

            training.messageProperty().addListener(new ChangeListener<String>() {
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue == "finished") {
                        networkStatus.setText("zakończono trening sieci");
                        btnStartTrainingNetwork.setDisable(false);
                        btnStopTrainingNetwork.setDisable(true);
                    }
                }
            });

            Thread networkThread = new Thread(training);
            networkThread.start();
        }
    };

    EventHandler<ActionEvent> trainingNetworkStopEvent = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                btnStartTrainingNetwork.setDisable(false);
                btnStopTrainingNetwork.setDisable(true);
                training.cancel(true);
                progressTraining.progressProperty().unbind();
                progressTraining.setProgress(0);
                networkStatus.setText("zatrzymano trening sieci");
            }
    };

  /*  @FXML
    public void startTrainingNetwork(ActionEvent actionEvent) {


    }*/

   /* @FXML
    public void stopTrainingNetwork() {
        btnStopTrainingNetwork.setOnAction(new EventHandler<ActionEvent>() {

        });
    }*/

    @FXML
    public void updateNetworkInfo() {
        txtNetworkInfo.setText(network.toString());
    }

    private Task trainingNetworkTask() {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                Random random = new Random();
                final int numOfEpoch = Integer.parseInt(numOfEpochField.getText());
                boolean isRandomizePatterns = false;
                double errorRate = 0d;
                double momentumRate = Double.parseDouble(momentumRateField.getText());
                double learningRate = Double.parseDouble(learningRateField.getText());
                double globalError = 0d;
                Thread.sleep(100);
                int quartile = 1;
                if (!errorRateField.isDisable()) {
                    errorRate = Double.parseDouble(errorRateField.getText());
                }

                if(patterns.getSelectionModel().getSelectedIndex() == 0){
                    isRandomizePatterns = true;
                }


                for(int i=0; i< numOfEpoch; i++) {
                    List<Double[]> inputsClone = new ArrayList<>();
                    inputsClone.addAll(inputs);
                    List<Double[]> outputsClone = new ArrayList<>();
                    outputsClone.addAll(outputs);
                    int indexPattern;
                    for (int w = 0; w < inputsClone.size(); w++) {
                        if (isRandomizePatterns) {
                            indexPattern = random.nextInt(inputsClone.size());
                        } else{
                            indexPattern = w;
                        }

                        network.learningNetwork(
                                ArrayUtils.toPrimitive(inputsClone.get(indexPattern)),
                                ArrayUtils.toPrimitive(outputsClone.get(indexPattern)),
                                learningRate,
                                momentumRate);
                        inputsClone.remove(indexPattern);
                        outputsClone.remove(indexPattern);
                        globalError += network.getNetworkError();
                    }
                    globalError /= network.getNumOfNerounsInLayer(network.getNumOfNetworkLayers() - 1);

                    if (i % 50 == 0) {
                        globalErrorReport.append(globalError).append("\n");
                    }

                    if(globalError <= errorRate){
                        i = numOfEpoch-1;
                    }

                    if(i == (quartile*0.25*numOfEpoch) || i == numOfEpoch-1) {
                        errorReport(globalError);
                        updateProgress(i+1,numOfEpoch);
                        Thread.sleep(250);
                        quartile++;
                    }
                    globalError = 0d;

                }
                updateMessage("finished");
                return true;
            }
        };
    }

    public File openFile(String info) throws IOException {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(info);
        File getFile = fileChooser.showOpenDialog(new Stage());
        return getFile;
    }

    public File saveFile(String info) throws IOException {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(info);
        /*String path = "C:\\TELEKOM-Z1\\data.txt";
        inputFile = new File(path);*/
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Wszystkie pliki (*.*)", "*.*");
        fileChooser.getExtensionFilters().add(extFilter);
        File saveFile = fileChooser.showSaveDialog(new Stage());
        return saveFile;
        //reportArea.setText(new String(Files.readAllBytes(Paths.get(inputFile.getPath()))));
    }

    public void errorReport(double error) {
        txtTrainingInfo.setText(String.valueOf(error));
    }

    @FXML
    public void readPatternFromTextArea() throws IOException {
        parsePattern(textAreaPattern.getText());
    }

    @FXML
    public void readPatternFromFile() throws IOException {
        File patternsFile = openFile("Wybierz plik z wzorcami");
        String textFile = new String(Files.readAllBytes(Paths.get(patternsFile.getPath())));
        textAreaPattern.clear();
        textAreaPattern.appendText(textFile);
        parsePattern(textFile);
    }


    public void parsePattern(String buffer) throws IOException {
        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
        CsvParserSettings parserSettings = new CsvParserSettings();
        parserSettings.setLineSeparatorDetectionEnabled(true);
        parserSettings.setDelimiterDetectionEnabled(true);
        RowListProcessor rowProcessor = new RowListProcessor();
        parserSettings.setProcessor(rowProcessor);
        parserSettings.setHeaderExtractionEnabled(true);

        CsvParser parser = new CsvParser(parserSettings);
        parser.parse(new StringReader(buffer));
        String[] headers = rowProcessor.getHeaders();
        patterns.getItems().clear();
        List<String[]> rows = rowProcessor.getRows();
        for (int i = 0; i < rows.size(); i++){
            patterns.getItems().add("(" + rows.get(i)[0] + "),("+rows.get(i)[1]+")");
            String[] inputString = (rows.get(i)[0]).split(",");
            String[] outputString = (rows.get(i)[1]).split(",");
            Double[] input = new Double[inputString.length];
            Double[] output = new Double[outputString.length];
            for (int j = 0; j < inputString.length; j++) {
                input[j] = Double.parseDouble(inputString[j]);
            }

            for (int j = 0; j < outputString.length; j++) {
                output[j] = Double.parseDouble(outputString[j]);
            }

            inputs.add(input);
            outputs.add(output);
        }

        patterns.getSelectionModel().select(0);

        for (int i=0;i<inputs.size();i++) {
            System.out.println(Arrays.toString(inputs.get(i))+","+Arrays.toString(outputs.get(i)));
        }
    }

    @FXML
    public void quitApp() {
        Platform.exit();
    }
}
