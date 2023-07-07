import java.io.Serializable;

//Data structure that represents a pair
class Pair implements Serializable {
    int key;
    Results value;
    String chunk;
    int size;

    Pair(int i, Results s) {
        key = i;
        value = s;
    }

    Pair(int i, String v) {
        key = i;
        chunk = v;
    }
    Pair(int i, int s) {
        key = i;
        size = s;
    }

}

//Data structure that represents a Results object
class Results implements Serializable {
    double distance;
    double time;
    double elevation;
    double avgspeed;

    Results(double d, double t, double e, double s) {
        distance = d;
        time = t;
        elevation = e;
        avgspeed = s;
    }
    public void printResults() {
        System.out.println(String.format("%.1f",distance)+"m | "+time+"s | "+String.format("%.3f",elevation)+" | "+String.format("%.2f",avgspeed)+"m/s");
    }
}