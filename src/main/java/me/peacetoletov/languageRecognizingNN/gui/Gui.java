package me.peacetoletov.languageRecognizingNN.gui;

/**
 * Created by lukas on 26.8.2017.
 */

import javax.swing.*;
import java.awt.*;

public class Gui extends JFrame {
    private JLabel labEnterWord;
    private JTextField tfWord;
    private JButton butOk;

    public Gui(String title) {
        super(title);
        JPanel p = new JPanel();
        p.setLayout(null);

        //Create components and add them to the container
        addComponents(p);

        //Add container to window
        getContentPane().add(p);

        //Set window properties
        setWindowProperties();
    }

    private void addComponents(JPanel p) {
        labEnterWord = new JLabel("Enter your word:");
        labEnterWord.setBounds(100, 60, 200, 30);                   //(x, y, width, height)
        labEnterWord.setFont(new Font("Arial", Font.PLAIN, 20));
        p.add(labEnterWord);

        tfWord = new JTextField();
        tfWord.setBounds(100, 100, 100, 25);
        tfWord.setFont(new Font("Arial", Font.PLAIN, 14));
        p.add(tfWord);

        butOk = new JButton("OK");
        butOk.setBounds(220, 100, 51, 25);
        p.add(butOk);
    }

    private void setWindowProperties(){
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        setSize(1280, 720);
        setResizable(false);
        setLocationRelativeTo(null);
    }
}
