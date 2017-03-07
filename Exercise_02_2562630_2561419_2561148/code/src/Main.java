import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.*;

public class
Main {

    public static final int MAX_PLOT = 50;

    /* args[]: inputPath */
    public static void main(String[] args) throws Exception {
        Tokenizer tokenizer = new Tokenizer();

        File inputF = new File(args[0]);
        File[] files;
        if (inputF.isDirectory()) {
            ArrayList<File> arr = new ArrayList<>();
            for (File f : inputF.listFiles()) {
                if (f.isFile()) {
                    arr.add(f);
                }
            }
            files = arr.toArray(new File[0]);
        } else {
            files = new File[]{inputF};
        }
        Document doc = Document.fromFile(files);
        tokenizer.process(doc);

        Map<String, Integer> ofCount = new HashMap<>();
        Map<String, Integer> theCount = new HashMap<>();
        int numOf = 0, numThe = 0;
        for (int i = 0; i < doc.getTokenCount() - 1; ++i) {
            String a = doc.getToken(i), b = doc.getToken(i + 1);
            if (a.equals("of")) {
                Integer c = ofCount.get(b);
                ofCount.put(b, c == null ? 1 : c + 1);
                ++numOf;
            } else if (a.equals("the")) {
                Integer c = theCount.get(b);
                theCount.put(b, c == null ? 1 : c + 1);
                ++numThe;
            }
        }

        Comparator<Map.Entry<String, Integer>> comparator = new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        };
        ArrayList<Map.Entry<String, Integer>> ofArr = new ArrayList<>();
        ofArr.addAll(ofCount.entrySet());
        Collections.sort(ofArr, comparator);

        ArrayList<Map.Entry<String, Integer>> theArr = new ArrayList<>();
        theArr.addAll(theCount.entrySet());
        Collections.sort(theArr, comparator);

        double entropyOf = 0, entropyThe = 0;

        PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File("ofProb.txt")), Charset.forName("UTF-8")));
        for (Map.Entry<String, Integer> e : ofArr) {
            double freq = (double) e.getValue() / numOf;
            entropyOf += -freq * Math.log(freq) / Math.log(2);
            out.printf("%d %.3f%% %s\r\n", e.getValue(), freq * 100, e.getKey());

        }
        out.close();

        out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File("theProb.txt")), Charset.forName("UTF-8")));
        for (Map.Entry<String, Integer> e : theArr) {
            double freq = (double) e.getValue() / numThe;
            entropyThe += -freq * Math.log(freq) / Math.log(2);
            out.printf("%d %.3f%% %s\r\n", e.getValue(), freq * 100, e.getKey());
        }
        out.close();

        float[] valuesOf = new float[MAX_PLOT];
        for (int i = 0; i < Math.min(50, ofArr.size()); ++i) {
            valuesOf[i] = ofArr.get(i).getValue() / (float) numOf;
        }

        float[] valuesThe = new float[MAX_PLOT];
        for (int i = 0; i < Math.min(50, theArr.size()); ++i) {
            valuesThe[i] = theArr.get(i).getValue() / (float) numThe;
        }

        Simulator.plot(valuesOf, "Distribution for P (W[i]|W[i−1] = ”of”)", true);
        Simulator.plot(valuesThe, "Distribution for P (W[i]|W[i−1] = ”the”)", false);

        System.out.printf("Entropy of Distribution for P (W[i]|W[i−1] = ”of”) = %.4f\r\n", entropyOf);
        System.out.printf("Entropy of Distribution for P (W[i]|W[i−1] = ”the”) = %.4f\r\n", entropyThe);
    }
}