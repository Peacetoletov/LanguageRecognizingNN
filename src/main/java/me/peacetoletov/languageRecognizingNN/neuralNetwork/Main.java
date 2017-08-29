package me.peacetoletov.languageRecognizingNN.neuralNetwork;

/**
 * Created by lukas on 27.7.2017.
 */

import me.peacetoletov.languageRecognizingNN.filesManaging.FileManager;
import me.peacetoletov.languageRecognizingNN.gui.Gui;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args){
        //Check start time
        long startTime = System.currentTimeMillis();

        //Create GUI
        Gui gui = new Gui("LanguageRecognizingNN");

        //Create the neural network
        createNN(args);

        //Calculate total time
        long endTime = System.currentTimeMillis();
        float totalTime = (float) (endTime - startTime);
        float seconds = totalTime / 1000;
        System.out.println("The neural network needed a total of " + seconds + " seconds to execute all actions.");
    }

    private static void createNN(String[] args){
        //Read data
        String allowedChars = "abcdefghijklmnopqrstuvwxyzáčďéěíňóřšťúůýžäöüß";
        int maxWordLength = 15;

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
        fm.copyFileFromJarUsingStream("/weights.txt", localWeightsFile);

        ArrayList<String> czechWords = fm.readTextFile(localCzechWordsEditedFile);
        ArrayList<String> englishWords = fm.readTextFile(localEnglishWordsEditedFile);
        ArrayList<String> germanWords = fm.readTextFile(localGermanWordsEditedFile);

        //Create the body of the NN
        int hiddenLayerSize = 15;
        int inputLayerSize = allowedChars.length() * maxWordLength;
        Body body = new Body(3, inputLayerSize, allowedChars, maxWordLength, localWeightsFile);
        body.createNeurons(0, inputLayerSize);
        body.createNeurons(1, hiddenLayerSize);
        body.createNeurons(2, 3);
        body.createSynapses();

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
        //body.train(10);     //20000 learning iterations so far

        /**
         * 100 - 54 %
         * 1000 - 80 %
         * 2000 - 86 %
         * 5000 - 91 %
         * 20000 - 96.9 %
         */

        //Guess
        //celkem 104/120 = 86 %
        /*
        body.guessLanguage("hranolky");
        body.guessLanguage("rottenberg");
        body.guessLanguage("fingers");
        body.guessLanguage("vorkommen");
        body.guessLanguage("klučina");
        body.guessLanguage("inhale");
        body.guessLanguage("arbeit");
        body.guessLanguage("macht");
        body.guessLanguage("frei");
        body.guessLanguage("cleaning");
        body.guessLanguage("pstruh");
        body.guessLanguage("najednou");
        body.guessLanguage("papoušek");
        body.guessLanguage("papagei");
        body.guessLanguage("parrot");
        body.guessLanguage("shotgun");
        body.guessLanguage("brokovnice");
        body.guessLanguage("omezit");
        body.guessLanguage("restrict");
        body.guessLanguage("kreuz");
        body.guessLanguage("durchfall");
        body.guessLanguage("meine");
        body.guessLanguage("hallo");
        body.guessLanguage("warum");
        body.guessLanguage("katze");
        body.guessLanguage("zeit");
        body.guessLanguage("schreiben");
        body.guessLanguage("fragen");
        body.guessLanguage("kaufen");
        body.guessLanguage("ahoj");
        body.guessLanguage("proč");
        body.guessLanguage("kočka");
        body.guessLanguage("čas");
        body.guessLanguage("psát");
        body.guessLanguage("zeptat");
        body.guessLanguage("nekupovat");
        body.guessLanguage("hello");
        body.guessLanguage("why");
        body.guessLanguage("cat");
        body.guessLanguage("time");
        body.guessLanguage("write");
        body.guessLanguage("ask");
        body.guessLanguage("buy");
        body.guessLanguage("germany");
        body.guessLanguage("něměcko");
        body.guessLanguage("deutschland");
        body.guessLanguage("pes");
        body.guessLanguage("dog");
        body.guessLanguage("hund");
        body.guessLanguage("švýcarsko");
        body.guessLanguage("schweiz");
        body.guessLanguage("switzerland");
        body.guessLanguage("papír");
        body.guessLanguage("paper");
        body.guessLanguage("papier");
        body.guessLanguage("farbstifte");
        body.guessLanguage("pastelka");
        body.guessLanguage("pencil");
        body.guessLanguage("history");
        body.guessLanguage("geschichte");
        body.guessLanguage("dějepis");
        body.guessLanguage("zeměpis");
        body.guessLanguage("erdkunde");
        body.guessLanguage("geography");
        body.guessLanguage("hrát");
        body.guessLanguage("play");
        body.guessLanguage("spielen");
        body.guessLanguage("vařit");
        body.guessLanguage("cook");
        body.guessLanguage("kuchen");
        body.guessLanguage("immediately");
        body.guessLanguage("okamžitě");
        body.guessLanguage("sofort");
        body.guessLanguage("nemocnice");
        body.guessLanguage("krankenhaus");
        body.guessLanguage("hospital");
        body.guessLanguage("začít");
        body.guessLanguage("begin");
        body.guessLanguage("beginnen");
        body.guessLanguage("kamarádka");
        body.guessLanguage("friend");
        body.guessLanguage("freundin");
        body.guessLanguage("žena");
        body.guessLanguage("woman");
        body.guessLanguage("frau");
        body.guessLanguage("chodit");
        body.guessLanguage("went");
        body.guessLanguage("gehen");
        body.guessLanguage("nichts");
        body.guessLanguage("nic");
        body.guessLanguage("nothing");
        body.guessLanguage("free");
        body.guessLanguage("zdarma");
        body.guessLanguage("frei");
        body.guessLanguage("sníh");
        body.guessLanguage("snow");
        body.guessLanguage("schnee");
        body.guessLanguage("wasser");
        body.guessLanguage("voda");
        body.guessLanguage("water");
        body.guessLanguage("kniha");
        body.guessLanguage("buch");
        body.guessLanguage("book");
        body.guessLanguage("blbec");
        body.guessLanguage("polsko");
        body.guessLanguage("sea");
        body.guessLanguage("france");
        body.guessLanguage("cant");
        body.guessLanguage("believe");
        body.guessLanguage("that");
        body.guessLanguage("when");
        body.guessLanguage("breathe");
        body.guessLanguage("there");
        body.guessLanguage("is");
        body.guessLanguage("something");
        body.guessLanguage("good");
        body.guessLanguage("inside");
        body.guessLanguage("of");
        body.guessLanguage("just");
        body.guessLanguage("one");
        */
        //body.guessLanguage("královna");


        for (String word: args){
            body.guessLanguage(word);
        }
    }
}
