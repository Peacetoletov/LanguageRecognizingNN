package me.peacetoletov.languageRecognizingNN.gui;

import me.peacetoletov.languageRecognizingNN.neuralNetwork.Main;
import me.peacetoletov.languageRecognizingNN.neuralNetwork.Neuron;
import me.peacetoletov.languageRecognizingNN.neuralNetwork.Synapse;

import java.awt.*;

/**
 * Created by lukas on 24.9.2017.
 */

public class Diagram {
    private int layersAmount;
    private int maxWordLength;
    private int hiddenLayerSize;
    private int outputLayerSize;
    private String allowedChars;

    private final int startX = 600;
    private final int neuronSize = 35;
    private final int heightBetweenNeurons = 8;
    private final int widthBetweenNeurons = 250;
    private final int realWindowHeight = 690;

    public Diagram(int layersAmount, int maxWordLength, int hiddenLayerSize, int outputLayerSize, String allowedChars) {
        this.layersAmount = layersAmount;
        this.maxWordLength = maxWordLength;
        this.hiddenLayerSize = hiddenLayerSize;
        this.outputLayerSize = outputLayerSize;
        this.allowedChars = allowedChars;
    }

    public void draw(Graphics g) {
        int[] neuronsInLayer = new int[3];
        Neuron[][] neuronArray = Main.getBody().getNeuronArray();
        Synapse[][][] synapseArray = Main.getBody().getSynapseArray();
        neuronsInLayer[0] = maxWordLength;
        neuronsInLayer[1] = hiddenLayerSize;
        neuronsInLayer[2] = outputLayerSize;

        //Draw background (?)
        g.setColor(new Color(0, 160, 255));
        g.fillRect(500, 0, 780, 720);
        g.setColor(Color.black);
        g.fillRect(497, 0, 6, 720);
        //g.fillRect(0, 0, 1280, 720);

        //Draw synapses
        double[][] avgWeights = getFirstLayerAverageWeights(neuronsInLayer, synapseArray);
        for (int layer = 0; layer < layersAmount-1; layer++) {
            double edgeWeightValues[][] = new double[layersAmount-1][2];            //edgeWeightValues[layer][lowest value, highest value]
            getEdgeWeightValues(edgeWeightValues, layer, synapseArray, neuronsInLayer, avgWeights);
            for (int firstNeuronPos = 0; firstNeuronPos < neuronsInLayer[layer]; firstNeuronPos++) {
                for (int secondNeuronPos = 0; secondNeuronPos < neuronsInLayer[layer + 1]; secondNeuronPos++) {
                    drawSynapse(g, layer, firstNeuronPos, secondNeuronPos, neuronsInLayer, synapseArray, edgeWeightValues, avgWeights);
                }
            }
        }

        //Draw neurons
        for (int layer = 0; layer < layersAmount; layer++) {
            for (int firstNeuronPos = 0; firstNeuronPos < neuronsInLayer[layer]; firstNeuronPos++) {
                drawNeuron(g, layer, firstNeuronPos, neuronsInLayer, neuronArray);
            }
        }
    }

    private void getEdgeWeightValues(double[][] edgeWeightValues, int layer, Synapse[][][] synapseArray, int[] neuronsInLayer, double[][] avgWeights) {
        double minValue = synapseArray[layer][0][0].getWeight();
        double maxValue = synapseArray[layer][0][0].getWeight();

        if (layer == 0) {
            for (int bigInputNeuron = 0; bigInputNeuron < maxWordLength; bigInputNeuron++) {
                for (int secondNeuronPos = 0; secondNeuronPos < neuronsInLayer[layer+1]; secondNeuronPos++) {
                    double value = getFirstLayerWeight(bigInputNeuron, secondNeuronPos, synapseArray, avgWeights);
                    if (value < minValue) {
                        minValue = value;
                    }
                    if (value > maxValue) {
                        maxValue = value;
                    }
                }
            }

        } else {
            for (int firstNeuronPos = 0; firstNeuronPos < neuronsInLayer[layer]; firstNeuronPos++) {
                for (int secondNeuronPos = 0; secondNeuronPos < neuronsInLayer[layer+1]; secondNeuronPos++) {
                    double value = synapseArray[layer][firstNeuronPos][secondNeuronPos].getWeight();
                    if (value < minValue) {
                        minValue = value;
                    }
                    if (value > maxValue) {
                        maxValue = value;
                    }
                }
            }
        }

        edgeWeightValues[layer][0] = minValue;
        edgeWeightValues[layer][1] = maxValue;
    }

