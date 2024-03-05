package org.elasticsearch.plugin.ingest.rest;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.plugins.IngestPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.ingest.Processor;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;


public class IngestRestPlugin extends Plugin implements IngestPlugin {

    private final ExampleCustomSettingsConfig config;

    public IngestRestPlugin(Settings settings, Path configPath) {
        this.config = new ExampleCustomSettingsConfig(new Environment(settings, configPath));
    }

    @Override
    public List<Setting<?>> getSettings() {
        return Arrays.asList(ExampleCustomSettingsConfig.SIMPLE_SETTING,
                             ExampleCustomSettingsConfig.BOOLEAN_SETTING,
                             ExampleCustomSettingsConfig.VALIDATED_SETTING,
                             ExampleCustomSettingsConfig.FILTERED_SETTING,
                             ExampleCustomSettingsConfig.SECURED_SETTING,
                             ExampleCustomSettingsConfig.LIST_SETTING);
    }    

    @Override
    public Map<String, Processor.Factory> getProcessors(Processor.Parameters parameters) {
      Map<String, Processor.Factory> processors = new HashMap<>();
      processors.put(IngestRestProcessor.TYPE, new IngestRestProcessor.Factory());
      return processors;
   }
}
