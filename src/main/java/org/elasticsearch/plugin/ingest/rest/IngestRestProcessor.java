package org.elasticsearch.plugin.ingest.rest;

import org.elasticsearch.ingest.AbstractProcessor;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;
import org.elasticsearch.ingest.ConfigurationUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;


import java.io.IOException;
import java.util.Map;

public class IngestRestProcessor extends AbstractProcessor {
    public static final String TYPE = "ingest_rest";

    private final String field;
    private final String targetField;
    private final String model_id;
    private final String task;
    private final String authorization;
    private final String content_type;
    private final String endpoint;

    public IngestRestProcessor(String tag, String description, String field,
            String targetField, String model_id, String task, String authorization, String content_type, String endpoint)
            throws IOException {
        super(tag, description);
        this.field = field;
        this.targetField = targetField;
        this.model_id = model_id;
        this.task = task;
        this.authorization = authorization;
        this.content_type = content_type;
        this.endpoint = endpoint;
    }

    private String MakeRestCall(String url, String method, String body, String headers) {
            // Make HTTPS request to external REST service
            try (CloseableHttpClient httpClient = HttpClients.custom().build()) {
                HttpGet request = new HttpGet("https://external-service.com/api/data");
                HttpResponse response = httpClient.execute(request);
    
                // Process response
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Read response body and process data
                } else {
                    // Handle error response
                }
            } catch (Exception e) {
                // Handle exception
            }
        // implement the rest call logic here
        return "response";
    }



    @Override
    public IngestDocument execute(IngestDocument ingestDocument) throws Exception {
        IngestDocument document = ingestDocument;
        if (document.hasField(field)) {
            String value = document.getFieldValue(field, String.class);
            document.setFieldValue(targetField, value);
            // implement the processor logic here
        }
        return document;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public static final class Factory implements Processor.Factory {
        @Override
        public IngestRestProcessor create(Map<String, Processor.Factory> registry,
                String processorTag, String description, Map<String, Object> config) throws IOException {

            String field = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "field",
                    "default_field_name");
            String targetField = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "target_field",
                    "default_target_field_name");
            String model_id = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "model_id",
                    "default_model_id");
            String task = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "task",
                    "default_task");
            String authorization = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "authorization",
                    "default_authorization");
            String content_type = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "content_type",
                    "application/json");
            String endpoint = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "endpoint",
                    "default_endpoint");
            return new IngestRestProcessor(processorTag, description, field, targetField, model_id, task, authorization,
                    content_type, endpoint);
        }
    }

    /*
     * Headers should be in format of JSON string. For example:
     * headers: '{"Authorization":
     * "Basic bWljaGFlbC5jaXptYXJAbWNwbHVzYS5jb206bWljaGFlbA==",
     * "Content-Type": "application/json"}'
     */

}