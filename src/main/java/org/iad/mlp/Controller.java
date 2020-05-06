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
import java.util.*;

public class Controller {
    public Label testingPatternsLabel;
    Network network;
    Task<?> training;
    StringBuilder globalErrorReport;
    StringBuilder globalValidationErrorReport;
    File lastFile;

    @FXML
    private TextField collectionDivisonField;

    @FXML
    private TextField collectionTrainingDivisionField;

    @FXML
    private TextField neuronsOnLayers;

    @FXML
    private ChoiceBox<String> choiceBias;

    @FXML
    private TextArea textAreaTrainingPatterns;

    @FXML
    private TextArea txtNetworkInfo;

    @FXML
    private TextArea txtTestInfo;

    @FXML
    private TextArea classificationTextArea;

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
    private ListView<String> testingPatterns;

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
    private XYChart.Series<Number,Number> globalErrorTrainingSeries;

    @FXML
    private XYChart.Series<Number,Number> globalErrorValidationSeries;

    Patterns patterns;

    ArrayList<Double[]> inputsTestingPatterns;
    ArrayList<Double[]> outputsTestingPatterns;

    public void initialize() {
        choiceBias.getItems().addAll("tak","nie");
        orderPattern.getItems().addAll("losowa", "stała");
        choiceBias.getSelectionModel().select(0);
        orderPattern.getSelectionModel().select(0);

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
            globalErrorTrainingSeries = new XYChart.Series<>();
            globalErrorValidationSeries = new XYChart.Series<>();
            globalErrorTrainingSeries.setName("zbiór treningowy");
            globalErrorValidationSeries.setName("zbiór walidacyjny");
            epochAxis = new NumberAxis();
            errorAxis = new NumberAxis();
            globalErrorLineChart.getData().clear();
            globalErrorLineChart.getData().add(globalErrorTrainingSeries);
            globalErrorLineChart.getData().add(globalErrorValidationSeries);

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

            final int numOfEpoch = Integer.parseInt(numOfEpochField.getText());
            final int epochFreq = Integer.parseInt(epochFreqField.getText());
            boolean isRandomizePatterns = false;
            if (orderPattern.getSelectionModel().getSelectedItem().equals("losowa")) {
                isRandomizePatterns = true;
            }
            double errorRate = 0d;
            double momentumRate = Double.parseDouble(momentumRateField.getText());
            double learningRate = Double.parseDouble(learningRateField.getText());
            if (!errorRateField.isDisable()) {
                errorRate = Double.parseDouble(errorRateField.getText());
            }

            //Raportowanie treningu
            infoSection.setExpandedPane(infoSection.getPanes().get(1));
            initializeTrainingReport();

            btnStartTrainingNetwork.setDisable(true);
            btnStopTrainingNetwork.setDisable(false);
            progressTraining.progressProperty().unbind();
            progressTraining.setProgress(0);
            training = trainingNetworkTask(numOfEpoch, epochFreq, momentumRate, learningRate ,errorRate,  isRandomizePatterns);
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
    private Task<?> trainingNetworkTask(final int numOfEpoch, final int epochFreq, double momentumRate,
                                        double learningRate , double errorRate, boolean isRandomizePatterns ) {
        return new Task<>() {
            @Override
            protected Object call() throws Exception {
                Random random = new Random();
                List<Double[]> inputsValidation = new ArrayList<>(patterns.getValidationInputs());
                List<Double[]> outputsValidation = new ArrayList<>(patterns.getValidationOutputs());
                List<Double[]> actualInputsTraining = new ArrayList<>(patterns.getTrainingInputs());
                List<Double[]> actualOutputsTraining = new ArrayList<>(patterns.getTrainingOutputs());
                double globalErrorTraining = 0d;
                double globalErrorValidation = 0d;
                Thread.sleep(100);
                int progress = 1;

                for (int i = 0; i < numOfEpoch; i++) {
                    List<Double[]> inputsClone = new ArrayList<>(actualInputsTraining);
                    List<Double[]> outputsClone = new ArrayList<>(actualOutputsTraining);
                    int indexPattern;
                    for (int w = 0; w < actualInputsTraining.size(); w++) {
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
                        globalErrorTraining += network.getNetworkError();
                    }
                    globalErrorTraining /= actualInputsTraining.size();

                    if (i % epochFreq == 0) {
                        globalErrorReport.append(globalErrorTraining).append("\n");
                        for(int j=0; j<inputsValidation.size();j++){
                            network.testingNetwork(ArrayUtils.toPrimitive(inputsValidation.get(j)),ArrayUtils.toPrimitive(outputsValidation.get(j)));
                            globalErrorValidation += network.getNetworkError();
                        }
                        globalErrorValidation /= inputsValidation.size();
                        globalValidationErrorReport.append(globalErrorValidation).append("\n");
                        addDataToErrorChart(i, globalErrorTraining,globalErrorValidation);
                        globalErrorValidation = 0d;
                    }

                    if (globalErrorTraining <= errorRate) {
                        i = numOfEpoch - 1;
                    }

                    if (i == (progress * numOfEpoch / 10) || i == numOfEpoch - 1) {
                        /*errorReport(globalErrorTraining);*/
                        updateProgress(i + 1, numOfEpoch);
                        Thread.sleep(150);
                        progress++;
                    }
                    globalErrorTraining = 0d;
                }
                updateMessage("finished");
                return true;
            }
        };
    }

    /**
     * Aktualizacja live wykresu błędu globalnego podczas treningu sieci
     * @param epoch aktualna epoka
     * @param errorTraining wartość błędu globalnego treningu
     * @param errorValidation wartość błędu globalnego walidacji
     */
    public void addDataToErrorChart(int epoch, double errorTraining, double errorValidation) {
        Platform.runLater(()-> globalErrorTrainingSeries.getData().add(new XYChart.Data<>(epoch,errorTraining)));
        Platform.runLater(()-> globalErrorValidationSeries.getData().add(new XYChart.Data<>(epoch,errorValidation)));
    }

    @FXML
    public void testingPattern() {
        infoSection.setExpandedPane(infoSection.getPanes().get(2));
        Double[] inputTestingPattern = inputsTestingPatterns.get(testingPatterns.getSelectionModel().getSelectedIndex());
        Double[] outputTestingPattern = outputsTestingPatterns.get(testingPatterns.getSelectionModel().getSelectedIndex());
        txtTestInfo.clear();
        txtTestInfo.appendText("Oczekiwane wyjście: " + Arrays.toString(outputTestingPattern) + "\nOtrzymane wyjście: ");
        txtTestInfo.appendText(Arrays.toString(roundDoubleArray(network.testingNetwork(ArrayUtils.toPrimitive(inputTestingPattern),ArrayUtils.toPrimitive(outputTestingPattern)))));
        txtTestInfo.appendText("\n\nBłąd średniokwadratowy dla testowanego wzorca: "+network.getNetworkError());
        networkStatus.setText("zaktualizowano informacje o sieci");
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
            Files.writeString(Paths.get(saveFile.getPath()),globalErrorReport.append(globalValidationErrorReport));
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
            File saveFile = saveFile("Wybierz plik do zapisu raportu z treningu sieci");
            String testingReport = txtTestInfo.getText() + "\nSieć:\n" + network.toString();
            Files.writeString(Paths.get(saveFile.getPath()),testingReport);
            networkStatus.setText("informacje zostały zapisane poprawnie do pliku "+saveFile.getName());

        } catch (IOException e) {
            e.printStackTrace();
            networkStatus.setText("nie udało się zapisać raportui! spróbuj ponownie");
        }
    }

