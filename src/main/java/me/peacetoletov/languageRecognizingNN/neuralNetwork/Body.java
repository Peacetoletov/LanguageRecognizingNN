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
        System.out.println("fm.checkIfFileExists(weightsFile) = " + fm.checkIfFileExists(weightsFile));
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

        /**
         * TADY JE NĚKDE PROBLÉM! Synapse z této části se po zapsání do souboru neshodují s tím, co je ze souboru přečteno.
         */

        while (true) {
            success = 0;
            double totalCost = 0;
            for (int example = 0; example < trainingDataInput.size(); example++) {
                Integer[] target = getTarget(example);
                String inputWord = trainingDataInput.get(example);
                setInput(inputWord);
                passForward();
                success += countSuccess(target);
                totalCost += calculateCost(target);

                if (i >= 3) {
                    backpropagate(target);
                }
            }

            /*
            //test
            testSynapse = synapseArray[3][0][0].getWeight();
            System.out.println("Synapse[3][0][0] = " + testSynapse);
            */

            //double successRate = success / (trainingDataInput.size() * i);
            double successRate = success / trainingDataInput.size();
            System.out.println("Iteration " + i + " completed. Success rate = " + successRate + "; total cost = " + totalCost);

            if (i % 10 == 0) {
                //System.out.println("Saving weights!");
                //System.out.println("Synapse[3][0][0] = " + testSynapse);
                //saveWeights();
            }

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
        double stepSize = 0.0001;     //0.001 or 0.0005      //changed to a lower value for faster testing


        //Define deltaK
        /*
        double[] deltaK = new double[neuronsInLayer[4]];
        for (int k = 0; k < deltaK.length; k++) {
            double outputK = neuronArray[4][k].getValue();      //makes the variable name shorter
            deltaK[k] = outputK * (1 - outputK) * (outputK - target[k]);
            System.out.println("Correct OutputE = " + outputK + "; target[e] = " + target[k] + "; delta = " + deltaK[k]);
        }
        */

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

        //NEW
        //Define constant deltas (deltaX)
        double[] deltaE = new double[neuronsInLayer[4]];
        for (int e = 0; e < deltaE.length; e++) {
            double outputE = neuronArray[4][e].getValue();
            deltaE[e] = outputE * (1 - outputE) * (outputE - target[e]);
        }

        double[] deltaD = new double[neuronsInLayer[3]];
        for (int d = 0; d < deltaD.length; d++) {
            double outputD = neuronArray[3][d].getValue();
            double sumE = 0;
            for (int e = 0; e < deltaE.length; e++) {
                sumE += deltaE[e] * synapseArray[3][d][e].getWeight();
            }
            deltaD[d] = outputD * (1 - outputD) * sumE;
        }

        double[] deltaC = new double[neuronsInLayer[2]];
        for (int c = 0; c < deltaC.length; c++) {
            double outputC = neuronArray[2][c].getValue();
            double sumD = 0;
            for (int d = 0; d < deltaD.length; d++) {
                sumD += deltaD[d] * synapseArray[2][c][d].getWeight();
            }
            deltaC[c] = outputC * (1 - outputC) * sumD;
        }

        double[] deltaB = new double[neuronsInLayer[1]];
        for (int b = 0; b < deltaB.length; b++) {
            double outputB = neuronArray[1][b].getValue();
            double sumC = 0;
            for (int c = 0; c < deltaC.length; c++) {
                sumC += deltaC[c] * synapseArray[1][b][c].getWeight();
            }
            deltaB[b] = outputB * (1 - outputB) * sumC;
        }

        //Loop through each synapse to calculate the derivative

        int layersAmount = neuronsInLayer.length;
        for (int layer = 0; layer < layersAmount - 1; layer++) {   //loop through each neuron layer except the output layer
            for (int firstNeuronPos = 0; firstNeuronPos < neuronsInLayer[layer]; firstNeuronPos++) {      //loop through each neuron in the layer
                for (int secondNeuronPos = 0; secondNeuronPos < neuronsInLayer[layer + 1]; secondNeuronPos++) {      //loop through each neuron in the next layer

                    double derivative;

                    //test
                    /**
                     * POZOR! layer 3 je divný. Když nechám běžet samotný layer 3, tak cost místy roste. To se nikde jinde neděje.
                     * Edit: tak nevím. Teď to zase funguje dobře. Cost pouze klesá.
                     * Edit 2: když nechám layery 3 a 2 současně, tak se také po chvíli objeví stoupání, ale brzy zase přijde pomalé klesání.
                     * Edit 3: když nechám všechny layery, tak se cost brzy zasekne (klesá a roste) na jednom místě (konkrétně 5018).
                     * Edit 4: Seru na to. Prostě nebude multi hidden layer. Nemám to zapotřebí.
                     */


                    if (layer == 3) {       //funguje ?
                        double outputD = neuronArray[layer][firstNeuronPos].getValue();
                        derivative = stepSize * deltaE[secondNeuronPos] * outputD;
                        //System.out.println("Correct derivative = " + derivative);
                        synapseArray[layer][firstNeuronPos][secondNeuronPos].setDerivative(derivative);
                        //synapseArray[layer][firstNeuronPos][secondNeuronPos].updateWeight();
                    }

                    if (layer == 2) {       //funguje
                        double outputC = neuronArray[layer][firstNeuronPos].getValue();
                        derivative = stepSize * deltaD[secondNeuronPos] * outputC;
                        //System.out.println("Correct derivative = " + derivative);
                        synapseArray[layer][firstNeuronPos][secondNeuronPos].setDerivative(derivative);
                        //synapseArray[layer][firstNeuronPos][secondNeuronPos].updateWeight();
                    }


                    if (layer == 1) {       //funguje
                        double outputB = neuronArray[layer][firstNeuronPos].getValue();
                        derivative = stepSize * deltaC[secondNeuronPos] * outputB;
                        synapseArray[layer][firstNeuronPos][secondNeuronPos].setDerivative(derivative);
                        //synapseArray[layer][firstNeuronPos][secondNeuronPos].updateWeight();
                    }

                    if (layer == 0) {       //funguje
                        double outputA = neuronArray[layer][firstNeuronPos].getValue();
                        derivative = stepSize * deltaB[secondNeuronPos] * outputA;
                        synapseArray[layer][firstNeuronPos][secondNeuronPos].setDerivative(derivative);
                        //synapseArray[layer][firstNeuronPos][secondNeuronPos].updateWeight();
                    }

                }
            }
        }


        //Loop through each synapse to update weights
        /*
        for (int layer = 0; layer < layersAmount - 1; layer++) {   //loop through each neuron layer except the output layer
            for (int firstNeuronPos = 0; firstNeuronPos < neuronsInLayer[layer]; firstNeuronPos++) {      //loop through each neuron in the layer
                for (int secondNeuronPos = 0; secondNeuronPos < neuronsInLayer[layer + 1]; secondNeuronPos++) {      //loop through each neuron in the next layer
                    synapseArray[layer][firstNeuronPos][secondNeuronPos].updateWeight();
                }
            }
        }
        */
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
