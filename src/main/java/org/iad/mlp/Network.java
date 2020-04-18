package org.iad.mlp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Network implements Serializable {
    private List<Layer> networkLayers;
    private double networkError;

    /**
     * Konstruktor parametrowy tworzący sieć składająca się z warstw, które posiadają poszczególne ilość neuronów.
     * W warstwie wejściowej(nieprzetwarzającej) neuron posiada jedną wagę, w kolejnych warstwach ilością wag jest liczba neuronów w poprzedniej warstwie.
     * @param numOfNeuronsInLayers ilość neuronów w poszczególnych warstwach
     * @param isBias czy sieć ma BIAS
     */
    public Network(List<Integer> numOfNeuronsInLayers, boolean isBias) {
        networkLayers = new ArrayList<>();
        int numOfWeights = 1;
        for (int numOfNeurons : numOfNeuronsInLayers) {
            networkLayers.add(new Layer(numOfNeurons, numOfWeights, isBias));
            numOfWeights = numOfNeurons;
        }
    }

    /**
     * Wzorzec treningowy podawany jest na wejścia sieci, po czym odbywa się jego propagacja wprzód.
     * Następnie na podstawie wartości odpowiedzi wygenerowanej przez sieć oraz wartości pożądanego wzorca odpowiedzi
     * następuje wyznaczenie błędów, po czym propagowane są one wstecz.
     * Na koniec zaś ma miejsce wprowadzenie poprawek na wagi.
     * @param inputs wzorzec wejściowy
     * @param outputs wzorzec wyjściowy
     * @param learningRate współczynnik nauki
     * @param momentum współczynnik momentum
     */
    public void learningNetwork(double[] inputs, double[] outputs, double learningRate, double momentum) {
        forwardPropagation(inputs);
        calcError(outputs);
        updateWeights(learningRate, momentum);
    }

    /**
     * W trybie testowania wyznaczamy odpowiedzi sieci dla poszczególnych wzorców.
     * Na wejście sieci podajemy wzorzec treningowy, nastepnie wykonywana jest propagacja w przód,
     * a na koniec na podstawie odpowiedzi wygenerowanej przez sieć oraz pożądanego wzorca odpowiedzi
     * następuje wyznaczanie błędów.
     * @param inputs wzorzec treningowy
     * @return tablica z wartościami wyjść neuronów warstwy wyjściowej (licząc od zera do ostatniego neuronu w warstwie)
     */
    public double[] testingNetwork(double[] inputs){
        forwardPropagation(inputs);
        calcError(inputs);
        return networkLayers.get(getNumOfNetworkLayers() - 1).getNeuronsOutputs();
    }

    /**
     * Dla warstw nieliniowych suma ważona neuronu ustalana jest na podstawie sumy iloczynów wartości wejściowych i wag neuronu.
     * Jeśli neuron ma BIAS jego wartość dodawana jest do sumy ważonej.
     *
     * @param inputs dane wejściowe dla warstwy sieci
     * @param layerIndex indeks warstwy sieci
     */
    private void weightedSum(double[] inputs, int layerIndex) {
        if (layerIndex == 0) {
            throw new ArrayIndexOutOfBoundsException("Index musi być większy od 0!");
        }
        for (int i = 0; i < networkLayers.get(layerIndex).getNumOfLayerNeurons(); i++) {
            networkLayers.get(layerIndex).getNeuron(i).calcWeightedSum(inputs);
        }
    }

    /**
     * Dla warstw nieliniowych wartość wyjścia neuronu ustalana jest przez funkcję aktywacji.
     *
     * @param layerIndex indeks warstwy sieci
     */
    private void setOutputValues(int layerIndex) {
        if (layerIndex == 0) {
            throw new ArrayIndexOutOfBoundsException("Index musi być większy od 0!");
        }
        for (int i = 0; i < networkLayers.get(layerIndex).getNumOfLayerNeurons(); i++) {
            networkLayers.get(layerIndex).getNeuron(i).activationFunction();
        }
    }

    /**
     * Dla warstwy wejściowej(nieprzetwarzającej) poszczególnym neuronom warstwy jako wartości sum ważonych przypisywane są wartości wzorca.
     * Jeśli neuron ma BIAS jego wartość dodawana jest do sumy ważonej.
     * Jako wartość wyjściowa przypisywana jest wartość sumy ważonej.
     *
     * @param inputs wzorzec dla warstwy wejsciowej (nieprzetwarzajacej)
     */
    private void forwardPropagation(double[] inputs){
        for (int i = 0; i < getNumOfNetworkLayers(); i++) {
            if (i == 0) {
                for (int j = 0; j < networkLayers.get(i).getNumOfLayerNeurons(); j++) {
                    networkLayers.get(i).getNeuron(j).setWeightedSum(inputs[j]);
                    networkLayers.get(i).getNeuron(j).setOutputValue(networkLayers.get(i).getNeuron(j).getWeightedSum());
                }
            } else {
                weightedSum(networkLayers.get(i - 1).getNeuronsOutputs(), i);
                setOutputValues(i);
            }
        }
    }

    /**
     * Metoda oblicza błąd dla każdej warstwy sieci, z pominięciem warstwy wejściowej.
     * Błąd dla warstw ukrytych oblicza się za pomocą wzoru f'(x) * suma(waga j-tego neuronu * błąd j-tego neuronu),
     * warstwy która wystepuję po aktualnej warstwie.
     * Błąd warstwy wyjściowej obliczany jest jako różnica pomiedzy wyjściem otrzymanym, a wyjściem oczekiwanym,
     * pomnożonym przez wartość pochodnej funkcji aktywacji.
     * @param outputs wyjście wzorcowe
     */
    private void calcError(double[] outputs){
        networkError = 0d;
        double diff;
        int indexLastLayer = getNumOfNetworkLayers() - 1;

        //Obliczenie błędu warstwy wyjściowej
        for (int i = 0; i < networkLayers.get(indexLastLayer).getNumOfLayerNeurons(); i++) {
            diff = outputs[i] - networkLayers.get(indexLastLayer).getNeuron(i).getOutputValue();
            networkLayers.get(indexLastLayer).getNeuron(i).setError(derivative(networkLayers.get(indexLastLayer).getNeuron(i).getOutputValue()) * diff);
            networkError += diff * diff;
        }
        networkError /=2;

        //Obliczenie błedów warstw ukrytych
        for (int l = indexLastLayer; l > 0; l--) {
            for (int w = 0; w < networkLayers.get(l).getNumOfNeuronWeights(); w++) {
                double error = 0d;
                for (int n = 0; n < networkLayers.get(l).getNumOfLayerNeurons(); n++) {
                    error += networkLayers.get(l).getNeuron(n).getWeight(w) * networkLayers.get(l).getNeuron(n).getError();
                }
                error *= derivative(networkLayers.get(l-1).getNeuron(w).getOutputValue());
                networkLayers.get(l-1).getNeuron(w).setError(error);
            }
        }
    }

    /**
     * metoda ta aktualizuje wagi dla neuronów wszystkich warstw, z wyjątkiem warstwy wejściowej
     * @param learningRate współczynnik nauki
     * @param momentum współczynnik momentu
     */
    private void updateWeights(double learningRate, double momentum) {
        double delta, deltaWeights;
        boolean isBias = networkLayers.get(0).getNeuron(0).isBias();
        for (int l = 1; l < getNumOfNetworkLayers(); l++) {
            for (int n = 0; n < networkLayers.get(l).getNumOfLayerNeurons(); n++) {
                for (int w = 0; w < networkLayers.get(l).getNumOfNeuronWeights(); w++) {
                    delta = learningRate * networkLayers.get(l).getNeuron(n).getError() *
                            networkLayers.get(l - 1).getNeuron(w).getOutputValue();
                    deltaWeights = networkLayers.get(l).getNeuron(n).getPrevWeight(w) -
                            networkLayers.get(l).getNeuron(n).getPrevPrevWeight(w);
                    networkLayers.get(l).getNeuron(n).setWeight(networkLayers.get(l).getNeuron(n).getWeight(w)
                            + delta + momentum * deltaWeights, w);

                    if (w == (networkLayers.get(l).getNumOfNeuronWeights() - 1) && isBias) {
                        delta = learningRate * networkLayers.get(l).getNeuron(n).getError();
                        networkLayers.get(l).getNeuron(n).setWeight(networkLayers.get(l).getNeuron(n).getWeight(w + 1)
                                + delta + momentum * delta, w + 1);
                    }
                }
            }
        }

    }

    /**
     * Pochodna funkcji sigmoidalnej unipolarnej wyraża się wzorem f(x) * (1 - f(x))
     * Pochodna funkcji sigmoidalnej bipolarnej wyraża się wzorem 1 - f^2(x)
     * @param input wartość funkcji sigmoidalnej f(x)
     * @return pochodna funkcji sigmoidalnej f'(x)
     */
    private double derivative(double input){
        return input * (1 - input);
        //return 1 - (input * input);
    }

    /**
     * Ilość warstw w sieci
     * @return zwracana jest ilość warstw w sieci
     */
    public int getNumOfNetworkLayers() {
        return networkLayers.size();
    }

    /**
     * Ilość neuronów na warstwie w sieci
     * @return zwracana jest ilość neuoronów na warstwie sieci
     */
    public int getNumOfNerounsInLayer(int indexLayer) {
        return networkLayers.get(indexLayer).getNumOfLayerNeurons();
    }

    /**
     * Błąd globalny sieci
     * @return zwraca błąd globalny sieci
     */
    public double getNetworkError() {
        return networkError;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (int l = 0; l < getNumOfNetworkLayers(); l++) {
            sb.append("Warstwa[").append(l+1).append("] {\n");
            sb.append(networkLayers.get(l).toString()).append("}\n\n");
        }
        return sb.toString();
    }
}
