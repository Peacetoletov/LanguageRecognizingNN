package me.peacetoletov.languageRecognizingNN.filesManaging;

/**
 * Created by lukas on 27.7.2017.
 */

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.stream.Collectors;

import me.peacetoletov.languageRecognizingNN.neuralNetwork.Synapse;

public class FileManager {
    Filter filter;

    public void createLocalDirectory(String mainDirName, String resourcesDirName, String editedDirName, String weightsDirName){
        if (!checkIfFileExists(mainDirName)){
            File dir = new File(mainDirName);
            dir.mkdir();
            File resourceDir = new File(resourcesDirName);
            resourceDir.mkdir();
            File editedDir = new File(editedDirName);
            editedDir.mkdir();
            File weightsDir = new File(weightsDirName);
            weightsDir.mkdir();
        }
    }

    public void createLocalFiles(String jarResourceFile, String localResourceFile, String editedFile, int maxWordLength, FileManager fm){
        if (!checkIfFileExists(localResourceFile)){
            copyFileFromJarUsingStream(jarResourceFile, localResourceFile);
        }
        edit(localResourceFile, editedFile, maxWordLength);
    }

    public void copyFileFromJarUsingStream(String oldFile,  String newFile) {
        File dest = new File(newFile);
        InputStream is = null;
        OutputStream os = null;
        try {
            is = FileManager.class.getResourceAsStream(oldFile);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        finally {
            try {
                is.close();
                os.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public void edit(String resourceFile, String editedFile, int maxWordLength){
        ArrayList<String> wordList = readTextFile(resourceFile);
        ArrayList<String> filteredWordList = filter.filterWords(wordList, maxWordLength);
        write(editedFile, filteredWordList);
    }

    public ArrayList<String> readTextFile(String path){
        ArrayList<String> wordList = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path));
            String line;

            while((line = br.readLine()) != null){
                String[] lineSplit = line.split("\\s+");
                for (String word: lineSplit){
                    wordList.add(word);
                }
            }

        } catch (IOException e){
            e.printStackTrace();
        }
        finally {
            try {
                br.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        return wordList;
    }

    public boolean checkIfFileExists(String path){
        File file = new File(path);
        return file.exists();
    }

    public void saveWeights(String path, Synapse[][][] synapseArray, int[] neuronsInLayer){
        try {
            File file = new File(path);
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

    public ArrayList<Double[]> readWeights(String path){
        ArrayList<String> weightsString = readTextFile(path);

        ArrayList<Double[]> weights = new ArrayList<>();

        for (int group = 0; group < weightsString.size(); group += 4){
            Double[] values = new Double[4];
            for (int i = 0; i < 4; i++){
                values[i] = Double.parseDouble(weightsString.get(group + i));
                if (values[3] != null){
                    weights.add(values);
                    break;
                }
            }
        }

        return weights;
    }

    public void createFilter(String allowedChars){
        filter = new Filter(allowedChars);
    }

    private String getResourceAsString(String path) {
        InputStream stream = FileManager.class.getResourceAsStream(path);

        if (stream == null) {
            throw new IllegalArgumentException(String.format("No resource found at '%s'!", path));
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void write(String path, ArrayList<String> wordList) {
        try {
            File file = new File(path);
            file.createNewFile();
            PrintWriter writer = new PrintWriter(file);     //overwrites

            for (int i = 0; i < wordList.size(); i++) {
                writer.println(wordList.get(i));
            }

            writer.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

}