package me.peacetoletov.languageRecognizingNN.gui;

import me.peacetoletov.languageRecognizingNN.neuralNetwork.Main;

/**
 * Created by lukas on 2.9.2017.
 */

public class InputChecker {
    public static boolean checkWord(String word) {
        if (word.length() <= 15) {
            return compareChars(word);
        } else {
            return false;
        }
    }

    private static boolean compareChars(String word) {
        //Compare all word characters with allowed character. If all match, return true.
        String allowedChars = Main.getAllowedChars();
        for (int wordCharPos = 0; wordCharPos < word.length(); wordCharPos++) {      //loop through input word
            boolean thisCharAllowed = false;
            for (int allowedCharPos = 0; allowedCharPos < allowedChars.length(); allowedCharPos++) {        //loop through each allowed character
                if (word.charAt(wordCharPos) == allowedChars.charAt(allowedCharPos)) {
                    thisCharAllowed = true;
                }
            }
            if (!thisCharAllowed) {
                return false;
            }
        }

        return true;
    }
}
