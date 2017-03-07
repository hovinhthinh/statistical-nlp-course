import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("No document to be processed");
            return;
        }

        Tokenizer tokenizer = new Tokenizer();
        Document[] docs = new Document[args.length];

        for (int i = 0; i < args.length; ++i) {
            File f = new File(args[i]);
            docs[i] = Document.fromFile(f);

            tokenizer.process(docs[i]);
        }

        System.out.println("Entropy:\r\n");
        for (int i = 0; i < args.length; ++i) {
            System.out.printf("Entropy of document \"%s\": %.8f\r\n", args[i], docs[i].getEntropy());
        }
        System.out.println();

        System.out.println("Kullback-Leibler Divergence:\r\n");
        for (int i = 0; i < args.length; ++i)
            for (int j = i + 1; j < args.length; ++j) {

                Set<String> vocab = new HashSet<>();

                vocab.addAll(docs[i].getTokens());
                vocab.addAll(docs[j].getTokens());

                double kld_ij = 0;
                double kld_ji = 0;
                for (String word : vocab) {
                    double p = docs[i].getSmoothedTokenProbability(word), q = docs[j].getSmoothedTokenProbability(word);
                    kld_ij += p * Math.log(p / q) / Math.log(2);
                    kld_ji += q * Math.log(q / p) / Math.log(2);
                }
                System.out.printf("D(\"%s\"||\"%s\") = %.8f\r\n", args[i], args[j], kld_ij);
                System.out.printf("D(\"%s\"||\"%s\") = %.8f\r\n", args[j], args[i], kld_ji);
                System.out.println();
            }

    }
}