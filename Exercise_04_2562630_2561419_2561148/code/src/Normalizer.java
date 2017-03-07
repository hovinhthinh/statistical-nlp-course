/**
 * Normalizer will:
 * - Remove all special characters
 * - Lowercase
 * - Remove consecutive spaces
 * - Trim
 * - Split into tokens
 * - Call the stemmer
 */
public class Normalizer {
    private static final String SPECIAL_CHARACTERS = "`—~!@#$%^&*()_+=}{][;:\"/?.>,<\\"; // not use '-
    private static final Stemmer stemmer = new Stemmer();
    
    private boolean isSpecialCharacter(char c) {
        return SPECIAL_CHARACTERS.indexOf(c) != -1;
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
        stemmer.process(doc);
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
