import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class Document {
    public String getOriginalContent() {
        return originalContent;
    }

    private String originalContent;

    private ArrayList<String> tokens;

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

    public void addToken(String token) {
        tokens.add(token.toLowerCase());
    }

    public int getTokenCount() {
        return tokens.size();
    }

    public ArrayList<String> getTokens() {
        return tokens;
    }

    public String getToken(int index) {
        return tokens.get(index);
    }
}
