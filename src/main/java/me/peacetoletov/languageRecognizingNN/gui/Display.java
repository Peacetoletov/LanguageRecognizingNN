package me.peacetoletov.languageRecognizingNN.gui;

/**
 * Created by lukas on 30.8.2017.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Display extends JPanel {
    private JLabel labEnterWord;
    private JLabel labErrorMessage;
    private JButton butOk;
    private JTextField tfWord;

    private Graph graph;
    private Diagram diagram;

    private String wordToGuess = "fracture";

    public Display(int layersAmount, int maxWordLength, int hiddenLayerSize, int outputLayerSize, String allowedChars) {
        graph = new Graph();
        diagram = new Diagram(layersAmount, maxWordLength, hiddenLayerSize, outputLayerSize, allowedChars);
        setLayout(null);
        addComponents();
        graph.update(wordToGuess);
    }

    private void addComponents() {
        //Input
        labEnterWord = new JLabel("Enter your word:");
        labEnterWord.setBounds(100, 60, 200, 30);                   //(x, y, width, height)
        labEnterWord.setFont(new Font("Arial", Font.PLAIN, 20));
        add(labEnterWord);

        tfWord = new JTextField(wordToGuess);
        tfWord.setBounds(100, 100, 100, 25);
        tfWord.setFont(new Font("Arial", Font.PLAIN, 14));
        add(tfWord);

        butOk = new JButton("OK");
        butOk.setBounds(220, 100, 51, 25);
        butOk.addActionListener(new Event());
        add(butOk);

        labErrorMessage = new JLabel("");
        labErrorMessage.setBounds(100, 130, 200, 30);                   //(x, y, width, height)
        labErrorMessage.setFont(new Font("Arial", Font.PLAIN, 20));
        add(labErrorMessage);

        //Graph
        add(graph.labGuessingWord);
        add(graph.labCzechPercentage);
        add(graph.labEnglishPercentage);
        add(graph.labGermanPercentage);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(new Color(200, 255, 255));

        //Graph
        graph.draw(g);

        //Neural network diagram
        diagram.draw(g);
    }

    private class Event implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String word = tfWord.getText().toLowerCase();
            if (InputChecker.checkWord(word)) {
                labErrorMessage.setText("");
                graph.update(word);
                repaint();
            } else {
                labErrorMessage.setText("Error: invalid input.");
            }
        }
    }
}
