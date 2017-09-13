package me.peacetoletov.languageRecognizingNN.neuralNetwork;

/**
 * Created by lukas on 29.7.2017.
 */

import me.peacetoletov.languageRecognizingNN.filesManaging.FileManager;

import java.util.ArrayList;

public class Body {
    private Neuron[][] neuronArray;     //neuronArray[layer][position]
    private Synapse[][][] synapseArray;     //synapseArray[firstLayer][firstPosition][secondPosition]
    private int neuronsInLayer[];
    private ArrayList<String> trainingDataInput = new ArrayList<>();
    private ArrayList<Integer[]> trainingDataTarget = new ArrayList<>();
    private FileManager fm = new FileManager();
    private String weightsFile;
    private String allowedChars;
    private int maxWordLength;
    private ArrayList<Double[]> weightList;
    private boolean createRandomWeights;

    Body(int layersAmount, int biggestLayerNeuronsAmount, String allowedChars, int maxWordLength, boolean createRandomWeights, String weightsFile){
        neuronArray = new Neuron[layersAmount][biggestLayerNeuronsAmount];
        synapseArray = new Synapse[layersAmount-1][biggestLayerNeuronsAmount][biggestLayerNeuronsAmount];
        neuronsInLayer = new int[layersAmount];
        this.allowedChars = allowedChars;
        this.maxWordLength = maxWordLength;
        this.weightsFile = weightsFile;
        this.createRandomWeights = createRandomWeights;

        if (fm.checkIfFileExists(weightsFile) && !createRandomWeights){
            weightList = fm.readWeights(weightsFile);
        }
    }

    public void createNeurons(int layer, int neuronsAmount){
        for(int i = 0; i < neuronsAmount; i++){
            neuronArray[layer][i] = new Neuron();
        }
        neuronsInLayer[layer] = neuronsAmount;
    }

    public void createSynapses(){
        int layersAmount = neuronsInLayer.length;
        for (int layer = 0; layer < layersAmount-1; layer++) {   //loop through each neuron layer except the output layer
            for (int firstNeuronPos = 0; firstNeuronPos < neuronsInLayer[layer]; firstNeuronPos++) {      //loop through each neuron in the layer
                for (int secondNeuronPos = 0; secondNeuronPos < neuronsInLayer[layer+1]; secondNeuronPos++) {      //loop through each neuron in the next layer
                    double synapseWeight;
                    if (fm.checkIfFileExists(weightsFile) && !createRandomWeights){
                        synapseWeight = getSynapseWeight(weightList, layer, firstNeuronPos, secondNeuronPos);
                    } else {
                        synapseWeight = Math.random() - 0.5;        //assigns a number between -0.5 and 0.5
                    }

                    synapseArray[layer][firstNeuronPos][secondNeuronPos] = new Synapse(synapseWeight);
                    //System.out.println("Synapse [" + layer + "][" + firstNeuronPos + "][" + secondNeuronPos + "] has weight " + synapseWeight);
                }
            }
        }

    }

    public void initializeTrainingData(String data, Integer[] target){
        trainingDataInput.add(data);
        trainingDataTarget.add(target);
    }

    public void train(){
        double success = 0;
        /*
        for (int i = 0; i < trainingIterations; i++) {
            for (int example = 0; example < trainingDataInput.size(); example++) {
                Integer[] target = getTarget(example);
                String inputWord = trainingDataInput.get(example);
                setInput(inputWord);
                passForward();
                success += countSuccess(target);
                backpropagate(target);
            }
            if (i % 10 == 0)
                System.out.println("Iteration " + i + " completed.");
        }
        saveWeights();
        double successRate = success / (trainingDataInput.size() * i);
        System.out.println("Success rate = " + successRate);

        */
        System.out.println("Starting learning iterations. Traning data size = " + trainingDataInput.size());
        int i = 1;
        while (true) {
            for (int example = 0; example < trainingDataInput.size(); example++) {
                Integer[] target = getTarget(example);
                String inputWord = trainingDataInput.get(example);
                setInput(inputWord);
                passForward();
                success += countSuccess(target);

                if (i >= 3){
                    //testing
                    backpropagate(target);
                }

                System.out.println("example = " + example);
            }


            if (i % 1 == 0) {   //10 replaced by 1 temporarily
                double successRate = success / (trainingDataInput.size() * i);
                System.out.println("Iteration " + i + " completed. Success rate = " + successRate);
                saveWeights();
            }

            System.out.println(i);
            i++;
        }
    }

    public void saveWeights() {
        fm.saveWeights(weightsFile, synapseArray, neuronsInLayer);
    }

    public double[] guessLanguage(String word){
        setInput(word);
        passForward();
        return calculatePercentage(word);
    }

    private double getSynapseWeight(ArrayList<Double[]> weightList, int layer, int firstNeuronPos, int secondNeuronPos){
        double weight = 0;
        for (int i = 0; i < weightList.size(); i++){
            Double[] x = weightList.get(i);
            if (x[0] == layer && x[1] == firstNeuronPos && x[2] == secondNeuronPos){
                weight = x[3];
            }
        }
        return weight;
    }

