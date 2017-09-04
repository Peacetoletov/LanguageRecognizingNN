package me.peacetoletov.languageRecognizingNN.gui;

/**
 * Created by lukas on 26.8.2017.
 */

import javax.swing.*;

public class Gui extends JFrame {

    public Gui(String title) {
        super(title);
        add(new Display());
        setWindowProperties();  //ZASRANÁ JAVA, ZASRANEJ SWING
    }

    private void setWindowProperties(){
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        setSize(1280, 720);
        setResizable(false);
        setLocationRelativeTo(null);
    }
}
