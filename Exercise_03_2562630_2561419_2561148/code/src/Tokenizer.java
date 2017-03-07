/**
 * Tokenizer will:
 * - Remove all special characters
 * - Lowercase
 * - Remove consecutive spaces
 * - Trim
 * - Split into tokens
 */
public class Tokenizer {
    private static final String SPECIAL_CHARACTERS = "`~!@#$%^&*()_+=}{][;:\"/?.>,<\\"; // not use '-
    private static boolean[] TO_BE_REMOVED = new boolean[8000];

    static {
        for (int i = 0; i < SPECIAL_CHARACTERS.length(); ++i) {
            int c = (int) SPECIAL_CHARACTERS.charAt(i);
            if (c >= 0 && c < TO_BE_REMOVED.length) {
                TO_BE_REMOVED[c] = true;
            }
        }
    }

    private boolean isSpecialCharacter(char c) {
        int v = (int) c;
        if (v < 0 || v >= TO_BE_REMOVED.length) {
            return true;
        }
        return TO_BE_REMOVED[v];
    }

    public Document process(Document doc) {
        StringBuilder sb = new StringBuilder(doc.getOriginalContent());
        for (int i = 0; i < sb.length(); ++i) {
            if (isSpecialCharacter(sb.charAt(i))) {
                sb.setCharAt(i, ' ');
            }
        }
        String[] tokens = sb.toString().replaceAll("\\s++", " ").toLowerCase().trim().split(" ");
        for (String token : tokens) {
            if (isToken(token)) {
                doc.addToken(token);
            }
        }
        return doc;
    }

    private boolean isToken(String s) {
        for (int i = 0; i < s.length(); ++i)
            if (Character.isLetterOrDigit(s.charAt(i))) {
                return true;
            }
        return false;
    }
}
