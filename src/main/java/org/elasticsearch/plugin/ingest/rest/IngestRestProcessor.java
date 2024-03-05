package org.elasticsearch.plugin.ingest.rest;

import org.elasticsearch.ingest.AbstractProcessor;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;
import org.elasticsearch.ingest.ConfigurationUtils;

import java.io.IOException;
import java.util.Map;

public class IngestRestProcessor extends AbstractProcessor {
    public static final String TYPE = "ingest_rest";

    private final String field;
    private final String targetField;

    public IngestRestProcessor(String tag, String field, String description,
        String targetField) throws IOException {
        super(tag, description);
        this.field = field;
        this.targetField = targetField;
    }

    @Override
    public IngestDocument execute(IngestDocument ingestDocument) throws Exception {
        String content = ingestDocument.getFieldValue(field, String.class);
        ingestDocument.setFieldValue(targetField, content);
        // implement the processor logic here
        return ingestDocument;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public static final class Factory implements Processor.Factory {
        @Override
        public Processor create(Map<String, Processor.Factory> registry, 
            String processorTag, String description, Map<String, Object> config) throws IOException {
                String field = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "field", 
                    "default_field_name");
                String targetField = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "target_field", 
                    "default_target_field_name");
    
            return new IngestRestProcessor(processorTag, description, field, targetField);
        }
    }

}