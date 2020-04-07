package org.iad.mlp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NeuronTest {

    Neuron neuron;
    double input[];
    
    @BeforeEach
    void setUp() {
        neuron = new Neuron(2,true);
        input = new double[]{1.0, 0.0};
        neuron.calcWeightedSum(input);
    }

    @Test
    void setWeight() {
        neuron.setWeight(0.55,0);
        assertEquals(0.55,neuron.getWeight(0));
    }

    @Test
    void getWeight() {
        assertTrue(neuron.getWeight(1) > -1 && neuron.getWeight(1) < 1);
    }
    @Test
    void getNumOfWeight() {
        assertEquals(2,neuron.getNumOfWeight());
    }

    @Test
    void setOutputValue() {
        neuron.setOutputValue(1.0);
        assertEquals(1.0, neuron.getOutputValue());
    }

    @Test
    void getOutputValue() {
        assertEquals(0.0,neuron.getOutputValue());
    }

    @Test
    void setWeightedSum(){
        neuron.setWeightedSum(5.0);
        assertEquals(5.0+neuron.getWeight(neuron.getNumOfWeight()),neuron.getWeightedSum());
    }


    @Test
    void calcWeightedSum() {
        assertEquals(neuron.getWeight(0) * input[0] + neuron.getWeight(1)*input[1]
                + neuron.getWeight(neuron.getNumOfWeight()), neuron.getWeightedSum());
    }

    @Test
    void activationFunction() {
        neuron.activationFunction();
        assertEquals(Math.tanh(neuron.getWeightedSum()),neuron.getOutputValue());
    }
}