    private double getFirstLayerWeight(int firstNeuronPos, int secondNeuronPos, Synapse[][][] synapseArray, double[][] avgWeights) {
        double value;
        //If the big input neuron contains a letter, it will show color of all synapses from that small input neuron instead of the average of all small neurons
        String inputWord = Main.getBody().getInputWord();
        Neuron[][] neuronArray = Main.getBody().getNeuronArray();
        if (firstNeuronPos < inputWord.length()) {
            //Assign the correct value
            value = 0;
            for (int smallInputNeuron = 0; smallInputNeuron < allowedChars.length(); smallInputNeuron++) {
                int smallNeuronPos = firstNeuronPos * allowedChars.length() + smallInputNeuron;
                int input = (int) neuronArray[0][smallNeuronPos].getValue();
                if (input == 1) {
                    value = synapseArray[0][smallNeuronPos][secondNeuronPos].getWeight();
                }
            }

        } else {
            value = avgWeights[firstNeuronPos][secondNeuronPos];
        }

        return value;
    }

    private double[][] getFirstLayerAverageWeights(int[] neuronsInLayer, Synapse[][][] synapseArray) {
        //Create an array of the average of all weights to a neuron in the hidden layer
        double[][] avgWeights = new double[maxWordLength][neuronsInLayer[1]];
        for (int bigInputNeuron = 0; bigInputNeuron < maxWordLength; bigInputNeuron++) {
            for (int secondNeuronPos = 0; secondNeuronPos < neuronsInLayer[1]; secondNeuronPos++) {
                double totalSynapseWeight = 0;
                for (int smallInputNeuron = 0; smallInputNeuron < allowedChars.length(); smallInputNeuron++) {      //loop through all small input neurons which make up a big input neuron
                    int firstNeuronPos = bigInputNeuron * allowedChars.length() + smallInputNeuron;
                    double value = synapseArray[0][firstNeuronPos][secondNeuronPos].getWeight();
                    totalSynapseWeight += value;
                }
                double avgSynapseWeight = totalSynapseWeight / allowedChars.length();
                avgWeights[bigInputNeuron][secondNeuronPos] = avgSynapseWeight;
            }
        }

        return avgWeights;
    }

    private void drawSynapse(Graphics g, int layer, int firstNeuronPos, int secondNeuronPos, int[] neuronsInLayer, Synapse[][][] synapseArray, double[][] edgeWeightValues, double[][] avgWeights) {
        int color = (int) Math.floor(Math.random() * 255);
        //g.setColor(new Color(color, color, color));         //set the color
        g.setColor(chooseSynapseColor(layer, firstNeuronPos, secondNeuronPos, edgeWeightValues, synapseArray, avgWeights));         //set the color
        int x1 = getNeuronCentralizedX(layer);
        int y1 = getNeuronCentralizedY(neuronsInLayer[layer], firstNeuronPos);
        int x2 = getNeuronCentralizedX(layer + 1);
        int y2 = getNeuronCentralizedY(neuronsInLayer[layer + 1], secondNeuronPos);
        g.drawLine(x1, y1, x2, y2);
    }

