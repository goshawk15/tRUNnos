import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

        private static final Pattern WAYPOINT_PATTERN = Pattern.compile("<wpt\\s+lat=\"([^\"]*)\"\\s+lon=\"([^\"]*)\">\\s*<ele>([^<]*)</ele>\\s*<time>([^<]*)</time>\\s*</wpt>");

        public Parser() {
        }

        public List<Waypoint> parse(String input) {
            List<Waypoint> waypoints = new ArrayList<>();
            Matcher matcher = WAYPOINT_PATTERN.matcher(input);
            while (matcher.find()) {
                double lat = Double.parseDouble(matcher.group(1));
                double lon = Double.parseDouble(matcher.group(2));
                double ele = Double.parseDouble(matcher.group(3));
                String time = matcher.group(4);
                waypoints.add(new Waypoint(lat, lon, ele, time));
            }
            return waypoints;
        }
    }

class Waypoint{
    double lat;
    double lon;
    double ele;
    String time;

    public Waypoint(double lat, double lon, double ele, String time) {
        this.lat = lat;
        this.lon = lon;
        this.ele = ele;
        this.time = time;
    }

}
