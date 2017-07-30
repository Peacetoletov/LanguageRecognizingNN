/**
 * Created by lukas on 29.7.2017.
 */

public class Neuron {
    private double value;

    public void setValue(double value){
        this.value = value;
    }

    public double getValue(){
        return value;
    }

    public void sigmoid(double x){
        double newValue =  1 / (1 + Math.exp(-x));
        setValue(newValue);
    }
}
