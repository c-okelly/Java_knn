package knn;
// Author Conor O'Kelly
import java.util.Hashtable;

public class Document {

    public static void main(String[] args){
        Document d = new Document(0,20);

        d.updateFreqTableInput(2, 10);
        d.updateFreqTableInput(2, 15);
        System.out.println(d.getFreqOfColumn(5));
    }

    // Varailbes
    int docID;
    int noColumns;
    String label = null;
    private Hashtable<Integer, Integer> freqTables = new Hashtable<Integer, Integer>();
    public double vectorNorm = 0;

    // Constructor
    public Document(int documentNumber, int numberOfColumns){
        docID = documentNumber;
        noColumns = numberOfColumns;
    }
    // Getter and setter methods

    public int getID(){
        return docID;
    }
    public void setLabel(String newLabel){
        label = newLabel;
    }
    public String getLabel(){
        return label;
    }
    public void updateFreqTableInput(int columnID, int frequency){
        freqTables.put(columnID, frequency);
    }
    public int getFreqOfColumn(int columnID){
        // Attempt to get freq. Return -1 if not found.
        int freq;
        if (freqTables.containsKey(columnID)) {
            freq = freqTables.get(columnID);
        } else {
            freq = 0;
        }

        return freq;
    }
    public Hashtable<Integer, Integer> getHashtable(){
        return freqTables;
    }

    public String toString(){
        return "Document object of id "+docID+ " and class label "+ label;
    }
    public void preCalcVectorNorms(){
        double preSqrt = 0;
        for (Integer value : freqTables.values()) {
            preSqrt += (Math.pow(value,2));
        }
        vectorNorm = Math.sqrt(preSqrt);
    }
}
