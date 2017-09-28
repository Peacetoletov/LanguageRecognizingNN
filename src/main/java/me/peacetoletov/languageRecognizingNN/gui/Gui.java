package me.peacetoletov.languageRecognizingNN.gui;

/**
 * Created by lukas on 26.8.2017.
 */

import javax.swing.*;

public class Gui extends JFrame {

    public Gui(String title, int layersAmount, int maxWordLength, int hiddenLayerSize, int outputLayerSize, String allowedChars) {
        super(title);
        add(new Display(layersAmount, maxWordLength, hiddenLayerSize, outputLayerSize, allowedChars));
        setWindowProperties();
    }

    private void setWindowProperties(){
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
        setSize(1280, 720);
        setResizable(false);
        setLocationRelativeTo(null);
    }
}
