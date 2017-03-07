public class TextPreprocessor implements Preprocessor {

    private Preprocessor[] preprocessors;

    public TextPreprocessor(Preprocessor... preprocessors) {
        this.preprocessors = preprocessors;
    }

    @Override
    public Document process(Document doc) {
        for (Preprocessor preprocessor : this.preprocessors) {
            doc = preprocessor.process(doc);
        }
        return doc;
    }

}
