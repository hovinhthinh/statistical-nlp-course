import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Document {

    private String originalContent;
    private ArrayList<String> tokens;

    private Set<String> vocab = null;
    private Set<String> bigram = null;
    private Set<String> trigram = null;

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

    public void addToken(String token) {
        tokens.add(token.toLowerCase());
        vocab = null;
        bigram = null;
        trigram = null;
    }

    public ArrayList<String> getTokens() {
        return tokens;
    }

    public double getLongRangeCorrelation(String word, int d) {
        word = word.toLowerCase();

        int p = 0, pp = 0;
        for (String token : tokens) {
            if (token.equals(word)) {
                p++;
            }
        }
        if (p == 0) return -1;
        for (int i = 0; i < tokens.size(); ++i) {
            int j = i + d;
            if (j >= tokens.size()) {
                break;
            }
            if (tokens.get(i).equals(word) && tokens.get(j).equals(word)) {
                ++pp;
            }
        }
        return ((double) pp / (tokens.size() - d)) / Math.pow((double) p / tokens.size(), 2);
    }

    public Set<String> getVocabulary() {
        if (vocab != null) {
            return vocab;
        }
        vocab = new HashSet<>();
        vocab.addAll(tokens);
        return vocab;
    }

    public double getOOVOfTestCorpus(Document test) {
        Set<String> vocab = getVocabulary();
        int c = 0;
        for (String token : test.getTokens()) {
            if (!vocab.contains(token)) {
                ++c;
            }
        }
        return (double) c / test.getTokens().size();
    }

    public Set<String> getUniqueBiGrams() {
        if (bigram != null) {
            return bigram;
        }

        bigram = new HashSet<>();
        for (int i = 0; i < tokens.size() - 1; ++i) {
            bigram.add(tokens.get(i) + " " + tokens.get(i + 1));
        }
        return bigram;
    }

    public Set<String> getUniqueTriGrams() {
        if (trigram != null) {
            return trigram;
        }

        trigram = new HashSet<>();
        for (int i = 0; i < tokens.size() - 2; ++i) {
            trigram.add(tokens.get(i) + " " + tokens.get(i + 1) + " " + tokens.get(i + 2));
        }
        return trigram;
    }

    public int getMissingBiGramCount(Document test) {
        Set<String> bigram = getUniqueBiGrams();
        int c = 0;
        for (int i = 0; i < test.getTokens().size() - 1; ++i) {
            if (!bigram.contains(test.getTokens().get(i) + " " + test.getTokens().get(i + 1))) {
                ++c;
            }
        }
        return c;
    }

    public int getMissingTriGramCount(Document test) {
        Set<String> trigram = getUniqueTriGrams();
        int c = 0;
        for (int i = 0; i < test.getTokens().size() - 2; ++i) {
            if (!trigram.contains(test.getTokens().get(i) + " " + test.getTokens().get(i + 1) + " " + test.getTokens().get(i + 2))) {
                ++c;
            }
        }
        return c;
    }

}