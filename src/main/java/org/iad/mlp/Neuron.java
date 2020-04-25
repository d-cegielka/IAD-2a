package org.iad.mlp;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;

public class Neuron implements Serializable {
    private double error;
    private double weightedSum;
    private double outputValue;
    private final boolean isBias;
    private double[] weights;
    private final double[] prevWeights;
    private final double[] prevPrevWeights;

    /**
     * Konstruktor parametrowy
     *
     * @param numOfWeight ilość wag neuronu
     * @param isBias czy neuron jest ma BIAS
     */
    public Neuron(final int numOfWeight, boolean isBias) {
        this.isBias = isBias;
        if (isBias) {
            weights = new double[numOfWeight + 1];
            prevWeights = new double[numOfWeight + 1];
            prevPrevWeights = new double[numOfWeight + 1];
        } else {
            weights = new double[numOfWeight];
            prevWeights = new double[numOfWeight];
            prevPrevWeights = new double[numOfWeight];
        }
        RandomWeights();
    }

    /**
     * Jeśli neuron ma BIAS do sumy ważonej dodawana jest jego wartość.
     *
     * @param weightedSum suma ważona
     */
    public void setWeightedSum(double weightedSum) {
        if(isBias()){
            this.weightedSum = weightedSum + getWeight(getNumOfWeight());
        } else{
            this.weightedSum = weightedSum;
        }
    }

    /**
     * Sumą ważona neuronu są wartości wejścia pomnożone przez wagi neuronu.
     *
     * @param inputs wejście przekazywane warstwie sieci
     */
    public void calcWeightedSum(final double[] inputs) {
        double sum = 0.0;
        for (int i = 0; i < getNumOfWeight(); i++) {
            sum += inputs[i] * getWeight(i);
        }
        setWeightedSum(sum);
    }

    /**
     * funkcja aktywacji ustawia wartość na wyjściu każdego neuronu z podanej warstwy,
     * funkcja sigmoidalna unipolarna wyrażona jest wzorem (1 / 1 + e ^ -x)
     */
    public void activationFunction() {
        //funkcja sigmoidalna unipolarna [0;1]
        outputValue = 1.0 / (1.0 + Math.exp(-weightedSum));
    }

    /**
     * Losowanie wag neuronu z wartościami z zakresu -0.5:0.5
     */
    private void RandomWeights() {
        Random random = new Random();
        weights = random.doubles(weights.length,-0.5,0.5).toArray();
    }

    public void setWeight(final double weight, final int weightIndex) {
        prevPrevWeights[weightIndex] = prevWeights[weightIndex];
        prevWeights[weightIndex] = weights[weightIndex];
        weights[weightIndex] = weight;
    }

    public double getOutputValue() {
        return outputValue;
    }

    public void setOutputValue(double outputValue) {
        this.outputValue = outputValue;
    }

    public double getWeightedSum() {
        return weightedSum;
    }

    public void setError(final double error) {
        this.error = error;
    }

    public double getError() {
        return error;
    }

    public double getWeight(final int weightIndex) {
        return weights[weightIndex];
    }

    public double getDeltaWeights(final int weightIndex) {
        return prevWeights[weightIndex]-prevPrevWeights[weightIndex];
    }

    public int getNumOfWeight() {
        if(isBias)
            return weights.length - 1;
        else
            return weights.length;
    }

    public boolean isBias() {
        return isBias;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Wagi: ").append(Arrays.toString(Arrays.copyOf(weights, getNumOfWeight()))).append("\n\t\t");
        if (isBias) {
            sb.append("Waga biasu: ").append(weights[weights.length - 1]).append("\n\t\t");
        }
        sb.append("Suma ważona wejść: ").append(weightedSum).append("\n\t\t");
        sb.append("Wyjście neuronu: ").append(outputValue).append("\n\t\t");
        sb.append("Błąd neuronu: ").append(error).append("\n\t");
        return sb.toString();
    }

}
