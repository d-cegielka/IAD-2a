package org.iad.mlp;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;
import java.util.Spliterator;

public class Weights implements Serializable {
    private boolean isBias;
    private double[] weights;
    private double[] prevWeights;
    private double[] prevPrevWeights;

    /**
     * Konstruktor parametrowy
     *
     * @param numOfWeight ilość wag neuronu
     * @param isBias czy neuron jest ma BIAS
     */
    public Weights(final int numOfWeight, boolean isBias /*, final boolean isInputLayer*/) {
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
     * Losowanie wag neuronu z wartościami z zakresu -1:1
     */
    private void RandomWeights() {
        Random random = new Random();
        weights = random.doubles(weights.length,-1.0,1.0).toArray();
        /*if(isInputLayer) {
            Arrays.fill(weights, 1.0);
        } else {
            weights = random.doubles(weights.length,-1.0,1.0).toArray();
        }*/
    }

    public void setWeight(final double weight, final int weightIndex) {
        prevPrevWeights[weightIndex] = prevWeights[weightIndex];
        prevWeights[weightIndex] = weights[weightIndex];
        weights[weightIndex] = weight;
    }

    public double getWeight(final int weightIndex) {
        return weights[weightIndex];
    }

    public double getPrevWeight(final int weightIndex) {
        return prevWeights[weightIndex];
    }

    public double getPrevPrevWeight(final int weightIndex) {
        return prevPrevWeights[weightIndex];
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
        return sb.toString();
    }
}
