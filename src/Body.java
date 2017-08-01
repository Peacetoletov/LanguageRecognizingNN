/**
 * Created by lukas on 29.7.2017.
 */

import java.util.ArrayList;

public class Body {
    private Neuron[][] neuronArray;     //neuronArray[layer][position]
    private Synapse[][][] synapseArray;     //synapseArray[firstLayer][firstPosition][secondPosition]
    private int neuronsInLayer[];
    private ArrayList<String> trainingDataInput = new ArrayList<>();
    private ArrayList<Integer> trainingDataTarget = new ArrayList<>();
    FileManager fm = new FileManager();
    String weightsFileName = "weights.txt";
    String allowedChars;
    int maxWordLength;
    ArrayList<Double[]> weightList;


    Body(int layersAmount, int biggestLayerNeuronsAmount, String allowedChars, int maxWordLength){
        neuronArray = new Neuron[layersAmount][biggestLayerNeuronsAmount];
        synapseArray = new Synapse[layersAmount-1][biggestLayerNeuronsAmount][biggestLayerNeuronsAmount];
        neuronsInLayer = new int[layersAmount];
        this.allowedChars = allowedChars;
        this.maxWordLength = maxWordLength;
        if (fm.checkIfFileExists(weightsFileName)){
            weightList = fm.readWeights(weightsFileName);
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
                    if (fm.checkIfFileExists(weightsFileName)){
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

    public void initializeTrainingData(String data, int target){
        trainingDataInput.add(data);
        trainingDataTarget.add(target);
    }

    public void train(int trainingIterations){
        double success = 0;
        for (int i = 0; i < trainingIterations; i++) {
            for (int example = 0; example < trainingDataInput.size(); example++) {
                int target = getTarget(example);
                String inputWord = trainingDataInput.get(example);
                setInput(inputWord);
                passForward();
                success += countSuccess(target);
                backpropagate(target);
            }
            if (i % 10 == 0)
                System.out.println("Iteration " + i + " completed.");
        }

        fm.saveWeights(weightsFileName, synapseArray, neuronsInLayer);
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

    private int getTarget(int example){
        int target = trainingDataTarget.get(example);
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

    private void backpropagate(double target) {
        //double cost = calculateCost(target);        //might not be needed
        double stepSize = 0.001;     //0.001 or 0.0005

        //Define deltaK
        double outputK = neuronArray[neuronsInLayer.length - 1][0].getValue();      //makes the variable name shorter
        double deltaK = outputK * (1 - outputK) * (outputK - target);

        //Define deltaJ
        double[] deltaJ = new double[neuronsInLayer[1]];
        for (int i = 0; i < deltaJ.length; i++) {
            double outputJ = neuronArray[1][i].getValue();
            deltaJ[i] = outputJ * (1 - outputJ) * deltaK * synapseArray[1][i][0].getWeight();
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
                        double derivative = stepSize * deltaK * outputJ;
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

    private double countSuccess(double target){
        double success = 0;
        double output = neuronArray[neuronsInLayer.length - 1][0].getValue();
        if (target - output < 0.5){
            success = 1;
        }
        return success;
    }

    private void callbackMessage(String word){
        double output = neuronArray[neuronsInLayer.length - 1][0].getValue();
        double certainty;
        String language;
        if (output > 0.5){
            certainty = Math.round(output * 1000);
            certainty /= 10;
            language = "English";
        }
        else {
            certainty = Math.round((1 - output) * 1000);
            certainty /= 10;
            language = "Czech";
        }

        System.out.println("Your word is " + word + ". My guess is " + language + " (" + certainty + " % certain)");
    }
}
