package org.iad.mlp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NetworkTest {

    private Network network;
    @BeforeEach
    void setUp() {
        List<Integer> neurons = new ArrayList<>(List.of(4, 2, 4));
        network = new Network(neurons,true);
    }

    @Test
    void learningNetwork() {
    }

    @Test
    void testingNetwork() {
    }

    @Test
    void getNumOfNetworkLayers() {
    }

    @Test
    void getNetworkError() {
    }

    @Test
    void testToString() {
        assertEquals(" ",network.toString());
    }
}