/**
 * Created by lukas on 27.7.2017.
 */

import java.util.ArrayList;

public class Main {
    public static void main(String[] args){
        //Check start time
        long startTime = System.currentTimeMillis();

        //Read data
        String allowedChars = "abcdefghijklmnopqrstuvwxyzáčďéěíňóřšťúůýžäöüß";
        FileManager fm = new FileManager();
        fm.createFilter(allowedChars);
        int maxWordLength = 15;
        fm.edit("czechWordsUnedited.txt", "czechWords.txt", maxWordLength);
        fm.edit("englishWordsUnedited.txt", "englishWords.txt", maxWordLength);
        fm.edit("germanWordsUnedited.txt", "germanWords.txt", maxWordLength);
        ArrayList<String> czechWords = fm.readTextFile("czechWords.txt");
        ArrayList<String> englishWords = fm.readTextFile("englishWords.txt");

        //Create the body of the NN
        int hiddenLayerSize = 15;
        int inputLayerSize = allowedChars.length() * maxWordLength;
        Body body = new Body(3, inputLayerSize, allowedChars, maxWordLength);
        body.createNeurons(0, inputLayerSize);
        body.createNeurons(1, hiddenLayerSize);
        body.createNeurons(2, 1);
        body.createSynapses();

        //Initialize training data
        for (String x: czechWords){
            body.initializeTrainingData(x, 0);
        }
        for (String y: englishWords){
            body.initializeTrainingData(y, 1);
        }

        //Train
        //body.train(100);     //10000 learning iterations so far

        //Guess
        /*
        body.guessLanguage("trousers");
        body.guessLanguage("shirt");
        body.guessLanguage("fingers");
        body.guessLanguage("hand");
        body.guessLanguage("leg");
        body.guessLanguage("heart");
        body.guessLanguage("arm");
        body.guessLanguage("elbow");
        body.guessLanguage("head");
        body.guessLanguage("hair");
        body.guessLanguage("hairs");
        body.guessLanguage("nail");
        */



        //Calculate total time
        long endTime   = System.currentTimeMillis();
        float totalTime = (float) (endTime - startTime);
        float seconds = totalTime / 1000;
        System.out.println("The neural network needed a total of " + seconds + " seconds to execute all actions.");
    }
}
