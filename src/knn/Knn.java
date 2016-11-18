package knn;
import java.io.File;
import java.io.FileNotFoundException;
// Author Conor O'Kelly
import java.util.*;


public class Knn{

    public static void main(String[] args) {

        System.out.println("Starting");

        // Create document array
        Knn ob = new Knn("../news_data/news_articles.mtx","../news_data/news_articles.labels");
        Document a = ob.documentsArray[0];
        Document b = ob.documentsArray[1];
        ob.calculateCosineDistance(a, b);
    }

    // Variables
    String docPath;
    Document[] documentsArray;

    // Constructor
    public Knn(String targetDocument){
        docPath = targetDocument;
        createDocumnetsArray(docPath);
    }

    public Knn(String targetDocument, String labelsDocuments){
        docPath = targetDocument;
        createDocumnetsArray(docPath);
        this.addLables(labelsDocuments);
    }

    // Create data structure methods
    private ArrayList<String> processDocument(String documentPath){
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

    private void createDocumnetsArray(String documentName) {

        // Convert document into array list
        ArrayList<String> stringsArray = processDocument(documentName);


        // Get format of all object from line 2 of documents
        String infoFormat[] = stringsArray.get(1).split(" ");
        // System.out.println(Arrays.toString(infoFormat));

        int noDocuments = Integer.parseInt(infoFormat[0]); // no rows or number of documents
        int noColumns = Integer.parseInt(infoFormat[1]);
        int totalNoZeroValues = Integer.parseInt(infoFormat[2]);

        // Create documents array
        documentsArray = new Document[noDocuments];

        // Iterate throught arrayList and craete objects
        String currentRow[];
        int docID;
        int columndID;
        int wordFreq;
        for (int x = 2; x < stringsArray.size(); x++){
            // Split current row and find values
            currentRow = stringsArray.get(x).split(" ");
            docID = Integer.parseInt(currentRow[0]);
            columndID = Integer.parseInt(currentRow[1]);
            wordFreq = Integer.parseInt(currentRow[2]);
            // System.out.println(docID);
            // Set position of doc object in array to docID -1
            int arrayPosition = docID - 1;

            if (documentsArray[arrayPosition] == null){
                Document d = new Document(docID,noColumns);
                documentsArray[arrayPosition] = d;
            }
            Document doc = documentsArray[arrayPosition];
            doc.updateFreqTableInput(columndID, wordFreq);

        }
    }
    // General methods

    public void addLables(String labelsDoc){

        try {
            //Crate new scanner
            Scanner sc = new Scanner(new File(labelsDoc));
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                int documentID = Integer.parseInt(line.split(",")[0]);
                String label = line.split(",")[1];
                Document curr = documentsArray[documentID-1];
                curr.setLabel(label);
            }
            sc.close();
        }
        catch (FileNotFoundException e){
            System.out.println("File was not found");
            System.out.println(e);
        }
    }

    // Knn methods

    public void calculateCosineDistance(Document doc1, Document doc2){

        Hashtable<Integer, Integer> doc1Hashtable = doc1.getHashtable();
        Hashtable<Integer, Integer> doc2Hashtable = doc2.getHashtable();

        // Create new sets
        HashSet<Integer> inter = new HashSet<Integer>(doc1Hashtable.keySet());
        HashSet<Integer> set1 = new HashSet<Integer>(doc1Hashtable.keySet());
        HashSet<Integer> set2 = new HashSet<Integer>(doc2Hashtable.keySet());
        // Intersect of both set
        inter.retainAll(set2);
        // Only unique keys in set 1 and 2
        set1.removeAll(inter);
        set2.removeAll(inter);

        // Numerator of cosine - Only looking at intersection a (x * 0) = 0
        float numTot = 0;
        for (Integer columnID : inter){
            System.out.println(columnID);
        }

        // Denominaotr of the cosine

    }
    public void findNearestNeighbours(){
    }
    public void findWeightedNearestNeighbours(){

    }
    
}
