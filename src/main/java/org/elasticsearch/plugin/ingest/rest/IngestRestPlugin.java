package org.elasticsearch.plugin.ingest.rest;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.plugins.IngestPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.ingest.Processor;
import org.elasticsearch.common.settings.Setting;


public class IngestRestPlugin extends Plugin implements IngestPlugin {
/*    public String name() {
        return "ingest-rest";
    }

    public String description() {
        return "Ingest Rest Plugin";
    }
*/    
   public static final Setting<String> YOUR_SETTING =
            new Setting<>("ingest.awesome.setting", "foo", (value) -> value, Setting.Property.NodeScope);

    @Override
    public List<Setting<?>> getSettings() {
        return Arrays.asList(YOUR_SETTING);
    }    

        /**
     * Returns additional ingest processor types added by this plugin.
     *
     * The key of the returned {@link Map} is the unique name for the processor which is specified
     * in pipeline configurations, and the value is a {@link org.elasticsearch.ingest.Processor.Factory}
     * to create the processor from a given pipeline configuration.
     */

    /*@Override
    public Map<String, Processor.Factory> getProcessors(Processor.Parameters parameters) {
        return Map.<String, Processor.Factory>of(
            IngestRestProcessor.TYPE, new IngestRestProcessor.Factory()
        );
    }
*/
    @Override
    public Map<String, Processor.Factory> getProcessors(Processor.Parameters parameters) {
      Map<String, Processor.Factory> processors = new HashMap<>();
      processors.put(IngestRestProcessor.TYPE, new IngestRestProcessor.Factory());
      return processors;
   }
}
