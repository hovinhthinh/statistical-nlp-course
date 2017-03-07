public class Tokenizer {

    public Document process(Document doc) {
        String[] tokens = doc.getOriginalContent().replaceAll("\\s++", " ").trim().split(" ");
        for (String token : tokens) {
            int p = token.indexOf("/");
            if (p != -1) {
                token = token.substring(0, p);
            }
            if (isToken(token)) {
                doc.addToken(token);
            }
        }
        return doc;
    }

    private boolean isToken(String s) {
        for (int i = 0; i < s.length(); ++i)
            if (Character.isLetter(s.charAt(i))) {
                return true;
            }
        return false;
    }
}
