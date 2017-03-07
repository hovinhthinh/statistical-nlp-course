import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.*;

/**
 * We use N-Gram stemmer here
 * In addition, before applying N-Gram stemmer, we check 2 tokens are similar if it have some common starting part.
 */
public class Stemmer implements Preprocessor {

    private static final int NGRAM_FACTOR = 3;
    private static final float NGRAM_THRESHOLD = 0.6f;

    @Override
    public Document process(Document doc) {

        HashSet<String> wordSet = new HashSet<>();
        for (Document.Token token : doc.getTokens()) {
            if (token.isInUse()) {
                wordSet.add(token.getContent());
            }
        }
        String[] tokens = wordSet.toArray(new String[0]);
        wordSet = null;

        /* N-Gram Stemmer */
        BiDirectedGraph graph = new BiDirectedGraph(tokens.length);
        for (int i = 0; i < tokens.length; ++i) {
            for (int j = i + 1; j < tokens.length; ++j) {
                if (isSimilar(tokens[i], tokens[j])) {
                    graph.addEdge(i, j);
                }
            }
        }

        int[] result = graph.getConnectedComponent();
        int num = -1;
        for (int i = 0; i < result.length; ++i) num = Math.max(num, result[i]);

        try {
            PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File("./StemmerTermMap.txt")), "UTF-8"));
            for (int k = 0; k < num; ++k) {
                boolean first = true;
                for (int i = 0; i < result.length; ++i)
                    if (result[i] == k) {
                        if (!first) {
                            out.print("|");
                        } else {
                            first = false;
                        }
                        out.print(tokens[i]);
                    }
                out.println();
            }

            out.close();
        } catch (Exception e) {

        }

        Map<Integer, TreeSet<String>> tempMap = new HashMap<>();
        for (int i = 0; i < result.length; ++i) {
            TreeSet<String> tree = tempMap.get(result[i]);
            if (tree == null) {
                tree = new TreeSet<>(new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        if (o1.length() != o2.length()) return o1.length() - o2.length();
                        return o1.compareTo(o2);
                    }
                });
                tempMap.put(result[i], tree);
            }
            tree.add(tokens[i]);
        }

        HashMap<String, String> termMap = new HashMap<>();
        for (TreeSet<String> set : tempMap.values()) {
            for (String s : set) {
                termMap.put(s, set.first());
            }
        }

        for (Document.Token token : doc.getTokens()) {
            if (token.isInUse()) {
                token.setContent(termMap.get(token.getContent()));
            }
        }

        return doc;
    }

    private boolean isSimilar(String a, String b) {
        int c = 0;
        for (int i = 0; i < Math.min(a.length(), b.length()); ++i) {
            if (a.charAt(i) == b.charAt(i)) {
                c++;
            } else {
                break;
            }
        }
        /* Check if common starting part is long enough */
        if ((float) c * 2 / (a.length() + b.length()) < NGRAM_THRESHOLD) {
            return false;
        }
        /* N-Gram checking */
        Set<String> aGram = new HashSet<>();
        Set<String> bGram = new HashSet<>();
        for (int i = 0; i < a.length() - NGRAM_FACTOR + 1; ++i) {
            aGram.add(a.substring(i, i + NGRAM_FACTOR));
        }
        for (int i = 0; i < b.length() - NGRAM_FACTOR + 1; ++i) {
            bGram.add(b.substring(i, i + NGRAM_FACTOR));
        }
        c = 0;
        for (String s : aGram) {
            if (bGram.contains(s)) {
                ++c;
            }
        }
        if (c == 0) {
            return false;
        }
        return (float) c * 2 / (aGram.size() + bGram.size()) >= NGRAM_THRESHOLD;
    }
}

class BiDirectedGraph {
    int n;
    List<Integer> edges[];

    public BiDirectedGraph(int n) {
        this.n = n;
        edges = new List[n];
        for (int i = 0; i < n; i++) {
            edges[i] = new LinkedList<>();
        }
    }

    public void addEdge(int u, int v) {
        edges[u].add(v);
        edges[v].add(u);
    }

    public int[] getConnectedComponent() {
        int[] result = new int[n];
        Arrays.fill(result, -1);
        int count = 0;
        for (int i = 0; i < result.length; ++i) {
            if (result[i] != -1) {
                continue;
            }
            Queue<Integer> queue = new LinkedList<>();
            result[i] = count;
            queue.add(i);
            while (!queue.isEmpty()) {
                int u = queue.poll();
                for (int v : edges[u]) {
                    if (result[v] != -1) {
                        continue;
                    }
                    result[v] = count;
                    queue.add(v);
                }
            }
            ++count;
        }
        return result;
    }
}
