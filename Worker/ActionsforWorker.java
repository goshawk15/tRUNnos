import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ActionsforWorker extends Thread {
    Pair pair;
    ObjectOutputStream out;
    int id;

    public ActionsforWorker(int i ,Pair p, ObjectOutputStream o){
        pair = p;
        id = i;
        out = o;
    }

    //Implementation of the Map function
    public Results map(int key, String value){
        //Get the waypoints from the value
        Parser parser = new Parser();
        List<Waypoint> waypoints = parser.parse(value);

        //Initialization of the results
        double R = 6371e3; // Radious of the earth
        double distance = 0;
        long time = 0;
        double elevation = 0;
        double speed;

        //Parse through the waypoints
        for (int i = 1; i < waypoints.size(); i++) {
            //Get the waypoints in pairs of 2
            Waypoint w1 = waypoints.get(i);
            Waypoint w2 = waypoints.get(i - 1);

            //Calculate distance
            double f1 = w2.lat * Math.PI/180; // φ, λ in radians
            double f2 = w1.lat * Math.PI/180;
            double df = (w1.lat-w2.lat) * Math.PI/180;
            double dl = (w1.lon-w2.lon) * Math.PI/180;

            double a = Math.sin(df/2) * Math.sin(df/2) +
                    Math.cos(f1) * Math.cos(f2) *
                            Math.sin(dl/2) * Math.sin(dl/2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

            distance += R * c; // in metres

            //Calculate time
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            LocalDateTime dateTime1 = LocalDateTime.parse(w2.time, formatter);
            LocalDateTime dateTime2 = LocalDateTime.parse(w1.time, formatter);
            Duration duration = Duration.between(dateTime1, dateTime2);

            time += duration.getSeconds();

            //Calculate elevation
            double ele = w1.ele - w2.ele;
            if (ele > 0)
                elevation += ele;
        }
        //Convert meters to km
        distance = distance/1000;

        //Calculate speed
        speed = distance / time;

        //Return the results
        return new Results(distance, time, elevation,speed);
    }

    public void run() {
        //Get the key and value from the arguments
        String value = pair.chunk;
        int key = pair.key;

        //Calculate the Results using the map function
        Results results = map(key,value);

        //Attempt to send the results back to the Server
        try {
            out.writeObject(new Pair(key,results));
            out.flush();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
