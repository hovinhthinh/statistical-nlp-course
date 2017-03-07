import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Document {

    private static final double LIDSTONE_ALPHA = 0.1f;

    private String originalContent;
    private ArrayList<String> tokens;

    private Map<String, Integer> tokenFrequency = null;
    private Map<String, Double> tokenProbability = null;

    public Document(String originalContent) {
        this.originalContent = originalContent;
        this.tokens = new ArrayList<>();
    }

    public static Document fromFile(File... files) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        for (File f : files) {
            try {
                FileInputStream input = new FileInputStream(f);

                byte[] buffer = new byte[1024];
                int c = 0;
                while ((c = input.read(buffer)) > 0) {
                    stream.write(buffer, 0, c);
                }
                stream.write("\r\n".getBytes());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return new Document(new String(stream.toByteArray(), Charset.forName("UTF-8")));
    }

    public String getOriginalContent() {
        return originalContent;
    }

    /* Get dictionary of token and frequency */
    public Map<String, Integer> getTokenFrequency() {
        if (tokenFrequency != null) {
            return tokenFrequency;
        }

        tokenFrequency = new HashMap<>();
        for (String token : tokens) {
            Integer c = tokenFrequency.get(token);
            tokenFrequency.put(token, c == null ? 1 : c + 1);
        }
        return tokenFrequency;
    }

    /* Get dictionary of token and probability */
    public Map<String, Double> getTokenProbability() {
        if (tokenProbability != null) {
            return tokenProbability;
        }

        Map<String, Integer> tokenFreq = getTokenFrequency();
        tokenProbability = new HashMap<>();
        for (Map.Entry<String, Integer> e : tokenFreq.entrySet()) {
            tokenProbability.put(e.getKey(), (double) e.getValue() / tokens.size());
        }
        return tokenProbability;
    }

    /* Get the lidstone smoothed probability for a token */
    public double getSmoothedTokenProbability(String word) {
        Map<String, Integer> tokenFreq = getTokenFrequency();

        Integer freq = tokenFreq.containsKey(word) ? tokenFreq.get(word) : 0;

        return (freq + LIDSTONE_ALPHA) / (tokens.size() + LIDSTONE_ALPHA * tokenFreq.size());
    }

    /* Get entropy of the document */
    public double getEntropy() {
        double result = 0;
        for (Map.Entry<String, Double> e : getTokenProbability().entrySet()) {
            result -= e.getValue() * Math.log(e.getValue()) / Math.log(2);
        }
        return result;
    }

    public void addToken(String token) {
        tokens.add(token.toLowerCase());
        tokenFrequency = null;
        tokenProbability = null;
    }

    public ArrayList<String> getTokens() {
        return tokens;
    }
}