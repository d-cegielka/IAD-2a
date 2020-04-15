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


    public Neuron getNeuronFromLayer(int neuronIndex){
        return layerNeurons.get(neuronIndex);
    }

    @Override
    public String toString() {
        return "Layer{" +
                "layerNeurons=" + layerNeurons +
                '}';
    }

    public double[] getNeuronsOutputs(){
        double[] outputs = new double[getNumOfLayerNeurons()];
        for (int i = 0; i < getNumOfLayerNeurons(); i++) {
            outputs[i] = layerNeurons.get(i).getOutputValue();
        }
        return outputs;
    }
}
