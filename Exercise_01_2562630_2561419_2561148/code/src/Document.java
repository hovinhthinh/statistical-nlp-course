import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.*;

public class Document {
    private String originalContent;

    /* Result from Tokenizer will be stored here */
    private ArrayList<Token> tokens;

    public Document(String originalContent) {
        this.originalContent = originalContent;
        this.tokens = new ArrayList<>();
    }

    public static Document fromFile(File f) {
        try {
            FileInputStream input = new FileInputStream(f);
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            int c = 0;
            while ((c = input.read(buffer)) > 0) {
                stream.write(buffer, 0, c);
            }
            return new Document(new String(stream.toByteArray(), Charset.forName("UTF-8")));

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getOriginalContent() {
        return originalContent;
    }

    public ArrayList<Token> getTokens() {
        return tokens;
    }

    public Token getToken(int ind) {
        return tokens.get(ind);
    }

    public void addToken(Token token) {
        tokens.add(token);
    }

    public String getTokensString() {
        StringBuilder sb = new StringBuilder();
        for (Token token : tokens) {
            if (token.isInUse()) {
                if (sb.length() > 0) {
                    sb.append("|");
                }
                sb.append(token.getContent());
            }
        }
        return sb.toString();
    }

    public String getContent() {
        StringBuilder sb = new StringBuilder();

        int next = 0;
        for (int i = 0; i < originalContent.length(); ++i) {
            if (Tokenizer.isSpecialCharacter(originalContent.charAt(i))) {
                sb.append(' ');
                continue;
            }
            if (next == tokens.size()) {
                sb.append(originalContent.charAt(i));
            } else {
                if (i < tokens.get(next).getStartInd()) {
                    sb.append(originalContent.charAt(i));
                } else {
                    if (tokens.get(next).isInUse()) {
                        sb.append(tokens.get(next).getContent());
                    }
                    i = tokens.get(next).getEndInd() - 1;
                    next++;
                }
            }
        }
        String[] arr = sb.toString().split("[\\r\\n]");
        sb = new StringBuilder();
        for (String s : arr) {
            s = s.trim().replaceAll("\\s++", " ");
            if (!s.isEmpty()) {
                sb.append(s).append("\r\n");
            }
        }
        return sb.toString();
    }

    public Pair<Integer, String>[] getWordFrequencies() {
        Map<String, Integer> map = new HashMap<>();
        for (Token token : tokens) {
            if (token.isInUse()) {
                if (map.containsKey(token.getContent())) {
                    map.put(token.getContent(), map.get(token.getContent()) + 1);
                } else {
                    map.put(token.getContent(), 1);
                }
            }
        }
        Pair<Integer, String>[] result = new Pair[map.size()];
        int c = 0;
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            result[c++] = new Pair<Integer, String>(e.getValue(), e.getKey());
        }
        Arrays.sort(result, new Comparator<Pair<Integer, String>>() {
            @Override
            public int compare(Pair<Integer, String> t0, Pair<Integer, String> t1) {
                return t1.first.compareTo(t0.first);
            }
        });
        return result;
    }

    public Integer[] getZipfLawFrequencies() {
        Pair<Integer, String>[] frequencies = getWordFrequencies();
        Integer[] result = new Integer[frequencies.length];
        for (int i = 0; i < frequencies.length; ++i) {
            result[i] = frequencies[i].first;
        }
        return result;
    }

    public static class Token {
        private String content;
        private boolean inUse; // StopWordRemoval will set this to TRUE if token is a stop word.
        private int startInd, endInd; // Start and end index in original document

        public Token(String content, int startInd, int endInd) {
            this.content = content;
            this.startInd = startInd;
            this.endInd = endInd;
            this.inUse = true;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public boolean isInUse() {
            return inUse;
        }

        public void setInUse(boolean inUse) {
            this.inUse = inUse;
        }

        public int getStartInd() {
            return startInd;
        }

        public void setStartInd(int startInd) {
            this.startInd = startInd;
        }

        public int getEndInd() {
            return endInd;
        }

        public void setEndInd(int endInd) {
            this.endInd = endInd;
        }
    }
}
