package constructs.template.debugging;

import constructs.example.LogicSample;
import constructs.template.Template;
import pipelines.Pipeline;
import pipelines.debuging.End2EndDebugger;
import pipelines.pipes.generic.FirstFromPairPipe;
import pipelines.pipes.generic.StreamifyPipe;
import settings.Settings;
import settings.Sources;
import constructs.template.debugging.drawing.TemplateDrawer;
import utils.generic.Pair;

import java.util.logging.Logger;
import java.util.stream.Stream;

public class TemplateDebugger extends End2EndDebugger<Template> {
    private static final Logger LOG = Logger.getLogger(TemplateDebugger.class.getName());

    public TemplateDebugger(Settings settings) {
        super(settings);
        drawer = new TemplateDrawer(settings);
    }

    public TemplateDebugger(Sources sources, Settings settings) {
        super(sources, settings);
        drawer = new TemplateDrawer(settings);

    }

    @Override
    public void debug(Template obj) {
        drawer.draw(obj);
    }

    /**
     * Just build template, nothing more
     *
     * @return
     */
    @Override
    public Pipeline<Sources, Stream<Template>> buildPipeline() {
        Pipeline<Sources, Pair<Template, Stream<LogicSample>>> sourcesPairPipeline = pipeline.registerStart(end2endTrainigBuilder.buildFromSources(sources, settings));
        pipeline.registerEnd(pipeline.register(sourcesPairPipeline.connectAfter(new FirstFromPairPipe<>())).connectAfter(new StreamifyPipe<>()));
        return pipeline;
    }
}
