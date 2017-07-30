/**
 * Created by lukas on 27.7.2017.
 */

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;

public class FileManager {
    Filter filter = new Filter();

    public void edit(String oldFileName, String newFileName, int maxWordLength){
        ArrayList<String> wordList = readTextFile(oldFileName);
        ArrayList<String> filteredWordList = filter.filterWords(wordList, maxWordLength);
        write(newFileName, filteredWordList);
    }

    public ArrayList<String> readTextFile(String fileName){
        BufferedReader br = null;
        ArrayList<String> wordList = new ArrayList<>();
        try {
            br = new BufferedReader(new FileReader(fileName));
            String line;

            while((line = br.readLine()) != null){
                separateWords(wordList, line);
            }

        } catch (IOException e){
            e.printStackTrace();
        }
        finally {     //Finally = executes no matter what (exception / no exception)
            try {
                br.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        return wordList;
    }

    public Boolean checkIfFileExists(String fileName){
        File file = new File(fileName);
        return file.exists();
    }

    public void saveWeights(String fileName, Synapse[][][] synapseArray, int[] neuronsInLayer){
        try {
            File file = new File(fileName);
            PrintWriter pw = new PrintWriter(file);     //overwrites

            for (int firstLayer = 0; firstLayer < neuronsInLayer.length - 1; firstLayer++){
                for (int firstPosition = 0; firstPosition < neuronsInLayer[firstLayer]; firstPosition++){
                    for (int secondPosition = 0; secondPosition < neuronsInLayer[firstLayer + 1]; secondPosition++){
                        double weight = synapseArray[firstLayer][firstPosition][secondPosition].getWeight();
                        String weightFormat = BigDecimal.valueOf(weight).toPlainString();
                        pw.println(firstLayer + " " + firstPosition + " " + secondPosition + " " + weightFormat);
                    }
                }
            }
            pw.close();

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Double[]> readWeights(String fileName){
        BufferedReader br = null;
        ArrayList<Double[]> weights = new ArrayList<>();
        try {
            br = new BufferedReader(new FileReader(fileName));
            String line;

            while((line = br.readLine()) != null){
                weights.add(createWeightArray(weights, line));
            }

        } catch (IOException e){
            e.printStackTrace();
        }
        finally {     //Finally = executes no matter what (exception / no exception)
            try {
                br.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        return weights;
    }

    private void write(String fileName, ArrayList<String> wordList) {
        try {
            File file = new File(fileName);
            file.createNewFile();
            PrintWriter pw = new PrintWriter(file);         //overwrites

            for (int i = 0; i < wordList.size(); i++){
                pw.println(wordList.get(i));
            }

            pw.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void separateWords(ArrayList<String> wordList, String line){
        String tempString = "";
        for (int i = 0; i < line.length(); i++) {
            if (!String.valueOf(line.charAt(i)).equals(" ")) {
                tempString += String.valueOf(line.charAt(i));
            }

            if ((String.valueOf(line.charAt(i)).equals(" ") || i == line.length() - 1) && !tempString.equals("")) {
                wordList.add(tempString);
                tempString = "";
            }
        }
    }

    private Double[] createWeightArray(ArrayList<Double[]> weights, String line){
        int arrayElements = 4;
        Double[] weightArray = new Double[arrayElements];      //weightArray[firstLayer][firstPosition][secondPosition][weight]
        String tempString = "";
        for (int i = 0; i < line.length(); i++) {
            if (!String.valueOf(line.charAt(i)).equals(" ") && i != line.length() - 1) {
                tempString += String.valueOf(line.charAt(i));
            }
            else {
                try {
                    double value = Double.parseDouble(tempString);
                    tempString = "";
                    for (int j = 0; j < arrayElements; j++){
                        if (weightArray[j] == null){
                            weightArray[j] = value;
                            break;
                        }
                    }
                }
                catch(NumberFormatException e){
                    e.printStackTrace();
                }
            }
        }

        return weightArray;
    }
}