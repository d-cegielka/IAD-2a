package org.iad.mlp;

import java.util.ArrayList;
import java.util.List;

public class Network {
    private List<Layer> networkLayers;
    private double networkError;

    public Network(List<Integer> numOfNeuronsInLayers, boolean isBias) {
        networkLayers = new ArrayList<>();
        int numOfWeights = 1;
        for (int numOfNeurons : numOfNeuronsInLayers) {
            networkLayers.add(new Layer(numOfNeurons, numOfWeights, isBias));
            numOfWeights = numOfNeurons;
        }
    }

    /**
     * Dla warstw nieliniowych suma ważona neuronu ustalana jest na podstawie sumy iloczynów wartości wejściowych i wag neuronu.
     * Jeśli neuron ma BIAS jego wartość dodawana jest do sumy ważonej.
     *
     * @param input dane wejściowe dla warstwy sieci
     * @param layerIndex indeks warstwy sieci
     */
    private void sum(double[] input, int layerIndex) {
        if(layerIndex == 0) {
            throw new ArrayIndexOutOfBoundsException("Index musi być większy od 0!");
        }
        for (int i = 0; i < networkLayers.get(layerIndex).getNumOfLayerNeurons(); i++) {
            networkLayers.get(layerIndex).getNeuronFromLayer(i).calcWeightedSum(input);
        }
    }

    /**
     * Dla warstwy wejściowej(nieprzetwarzającej) poszczególnym neuronom warstwy jako wartości sum ważonych przypisywane są wartości wzorca.
     * Jeśli neuron ma BIAS jego wartość dodawana jest do sumy ważonej.
     * Jako wartość wyjściowa przypisywana jest wartość sumy ważonej.
     *
     * @param input wzorzec dla warstwy wejsciowej (nieprzetwarzajacej)
     */
    private void forwardPropagation(double[] input){
        for (int i = 0; i < getNumOfNetworkLayers(); i++) {
            if (i == 0) {
                for (int j = 0; j < networkLayers.get(i).getNumOfLayerNeurons(); j++) {
                    networkLayers.get(i).getNeuronFromLayer(j).setWeightedSum(input[j]);
                    networkLayers.get(i).getNeuronFromLayer(j).setOutputValue(networkLayers.get(i).getNeuronFromLayer(j).getWeightedSum());
                }
            } else {
                sum(networkLayers.get(i - 1).getNeuronsOutputs(), i);
                setOutputValues(i);
            }
        }
    }

    /**
     * Dla warstw nieliniowych wartość wyjścia neuronu ustalana jest przez funkcję aktywacji.
     * @param layerIndex indeks warstwy sieci
     */
    private void setOutputValues(int layerIndex){
        if(layerIndex == 0) {
            throw new ArrayIndexOutOfBoundsException("Index musi być większy od 0!");
        }
        for (int i = 0; i < networkLayers.get(layerIndex).getNumOfLayerNeurons(); i++) {
            networkLayers.get(layerIndex).getNeuronFromLayer(i).activationFunction();
        }
    }

    public int getNumOfNetworkLayers() {
        return networkLayers.size();
    }
}
