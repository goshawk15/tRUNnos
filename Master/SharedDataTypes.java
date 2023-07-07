import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
//Custom data structure to store the statistics of the users
class user_stats implements Serializable{
    String user;

    List<Pair> routes;
    int total_routes;

    double avg_time;
    double total_time;

    double avg_distance;
    double total_distance;

    double avg_ele;
    double total_ele;

    public user_stats(String name){
        user = name;
        total_routes = 0;
        avg_time = 0;
        total_time = 0;
        avg_distance = 0;
        total_distance = 0;
        avg_ele = 0;
        total_ele = 0;
        routes = new ArrayList<>();
    }
    public void updateStatistics(){
        avg_time = total_time/total_routes;
        avg_ele = total_ele/total_routes;
        avg_distance = total_distance/total_routes;
    }
    public void addResults(Results results){
        total_time += results.time;
        total_distance += results.distance;
        total_ele += results.elevation;
        total_routes++;

        Pair pair = new Pair(total_routes,results);
        routes.add(pair);

        updateStatistics();
    }


}

//Custom data structure to store the global statistics
class global_stats implements Serializable{
    double avg_distance;
    double total_distance;

    double avg_time;
    double total_time;

    double avg_elevation;
    double total_elevation;

    int total_routes;
    public global_stats(){
        total_routes = 0;
        total_distance = 0;
        avg_distance = 0;
        total_time = 0;
        avg_time = 0;
        total_elevation = 0;
        avg_elevation = 0;
    }
        public void updateStatistics(Results new_result){
            total_routes++;
            total_elevation += new_result.elevation;
            total_time += new_result.time;
            total_distance += new_result.distance;

            avg_elevation = total_elevation / total_routes;
            avg_time = total_time / total_routes;
            avg_distance = total_distance / total_routes;
        }


}