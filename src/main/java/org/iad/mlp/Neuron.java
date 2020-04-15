package org.iad.mlp;

public class Neuron extends Weights {
    private double error;
    private double weightedSum;
    private double outputValue;

    public double getOutputValue() {
        return outputValue;
    }

    public void setOutputValue(double outputValue) {
        this.outputValue = outputValue;
    }

    public Neuron(int numOfWeight, boolean isBias/*, boolean isInputLayer*/) {
        super(numOfWeight, isBias/*, isInputLayer*/);
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
     * @param input wejście przekazywane warstwie sieci
     */
    public void calcWeightedSum(final double[] input/*, boolean isInputLayer*/) {
        double sum = 0.0;
        for (int i = 0; i < getNumOfWeight(); i++) {
            sum += input[i] * getWeight(i);
        }
        setWeightedSum(sum);
    }

    /**
     * funkcja aktywacji ustawia wartość na wyjściu każdego neuronu z podanej warstwy,
     * funkcja sigmoidalna wyrażona jest wzorem (1 / 1 + e ^ -x)
     */
    public void activationFunction() {
        outputValue = Math.tanh(weightedSum);
        //outputValue = 1.0 / (1.0 + Math.exp(-weightedSum));
    }

    @Override
    public String toString() {
        return "Neuron{" +
                "error=" + error +
                ", weightedSum=" + weightedSum +
                ", outputValue=" + outputValue +
                '}';
    }
}
