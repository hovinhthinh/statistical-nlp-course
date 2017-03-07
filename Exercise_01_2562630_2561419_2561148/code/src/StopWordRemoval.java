import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Stop word removal currently support English only.
 */
public class StopWordRemoval implements Preprocessor {

    private static final Set<String> STOP_WORDS = new HashSet<>();

    private static final String[] EN_SW = new String[]{"a", "about", "above", "after", "again", "against", "all",
            "am", "an", "and", "any", "are", "as", "at", "be", "because", "been", "before", "being", "below", "between",
            "both", "but", "by", "cannot", "could", "did", "do", "does", "doing", "down", "during", "each", "few",
            "for", "from", "further", "had", "has", "have", "having", "he", "her", "here", "hers", "herself", "him",
            "himself", "his", "how", "i", "if", "in", "into", "is", "it", "its", "itself", "me", "more", "most", "my",
            "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or", "other", "ought", "our", "ours",
            "ourselves", "out", "over", "own", "same", "she", "should", "so", "some", "such", "than", "that", "the",
            "their", "theirs", "them", "themselves", "then", "there", "these", "they", "this", "those", "through", "to",
            "too", "under", "until", "up", "very", "was", "we", "were", "what", "when", "where", "which", "while",
            "who", "whom", "why", "with", "would", "you", "your", "yours", "yourself", "yourselves"};
    private static final String[] EN_FR = new String[]{};
    private static final String[] EN_DE = new String[]{};

    static {
        STOP_WORDS.addAll(Arrays.asList(EN_SW));
        STOP_WORDS.addAll(Arrays.asList(EN_FR));
        STOP_WORDS.addAll(Arrays.asList(EN_DE));
    }

    @Override
    public Document process(Document doc) {
        for (Document.Token token : doc.getTokens()) {
            if (STOP_WORDS.contains(token.getContent())) {
                token.setInUse(false);
            }
        }
        return doc;
    }
}
