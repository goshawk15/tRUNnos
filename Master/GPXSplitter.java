import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GPXSplitter implements Serializable {
    private static int n_chunks;
    private String content;
    private File gpxFile;

    GPXSplitter(String f, int n) throws IOException {
        content = f;
        n_chunks = n;
        File tempfile = File.createTempFile("receivedfile",".gpx");
        tempfile.deleteOnExit();
        FileWriter fileWriter = new FileWriter(tempfile);
        fileWriter.write(content);
        fileWriter.close();
        this.gpxFile = tempfile;
    }

    public String getCreator() throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(gpxFile)))
        {
            br.readLine();
            String line = br.readLine();
            line = line.substring(28, line.length() - 2);

            return line;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> readLines() throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(gpxFile))) {
            String line;
            br.readLine();
            br.readLine();
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            lines.remove(lines.size() - 1);
        }
        return lines;
    }

    private List<String> create_waypoints(List<String> lines) throws IOException {
        StringBuilder line = new StringBuilder();
        List<String> waypoints = new ArrayList<>();

        for (String s : lines) {
            if (line.toString().equals("")) {
                line.append(s);
            } else
                line.append('\n').append(s);

            if (s.equals("</wpt>")) {
                waypoints.add(line.toString());
                line = new StringBuilder();
            }
        }
        return waypoints;
    }

    private List<String> create_chunks(List<String> waypoints) throws IOException {
        List<String> chunks = new ArrayList<>();
        StringBuilder element = new StringBuilder();
        int counter = 0;
        String temp = "";

        for (int i = 0; i < n_chunks; i++) {
            element.append(waypoints.get(i)).append('\n');
            temp = waypoints.get(i);
        }

        chunks.add(element.toString());
        element = new StringBuilder(temp);

        for (String waypoint : waypoints.subList(n_chunks, waypoints.size())) {
            if (counter < n_chunks - 1) {
                element.append(waypoint).append('\n');
                counter = counter + 1;
                temp = waypoint;
            }
            else {
                chunks.add(element.toString());
                element = new StringBuilder(temp);
                counter = 0;
            }
            if(counter == 0){
                element.append(waypoint).append('\n');
                counter += 1;
            }
        }
        //add any remaining waypoints
        chunks.add(element.toString());

        return chunks;
    }

        public List<String> run() throws IOException {
            List<String> lines = readLines();
            List<String> waypoints = create_waypoints(lines);
            List<String> chunks = create_chunks(waypoints);
            return chunks;
    }
}