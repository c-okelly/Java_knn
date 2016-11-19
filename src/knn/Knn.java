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
        // Test distance calculator
        Document testDoc = ob.documentsArray[150];
        ob.documentsArray[10] = null;
        long startTime = System.currentTimeMillis();

        Map nearest = ob.findWeightedNearestNeighbours(testDoc, ob.documentsArray, 10);
        System.out.println(nearest);

        System.out.print((System.currentTimeMillis() - startTime) / 1000d + " s");
    }

    // Inner class to store measuremet of doc distances
    class MeasuredDoc implements Comparable<MeasuredDoc>{
        Document doc;
        Double distance;

        MeasuredDoc(Document inputDoc,Double dis){
            doc = inputDoc;
            distance = dis;
        }
        public String toString(){
            return "Document id " + doc.getID() + " distance " + distance;
        }
        @Override
        public int compareTo(MeasuredDoc o){
            return distance.compareTo(o.distance);
        }
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

    public double calculateCosineDistance(Document doc1, Document doc2){


        // If one of docuemnts is null return 0
        if (doc1==null || doc2==null){
            return -1;
        }
        Hashtable<Integer, Integer> doc1Hashtable = doc1.getHashtable();
        Hashtable<Integer, Integer> doc2Hashtable = doc2.getHashtable();

        // Create new sets
        HashSet<Integer> inter = new HashSet<Integer>(doc1Hashtable.keySet());
        HashSet<Integer> set1 = new HashSet<Integer>(doc1Hashtable.keySet());
        HashSet<Integer> set2 = new HashSet<Integer>(doc2Hashtable.keySet());
        // Intersect of both set
        inter.retainAll(set2);

        // Numerator of cosine - Only looking at intersection a (x * 0) = 0
        float numTot = 0;
        for (Integer colmID : inter){
            numTot += (doc1.getFreqOfColumn(colmID)* doc2.getFreqOfColumn(colmID));
        }

        // Denominaotr of the cosine
        double d1Tot = 0;
        double d2Tot = 0;

        double preSqrtD1 = 0;
        double preSqrtD2 = 0;

        //
        for (Integer colmID : set1) {
            int itemFreq = doc1.getFreqOfColumn(colmID);
            preSqrtD1 += (itemFreq * itemFreq);
        }
        for (Integer colmID : set2) {
            int itemFreq = doc2.getFreqOfColumn(colmID);
            preSqrtD2 += (itemFreq * itemFreq);
        }

        d1Tot = Math.sqrt(preSqrtD1);
        d2Tot = Math.sqrt(preSqrtD2);

        double domTotal = d1Tot * d2Tot;
        double distance = numTot / domTotal;

        return distance;
    }

    public Map<String,Integer> findNearestNeighbours(Document testDoc, Document[] trainingDocs, Integer noNeighbours ){

        MeasuredDoc[] closeNeigh = measureDistance(testDoc, trainingDocs, noNeighbours);

        // Calculate class based on neighbours - flexible for multi class soltuion

        Map<String,Integer> classCount = new HashMap<String,Integer>();

        for (int i =0; i <closeNeigh.length; i++) {
            // System.out.println(closeNeigh[i].doc.getLabel());
            if (classCount.get(closeNeigh[i].doc.getLabel())== null){
                classCount.put(closeNeigh[i].doc.getLabel(),1);
            }else{
                classCount.put(closeNeigh[i].doc.getLabel(),classCount.get(closeNeigh[i].doc.getLabel())+1);
            }
        }

        return classCount;

    }

    public Map<String,Double> findWeightedNearestNeighbours(Document testDoc, Document[] trainingDocs, Integer noNeighbours ){

        MeasuredDoc[] closeNeigh = measureDistance(testDoc, trainingDocs, noNeighbours);

        // Calculate class based on neighbours - flexible for multi class soltuion

        Map<String,Double> classCount = new HashMap<String,Double>();

        for (int i =0; i <closeNeigh.length; i++) {
            // System.out.println(closeNeigh[i].doc.getLabel());
            if (classCount.get(closeNeigh[i].doc.getLabel())== null){
                classCount.put(closeNeigh[i].doc.getLabel(),(1/closeNeigh[i].distance));
            }else{
                classCount.put(closeNeigh[i].doc.getLabel(),classCount.get(closeNeigh[i].doc.getLabel())+(1/closeNeigh[i].distance));
            }
        }

        return classCount;


    }
    private MeasuredDoc[] measureDistance(Document testDoc, Document[] trainingDocs, Integer noNeighbours){
        // Interate through docs in given strucutre and store reults in MeasuredDoc object
        MeasuredDoc[] nearestNeigh = new MeasuredDoc[documentsArray.length];

        int i =0;
        for (Document trainedDoc : trainingDocs){
            double dist = calculateCosineDistance(testDoc, trainedDoc);
            // System.out.printf("Testing dis agaisnt %d distance = %f \n", i, dist);

            MeasuredDoc mes = new MeasuredDoc(trainedDoc, dist);
            nearestNeigh[i] = mes;
            i++;
        }
        // Create sorted array of documents that are nearest.
        Arrays.sort(nearestNeigh,Collections.reverseOrder());
        // System.out.println(Arrays.toString(nearestNeigh));

        return Arrays.copyOfRange(nearestNeigh,0,noNeighbours);
    }

    public void measureAccuracy(){
        
    }
}
