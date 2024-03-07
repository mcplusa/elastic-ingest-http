package org.elasticsearch.plugin.ingest.rest;

import org.elasticsearch.SpecialPermission;
import org.elasticsearch.ingest.AbstractProcessor;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;
import org.elasticsearch.ingest.ConfigurationUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.io.InputStream;

public class IngestRestProcessor extends AbstractProcessor {
        public static final String TYPE = "ingest_rest";
        public static final SpecialPermission INSTANCE = new SpecialPermission();

        private final String field;
        private final String targetField;
        private final String model_id;
        private final String task;
        private final String authorization;
        private final String content_type;
        private final String endpoint;
        private final String method;
        private final HttpClient httpClient;

        public IngestRestProcessor(String processorTag, String description, String field,
                        String targetField, String model_id, String task, String authorization,
                        String content_type, String endpoint, String method) throws IOException {
                super(processorTag, description);
                this.field = field;
                this.targetField = targetField;
                this.model_id = model_id;
                this.task = task;
                this.authorization = authorization;
                this.content_type = content_type;
                this.endpoint = endpoint;
                this.method = method;
                this.httpClient = new HttpClient();
        }

        String jsonString = "{\n" +
                        "  \"text\": \"Japanese pitcher and freshly minted Dodgers team member Shohei Ohtani’s decade-long $700 million contract with the team — already a subject of interest in the sports and tax worlds for its structure — has now drawn the ire of California’s controller.\",\n"
                        +
                        "  \"question\": \"\",\n" +
                        "  \"context\": \"\",\n" +
                        "  \"instruction\": \"Extract entities from the model's output\"\n" +
                        "}";

        private String MakeRestCall(String endpoint, String method, String body, String authorization,
                        String content_type, Map<String,String> parameters) {
                StringBuilder response = new StringBuilder();
                try {
                        InputStream is = httpClient.post(endpoint, method, authorization, content_type, body, parameters);
                        // Read the response
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                        response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                                response.append(line);
                        }
                        reader.close();

                        // Print the response
                        System.out.println("Response: " + response.toString());

                } catch (Exception e) {
                        e.printStackTrace();
                        return e.getMessage();
                }
                return response.toString();
        }

        @Override
        public IngestDocument execute(IngestDocument ingestDocument) throws Exception {
                IngestDocument document = ingestDocument;
                if (document.hasField(field)) {
                        String value = document.getFieldValue(field, String.class);
                        Map<String,String> parameters = Map.of(
                                "model_id", this.model_id,
                                "task", this.task
                        );
                        String url = this.endpoint + "?model_id=" + this.model_id + "&task=" + this.task;
                        String response = MakeRestCall(url, this.method, jsonString, this.authorization,
                                        this.content_type, parameters);
                                        document.setFieldValue(field, jsonString);
                                        document.setFieldValue(targetField, response);
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
                                String processorTag, String description, Map<String, Object> config)
                                throws IOException {

                        String field = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, 
                                        "field",
                                        "foo");
                        String targetField = ConfigurationUtils.readStringProperty(TYPE, processorTag, config,
                                        "target_field",
                                        "bar");
                        String model_id = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, 
                                        "model_id",
                                        "7e009044-17cd-4132-b43f-cfd5cc5c61fd");
                        String task = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, 
                                        "task",
                                        "named_entity_recognition");
                        String authorization = ConfigurationUtils.readStringProperty(TYPE, processorTag, config,
                                        "authorization",
                                        "Basic bWljaGFlbC5jaXptYXJAbWNwbHVzYS5jb206bWljaGFlbA==");
                        String content_type = ConfigurationUtils.readStringProperty(TYPE, processorTag, config,
                                        "content_type",
                                        "application/json");
                        String endpoint = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, 
                                        "endpoint",
                                        "https://control-plane-gateway-44gp1iu3.uc.gateway.dev/mcplusa/models/predict");
                        String method = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, 
                                        "method",
                                        "POST");
                        return new IngestRestProcessor(processorTag, description, field, targetField, model_id, task,
                                        authorization,
                                        content_type, endpoint, method);
                }
        }

        /*
         * Headers should be in format of JSON string. For example:
         * headers: '{"Authorization":
         * "Basic bWljaGFlbC5jaXptYXJAbWNwbHVzYS5jb206bWljaGFlbA==",
         * "Content-Type": "application/json"}'
         */

}