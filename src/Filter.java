import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by lukas on 28.7.2017.
 */
public class Filter {
    String allowedChars;

    public Filter(String allowedChars){
        this.allowedChars = allowedChars;
    }

    public ArrayList<String> filterWords(ArrayList<String> wordList, int maxWordLength){
        ArrayList<String> filteredWordList = new ArrayList<>();
        int duplicates = 0;
        int nonDuplicates = 0;
        int inappropriateWords = 0;
        for (int i = 0; i < wordList.size(); i++){
            String word = wordList.get(i);
            word = applyFilter(word);

            if (!word.equals("")){
                if (word.length() > 1 && word.length() <= maxWordLength){
                    if (!checkIfIsDuplicate(word, filteredWordList)) {
                        filteredWordList.add(word);
                        nonDuplicates++;
                    }
                    else
                        duplicates++;

                }
                else
                    inappropriateWords++;
            }
        }
        System.out.println("File filtered! " + nonDuplicates + " unique words added, " + duplicates + " duplicates and " + inappropriateWords + " inappropriate words filtered.");
        return filteredWordList;
    }

    public String applyFilter(String word){
        String filteredWord = "";
        word = word.toLowerCase();
        for (int i = 0; i < word.length(); i++) {
            char character = word.charAt(i);

            //Filters
            //Allowed characters
            int j;
            for (j = 0; j < allowedChars.length(); j++){
                if (character == allowedChars.charAt(j)){
                    filteredWord += character;
                    break;
                }
            }
        }
        return filteredWord;
    }

    private Boolean checkIfIsDuplicate(String word, ArrayList<String> wordList){
        for (int i = 0; i < wordList.size(); i++){
            if (word.equals(wordList.get(i))){
                return true;
            }
        }
        return false;
    }
}
