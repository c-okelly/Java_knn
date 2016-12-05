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
        Document testDoc = ob.documentsArray[1];
        // ob.documentsArray[10] = null;
        long startTime = System.currentTimeMillis();

        ob.measureAccuracy(ob.documentsArray);
        // String nearest = ob.findNearestNeighbours(testDoc, ob.documentsArray, 10);
        // System.out.println("Final -" + nearest);
        System.out.print((System.currentTimeMillis() - startTime) / 1000d + " s");
    }

    // Inner class to store measuremet of doc distances
    class MeasuredDoc implements Comparable<MeasuredDoc>{
        Document doc = null;
        Double distance = -1.0;

        MeasuredDoc(Document inputDoc,Double dis){
            doc = inputDoc;
            distance = dis;
        }
        public String toString(){
            if (doc != null){
                return "Document id " + doc.getID() + " distance " + distance;
            } else {
                return "Document missing";
            }

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

        // Calculate vector norms for each documents
        for (Document doc : documentsArray){
            doc.preCalcVectorNorms();
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
            return 0;
        }
        Hashtable<Integer, Integer> doc1Hashtable = doc1.getHashtable();
        Hashtable<Integer, Integer> doc2Hashtable = doc2.getHashtable();

        // Denominaotr of the cosine
        // double d1Tot = 0;
        // double d2Tot = 0;

        // double preSqrtD1 = 0;
        // double preSqrtD2 = 0;

        // Numerator of cosine - Only looking at intersection a (x * 0) = 0
        float numTot = 0;
        for (int colmID : doc1Hashtable.keySet()){
            int a,b;
            if (doc2Hashtable.containsKey(colmID)){
                    a = doc1Hashtable.get(colmID);
                    b = doc2Hashtable.get(colmID);
                    numTot += (a*b);
                }
        }



        // for (Integer value : doc1Hashtable.values()) {
        //     preSqrtD1 += (Math.pow(value,2));
        // }
        // for (int value : doc2Hashtable.values()) {
            // preSqrtD2 += (Math.pow(value,2));
        // }

        // d1Tot = Math.sqrt(preSqrtD1);
        // d2Tot = Math.sqrt(preSqrtD2);

        double domTotal = doc1.vectorNorm * doc2.vectorNorm;
        double distance = numTot / domTotal;

        return distance;
    }

    public String findNearestNeighbours(Document testDoc, Document[] trainingDocs, Integer noNeighbours ){

        MeasuredDoc[] closeNeigh = measureDistance(testDoc, trainingDocs, noNeighbours);
        // Return 10 

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
        // Predict class by finding neigbhours with highest coutn
        String predictedClass = "None";
        int highestNo = 0;
        for (String key: classCount.keySet()){
            if (highestNo == 0 || highestNo < classCount.get(key)){
                predictedClass = key;
                highestNo = classCount.get(key);
            }
        }
        // System.out.println(classCount.toString());
        // Check for tie - if exisits run againt and look for one less neighbour
        int maxValueCount =0;
        for (String key: classCount.keySet()){
            if (classCount.get(key) == highestNo){
                maxValueCount += 1;
            }
        }
        // System.out.println(maxValueCount + predictedClass);
        if (maxValueCount > 1 && noNeighbours > 1){
            predictedClass = findNearestNeighbours(testDoc, trainingDocs, noNeighbours-1);
        }

        // System.out.println(classCount);
        return predictedClass;

    }

    public String findWeightedNearestNeighbours(Document testDoc, Document[] trainingDocs, Integer noNeighbours ){

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

        // Predict class by finding neigbhours with highest coutn
        String predictedClass = "None";
        double highestNo = 0;
        for (String key: classCount.keySet()){
            if (highestNo == 0 || highestNo < classCount.get(key)){
                predictedClass = key;
                highestNo = classCount.get(key);
            }
        }

        return predictedClass;


    }
    private MeasuredDoc[] measureDistance(Document testDoc, Document[] trainingDocs, Integer noNeighbours){

        // Interate through docs in given strucutre and store reults in MeasuredDoc object
        MeasuredDoc[] nearestNeigh = new MeasuredDoc[documentsArray.length];

        int i =0;
        MeasuredDoc mes;
        for (Document trainedDoc : trainingDocs){
            double dist = calculateCosineDistance(testDoc, trainedDoc);
            // System.out.printf("Testing dis agaisnt %d distance = %f \n", i, dist);

            mes = new MeasuredDoc(trainedDoc, dist);
            nearestNeigh[i] = mes;
            i++;
        }

        // Create sorted array of documents that are nearest.
        Arrays.sort(nearestNeigh,Collections.reverseOrder());
        // System.out.println(Arrays.toString(nearestNeigh));

        return Arrays.copyOfRange(nearestNeigh,0,noNeighbours);
    }

    public void measureAccuracy(Document[] testSet){
        // Implementation of leave one out cross validation for weighted.

        // Unweighted cross validation
        System.out.println("\n Accuracy testing for unweighted Knn using leave one out cross validation for k 1 - 10");
        for (int k =1;k<=10;k++){
            Document testDoc = null;

            int correctCount = 0;
            int incorectCount = 0;

            for (int y=0; y < testSet.length; y++){
                testDoc = testSet[y];
                // Remove test doc from set
                testSet[y] = null;

                String predClass = findNearestNeighbours(testDoc, testSet, k);
                // System.out.println(testDoc);
                // System.out.println(predClass);
                // if (predClass == null){
                //     nullC +=1;
                // }
               if (predClass.equals(testDoc.getLabel())){
                    correctCount += 1;
                } else {
                    incorectCount += 1;
               }
               // Add document back into testSet
               testSet[y] = testDoc;
            }

            double total = correctCount + incorectCount;
            double perCorrect = (correctCount / (double)total) * 100;

            System.out.printf("Using %d neighbor(s) accuracy level is  %f %% \n", k, perCorrect);
        }

        // Weighted cross validation
        System.out.println("\n Accuracy testing for weighted Knn using leave one out cross validation for k 1 - 10");
        for (int k =1;k<=10;k++){
            Document testDoc = null;

            int correctCount = 0;
            int incorectCount = 0;

            for (int y=0; y < testSet.length; y++){
                testDoc = testSet[y];
                // Remove test doc from set
                testSet[y] = null;

                String predClass = findWeightedNearestNeighbours(testDoc, testSet, k);
                // System.out.println(testDoc);
                // System.out.println(predClass);
                // if (predClass == null){
                //     nullC +=1;
                // }
               if (predClass.equals(testDoc.getLabel())){
                    correctCount += 1;
                } else {
                    incorectCount += 1;
               }
               // Add document back into testSet
               testSet[y] = testDoc;
            }

            double total = correctCount + incorectCount;
            double perCorrect = (correctCount / (double)total) * 100;

            System.out.printf("Using %d neighbor(s) accuracy level is  %f %% \n", k, perCorrect);
        }
    }
}
