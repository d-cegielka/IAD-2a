package org.iad.mlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.univocity.parsers.csv.Csv;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;


public class Controller {

    private File inputFile;

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
    private TextField numOfEpoch;

    @FXML
    private ToggleGroup treningType;

    @FXML
    private TextField errorRate;

    @FXML
    private TextField learningRate;

    @FXML
    private TextField momentumRate;

    @FXML
    private ChoiceBox<?> orderPattern;

    @FXML
    private ChoiceBox<?> pattern;

    ArrayList<Double[]> input;
    ArrayList<Double[]> output;

    @FXML
    public void openFile() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wczytaj sieć");
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Pliki tekstowe (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extensionFilter);
        inputFile = fileChooser.showOpenDialog(new Stage());
    }

    @FXML
    public void readPattern() throws IOException {
        parsePattern(textAreaPattern.getText());

    }

    public void parsePattern(String buffer) throws IOException {
        CsvParserSettings settings = new CsvParserSettings();
        settings.getFormat().setLineSeparator("\n");
        settings.getFormat().setDelimiter(';');
        CsvParser parser = new CsvParser(settings);
      //  parser.parse(new File(buffer));
       // CsvFormat separatorDetected = parser.getDetectedFormat();
        List<String[]> allRows = parser.parseAll(new File(buffer));
        /*String separator = "),(";
        String currentLine;
        while((currentLine = buffer.readLine()) != null) {
            String[] separatedDataLine = currentLine.split(separator);
                for (int i = 0; i < separatedDataLine.length-1; i++) {
                    Arr
                    columnsData.get(i).add(Double.parseDouble(separatedDataLine[i]));
                }
            }
        }*/

    }

    @FXML
    public void quitApp() {
        Platform.exit();
    }

}
