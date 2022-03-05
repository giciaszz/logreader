

import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.abs;

public class asystent {

    public static void setDir(String dir) {
        asystent.dir = dir;
    }

    private static String dir;

    public static void main(String args[]) throws IOException {


        String glob = "glob:**/logs/*.log";
        String path = "D:/";

        match(glob, path);



        File directory = new File(dir);
        File[] files = directory.listFiles();
        Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed()); //desc


        String wzor = "(\\d{4}-\\d\\d-\\d\\d\\s\\d\\d:\\d\\d:\\d\\d,\\d{3})\\s(INFO|WARN|FATAL|DEBUG|ERROR)\\s+(\\[.*\\])";
        Pattern mypat = Pattern.compile(wzor);


        for (File file : files) {

            Instant start = Instant.now(); //czas czyt pliku start
            Map<String, Integer> uniki = new HashMap<>();
            Map<String, Integer> warny = new HashMap<>();
            String txt = null;
            boolean flagaPierwLog = false;
            String pierwszyLog ="";
            String ostatniLog = "";



            try(Scanner s = new Scanner(file)){
                txt = s.useDelimiter("\\A").next();
                Matcher m = mypat.matcher(txt);

                while (m.find()) {
                    if (!flagaPierwLog) {
                        pierwszyLog = m.group(1);
                        flagaPierwLog = true;
                    }
                    ostatniLog = m.group(1);

                    wypelniacz(warny, m, 2);
                    wypelniacz(uniki, m, 3);
                }

            }
            Instant end = Instant.now();

            // zakres logow
            String start_date = pierwszyLog.replace(",",".");
            String end_date = ostatniLog.replace(",",".");

            findDifference(start_date, end_date);
            System.out.println("\nIlosc unikalnych wystapien bibliotek w logu:");
            System.out.println(uniki);

            System.out.println("\nIlosc logow pogrupowana wg severity");
            System.out.println(warny);
            System.out.printf("Czas czytania pliku %d s %d nanoseconds %n", Duration.between(start, end).getSeconds(), Duration.between(start, end).getNano());
            //stosunek logow err, fatal do nizszych
            int err = 0;
            if (warny.get("ERROR") != null) {
                err = warny.get("ERROR");
            }
            if (warny.get("FATAL") != null) {
                err += warny.get("FATAL");
            }
            float stosunek = (float)err / (float) iterValues(warny);
            System.out.println("Stosunek ilosci logow o severity ERROR lub wyzszym do wszystkich logow " + stosunek + "\n\n");


        }
    }

    public static int iterValues(Map<String, Integer> map) {
        int suma = 0;
        for (Integer value : map.values()) {
            suma = suma + value;
        }
        return suma;
    }


//treewalking
    public static void match(String glob, String location) throws IOException {

        final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(
                glob);

        Files.walkFileTree(Paths.get(location), new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                if (pathMatcher.matches(path)) {
                    setDir(path.getParent().toString());
                    return FileVisitResult.TERMINATE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc)
                    throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
    }



    public static void wypelniacz(@NotNull Map secik, @NotNull Matcher m, int grupa){
        if(secik.containsKey(m.group(grupa))){
            int ki = 1 + (int)secik.get(m.group(grupa));
            secik.put(m.group(grupa), ki);
        } else secik.put(m.group(grupa), 1);


    }



    public static void findDifference(String start_date,
                                      String end_date)
    {


        SimpleDateFormat sdf
                = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss.SSS");


        try {

            // parse method is used to parse
            // the text from a string to
            // produce the date
            Date d1 = sdf.parse(start_date);
            Date d2 = sdf.parse(end_date);


            long difference_In_Time
                    = abs(d1.getTime() - d2.getTime());

            // Calucalte time difference in
            // seconds, minutes, hours, years,
            // and days
            long difference_In_Ms
                    = difference_In_Time % 1000;

            long difference_In_Seconds
                    = (difference_In_Time
                    / 1000)
                    % 60;

            long difference_In_Minutes
                    = (difference_In_Time
                    / (1000 * 60))
                    % 60;

            long difference_In_Hours
                    = (difference_In_Time
                    / (1000 * 60 * 60))
                    % 24;

            long difference_In_Years
                    = (difference_In_Time
                    / (1000l * 60 * 60 * 24 * 365));

            long difference_In_Days
                    = (difference_In_Time
                    / (1000 * 60 * 60 * 24))
                    % 365;


            System.out.print(
                    "Zakres logow w pliku (czyli roznica czasu miedzy pierwszym logiem w pliku a ostatnim): ");

            System.out.println(
                    difference_In_Years
                            + " years, "
                            + difference_In_Days
                            + " days, "
                            + difference_In_Hours
                            + " hours, "
                            + difference_In_Minutes
                            + " minutes, "
                            + difference_In_Seconds
                            + " seconds, "
                            + difference_In_Ms
                            + " miliseconds.");
        }

        catch (ParseException e) {
            e.printStackTrace();
        }
    }



}
