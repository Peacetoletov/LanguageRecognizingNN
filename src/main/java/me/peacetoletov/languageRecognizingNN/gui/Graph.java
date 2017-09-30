package me.peacetoletov.languageRecognizingNN.gui;

import me.peacetoletov.languageRecognizingNN.neuralNetwork.Main;

import javax.swing.*;
import java.awt.*;

/**
 * Created by lukas on 3.9.2017.
 */

public class Graph {
    public JLabel labGuessingWord;
    public JLabel labCzechPercentage;
    public JLabel labEnglishPercentage;
    public JLabel labGermanPercentage;

    private double czechPercentage;
    private double englishPercentage;
    private double germanPercentage;

    private final int axisX1 = 100;
    private final int axisY1 = 600;
    private final int axisWidth = 350;
    private final int axisHeight = 300;
    private final int spaceBetweenColumns = 20;
    private final int columnWidth = 90;

    private final int columnMaxSize = 300;

    public Graph() {
        labGuessingWord = new JLabel("Guessing: ");
        labGuessingWord.setBounds(100, 200, 300, 30);
        labGuessingWord.setFont(new Font("Arial", Font.PLAIN, 20));

        int fontSize = 14;
        labCzechPercentage = new JLabel("Czech: ");
        labCzechPercentage.setBounds(getColumnX1(0), axisY1 + 10, getColumnX1(0) + 100, 30);
        labCzechPercentage.setFont(new Font("Arial", Font.PLAIN, fontSize));

        labEnglishPercentage = new JLabel("English: ");
        labEnglishPercentage.setBounds(getColumnX1(1), axisY1 + 10, getColumnX1(1) + 100, 30);
        labEnglishPercentage.setFont(new Font("Arial", Font.PLAIN, fontSize));

        labGermanPercentage = new JLabel("German: ");
        labGermanPercentage.setBounds(getColumnX1(2), axisY1 + 10, getColumnX1(2) + 100, 30);
        labGermanPercentage.setFont(new Font("Arial", Font.PLAIN, fontSize));
    }

    public void draw(Graphics g) {
        g.setColor(Color.black);
        g.drawLine(getAxisX1(), getAxisY1(), getAxisX1(), getAxisY2());       //vertical line      //x1, y1, x2, y2
        g.drawLine(getAxisX1(), getAxisY1(), getAxisX2(), getAxisY1());       //horizontal line

        g.setColor(new Color(0, 255, 0));
        g.fillRect(getColumnX1(0), getColumnY1(0), getColumnWidth(), getColumnHeight(0));       //x, y, width, height
        g.setColor(new Color(0, 0, 255));
        g.fillRect(getColumnX1(1), getColumnY1(1), getColumnWidth(), getColumnHeight(1));       //x, y, width, height
        g.setColor(new Color(255, 0, 0));
        g.fillRect(getColumnX1(2), getColumnY1(2), getColumnWidth(), getColumnHeight(2));       //x, y, width, height

    }

    public void update(String word) {
        labGuessingWord.setText("Guessing: " + word);
        double[] percentageGuess = Main.getBody().guessLanguage(word);
        this.czechPercentage = percentageGuess[0];
        this.englishPercentage = percentageGuess[1];
        this.germanPercentage = percentageGuess[2];
        labCzechPercentage.setText("Czech: " + czechPercentage + "%");
        labEnglishPercentage.setText("English: " + englishPercentage + "%");
        labGermanPercentage.setText("German: " + germanPercentage + "%");

        System.out.println("Your word is " + word + ". My guess is " + percentageGuess[0] + " % Czech, " + percentageGuess[1] + " % English, " + percentageGuess[2] + " % German.");
    }

    private int getAxisX1() {
        return axisX1;
    }

    private int getAxisY1() {
        return axisY1;
    }

    private int getAxisX2() {
        return axisX1 + axisWidth;
    }

    private int getAxisY2() {
        return axisY1 - axisHeight;
    }

    private int getColumnX1(int column) {
        return axisX1 + spaceBetweenColumns + column * (spaceBetweenColumns + columnWidth);
    }

    private int getColumnY1(int column) {
        return axisY1 - getColumnHeight(column);
    }

    private int getColumnWidth() {
        return columnWidth;
    }

    private int getColumnHeight(int column) {
        double percentage = getPercentage(column);
        return (int) Math.round(percentage * columnMaxSize / 100);
    }

    private double getPercentage(int column) {
        switch(column) {
            case 0:
                return czechPercentage;
            case 1:
                return englishPercentage;
            case 2:
                return germanPercentage;
            default:
                return 0;
        }
    }
}
