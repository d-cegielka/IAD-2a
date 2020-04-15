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
     * @param inputs wejście przekazywane warstwie sieci
     */
    public void calcWeightedSum(final double[] inputs/*, boolean isInputLayer*/) {
        double sum = 0.0;
        for (int i = 0; i < getNumOfWeight(); i++) {
            sum += inputs[i] * getWeight(i);
        }
        setWeightedSum(sum);
    }

    /**
     * funkcja aktywacji ustawia wartość na wyjściu każdego neuronu z podanej warstwy,
     * funkcja sigmoidalna wyrażona jest wzorem (1 / 1 + e ^ -x)
     */
    public void activationFunction() {
        //funkcja sigmoidalna unipolarna [0;1]
        //outputValue = 1.0 / (1.0 + Math.exp(-weightedSum));
        //funkcja sigmoidalna bipolarna [-1;1]
        outputValue = Math.tanh(weightedSum);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("Suma ważona wejść: ").append(weightedSum).append("\n\t\t");
        sb.append("Wyjście neuronu: ").append(outputValue).append("\n\t\t");
        sb.append("Błąd neuronu: ").append(error).append("\n\t");
        return sb.toString();
    }
}
