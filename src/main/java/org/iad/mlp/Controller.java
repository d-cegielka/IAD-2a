package org.iad.mlp;

import com.univocity.parsers.common.processor.RowListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.ArrayUtils;
import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Controller {
    Network network;
    Task<?> training;
    StringBuilder globalErrorReport;
    File lastFile;

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
    private RadioButton epochAndError;

    @FXML
    private TextField epochFreqField;

    @FXML
    private TextField errorRateField;

    @FXML
    private TextField learningRateField;

    @FXML
    private TextField momentumRateField;

    @FXML
    private ChoiceBox<String> orderPattern;

    @FXML
    private ListView<String> patterns;

    @FXML
    private Label networkStatus;

    @FXML
    private Accordion infoSection;

    @FXML
    private ProgressBar progressTraining;

    @FXML
    private Button btnStartTrainingNetwork;

    @FXML
    private Button btnStopTrainingNetwork;

    @FXML
    public LineChart<Number,Number> globalErrorLineChart;

    @FXML
    public NumberAxis epochAxis;

    @FXML
    public NumberAxis errorAxis;

    @FXML
    private XYChart.Series<Number,Number> globalErrorSeries;

    ArrayList<Double[]> inputs;
    ArrayList<Double[]> outputs;

    public void initialize() {
        choiceBias.getItems().addAll("tak","nie");
        orderPattern.getItems().addAll("losowa", "stała");
        choiceBias.getSelectionModel().select(0);
        orderPattern.getSelectionModel().select(0);
        readPatternFromTextArea();

        btnStartTrainingNetwork.setOnAction(trainingNetworkStartEvent);
        btnStopTrainingNetwork.setOnAction(trainingNetworkStopEvent);
    }

    /**
     * Zmiana typu warunku zakończenia treningu
     */
    @FXML
    public void trainingTypeChange() {
        errorRateField.setDisable(!epochAndError.isSelected());
    }

    /**
     * Wczytywanie sieci z pliku
     */
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

    /**
     * Zapisywanie sieci do pliku
     */
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

    /**
     * Tworzenie sieci
     */
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


    /**
     * Moduł obsługi rozpoczęcia treningu sieci
     */
    EventHandler<ActionEvent> trainingNetworkStartEvent = new EventHandler<>() {
        @Override
        public void handle(ActionEvent e) {
            //Obsługa wykresu
            globalErrorSeries = new XYChart.Series<>();
            epochAxis = new NumberAxis();
            errorAxis = new NumberAxis();
            globalErrorLineChart.getData().clear();
            globalErrorLineChart.getData().add(globalErrorSeries);

            //Inicializacja parametrów testu
            if (numOfEpochField.getText().isEmpty()) {
                numOfEpochField.setText(numOfEpochField.getPromptText());
            }
            if (epochFreqField.getText().isEmpty()) {
                epochFreqField.setText(epochFreqField.getPromptText());
            }
            if (learningRateField.getText().isEmpty()) {
                learningRateField.setText(learningRateField.getPromptText());
            }
            if (errorRateField.getText().isEmpty()) {
                errorRateField.setText(errorRateField.getPromptText());
            }
            if (momentumRateField.getText().isEmpty()) {
                momentumRateField.setText(momentumRateField.getPromptText());
            }

            //Raportowanie treningu
            infoSection.setExpandedPane(infoSection.getPanes().get(1));
            initializeTrainingReport();

            btnStartTrainingNetwork.setDisable(true);
            btnStopTrainingNetwork.setDisable(false);
            progressTraining.progressProperty().unbind();
            progressTraining.setProgress(0);
            training = trainingNetworkTask();
            progressTraining.progressProperty().bind(training.progressProperty());
            networkStatus.setText("rozpoczęto trening sieci");

            training.messageProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.equals("finished")) {
                    networkStatus.setText("zakończono trening sieci");
                    btnStartTrainingNetwork.setDisable(false);
                    btnStopTrainingNetwork.setDisable(true);
                    txtNetworkInfo.setText(network.toString());
                }
            });

            Thread networkThread = new Thread(training);
            networkThread.start();
        }
    };

    /**
     * Moduł obsługi zatrzymania treningu sieci
     */
    EventHandler<ActionEvent> trainingNetworkStopEvent = new EventHandler<>() {
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

    /**
     * Aktualizacja informacji o sieci
     */
    @FXML
    public void updateNetworkInfo() {
        txtNetworkInfo.setText(network.toString());
        infoSection.setExpandedPane(infoSection.getPanes().get(0));
    }

    /**
     * Trening sieci
     * @return zadanie treningu sieci
     */
    private Task<?> trainingNetworkTask() {
        return new Task<>() {
            @Override
            protected Object call() throws Exception {
                Random random = new Random();
                final int numOfEpoch = Integer.parseInt(numOfEpochField.getText());
                final int epochFreq = Integer.parseInt(epochFreqField.getText());
                boolean isRandomizePatterns = false;
                double errorRate = 0d;
                double momentumRate = Double.parseDouble(momentumRateField.getText());
                double learningRate = Double.parseDouble(learningRateField.getText());
                double globalError = 0d;
                Thread.sleep(100);
                int progress = 1;
                if (!errorRateField.isDisable()) {
                    errorRate = Double.parseDouble(errorRateField.getText());
                }

                if (patterns.getSelectionModel().getSelectedIndex() == 0) {
                    isRandomizePatterns = true;
                }

                for (int i = 0; i < numOfEpoch; i++) {
                    List<Double[]> inputsClone = new ArrayList<>(inputs);
                    List<Double[]> outputsClone = new ArrayList<>(outputs);
                    int indexPattern;
                    for (int w = 0; w < inputs.size(); w++) {
                        if (isRandomizePatterns) {
                            indexPattern = random.nextInt(inputsClone.size());
                        } else {
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
                    globalError /= inputs.size();

                    if (i % epochFreq == 0) {
                        globalErrorReport.append(globalError).append("\n");
                        addDataToErrorChart(i, globalError);
                    }

                    if (globalError <= errorRate) {
                        i = numOfEpoch - 1;
                    }

                    if (i == (progress * numOfEpoch / 10) || i == numOfEpoch - 1) {
                        errorReport(globalError);
                        updateProgress(i + 1, numOfEpoch);
                        Thread.sleep(150);
                        progress++;
                    }
                    globalError = 0d;
                }
                updateMessage("finished");
                return true;
            }
        };
    }

    /**
     * Aktualizacja live wykresu błędu globalnego podczas treningu sieci
     * @param epoch aktualna epoka
     * @param error wartość błędu globalnego
     */
    public void addDataToErrorChart(int epoch, double error) {
        Platform.runLater(()-> globalErrorSeries.getData().add(new XYChart.Data<>(epoch,error)));
    }



    public void errorReport(double error) {
        txtTrainingInfo.appendText(error +"\n");
    }

    @FXML
    public void testingPattern() {
        infoSection.setExpandedPane(infoSection.getPanes().get(2));
        Double[] testingPattern = inputs.get(patterns.getSelectionModel().getSelectedIndex());
        txtTestInfo.clear();
        txtTestInfo.appendText("Oczekiwane wyjście: " + Arrays.toString(testingPattern) + "\nOtrzymane wyjście: ");
        txtTestInfo.appendText(Arrays.toString(roundDoubleArray(network.testingNetwork(ArrayUtils.toPrimitive(testingPattern)))));
        txtTestInfo.appendText("\n\nBłąd średniokwadratowy dla testowanego wzorca: "+network.getNetworkError());
        networkStatus.setText("zaktualizowano informacje o sieci");
    }

    @FXML
    public void readPatternFromTextArea() {
        parsePattern(textAreaPattern.getText());
    }

    /**
     * Zapis wykresu do pliku png
     */
    public void saveChart() {
        try {
            File save = saveFile("Wybierz miejsce zapisu wykresu błędu globalnego");
            globalErrorLineChart.setAnimated(false);
            WritableImage image = globalErrorLineChart.snapshot(null, null);
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", save);
            globalErrorLineChart.setAnimated(true);
        } catch (IOException e) {
            e.printStackTrace();
            networkStatus.setText("nie udało się zapisać wykresu! spróbuj ponownie");
        }
    }

    /**
     * Zapis raportu z treningu sieci do pliku
     */
    @FXML
    public void saveTrainingReport() {
        try {
            File saveFile = saveFile("Wybierz plik do zapisu raportu treningu sieci");
            Files.writeString(Paths.get(saveFile.getPath()),globalErrorReport);
            networkStatus.setText("raport treningu został zapisany poprawnie do pliku "+saveFile.getName());

        } catch (IOException e) {
            e.printStackTrace();
            networkStatus.setText("nie udało się zapisać raportu z treningu! spróbuj ponownie");
        }
    }

    /**
     * Zapis informacji o sieci do pliku
     */
    public void saveNetworkReport() {
        try {
            File saveFile = saveFile("Wybierz plik do zapisu informacji o sieci");
            Files.writeString(Paths.get(saveFile.getPath()),network.toString());
            networkStatus.setText("informacje o sieci zostały zapisane poprawnie do pliku "+saveFile.getName());

        } catch (IOException e) {
            e.printStackTrace();
            networkStatus.setText("nie udało się zapisać informacji o sieci! spróbuj ponownie");
        }
    }

    /**
     * Zapis raportu z testowania sieci wzorcem do pliku
     */
    public void saveTestingReport() {
        try {
            File saveFile = saveFile("Wybierz plik do zapisu informacji o sieci");
            String testingReport = txtTestInfo.getText() + "\nSieć:\n" + network.toString();
            Files.writeString(Paths.get(saveFile.getPath()),testingReport);
            networkStatus.setText("informacje o sieci zostały zapisane poprawnie do pliku "+saveFile.getName());

        } catch (IOException e) {
            e.printStackTrace();
            networkStatus.setText("nie udało się zapisać informacji o sieci! spróbuj ponownie");
        }
    }

    /**
     * Inicjalizacja raportu z treningu sieci
     */
    public void initializeTrainingReport() {
        txtTrainingInfo.clear();
        globalErrorReport = new StringBuilder();
        globalErrorReport.append("Liczba epok: ").append(numOfEpochField.getText());
        globalErrorReport.append("\nSkok rejestracji błędu globalnego: ").append(epochFreqField.getText());
        if (!errorRateField.isDisable()) {
            globalErrorReport.append("\nWymagany poziom błędu globalnego kończący trening sieci:").append(errorRateField.getText());
        }
        globalErrorReport.append("\nWspółczynnik nauki: ").append(learningRateField.getText());
        globalErrorReport.append("\nWspółczynnik momentum: ").append(momentumRateField.getText());
        globalErrorReport.append("\nKolejność wzorców: ").append(orderPattern.getSelectionModel().getSelectedItem());
        globalErrorReport.append("\n\nBłąd globalny:\n");
    }

    /**
     * Odczyt wzorców z pliku
     */
    @FXML
    public void readPatternFromFile() {
        try {
            File patternsFile = openFile("Wybierz plik z wzorcami");
            String textFile = new String(Files.readAllBytes(Paths.get(patternsFile.getPath())));
            textAreaPattern.clear();
            textAreaPattern.appendText(textFile);
            parsePattern(textFile);
            networkStatus.setText("wzorce wczytano poprawnie");
        } catch (IOException e) {
            e.printStackTrace();
            networkStatus.setText("nie udało się odczytać wzorców z pliku! spróbuj ponownie");
        }
    }

    /**
     * Parsowanie wzorców do postaci list z wzorcami wejściowymi i wyjściowymi
     * @param allPattern nieprzetworzenie wzorce
     */
    public void parsePattern(String allPattern) {
        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
        CsvParserSettings parserSettings = new CsvParserSettings();
        parserSettings.setLineSeparatorDetectionEnabled(true);
        parserSettings.setDelimiterDetectionEnabled(true);
        RowListProcessor rowProcessor = new RowListProcessor();
        parserSettings.setProcessor(rowProcessor);
        parserSettings.setHeaderExtractionEnabled(true);

        CsvParser parser = new CsvParser(parserSettings);
        parser.parse(new StringReader(allPattern));
        rowProcessor.getHeaders();
        patterns.getItems().clear();
        List<String[]> rows = rowProcessor.getRows();
        for (String[] row : rows) {
            patterns.getItems().add("(" + row[0] + "),(" + row[1] + ")");
            String[] inputString = (row[0]).split(",");
            String[] outputString = (row[1]).split(",");
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
    }

    /**
     * Zaokrąglanie tablicy z wartościami zmiennoprzecinkowymi
     * @param input tablica z wartościami do zaokrąglenia
     * @return tablica z zaokrąglonymi wartościami
     */
    public double[] roundDoubleArray(double[] input) {
        double[] returnArray = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            returnArray[i] = Math.round(input[i] * 1000000.0) / 1000000.0;
        }
        return returnArray;
    }

    /**
     * Wybór pliku do odczytu
     * @param info komunikat jaki plik wybieramy
     * @return wybrany plik do odczytu
     */
    public File openFile(String info) {
        final FileChooser fileChooser = new FileChooser();
        if (lastFile != null) {
            fileChooser.setInitialDirectory(lastFile.getParentFile());
        }
        fileChooser.setTitle(info);
        File getFile = fileChooser.showOpenDialog(new Stage());
        lastFile = getFile;
        return getFile;
    }

    /**
     *
     * Wybór pliku do zapisu
     * @param info komunikat jaki plik zapisujemy
     * @return wybrany plik do zapisu
     */
    public File saveFile(String info) {
        final FileChooser fileChooser = new FileChooser();
        if (lastFile != null) {
            fileChooser.setInitialDirectory(lastFile.getParentFile());
        }
        fileChooser.setTitle(info);
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Wszystkie pliki (*.*)", "*.*");
        fileChooser.getExtensionFilters().add(extFilter);
        File saveFile = fileChooser.showSaveDialog(new Stage());
        lastFile = saveFile;
        return saveFile;
    }

    @FXML
    public void quitApp() {
        Platform.exit();
    }
}
