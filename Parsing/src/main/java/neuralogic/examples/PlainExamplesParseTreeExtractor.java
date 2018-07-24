package neuralogic.examples;

import com.sun.istack.internal.NotNull;
import constructs.Conjunction;
import constructs.example.LiftedExample;
import ida.utils.tuples.Pair;
import neuralogic.grammarParsing.PlainGrammarVisitor;
import parsers.neuralogic.NeuralogicParser;

import java.util.logging.Logger;
import java.util.stream.Stream;

import static utils.Utilities.zipStreams;

public class PlainExamplesParseTreeExtractor extends ExamplesParseTreeExtractor<PlainGrammarVisitor> {
    private static final Logger LOG = Logger.getLogger(PlainExamplesParseTreeExtractor.class.getName());

    public PlainExamplesParseTreeExtractor(PlainGrammarVisitor v) {
        super(v);
    }

    @Override
    public Stream<LiftedExample> getUnlabeledExamples(@NotNull NeuralogicParser.ExamplesFileContext ctx) {
        PlainGrammarVisitor.LiftedExampleVisitor liftedExampleVisitor = visitor.new LiftedExampleVisitor();
        if (ctx.liftedExample() != null) {
            Stream<LiftedExample> listStream = ctx.liftedExample().stream().map(rule -> rule.accept(liftedExampleVisitor));
            return listStream;
        } else
            LOG.severe("Could not extract any Unlabeled trainExamples");
        return null;
    }

    @Override
    public Stream<Pair<Conjunction, LiftedExample>> getLabeledSamples(@NotNull NeuralogicParser.ExamplesFileContext ctx) {
        return zipStreams(getQueries(ctx), getUnlabeledExamples(ctx), (q, e) -> new Pair(q, e));  //TODO check synchronization of labels and examples, what happens if some label is missing? (it should return null value)

    }

    @Override
    public Stream<Conjunction> getQueries(@NotNull NeuralogicParser.ExamplesFileContext ctx) {
        PlainGrammarVisitor.FactConjunctionVisitor factConjunctionVisitor = visitor.new FactConjunctionVisitor();
        if (ctx.label() != null)
            return ctx.liftedExample().stream().map(line -> line.accept(factConjunctionVisitor));
        else
            LOG.severe("Could not extract any trainQueries (weighted facts)");
        return null;
    }
}
