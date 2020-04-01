import com.google.gson.*;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

// The class that handling processing,
// storing word-meaning pairs
// and format them into Json into a file
// named by users
public class Memorizer {

    // This is the total since the user start using
    private int totalCount;
    // memoCount encourages user to memorize often
    // this is a combination of words stored and reviewed
    private int memoCount;
    // All relative data including cache lists, stored pairs, etc would be stored
    // under this path
    private File dir;
    private String dirPath;
    // Words to be reviewed
    private WordList toBeReviewed;
    // Words to be stored
    private WordList toBeStored;
    // Indicate whether the dirPath is valid
    private boolean isPathValid;
    // Words that users have made mistakes on
    private WordList mistakeList;

    // Wrapper class for deserializing a list of words
    // and serializing the list of words
    public static class WordList {
        public String name = "";
        public Map<String, String> list = new HashMap<>();
    }

    public Memorizer(String dirPath) {
        memoCount = 0;
        this.dirPath = dirPath;
        this.dir = new File(dirPath);
        isPathValid = dir.isDirectory();
        // Handle finding review.json
        // If not found, create one
        findReview();
        // Initiate a new toBeStored wordList
        toBeStored = new WordList();
        mistakeList = new WordList();
        totalCount = findTotalCount();
    }

    public int findTotalCount() {
        File countFile = new File(dirPath, "count.txt");
        int ret = 0;
        try {
            if (countFile.createNewFile()) {
                writeTotalCount(0);
            } else {
                Scanner in = new Scanner(countFile);
                if (in.hasNextInt()) {
                    ret = in.nextInt();
                } else {
                    in.close();
                    countFile.delete();
                    countFile.createNewFile();
                    writeTotalCount(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public void writeTotalCount(int count) {
        try {
            FileWriter countF = new FileWriter(dirPath + "\\count.txt");
            countF.write("" + count);
            countF.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isValid() {
        return isPathValid;
    }

    // Add to mistake list
    public void addToMis(String word, String meaning) {
        mistakeList.list.put(word, meaning);
    }

    // Add mistake list to review list
    public void addMisToReview() {
        if (toBeReviewed.list.size() >= mistakeList.list.size()) {
            toBeReviewed.list.putAll(mistakeList.list);
        } else {
            mistakeList.list.putAll(toBeReviewed.list);
            toBeReviewed = mistakeList;
        }
        mistakeList = new WordList();
    }
    // Return memoCount to the caller
    public int getMemoCount() {
        return memoCount;
    }

    // Increment the memo count,
    // so user can get reward after
    // finishing a lot of memorizing
    public void incrementMemoCount() {
        memoCount++;
    }


    // Helper method for creating file
    // and return the file back to the caller
    private File CreateFile(String fileName) {
        File newFile = new File(dirPath, fileName);
        try {
            if (newFile.createNewFile()) {
                System.out.println("New word list created successfully: " + fileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newFile;
    }

    // Initialize a container for storage if
    // given name is different, and we need to
    // serialize the previous container
    public void initStorage(String fileName) {
        if (!toBeStored.name.equals(fileName)) {
            serialize(toBeStored);
            toBeStored = new WordList();
            toBeStored.name = fileName;
        }
    }

    // Store a pair into current storage
    public void store(String word, String meaning) {
        toBeStored.list.put(word, meaning);
    }

    // Helper method for finding reviewFile
    private void findReview() {
        String[] wordLists = dir.list();
        for (String fileName: wordLists) {
            if (fileName.equals("review.json")) {
               toBeReviewed = deserialize(fileName);
                return;
            }
        }
        // If reviewFile does not exist we need to
        // Create one
        toBeReviewed = new WordList();
        toBeReviewed.name = "review";
        System.out.println("First time using memorizer");
        System.out.println("Creating review.json...");
        System.out.println("This means you should not create any file that");
        System.out.println("is named the same as review.json");
        serialize(toBeReviewed);
    }
    // Delete the specified file
    public void deleteList(String file) {
        String[] wordLists = dir.list();
        for (String fileName: wordLists) {
            if (fileName.equals(file + "json")) {
                File curFile = new File(dirPath, fileName);
                if (curFile.delete()) {
                    System.out.println("List deleted successfully: " + file);
                }
                return;
            }
        }
        System.out.println("File does not exist>_<");
        System.out.println();
    }

    // Remove a set of reviewed words from review list
    public void removeWords(Set<String> words) {
        for (String word: words) {
            toBeReviewed.list.remove(word);
        }
    }

    // Clear toBeReviewed list
    public void clearReviewed() {
        toBeReviewed = new WordList();
        toBeReviewed.name = "review";
    }

    // Add the specified list to review
    // in addition to the words in the toBeReviewed
    // file
    public void addToReview(String file) {
        String[] wordLists = dir.list();
        for (String fileName: wordLists) {
            if (fileName.equalsIgnoreCase(file + ".json")) {
                toBeReviewed.list.putAll(deserialize(fileName).list);
                System.out.println("Successfully added " + fileName.substring(0, fileName.length() - 5)
                        + " to the review list");
                return;
            }
        }
        if (file.equals(toBeStored.name)) {
            toBeReviewed.list.putAll(toBeStored.list);
            System.out.println("Successfully added " + toBeStored.name
                    + " to the review list");
            return;
        }
        System.out.println("File does not exist>_<");
        System.out.println();
    }

    // Add all word files to review
    public void addAllToReview() {
        // First clear the review list
        clearReviewed();
        String[] wordLists = dir.list();
        for (String fileName: wordLists) {
            if (!fileName.equals("review.json") && fileName.substring(fileName.length() - 5).equals(".json")) {
                toBeReviewed.list.putAll(deserialize(fileName).list);
                System.out.println("Successfully added --" + fileName.substring(0, fileName.length() - 5)
                        + "-- to the review list");
                System.out.println();
            }
        }
        if (!toBeStored.name.equals("")) {
            toBeReviewed.list.putAll(toBeStored.list);
            System.out.println("Successfully added --" + toBeStored.name
                    + "-- to the review list");
            System.out.println();
        }
    }



    // Deserialize an WordList
    // file must be a valid file!
    // If the file does not exist, return a new empty
    // WordList
    private WordList deserialize(String file) {
        File reviewFile = new File(dirPath, file);
        if (!reviewFile.exists()) {
            System.out.println("File is not valid");
            return new WordList();
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
        return gson.fromJson(jsonString.toString(),WordList.class);
    }

    // Serialize an WordList
    // It would overwrite the preexisting content
    // in the file if that file has been created
    private void serialize(WordList toBeSerialized) {
        if (toBeSerialized.name.equals("")) {
            return;
        }
        File reviewFile = CreateFile(toBeSerialized.name + ".json");
        try {
            FileWriter reviewWriter = new FileWriter(reviewFile.getAbsolutePath());
            Gson gson = new Gson();
            String jsonString = gson.toJson(toBeSerialized);
            reviewWriter.write(jsonString);
            reviewWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // User ready to quit the app
    // We need to store necessary information
    // in the file
    public void quit() {
        if (!toBeStored.name.equals("")) {
            serialize(toBeStored);
            // We want to add the last list that we have stored
            // to our review list
            toBeReviewed.list.putAll(toBeStored.list);
        }
        // We also want to add mistake list to review list
        // for future review
        addMisToReview();
        serialize(toBeReviewed);
        // Update totalCount
        totalCount += memoCount;
        writeTotalCount(totalCount);
    }


    // The main application
    public static void main(String[] args) {
        final String dirPath = "C:\\Users\\pguan\\Vocab-Memorizer\\Memorizer\\WordDir";
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to Vocab-Memorizer!");
        System.out.println("Is " + dirPath + " your working directory path? (Yes or No)");
        String resp = scanner.nextLine();
        while (!resp.equalsIgnoreCase("y") && !resp.equalsIgnoreCase("yes")
            && !resp.equalsIgnoreCase("n") && !resp.equalsIgnoreCase("no")) {
            System.out.println("Please speak something I understand: Y or N, Yes or No (Case-insensitive)");
            resp = scanner.nextLine();
        }
        if (!resp.equalsIgnoreCase("y") && !resp.equalsIgnoreCase("yes")) {
            System.out.println("Maybe you should go and change this path in the main to the correct " +
                    "path and then come back!");
            return;
        }
        System.out.println("Really? Emmm, let's check...");
        System.out.println("Try to cache unfinished review list...");
        Memorizer memo = new Memorizer(dirPath);
        if (!memo.isValid()) {
            System.out.println("How dare you lie to me! Go and change it to a valid directory path");
            return;
        }
        System.out.println("Alright! Looks like you are ready");
        System.out.println("Before we start, I want to mention that if hit q or Q for quiting");
        System.out.println("the activity, we will back up all of the current lists under the path you provide!");
        System.out.println("Automatic saving feature, I am so sweet right?");
        System.out.println("Make sure to wait for the program itself to end before you close the IDE...");
        System.out.println();
        while (true) {
            System.out.println("What is your instruction:");
            System.out.println("S or s: storing a list of word-meaning pairs");
            System.out.println("R or r: review previous vocabs");
            System.out.println("D or d: delete a specified list");
            System.out.println("Q or q: quit today's activity");
            System.out.println();
            resp = scanner.nextLine();
            if (resp.equalsIgnoreCase("s")) {
                storeList(memo, scanner);
            } else if (resp.equalsIgnoreCase("r")) {
                reviewList(memo, scanner);
            } else if (resp.equalsIgnoreCase("d")) {
                deleteList(memo, scanner);
            } else if (resp.equalsIgnoreCase("q")) {
                quitApp(memo);
                break;
            } else {
                System.out.println("Sorry, I don't understand your instruction");
                System.out.println("Please try again");
                System.out.println();
            }
        }
        scanner.close();
    }

    private static void deleteList(Memorizer memo, Scanner in) {
        System.out.println("What is list that you want to delete:");
        String file = in.nextLine();
        memo.deleteList(file);
        System.out.println();
    }

    private static void storeList(Memorizer memo, Scanner in) {
        int localStoreCount = 0;
        System.out.println("What is the file name for this list:");
        String listName = in.nextLine();
        memo.initStorage(listName);
        System.out.println("Now you can start recording you word-meaning pairs!");
        System.out.println("The following pattern has to be followed!");
        System.out.println("--------------Pattern Below--------------");
        System.out.println("Word: Meaning");
        System.out.println("--------------Pattern Above--------------");
        System.out.println("Don't worry about trailing and leading spaces!");
        System.out.println("Also if you ever want to stop, you can hit S or s: Stop");
        System.out.println();
        while (true) {
            System.out.println("What is next pair or s (stop):");
            String nextPair = in.nextLine();
            String[] pairs = nextPair.split(":");
            if (pairs.length != 2) {
                if (nextPair.equalsIgnoreCase("s")) {
                    break;
                } else {
                    System.out.println("I don't understand what you mean, please try again");
                }
            } else {
                memo.store(pairs[0].trim(), pairs[1].trim());
                memo.incrementMemoCount();
                localStoreCount++;
            }
        }
        System.out.println("You just tried to memorize " + localStoreCount + " words");
        System.out.println("What a huge effort! Keep up!");
        System.out.println("In total, your memoCount is " + memo.getMemoCount() + "!");
        System.out.println();
    }

    private static void reviewList(Memorizer memo, Scanner in) {
        System.out.println("Alright, in this review sector. There are several instructions you can take:");
        String resp = "";
        while (true) {
            System.out.println("C or c: clear review list");
            System.out.println("AM or am: add mistake list from current session to review list");
            System.out.println("A or a: add specified lists to the review list");
            System.out.println("AA or aa: clear review list and add all lists to the review list");
            System.out.println("S or s: start reviewing the review list!");
            System.out.println();
            resp = in.nextLine();
            if (resp.equalsIgnoreCase("c")) {
                memo.clearReviewed();
            } else if (resp.equalsIgnoreCase("a")) {
                String name = "";
                while (true) {
                    System.out.println("What is name of the list that you want to add:");
                    System.out.println("Or hit S or s: Stop current action of adding");
                    name = in.nextLine();
                    if (name.equalsIgnoreCase("s")) {
                        break;
                    } else {
                        memo.addToReview(name);
                    }
                }
            } else if (resp.equalsIgnoreCase("aa")) {
                memo.addAllToReview();
            } else if (resp.equalsIgnoreCase("am")) {
                memo.addMisToReview();
            }else if (resp.equalsIgnoreCase("s")) {
                break;
            } else {
                System.out.println("Sorry, I don't understand your instruction");
                System.out.println("Please try again");
                System.out.println();
            }
        }
        reviewStart(memo, in);
    }

    // Helper method specifically for managing reviewing activity
    private static void reviewStart(Memorizer memo, Scanner in) {
        int localReviewCount = 0;
        System.out.println("Ok, let's get review rolling~~~~~");
        System.out.println("Getting all the words shuffled...");
        System.out.println();
        Set<String> reviewedWords = new HashSet<>();
        List<String> words = new ArrayList<>(memo.toBeReviewed.list.keySet());
        Collections.shuffle(words);
        String resp = "";
        String ans = "";
        while (localReviewCount < words.size()) {
            String word = words.get(localReviewCount);
            String meaning = memo.toBeReviewed.list.get(word);
            System.out.println("(Y or N) Can you remember what does -- "
                    + word + " -- mean?");
            resp = in.nextLine();
            while (!resp.equalsIgnoreCase("n") && !resp.equalsIgnoreCase("y")) {
                System.out.println("Y or N, case-insensitive");
                resp = in.nextLine();
            }
            if (resp.equalsIgnoreCase("n")) {
                memo.addToMis(word, meaning);
            }
            List<String> options = pickMeaning(memo, meaning, words);
            int correctIndex = options.indexOf(meaning);
            String correctAns = "";
            switch (correctIndex) {
                case 0:
                    correctAns = "A";
                    break;
                case 1:
                    correctAns = "B";
                    break;
                case 2:
                    correctAns = "C";
                    break;
                case 3:
                    correctAns = "D";
                    break;
            }
            System.out.println("What is the meaning of the word: " + word);
            System.out.println("A: " + options.get(0));
            System.out.println("B: " + options.get(1));
            System.out.println("C: " + options.get(2));
            System.out.println("D: " + options.get(3));
            System.out.println("E: Stop and I need to rest");
            ans = in.nextLine().toUpperCase();
            while (!ans.equals("A") && !ans.equals("B")
             && !ans.equals("C") && !ans.equals("D")
            && !ans.equals("E")) {
                System.out.println("Can you just pick A B C D or E!!!!");
                ans = in.nextLine();
            }

            if (ans.equals("E")) {
                System.out.println("Ok, go have a rest...");
                break;
            } else if (ans.equals(correctAns)) {
                System.out.println("Yeah, you got it right :)");
            } else {
                System.out.println("Sad, you got it wrong :(");
                memo.addToMis(word, meaning);
            }
            System.out.println(correctAns + " is the correct option.");
            System.out.println();
            System.out.println("The meaning of -- " + word + " -- is:");
            System.out.println("    " + meaning);
            reviewedWords.add(word);
            localReviewCount++;
            memo.incrementMemoCount();
        }
        System.out.println("Managing review and mistake list...");
        memo.removeWords(reviewedWords);
        System.out.println("You reviewed " + localReviewCount + " words");
        if (reviewedWords.size() == words.size()) {
            System.out.println("Wow, you finished reviewing all the words in the review list!");
        }
        System.out.println("What a huge effort! Keep up!");
        System.out.println("In total, your memoCount is " + memo.getMemoCount() + "!");
        System.out.println();
    }

    // Help to pick a meaning if there are over 4 words; otherwise
    // the meaning can be repeated
    public static List<String> pickMeaning(Memorizer memo, String meaning, List<String> words) {
        int length = words.size();
        List<String> result = new ArrayList<>();
        result.add(meaning);
        Random rand = new Random();
        if (length < 4) {
            for (int i = 0; i < 4; i++) {
                int nextLuck = rand.nextInt(length);
                // Add a random meaning to confuse the user
                result.add(memo.toBeReviewed.list.get(words.get(nextLuck)));
            }
        } else {
            for (int i = 0; i < 3; i++) {
                int nextLuck = rand.nextInt(length);
                String pickedMeaning = memo.toBeReviewed.list.get(words.get(nextLuck));
                while (result.contains(pickedMeaning)) {
                    nextLuck = rand.nextInt(length);
                    pickedMeaning = memo.toBeReviewed.list.get(words.get(nextLuck));
                }
                // Add a random meaning to confuse the user
                result.add(pickedMeaning);
            }
        }
        Collections.shuffle(result);
        return result;
    }

    // Quit app by calling memo.quit to store necessary information
    private static void quitApp(Memorizer memo) {
        System.out.println("Storing necessary information...(Please don't force quit this application)");
        memo.quit();
        System.out.println("During this period, your total memo count is: " + memo.getMemoCount() + "!");
        System.out.println("You are gonna do fine in that test, trust me ^_^");
        System.out.println("Enjoy the rest of your day!");
        System.out.println();
    }
}
