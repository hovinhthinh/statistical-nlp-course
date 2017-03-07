import java.io.File;

public class Main {

    public static final int LIMIT = 50;

    public static void main(String[] args) throws Exception {
        File folder = new File(args[0]);
        Normalizer normalizer = new Normalizer();       
	// Exercise 1
	System.out.println("Exercise 1:\r\n");
	System.out.println("Correlation values:");

        Document doc = Document.fromFile(new File(folder, "poem.txt"));
        normalizer.process(doc);

        for (int i = 0; i < doc.getTokens().size(); ++i) {
            if (doc.getTokens().get(i).startsWith("you")) {
                doc.getTokens().set(i, "you");
            }
        }

        float[] c = new float[LIMIT];

        for (int i = 0; i < LIMIT; ++i) {
		c[i] = (float) doc.getLongRangeCorrelation("you", i + 1);
		System.out.print("With d = " + (i + 1) + ": ");
		System.out.printf("%.8f\r\n", c[i]);
        }

        Simulator simulator = new Simulator();
        simulator.plotLongRangeDependency(c, true);


        // Exercise 2
	System.out.println();
	System.out.println("Exercise 2:\r\n");
        System.out.println("Detail about 5 training documents:");

        Document test = Document.fromFile(new File(folder, "test/test.txt"));
        normalizer.process(test);

        float[][] f = new float[2][5];

        Document[] docs = new Document[5];
        for (int i = 0; i < 5; ++i) {
            System.out.println("------------------------------");
            File in = new File(folder, "train/train" + (i + 1) + ".txt");
            System.out.println("Document \"" + in.getName() + "\":");
            docs[i] = Document.fromFile(in);
            normalizer.process(docs[i]);
            float oov = (float) docs[i].getOOVOfTestCorpus(test);
            f[0][i] = docs[i].getVocabulary().size();
            f[1][i] = oov;
            System.out.printf("OOV-rate of test corpus \"test.txt\": %.8f\r\n", oov);
            System.out.println("Number of different Bigrams: " + docs[i].getUniqueBiGrams().size());
            System.out.println("Number of missing Bigrams in \"test.txt\": " + docs[i].getMissingBiGramCount(test) + "/" + (test.getTokens().size() - 1));
            System.out.println("Number of different Trigrams: " + docs[i].getUniqueTriGrams().size());
            System.out.println("Number of missing Trigrams in \"test.txt\": " + docs[i].getMissingTriGramCount(test) + "/" + (test.getTokens().size() - 2));
        }

        Simulator.plotOOV(f, false);

    }
}
