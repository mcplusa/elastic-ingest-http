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

    public static final Setting<String> YOUR_SETTING =
            new Setting<>("ingest.awesome.setting", "foo", (value) -> value, Setting.Property.NodeScope);

    @Override
    public List<Setting<?>> getSettings() {
        return Arrays.asList(YOUR_SETTING);
    }

    @Override
    public Map<String, Processor.Factory> getProcessors(Processor.Parameters parameters) {
      Map<String, Processor.Factory> processors = new HashMap<>();
      processors.put(IngestRestProcessor.TYPE, new IngestRestProcessor.Factory());
      return processors;
   }
}
