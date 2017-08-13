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


    Body(int layersAmount, int biggestLayerNeuronsAmount, String allowedChars, int maxWordLength, String weightsFile){
        neuronArray = new Neuron[layersAmount][biggestLayerNeuronsAmount];
        synapseArray = new Synapse[layersAmount-1][biggestLayerNeuronsAmount][biggestLayerNeuronsAmount];
        neuronsInLayer = new int[layersAmount];
        this.allowedChars = allowedChars;
        this.maxWordLength = maxWordLength;
        this.weightsFile = weightsFile;
        if (fm.checkIfFileExists(weightsFile)){
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
                    if (fm.checkIfFileExists(weightsFile)){
                        synapseWeight = getSynapseWeight(weightList, layer, firstNeuronPos, secondNeuronPos);
                    } else {
                        synapseWeight = Math.random() - 0.5;        //assigns a number between -0.5 and 0.5
                    }

                    synapseArray[layer][firstNeuronPos][secondNeuronPos] = new Synapse(synapseWeight);
                    //System.out.println("neuralNetwork.Synapse [" + layer + "][" + firstNeuronPos + "][" + secondNeuronPos + "] has weight " + synapseWeight);
                }
            }
        }

    }

    public void initializeTrainingData(String data, Integer[] target){
        trainingDataInput.add(data);
        trainingDataTarget.add(target);
    }

    public void train(int trainingIterations){
        double success = 0;
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

        fm.saveWeights(weightsFile, synapseArray, neuronsInLayer);
        double successRate = success / (trainingDataInput.size() * trainingIterations);
        System.out.println("Success rate = " + successRate);
    }

    public void guessLanguage(String word){
        setInput(word);
        passForward();
        callbackMessage(word);
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
        double stepSize = 0.001;     //0.001 or 0.0005

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
        /**
         * This part can be re-written.
         * If I start deriving the hidden layer first, I don't need to make a separate loop to update the weights.
         * That means about 9000 fewer iterations per each call of this function.
         */
        int layersAmount = neuronsInLayer.length;
        for (int layer = 0; layer < layersAmount - 1; layer++) {   //loop through each neuron layer except the output layer
            for (int firstNeuronPos = 0; firstNeuronPos < neuronsInLayer[layer]; firstNeuronPos++) {      //loop through each neuron in the layer
                for (int secondNeuronPos = 0; secondNeuronPos < neuronsInLayer[layer + 1]; secondNeuronPos++) {      //loop through each neuron in the next layer
                    //Hidden layer
                    if (layer == 0) {
                        double outputI = neuronArray[layer][firstNeuronPos].getValue();
                        double derivative = stepSize * deltaJ[secondNeuronPos] * outputI;
                        synapseArray[layer][firstNeuronPos][secondNeuronPos].setDerivative(derivative);
                    }

                    //Output layer
                    if (layer == 1) {
                        double outputJ = neuronArray[layer][firstNeuronPos].getValue();
                        double derivative = stepSize * deltaK[secondNeuronPos] * outputJ;
                        synapseArray[layer][firstNeuronPos][secondNeuronPos].setDerivative(derivative);
                    }
                }
            }
        }

        //Loop through each synapse to update weights
        for (int layer = 0; layer < layersAmount - 1; layer++) {   //loop through each neuron layer except the output layer
            for (int firstNeuronPos = 0; firstNeuronPos < neuronsInLayer[layer]; firstNeuronPos++) {      //loop through each neuron in the layer
                for (int secondNeuronPos = 0; secondNeuronPos < neuronsInLayer[layer + 1]; secondNeuronPos++) {      //loop through each neuron in the next layer
                    synapseArray[layer][firstNeuronPos][secondNeuronPos].updateWeight();
                }
            }
        }
    }

    private double countSuccess(Integer[] target){
        double success = 0;
        double maxValue = 0;
        int neuronPos = 0;
        for (int k = 0; k < neuronsInLayer[2]; k++){
            double output = neuronArray[2][k].getValue();
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

    private void callbackMessage(String word){
        double output[] = new double[neuronsInLayer[2]];
        for (int k = 0; k < neuronsInLayer[2]; k++){
            output[k] = neuronArray[2][k].getValue();
        }
        double[] percentage = calculatePercentage(output);

        System.out.println("Your word is " + word + ". My guess is " + percentage[0] + " % Czech, " + percentage[1] + " % English, " + percentage[2] + " % German.");
    }

    private double[] calculatePercentage(double[] output){
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
