package knn;

public class Test {

    public static void main(String[] args){

        System.out.printf("Test distance mesaure meant %b \n", testDistance());


    }
    public static boolean testDistance(){
        Knn ob = new Knn("../news_data/news_articles.mtx","../news_data/news_articles.labels");
        // Test distance calculator
        Document a = new Document(1,5);
        Document b = new Document(1,5);

        a.updateFreqTableInput(0, 1);
        a.updateFreqTableInput(1, 1);
        a.updateFreqTableInput(2, 1);
        a.updateFreqTableInput(3, 1);
        a.updateFreqTableInput(4, 0);

        b.updateFreqTableInput(0, 0);
        b.updateFreqTableInput(1, 1);
        b.updateFreqTableInput(2, 1);
        b.updateFreqTableInput(3, 0);
        b.updateFreqTableInput(4, 1);

        double dist = ob.calculateCosineDistance(a, b);
        return (((double) Math.round(dist * 1000) / 1000) == 0.577);
    }
}
