/**
 * Tokenizer will make the document lowercase, remove special character, trim, remove consecutive spaces,
 * then split it into tokens (words)
 */
public class Tokenizer implements Preprocessor {
    private static final String SPECIAL_CHARACTERS = "`~!@#$%^&*()_-+=}{][;:\"/?.>,<\\'";
    private static boolean[] TO_BE_REMOVED = new boolean[8000];

    static {
        for (int i = 0; i < SPECIAL_CHARACTERS.length(); ++i) {
            int c = (int) SPECIAL_CHARACTERS.charAt(i);
            if (c >= 0 && c < TO_BE_REMOVED.length) {
                TO_BE_REMOVED[c] = true;
            }
        }
    }

    public static boolean isSpecialCharacter(char c) {
        int v = (int) c;
        if (v < 0 || v >= TO_BE_REMOVED.length) {
            return true;
        }
        return TO_BE_REMOVED[v];
    }

    /* c is word character if it is not special character or space */
    public static boolean isWordCharacter(char c) {
        return !isSpecialCharacter(c) && !Character.isWhitespace(c);
    }

    @Override
    public Document process(Document doc) {
        String content = doc.getOriginalContent();
        for (int i = 0; i < content.length(); ++i) {
            if (!isWordCharacter(content.charAt(i))) {
                continue;
            }

            int j = i + 1;
            while (j < content.length() && isWordCharacter(content.charAt(j))) {
                ++j;
            }
            Document.Token token = new Document.Token(content.substring(i, j).toLowerCase(), i, j);
            i = j;
            doc.addToken(token);
        }

        return doc;
    }
}
