import com.google.gson.Gson;

import java.io.File;
import java.util.Scanner;

public class Test {

    public static Memorizer.WordList deserialize(String file) {
        final String dirPath = "C:\\Users\\pguan\\Vocab-Memorizer\\Memorizer\\WordDir";
        File reviewFile = new File(dirPath, file);
        if (!reviewFile.exists()) {
            System.out.println("File does not exist");
            return new Memorizer.WordList();
        }
        Gson gson = new Gson();
        StringBuilder jsonString = new StringBuilder("");
        try {
            Scanner sc = new Scanner(reviewFile);
            while (sc.hasNextLine()) {
                jsonString.append(sc.nextLine());
            }
            sc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return gson.fromJson(jsonString.toString(), Memorizer.WordList.class);
    }

    public static void main(String[] args) {
       Memorizer.WordList list = deserialize("review.json");
       System.out.println(list.name + " has " + list.list.size() + " pairs");
    }
}
