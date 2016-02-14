import java.util.ArrayList;
import java.util.Random;

public class Kmeans {

    private final int NUM_CLUSTERS;  // the number of clusters
    private final int TOTAL_DATA;    // Total data points(how many pictures)
    private final int y[][];         // multiple one dimensional Y array

    private ArrayList<Data> dataSet = new ArrayList<Data>(); // store every data
    private ArrayList<Centroid> centroids = new ArrayList<Centroid>(); // store centroids

    // Constructor
    public Kmeans(int y[][], int numCluster){
    	this.TOTAL_DATA = y.length;
        this.y = y;
        NUM_CLUSTERS = numCluster;
    }

    public ArrayList<Data> start() {
        // Randomly choose centroids and add to ArrayList
        Random rand = new Random();
        for(int i = 0 ; i< NUM_CLUSTERS ; i++){
            int rm = rand.nextInt(y.length);
            double[] tmp = new double[y[0].length];
            for(int j = 0 ; j < tmp.length ; j++){ // int array to double array
                tmp[j] = y[rm][j];
            }
            centroids.add(new Centroid(tmp));
        }
        // Clustering
        return cluster();
    }

    private ArrayList<Data> cluster() {
        final double bigNumber = Math.pow(10, 10); // some big number that's
        // sure to be larger than our
        // data range.
        double minimum = bigNumber; // The minimum value to beat.
        double distance = 0.0; // The current minimum value.
        int sampleNumber = 0;
        int cluster = 0;
        boolean isStillMoving = true;
        Data newData = null;

        // Add in new data, one at a time, recalculating centroids with each new
        // one.
        while (dataSet.size() < TOTAL_DATA) {
            newData = new Data(y[sampleNumber]);
            dataSet.add(newData);
            minimum = bigNumber;
            for (int i = 0; i < NUM_CLUSTERS; i++) {
                distance = dist(newData, centroids.get(i));
                if (distance < minimum) {
                    minimum = distance;
                    cluster = i;
                }
            }
            newData.cluster(cluster);
            // calculate new centroids
            update();
            sampleNumber++;
        }

        // Now, keep shifting centroids until equilibrium occurs.
        while (isStillMoving) {
            // calculate new centroids.
            update();

            // Assign all data to the new centroids
            isStillMoving = false;

            for (int i = 0; i < dataSet.size(); i++) {
                Data tempData = dataSet.get(i);
                minimum = bigNumber;
                for (int j = 0; j < NUM_CLUSTERS; j++) {
                    distance = dist(tempData, centroids.get(j));
                    if (distance < minimum) {
                        minimum = distance;
                        cluster = j;
                    }
                }
                // tempData.cluster(cluster);
                if (tempData.cluster() != cluster) {
                    tempData.cluster(cluster);
                    isStillMoving = true;
                }
            }
        }
        return dataSet;
    }

    // Update centroids
    private void update(){
        // calculate new centroids.
        for (int i = 0; i < NUM_CLUSTERS; i++) {
            int totalInCluster = 0; // how many in a cluster
            int[] total = new int[256];
            double[] rslt = new double[256];

            // initial
            for (int j = 0; j < total.length; j++) {
                total[j] = 0;
                rslt[j] = 0;
            }

            for (int j = 0; j < dataSet.size(); j++) {
                if (dataSet.get(j).cluster() == i) { // if it belongs to
                    // cluster i
                    int[] data = dataSet.get(j).pos();
                    for (int k = 0; k < data.length; k++) {
                        total[k] += data[k];
                    }
                    totalInCluster++;
                }
            }

            if (totalInCluster > 0) {
                for (int j = 0; j < total.length; j++) {
                    rslt[j] = total[j] / (double)totalInCluster;
                }
                centroids.get(i).pos(rslt);
            }
        }

    }

    // Calculate Euclidean distance.
    private static double dist(Data d, Centroid c) {
        int[] pos_data = d.pos();
        double[] pos_centroid = c.pos();
        int sum_square = 0;
        if (pos_data.length != pos_centroid.length) {
            System.out.println("different");
            return 0;
        }
        for (int i = 0; i < pos_data.length; i++) {
            sum_square += Math.pow(pos_data[i] - pos_centroid[i], 2);
        }
        return Math.sqrt(sum_square);
    }

    public static void main(String[] args) {
        //int[][] array = new int[300][256];
        //For test
        //Kmeans t = new Kmeans(array);
        //t.kmeans();
    }
}