    private Integer[] getTarget(int example){
        Integer[] target = trainingDataTarget.get(example);
        return target;
    }

    private void setInput(String inputWord){
        //String inputData = trainingDataInput.get(example);
        for (int i = 0; i < maxWordLength; i++){        //Loop through "big" neurons
            for (int j = 0; j < allowedChars.length(); j++){        //Loop through "small" neurons
                if (inputWord.length() > i && inputWord.charAt(i) == allowedChars.charAt(j)){
                    neuronArray[0][j+(i*allowedChars.length())].setValue(1);
                }
                else {
                    neuronArray[0][j+(i*allowedChars.length())].setValue(0);
                }
            }
        }
    }

    private void passForward(){
        int layersAmount = neuronsInLayer.length;
        for (int layer = 1; layer < layersAmount; layer++){     //loop through each layer except the input layer
            for (int neuronNode = 0; neuronNode < neuronsInLayer[layer]; neuronNode++){     //loop through each neuron
                double thisNeuronInput = 0;
                for (int synapse = 0; synapse < neuronsInLayer[layer-1]; synapse++){     //loop through each synapse
                    double previousNeuronValue = neuronArray[layer-1][synapse].getValue();
                    double synapseWeight = synapseArray[layer-1][synapse][neuronNode].getWeight();
                    thisNeuronInput += previousNeuronValue * synapseWeight;
                }
                neuronArray[layer][neuronNode].sigmoid(thisNeuronInput);
            }
        }
    }

    private void backpropagate(Integer[] target) {
        //double cost = calculateCost(target);        //might not be needed
        double stepSize = 0.1;     //0.001 or 0.0005      //changed to a lower value for faster testing


        //Define deltaK
        double[] deltaK = new double[neuronsInLayer[4]];
        for (int k = 0; k < deltaK.length; k++) {
            double outputK = neuronArray[4][k].getValue();      //makes the variable name shorter
            deltaK[k] = outputK * (1 - outputK) * (outputK - target[k]);
        }

        /*
        //Define deltaJ
        double[] deltaJ = new double[neuronsInLayer[1]];
        for (int j = 0; j < deltaJ.length; j++) {
            double influence = 0;
            for (int k = 0; k < deltaK.length; k++) {
                influence += deltaK[k] * synapseArray[1][j][k].getWeight();
            }
            double outputJ = neuronArray[1][j].getValue();
            deltaJ[j] = outputJ * (1 - outputJ) * influence;
        }
        */

        //Loop through each synapse to calculate the derivative

        int layersAmount = neuronsInLayer.length;
        for (int layer = 0; layer < layersAmount - 1; layer++) {   //loop through each neuron layer except the output layer
            for (int firstNeuronPos = 0; firstNeuronPos < neuronsInLayer[layer]; firstNeuronPos++) {      //loop through each neuron in the layer
                for (int secondNeuronPos = 0; secondNeuronPos < neuronsInLayer[layer + 1]; secondNeuronPos++) {      //loop through each neuron in the next layer

                    double derivative = 0;

                    /*
                    //Hidden layer
                    if (layer == 0) {
                        double outputI = neuronArray[layer][firstNeuronPos].getValue();
                        derivative = stepSize * deltaJ[secondNeuronPos] * outputI;
                    }

                    //Output layer
                    if (layer == 3) {
                        double outputJ = neuronArray[layer][firstNeuronPos].getValue();
                        derivative = stepSize * deltaK[secondNeuronPos] * outputJ;
                        //System.out.println("Correct derivative = " + derivative);
                        synapseArray[layer][firstNeuronPos][secondNeuronPos].setDerivative(derivative);
                        synapseArray[layer][firstNeuronPos][secondNeuronPos].updateWeight();
                    }
                    */


                    if (layer == 3) {
                        derivative = calculateDerivative(layer, firstNeuronPos, secondNeuronPos, target, stepSize);
                        synapseArray[layer][firstNeuronPos][secondNeuronPos].setDerivative(derivative);
                        synapseArray[layer][firstNeuronPos][secondNeuronPos].updateWeight();
                    }



                    if (layer == 2) {
                        derivative = calculateDerivative(layer, firstNeuronPos, secondNeuronPos, target, stepSize);
                        synapseArray[layer][firstNeuronPos][secondNeuronPos].setDerivative(derivative);
                        synapseArray[layer][firstNeuronPos][secondNeuronPos].updateWeight();
                    }


                    if (layer == 1) {
                        derivative = calculateDerivative(layer, firstNeuronPos, secondNeuronPos, target, stepSize);
                        synapseArray[layer][firstNeuronPos][secondNeuronPos].setDerivative(derivative);
                        synapseArray[layer][firstNeuronPos][secondNeuronPos].updateWeight();
                    }

                    if (layer == 0) {
                        derivative = calculateDerivative(layer, firstNeuronPos, secondNeuronPos, target, stepSize);
                        synapseArray[layer][firstNeuronPos][secondNeuronPos].setDerivative(derivative);
                        synapseArray[layer][firstNeuronPos][secondNeuronPos].updateWeight();
                    }
                    //System.out.println("Iteration " + layer + "." + firstNeuronPos + "." + secondNeuronPos + " completed.");
                }
            }
        }

        /*
        //Loop through each synapse to update weights
        for (int layer = 0; layer < layersAmount - 1; layer++) {   //loop through each neuron layer except the output layer
            for (int firstNeuronPos = 0; firstNeuronPos < neuronsInLayer[layer]; firstNeuronPos++) {      //loop through each neuron in the layer
                for (int secondNeuronPos = 0; secondNeuronPos < neuronsInLayer[layer + 1]; secondNeuronPos++) {      //loop through each neuron in the next layer
                    synapseArray[layer][firstNeuronPos][secondNeuronPos].updateWeight();
                }
            }
        }
        */
    }