    /**
     * Zapis raportu z klasyfikacji do pliku
     */
    public void saveClassificationReport() {
        try {
            File saveFile = saveFile("Wybierz plik do zapisu raportu z klasyfkiacji");
            String classificationReport = classificationTextArea.getText() + "\n\nSieć:\n" + network.toString();
            Files.writeString(Paths.get(saveFile.getPath()),classificationReport);
            networkStatus.setText("informacje  zostały zapisane poprawnie do pliku "+saveFile.getName());

        } catch (IOException e) {
            e.printStackTrace();
            networkStatus.setText("nie udało się raportu do pliku! spróbuj ponownie");
        }
    }

    /**
     * Inicjalizacja raportu z treningu sieci
     */
    public void initializeTrainingReport() {
        globalValidationErrorReport = new StringBuilder();
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
        globalValidationErrorReport.append("\n\nBłąd walidacyjny:\n");

    }

    /**
     * Odczyt wzorców treningowych z pliku
     */
    @FXML
    public void readPatternFromFile() {
        try {
            File patternsFile = openFile("Wybierz plik z wzorcami treningowymi");
            String textFile = new String(Files.readAllBytes(Paths.get(patternsFile.getPath())));
            textAreaTrainingPatterns.clear();
            textAreaTrainingPatterns.appendText(textFile);
            parseTrainingPattern(textFile);
            networkStatus.setText("wzorce treningowe wczytano poprawnie");
        } catch (IOException e) {
            e.printStackTrace();
            networkStatus.setText("nie udało się odczytać wzorców treningowych z pliku! spróbuj ponownie");
        }
    }

