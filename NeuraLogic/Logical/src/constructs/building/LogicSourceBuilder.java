package constructs.building;

import constructs.building.factories.ConstantFactory;
import constructs.building.factories.PredicateFactory;
import constructs.building.factories.WeightFactory;
import neuralogic.grammarParsing.PlainParseTree;
import org.antlr.v4.runtime.ParserRuleContext;
import settings.Settings;

import java.io.IOException;
import java.io.Reader;
import java.util.logging.Logger;

public abstract class LogicSourceBuilder<I extends ParserRuleContext, O> {
    private static final Logger LOG = Logger.getLogger(LogicSourceBuilder.class.getName());

    Settings settings;

    // Constants are shared over the whole template
    public ConstantFactory constantFactory = new ConstantFactory();
    // Predicates are shared over the whole template
    public PredicateFactory predicateFactory = new PredicateFactory();
    // Weights are shared over the whole template
    public WeightFactory weightFactory = new WeightFactory();

    @Deprecated
    public abstract O buildFrom(Reader reader) throws IOException;

    public abstract O buildFrom(PlainParseTree<I> parseTree) throws IOException;
}