package org.iad.mlp;

import java.util.ArrayList;
import java.util.List;

public class Layer {
    private List<Neuron> layerNeurons;

    /**
     * Konstruktor tworzy listę neuronów warstwy a następnie dodaje do niej określoną liczbę neuronów.
     *
     * @param numOfNeurons ilość neuronów w warstwie
     * @param numOfWeight ilość wag każdego z neuronów
     * @param isBias czy neuron jest ma BIAS
     */
    public Layer(int numOfNeurons, int numOfWeight, boolean isBias ) {
        layerNeurons = new ArrayList<>();
        for (int i = 0; i < numOfNeurons; i++) {
            layerNeurons.add(new Neuron(numOfWeight,isBias));
        }
    }

    /**
     * Ilość neuronów w warstwie.
     * @return zwracana jest liczba neuronów w warstwie
     */
    public int getNumOfLayerNeurons() {
        return layerNeurons.size();
    }

    /**
     * Ilość wag neuronu w warstwie
     * Każdy neuron w warstwie ma taką samą ilość wag
     * @return zwracana jest liczba wag pierwszego neuronu w warstwie
     */
    public int getNumOfNeuronWeights() {
        return layerNeurons.get(0).getNumOfWeight();
    };


    public Neuron getNeuron(int neuronIndex){
        return layerNeurons.get(neuronIndex);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (int n = 0; n < getNumOfLayerNeurons(); n++) {
            sb.append("\tNeuron[").append(n+1).append("] {\n\t\t");
            sb.append(layerNeurons.get(n).toString()).append("}\n\n");
        }
        return sb.toString();
    }

    /**
     *  Metoda tworzy tablicę z wartości wyjść każdego neuronu w warstwie
     * @return tablica wartości wyjściowych neuronów (licząc od zera do ostatniego neuronu w warstwie)
     */
    public double[] getNeuronsOutputs(){
        double[] outputs = new double[getNumOfLayerNeurons()];
        for (int i = 0; i < getNumOfLayerNeurons(); i++) {
            outputs[i] = layerNeurons.get(i).getOutputValue();
        }
        return outputs;
    }
}
