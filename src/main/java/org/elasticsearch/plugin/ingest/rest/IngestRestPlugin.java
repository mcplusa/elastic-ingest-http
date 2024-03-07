package org.elasticsearch.plugin.ingest.rest;

import java.util.HashMap;
import java.util.Map;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.elasticsearch.plugins.IngestPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.ingest.Processor;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;

public class IngestRestPlugin extends Plugin implements IngestPlugin {
    private final CustomSettingsConfig config;

    public IngestRestPlugin(final Settings settings, final Path configPath) {
        this.config = new CustomSettingsConfig(new Environment(settings, configPath));
    }

    /**
     * @return the plugin's custom settings
     */
    @Override
    public List<Setting<?>> getSettings() {
        return Arrays.asList(CustomSettingsConfig.SIMPLE_SETTING,
                CustomSettingsConfig.BOOLEAN_SETTING,
                CustomSettingsConfig.VALIDATED_SETTING,
                CustomSettingsConfig.FILTERED_SETTING,
                CustomSettingsConfig.SECURED_SETTING,
                CustomSettingsConfig.LIST_SETTING);
    }

    @Override
    public Map<String, Processor.Factory> getProcessors(Processor.Parameters parameters) {
        Map<String, Processor.Factory> processors = new HashMap<>();
        processors.put(IngestRestProcessor.TYPE, new IngestRestProcessor.Factory(this.config.getFiltered()));
        return processors;
    }
}
