package knn;
import java.io.File;
import java.io.FileNotFoundException;
// Author Conor O'Kelly
import java.util.*;


public class Knn{

    public static void main(String[] args) {

        System.out.println("Starting");

        // Create string list from file
        ArrayList<String> inputDocument = processDocument("../news_data/news_articles.mtx");

        // Create document array
        createDocumnetsArray(inputDocument);
    }

    // Variables
    

    // Constructor

    public static ArrayList<String> processDocument(String documentPath){
        ArrayList<String> stringArrayList = new ArrayList<String>();

        try {
            //Crate new scanner
            Scanner sc = new Scanner(new File(documentPath));
            while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    stringArrayList.add(line);
                }
            sc.close();
        }
        catch (FileNotFoundException e){
            System.out.println("File was not found");
            System.out.println(e);
        }

        // System.out.println(stringArrayList .get(1));

        return stringArrayList;
    }

    public static Document[] createDocumnetsArray(ArrayList<String> stringsArray) {

        // Get format of all object from line 2 of documents
        String infoFormat[] = stringsArray.get(1).split(" ");
        // System.out.println(Arrays.toString(infoFormat));

        int noDocuments = Integer.parseInt(infoFormat[0]); // no rows or number of documents
        int noColumns = Integer.parseInt(infoFormat[1]);
        int totalNoZeroValues = Integer.parseInt(infoFormat[2]);

        // Create documents array
        Document[] documentsArray = new Document[noDocuments];

        // Iterate throught arrayList and craete objects
        String currentRow[];
        int docID;
        int columndID;
        int wordFreq;
        for (int x = 2; x < 200; x++){
            // Split current row and find values
            currentRow = stringsArray.get(x).split(" ");
            docID = Integer.parseInt(currentRow[0]);
            columndID = Integer.parseInt(currentRow[1]);
            wordFreq = Integer.parseInt(currentRow[2]);
            // Set position of doc object in array to docID -1
            int arrayPosition = docID - 1;

            if (documentsArray[arrayPosition] == null){
                Document d = new Document(docID,noColumns);
                documentsArray[arrayPosition] = d;
            }
            Document doc = documentsArray[arrayPosition];
            doc.updateFreqTableInput(columndID, wordFreq);

        }

        System.out.println(documentsArray[1]);
        return documentsArray;
    }
}
