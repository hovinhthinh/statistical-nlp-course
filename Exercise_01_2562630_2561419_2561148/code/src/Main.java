import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

public class Main {

    /* args[]: inputFile outputFile frequencyFile */
    public static void main(String[] args) throws Exception {
        /* Create processor for document, we disabled StopWordRemoval */
        TextPreprocessor processor = new TextPreprocessor(
                new Tokenizer()
//				,new StopWordRemoval()
                , new Stemmer()
        );

        File inputEn = new File(args[0]);
        /* Create document */
        Document doc = Document.fromFile(inputEn);
        processor.process(doc);

        File outputEn = new File(args[1]);
        PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputEn), Charset.forName("UTF-8")));
        out.print(doc.getContent());
        out.close();

        outputEn = new File(args[2]);
        out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputEn), Charset.forName("UTF-8")));
        for (Pair<Integer, String> p : doc.getWordFrequencies()) {
            out.println(p.first + " " + p.second);
        }
        out.close();

        ZipfLawSimulator.plot(doc.getZipfLawFrequencies(), "Zipf's Law chart");
    }
}