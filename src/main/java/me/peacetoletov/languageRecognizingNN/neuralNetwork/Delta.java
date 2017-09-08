package me.peacetoletov.languageRecognizingNN.neuralNetwork;

/**
 * Created by lukas on 8.9.2017.
 */
public class Delta {
    /*
    private double[] deltaE;
    private double[] deltaD;
    private double[] deltaC;
    private double[] deltaB;

    public Delta(double[] deltaE, double[] deltaD, double[] deltaC, double[] deltaB) {
        this.deltaE = deltaE;
        this.deltaD = deltaD;
        this.deltaC = deltaC;
        this.deltaB = deltaB;
    }

    public double[] getDelta(int layer) {
        double[] delta = {};
        switch(layer) {
            case 4:
                delta = deltaE;
                break;
            case 3:
                delta = deltaD;
                break;
            case 2:
                delta = deltaC;
                break;
            case 1:
                delta = deltaB;
                break;
        }
        return delta;
    }
    */

    //Debugging - simpler version
    private double[] deltaE;
    private double[] deltaD;

    public Delta(double[] deltaE, double[] deltaD) {
        this.deltaE = deltaE;
        this.deltaD = deltaD;
    }

    public double[] getDelta(int layer) {
        double[] delta = {};
        switch(layer) {
            case 2:
                delta = deltaE;
                break;
            case 1:
                delta = deltaD;
                break;
        }
        return delta;
    }
}