    /**
     * Odczyt wzorców treningowych z GUI
     */
    @FXML
    public void readPatternFromTextArea() {
        parseTrainingPattern(textAreaTrainingPatterns.getText());
        networkStatus.setText("wzorce wczytano poprawnie");
    }

    /**
     * Parsowanie wzorców do postaci list z wzorcami treningowymi wejściowymi i wyjściowymi
     * @param allPattern nieprzetworzenie wzorce
     */
    public void parseTrainingPattern(String allPattern) {
        patterns = new Patterns();
        CsvParserSettings parserSettings = new CsvParserSettings();
        parserSettings.setLineSeparatorDetectionEnabled(true);
        parserSettings.setDelimiterDetectionEnabled(true);
        RowListProcessor rowProcessor = new RowListProcessor();
        parserSettings.setProcessor(rowProcessor);
        parserSettings.setHeaderExtractionEnabled(true);

        CsvParser parser = new CsvParser(parserSettings);
        parser.parse(new StringReader(allPattern));
        rowProcessor.getHeaders();
        List<String[]> rows = rowProcessor.getRows();
        for (String[] row : rows) {
            patterns.addClassType(row[1]);
        }
        patterns.initializePatterns();
        for (String[] row : rows) {
            String[] inputString = (row[0]).split(",");
            Double[] input = new Double[inputString.length];
            for (int j = 0; j < inputString.length; j++) {
                input[j] = Double.parseDouble(inputString[j]);
            }

            patterns.addPattern(row[1],input);
        }
        if(collectionDivisonField.getText().isEmpty()){
            collectionDivisonField.setText(collectionDivisonField.getPromptText());
        }
        if(collectionTrainingDivisionField.getText().isEmpty()){
            collectionTrainingDivisionField.setText(collectionTrainingDivisionField.getPromptText());
        }
        patterns.createSubsetsPatterns(Double.parseDouble(collectionDivisonField.getText()) / 100,
                Double.parseDouble(collectionTrainingDivisionField.getText()) / 100);
        inputsTestingPatterns = new ArrayList<>(patterns.getTestingInputs());
        outputsTestingPatterns = new ArrayList<>(patterns.getTestingOutputs());
        loadTestingPatterns();
    }

    private void loadTestingPatterns(){
        testingPatterns.getItems().clear();
        for (int i = 0; i < inputsTestingPatterns.size(); i++) {
            testingPatterns.getItems().add(Arrays.toString(inputsTestingPatterns.get(i)) + "," + Arrays.toString(outputsTestingPatterns.get(i)));
        }
        testingPatterns.getSelectionModel().select(0);
        testingPatternsLabel.setText("Wzorce:\n   ("+inputsTestingPatterns.size()+")");

    }

    @FXML
    private void classification(){
        classificationTextArea.clear();
        classificationTextArea.appendText("Macierz pomyłek: \n");
        classificationTextArea.appendText("\t\t\t");
        for(Double[] type: patterns.getClassTypes().values()){
            classificationTextArea.appendText(Arrays.toString(type) + "\t");
        }
        infoSection.setExpandedPane(infoSection.getPanes().get(3));
        for(Map.Entry<String,ArrayList<Double[]>> obj : patterns.getTestingPatterns().entrySet()) {
            HashMap<String, Integer> classificationResult = patterns.createClassificationMap();
            double[] output = ArrayUtils.toPrimitive(patterns.getClassTypes().get(obj.getKey()));
            for(Double[] input: obj.getValue()){
                int[] classifiedClass = new int[output.length];
                List<Double> outputToClassify = Arrays.asList(ArrayUtils.toObject(network.testingNetwork(ArrayUtils.toPrimitive(input), output)));
                classifiedClass[outputToClassify.indexOf(Collections.max(outputToClassify))] = 1;
                String txtclassifiedClass = Arrays.toString(classifiedClass);
                classificationResult.merge(txtclassifiedClass.substring(1, txtclassifiedClass.length() - 1).replaceAll("\\s", ""), 1, Integer::sum);
            }
            classificationTextArea.appendText("\n" + Arrays.toString(output) + "\t\t ");
            for(Integer num: classificationResult.values()){
                classificationTextArea.appendText(num + "\t\t\t ");
            }
        }
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
