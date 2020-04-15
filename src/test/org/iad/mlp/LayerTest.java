package org.iad.mlp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LayerTest {

    Layer layer1, layer2;
    @BeforeEach
    void setUp() {
        layer1 = new Layer(6,4,true);
        layer2 = new Layer(3,5,false);
    }

    @Test
    void getNumOfLayerNeurons() {
        assertEquals(6,layer1.getNumOfLayerNeurons());
        assertEquals(3,layer2.getNumOfLayerNeurons());
    }

}