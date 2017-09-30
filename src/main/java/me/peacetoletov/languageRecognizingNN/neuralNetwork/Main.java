package me.peacetoletov.languageRecognizingNN.neuralNetwork;

/**
 * Created by lukas on 27.7.2017.
 */

import me.peacetoletov.languageRecognizingNN.filesManaging.FileManager;
import me.peacetoletov.languageRecognizingNN.gui.Gui;

import java.util.ArrayList;

public class Main {
    //Variables for controlling the program
    private static final boolean createRandomWeights = false;
    private static final boolean train = false;

    //Variables for creating body of the NN
    private static final int hiddenLayerSize = 15;
    private static final int maxWordLength = 15;
    private static final int layersAmount = 3;
    private static final int outputLayerSize = 3;
    private static final String allowedChars = "abcdefghijklmnopqrstuvwxyzáčďéěíňóřšťúůýžäöüß";

    private static Body body;

    public static void main(String[] args){
        //Create the neural network
        createNN();

        //Create GUI
        Gui gui = new Gui("LanguageRecognizingNN", layersAmount, maxWordLength, hiddenLayerSize, outputLayerSize, allowedChars);

    }

    public static Body getBody() {
        return body;
    }

    public static String getAllowedChars() {
        return allowedChars;
    }

    private static void createNN(){
        //Read data
        FileManager fm = new FileManager();
        fm.createFilter(allowedChars);

        String home = System.getProperty("user.home");
        String dirName = home + "/LanguageRecognizingNN";
        String resourcesDirName = dirName + "/resources";
        String editedDirName = dirName + "/edited";
        String weightsDirName = dirName + "/weights";
        fm.createLocalDirectory(dirName, resourcesDirName, editedDirName, weightsDirName);

        String localCzechWordsResourceFile = resourcesDirName + "/czechWordsUnedited.txt";
        String localCzechWordsEditedFile = editedDirName + "/czechWords.txt";
        String localEnglishWordsResourceFile = resourcesDirName + "/englishWordsUnedited.txt";
        String localEnglishWordsEditedFile = editedDirName + "/englishWords.txt";
        String localGermanWordsResourceFile = resourcesDirName + "/germanWordsUnedited.txt";
        String localGermanWordsEditedFile = editedDirName + "/germanWords.txt";
        String localWeightsFile = weightsDirName + "/weights.txt";

        fm.createLocalFiles("/czechWordsUnedited.txt", localCzechWordsResourceFile, localCzechWordsEditedFile, maxWordLength, fm);
        fm.createLocalFiles("/englishWordsUnedited.txt", localEnglishWordsResourceFile, localEnglishWordsEditedFile, maxWordLength, fm);
        fm.createLocalFiles("/germanWordsUnedited.txt", localGermanWordsResourceFile, localGermanWordsEditedFile, maxWordLength, fm);

        ArrayList<String> czechWords = fm.readTextFile(localCzechWordsEditedFile);
        ArrayList<String> englishWords = fm.readTextFile(localEnglishWordsEditedFile);
        ArrayList<String> germanWords = fm.readTextFile(localGermanWordsEditedFile);

        //Create the body of the NN
        int inputLayerSize = allowedChars.length() * maxWordLength;

        if (createRandomWeights) {
            body = new Body(layersAmount, inputLayerSize, allowedChars, maxWordLength, createRandomWeights, localWeightsFile);
            createBodyElements(inputLayerSize, hiddenLayerSize, outputLayerSize);
            body.saveWeights();
        } else {
            if (!fm.checkIfFileExists(localWeightsFile)){
                fm.copyFileFromJarUsingStream("/weights.txt", localWeightsFile);
            }
            body = new Body(layersAmount, inputLayerSize, allowedChars, maxWordLength, createRandomWeights, localWeightsFile);
            createBodyElements(inputLayerSize, hiddenLayerSize, outputLayerSize);
        }


        //Initialize training data
        Integer[] czechTarget = {1, 0, 0};
        Integer[] englishTarget = {0, 1, 0};
        Integer[] germanTarget = {0, 0, 1};
        for (String x: czechWords){
            body.initializeTrainingData(x, czechTarget);
        }
        for (String y: englishWords){
            body.initializeTrainingData(y, englishTarget);
        }
        for (String z: germanWords){
            body.initializeTrainingData(z, germanTarget);
        }


        //Train
        if (train)
            body.train();

        /**
         * Training data size: 2000 word per language
         * 100 - 54 %
         * 1000 - 80 %
         * 2000 - 86 %
         * 5000 - 91 %
         * 20000 - 96.9 %
         *
         * Accuracy - about 85%
         */
    }

    private static void createBodyElements(int inputLayerSize, int hiddenLayerSize, int outputLayerSize) {
        body.createNeurons(0, inputLayerSize);
        body.createNeurons(1, hiddenLayerSize);
        body.createNeurons(2, outputLayerSize);
        body.createSynapses();
    }
}
