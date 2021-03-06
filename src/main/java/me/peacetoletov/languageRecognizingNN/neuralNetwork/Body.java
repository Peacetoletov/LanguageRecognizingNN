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
    private ArrayList<String> trainDataInput = new ArrayList<>();
    private ArrayList<Integer[]> trainDataTarget = new ArrayList<>();
    private ArrayList<String> testDataInput = new ArrayList<>();
    private ArrayList<Integer[]> testDataTarget = new ArrayList<>();
    private FileManager fm = new FileManager();
    private String weightsFile;
    private String allowedChars;
    private String inputWord;
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
                        //System.out.println("Weight " + layer + "|" + firstNeuronPos + "|" + secondNeuronPos + " = " + synapseWeight);
                    } else {
                        synapseWeight = Math.random() - 0.5;        //assigns a number between -0.5 and 0.5
                    }

                    synapseArray[layer][firstNeuronPos][secondNeuronPos] = new Synapse(synapseWeight);
                    //System.out.println("Synapse [" + layer + "][" + firstNeuronPos + "][" + secondNeuronPos + "] has weight " + synapseWeight);

                }
            }
        }
    }

    public void initializeTrainData(String data, Integer[] target){
        trainDataInput.add(data);
        trainDataTarget.add(target);
    }

    public void initializeTestData(String data, Integer[] target){
        testDataInput.add(data);
        testDataTarget.add(target);
    }

    public void startTraining() {
        System.out.println("Starting learning iterations. Traning data size = " + trainDataInput.size());
        int i = 1;
        while (true) {
            train(i);
            i++;
        }
    }

    public void startTraining(int iterations) {
        System.out.println("Starting learning iterations. Traning data size = " + trainDataInput.size());
        int i = 1;
        while (true) {
            train(i);
            if (i == iterations)
                break;
            i++;
        }
    }

    private void train(int i) {
        double trainSuccess = 0;
        double testSuccess = 0;
        double totalTrainCost = 0;
        double totalTestCost = 0;
        for (int example = 0; example < trainDataInput.size(); example++) {     //Training
            Integer[] target = trainDataTarget.get(example);
            String inputWord = trainDataInput.get(example);
            setInput(inputWord);
            passForward();
            trainSuccess += countSuccess(target);
            totalTrainCost += calculateCost(target);
            backpropagate(target);
        }

        for (int example = 0; example < testDataInput.size(); example++) {     //Testing
            Integer[] target = testDataTarget.get(example);
            String inputWord = testDataInput.get(example);
            setInput(inputWord);
            passForward();
            testSuccess += countSuccess(target);
            totalTestCost += calculateCost(target);
        }

        double trainSuccessRate = trainSuccess / trainDataInput.size();
        double testSuccessRate = testSuccess / testDataInput.size();
        System.out.println("Iteration " + i + " completed. Train success rate = " + trainSuccessRate + "; Test succes rate = " + testSuccessRate + "; Train cost = " + totalTrainCost + "; Test cost = " + totalTestCost);

        if (i % 10 == 0) {
            fm.saveSuccessRate(i, trainSuccessRate, testSuccessRate, totalTrainCost, totalTestCost);

            System.out.println("Saving weights!");
            saveWeights();
        }
    }

    public void saveWeights() {
        fm.saveWeights(weightsFile, synapseArray, neuronsInLayer);
    }

    public double[] guessLanguage(String word){
        setInput(word);
        passForward();
        return calculatePercentage();
    }

    public Neuron[][] getNeuronArray() {
        return neuronArray;
    }

    public Synapse[][][] getSynapseArray() {
        return synapseArray;
    }

    public String getInputWord() {
        return inputWord;
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

    private void setInput(String inputWord){
        this.inputWord = inputWord;
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
        double stepSize = 0.0005;     //0.0005

        //Define deltaK
        double[] deltaK = new double[neuronsInLayer[2]];
        for (int k = 0; k < deltaK.length; k++) {
            double outputK = neuronArray[2][k].getValue();      //makes the variable name shorter
            deltaK[k] = outputK * (1 - outputK) * (outputK - target[k]);
        }

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

        //Loop through each synapse to calculate the derivative
        int layersAmount = neuronsInLayer.length;
        for (int layer = 0; layer < layersAmount - 1; layer++) {   //loop through each neuron layer except the output layer
            for (int firstNeuronPos = 0; firstNeuronPos < neuronsInLayer[layer]; firstNeuronPos++) {      //loop through each neuron in the layer
                for (int secondNeuronPos = 0; secondNeuronPos < neuronsInLayer[layer + 1]; secondNeuronPos++) {      //loop through each neuron in the next layer
                    double derivative;
                    //Output
                    if (layer == 1) {
                        double outputJ = neuronArray[layer][firstNeuronPos].getValue();
                        derivative = stepSize * deltaK[secondNeuronPos] * outputJ;
                        synapseArray[layer][firstNeuronPos][secondNeuronPos].setDerivative(derivative);
                        synapseArray[layer][firstNeuronPos][secondNeuronPos].updateWeight();
                    }

                    //Hidden
                    if (layer == 0) {
                        double outputI = neuronArray[layer][firstNeuronPos].getValue();
                        derivative = stepSize * deltaJ[secondNeuronPos] * outputI;
                        synapseArray[layer][firstNeuronPos][secondNeuronPos].setDerivative(derivative);
                        synapseArray[layer][firstNeuronPos][secondNeuronPos].updateWeight();
                    }
                }
            }
        }

    }

    private double countSuccess(Integer[] target){
        double success = 0;
        double maxValue = 0;
        int neuronPos = 0;
        int layersAmount = neuronsInLayer.length;

        double[] test = new double[3];

        for (int e = 0; e < neuronsInLayer[layersAmount - 1]; e++){
            double output = neuronArray[layersAmount - 1][e].getValue();
            if (output > maxValue){
                maxValue = output;
                neuronPos = e;
            }

            test[e] = output;
        }
        if (target[neuronPos] == 1){
            success++;
        }

        //System.out.println("Desired output: " + target[0] + "|" + target[1] + "|" + target[2] + "; real output = " + test[0] + "|" + test[1] + "|" + test[2] + "; success = " + success);

        return success;
    }

    private double calculateCost(Integer[] target) {
        int layer = neuronsInLayer.length - 1;
        double layerCost = 0;       //added cost of all output neurons
        for (int neuronPos = 0; neuronPos < neuronsInLayer[layer]; neuronPos++) {
            double output = neuronArray[layer][neuronPos].getValue();
            double cost = 0.5 * Math.pow(output - target[neuronPos], 2);
            layerCost += cost;
        }

        return layerCost;
    }

    private double[] calculatePercentage(){
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
