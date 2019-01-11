import java.io.*;
import java.util.ArrayList;

public class ChatFilter {
    private File f;
    private ArrayList<String> badWords;
    private ArrayList<String> filteredBadWords;

    public ChatFilter(String badWordsFileName) throws FileNotFoundException {
        f = new File(badWordsFileName);
        if (!f.exists()) {
            throw new FileNotFoundException();
        }
        badWords = new ArrayList<>();
        filteredBadWords = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            while (true) {
                String badWord = br.readLine();
                if (badWord == null) {
                    break;
                }
                String filtered = new String("");
                for (int i = 0; i < badWord.length(); i++) {
                    filtered += "*";
                }

                badWords.add(badWord);
                filteredBadWords.add(filtered);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public String filter(String msg) {
        for (int i = 0; i < badWords.size(); i++) {
            msg = msg.replaceAll("(?i)" + badWords.get(i), filteredBadWords.get(i));
        }
        return msg;
    }

    public ArrayList<String> getBadWords() {
        return badWords;
    }

    public ArrayList<String> getFilteredBadWords() {
        return filteredBadWords;
    }

//    public static void main(String[] args) {
//        ChatFilter cf = new ChatFilter("/Users/Aman/IdeaProjects/CS180/'Chat Server'/src/badwords.txt");
//        //System.out.println(cf.getBadWords().get(0));
//        System.out.println(cf.filter("IU"));
//
//    }
}