    private void drawNeuron(Graphics g, int layer, int position, int[] neuronsInLayer, Neuron[][] neuronArray) {
        int x = getNeuronX(layer);
        int y = getNeuronY(neuronsInLayer[layer], position);
        double value = Math.round(neuronArray[layer][position].getValue() * 100);
        value /= 100;
        String valueString = String.valueOf(value);

        //Draw neuron
        Color[] colors = chooseNeuronColors(layer, value);
        g.setColor(colors[0]);
        g.fillOval(x, y, neuronSize, neuronSize);
        g.setColor(colors[1]);
        g.drawOval(x, y, neuronSize, neuronSize);

        //Draw text
        Font font;
        font = new Font("Arial", Font.PLAIN, 12);
        if (layer == 0) {
            font = new Font("Arial", Font.PLAIN, 16);
            String inputWord = Main.getBody().getInputWord();
            if (position < inputWord.length()){
                valueString = Character.toString(inputWord.charAt(position)).toUpperCase();
            } else {
                valueString = "";
            }
        }
        drawNeuronValue(g, colors[2], valueString, x, y, font);
    }

    private int getNeuronX(int layer) {
        int x = widthBetweenNeurons * layer + startX;
        return x;
    }

    private int getNeuronY(int neuronsInLayer, int position) {
        int spaceFromEdge = (realWindowHeight - (neuronsInLayer * neuronSize + (neuronsInLayer - 1) * heightBetweenNeurons)) / 2;
        int y = (neuronSize + heightBetweenNeurons) * position + spaceFromEdge;
        return y;
    }

    private int getNeuronCentralizedX(int layer) {
        int centralizedX = getNeuronX(layer) + neuronSize / 2;
        return centralizedX;
    }

    private int getNeuronCentralizedY(int neuronsInLayer, int position) {
        int centralizedY = getNeuronY(neuronsInLayer, position) + neuronSize / 2;
        return centralizedY;
    }

    private Color chooseSynapseColor(int layer, int firstNeuronPos, int secondNeuronPos, double[][] edgeWeightValues, Synapse[][][] synapseArray, double[][] avgWeights) {
        double value;
        if (layer == 0) {
            value = getFirstLayerWeight(firstNeuronPos, secondNeuronPos, synapseArray, avgWeights);
        } else {
            value = synapseArray[layer][firstNeuronPos][secondNeuronPos].getWeight();
        }

        double valueShifted = value - edgeWeightValues[layer][0];
        double maxWeightShifted = edgeWeightValues[layer][1] - edgeWeightValues[layer][0];
        double proportion = valueShifted / maxWeightShifted;
        int shadeReversed = (int) Math.round(proportion * 255);
        int shade = 255 - shadeReversed;

        Color color = new Color(shade, shade, shade);

        return color;
    }

    private Color[] chooseNeuronColors(int layer, double value) {
        Color colors[] = new Color[3];
        Color backgroundColor;
        Color edgeColor;
        Color textColor;

        if (layer == 0) {
            backgroundColor = new Color(255, 200, 0);
            textColor = new Color(0, 0, 0);
        } else {
            //bude se měnit dynamicky podle hodnoty neuronu - nízká hodnota světlá, vysoká hodnota tmavá; barva textu - u spíše tmavých bílá, u spíše světlých černá
            int shade = (int) Math.floor(255 - value * 255);
            backgroundColor = new Color(shade, shade, shade);

            if (value >= 0.5) {
                textColor = new Color(255, 255, 255);
            } else {
                textColor = new Color(0, 0, 0);
            }
        }
        edgeColor = new Color(0, 0, 0);

        colors[0] = backgroundColor;
        colors[1] = edgeColor;
        colors[2] = textColor;
        return colors;
    }

    private void drawNeuronValue(Graphics g, Color color, String value, int x, int y, Font font) {
        g.setColor(color);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics(g.getFont());
        int width = fm.stringWidth(value);
        int height = fm.getHeight();
        int textX = x + Math.round((neuronSize - width) / 2) + 1;       // + 1 = centering
        int textY = y + Math.round((neuronSize + height) / 2) - 2;      // - 2 = centering
        g.drawString(value, textX, textY);
    }

}