    private double calculateDerivative(int layer, int firstNeuronPos, int secondNeuronPos, Integer[] target, double stepSize) {
        double derivative = 0;
        double outputD;
        double outputC;
        double outputB;
        double outputA;
        double sumE;
        double sumD;
        double sumC;
        switch(layer) {
            case 3:
                outputD = neuronArray[3][firstNeuronPos].getValue();
                derivative = stepSize * outputD * outputDerived(secondNeuronPos, target);
                break;

            case 2:
                outputC = neuronArray[2][firstNeuronPos].getValue();
                outputD = neuronArray[3][secondNeuronPos].getValue();
                sumE = 0;
                for (int e = 0; e < neuronsInLayer[4]; e++) {
                    sumE += outputDerived(e, target) * synapseArray[3][secondNeuronPos][e].getWeight();
                }
                derivative = stepSize * outputC * outputD * (1 - outputD) * sumE;
                break;

            case 1:
                outputB = neuronArray[1][firstNeuronPos].getValue();
                outputC = neuronArray[2][secondNeuronPos].getValue();
                sumE = 0;
                for (int e = 0; e < neuronsInLayer[4]; e++) {
                    sumD = 0;
                    for (int d = 0; d < neuronsInLayer[3]; d++) {
                        sumD += layerXDerived(3, d, e) * synapseArray[2][secondNeuronPos][d].getWeight();
                    }
                    sumE += outputDerived(e, target) * sumD;
                }
                derivative = stepSize * outputB * outputC * (1 - outputC) * sumE;
                break;

            case 0:
                /**
                 * Možná tohle funguje, ale je to pomalý jak prase, takže nefunguje.
                 * Potřeba optimalizovat. Hodně.
                 */
                outputA = neuronArray[0][firstNeuronPos].getValue();
                outputB = neuronArray[1][secondNeuronPos].getValue();
                sumE = 0;
                for (int e = 0; e < neuronsInLayer[4]; e++) {
                    sumD = 0;
                    for (int d = 0; d < neuronsInLayer[3]; d++) {
                        sumC = 0;
                        for (int c = 0; c < neuronsInLayer[2]; c++) {
                            sumC += layerXDerived(2, c, d) * synapseArray[1][secondNeuronPos][c].getWeight();
                        }
                        sumD += layerXDerived(3, d, e) * sumC;
                    }
                    sumE += outputDerived(e, target) * sumD;
                }
                derivative = stepSize * outputA * outputB * (1 - outputB) * sumE;
                break;
        }
        return derivative;
    }

    private double outputDerived(int neuronPos, Integer[] target) {
        double outputE = neuronArray[4][neuronPos].getValue();
        double derivative = (outputE - target[neuronPos]) * outputE * (1 - outputE);
        return derivative;
    }

    private double layerXDerived(int layer, int firstNeuronPos, int secondNeuronPos) {
        double outputD = neuronArray[layer][firstNeuronPos].getValue();
        double derivative = synapseArray[layer][firstNeuronPos][secondNeuronPos].getWeight() * outputD * (1 - outputD);
        return derivative;
    }

    private double countSuccess(Integer[] target){
        double success = 0;
        double maxValue = 0;
        int neuronPos = 0;
        int layersAmount = neuronsInLayer.length;
        for (int k = 0; k < neuronsInLayer[layersAmount - 1]; k++){
            double output = neuronArray[layersAmount - 1][k].getValue();
            if (output > maxValue){
                maxValue = output;
                neuronPos = k;
            }
        }
        if (target[neuronPos] == 1){
            success++;
        }

        return success;
    }

    private double[] calculatePercentage(String word){
        int layersAmount = neuronsInLayer.length;
        double output[] = new double[neuronsInLayer[layersAmount - 1]];
        for (int k = 0; k < neuronsInLayer[layersAmount - 1]; k++){
            output[k] = neuronArray[layersAmount - 1][k].getValue();
        }
        double[] percentageGuess = recalculateOutputs(output);

        return percentageGuess;
    }

    private double[] recalculateOutputs(double[] output){
        double totalOutput = 0;
        for (int k = 0; k < output.length; k++){
            totalOutput += output[k];
        }

        for (int k = 0; k < output.length; k++){
            output[k] /= totalOutput;
            output[k] = Math.round(output[k] * 1000);
            output[k] /= 10;
        }

        return output;
    }
}
