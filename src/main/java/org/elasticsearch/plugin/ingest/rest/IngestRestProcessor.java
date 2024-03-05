package org.elasticsearch.plugin.ingest.rest;

import org.elasticsearch.ingest.AbstractProcessor;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;
import org.elasticsearch.ingest.ConfigurationUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.Header;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class IngestRestProcessor extends AbstractProcessor {
    public static final String TYPE = "ingest_rest";

    private final String field;
    private final String targetField;
    private final String model_id;
    private final String task;
    private final String authorization;
    private final String content_type;
    private final String endpoint;
    private final String method;

    public IngestRestProcessor(String tag, String description, String field,
            String targetField, String model_id, String task, String authorization,
            String content_type, String endpoint, String method) throws IOException {
        super(tag, description);
        this.field = field;
        this.targetField = targetField;
        this.model_id = model_id;
        this.task = task;
        this.authorization = authorization;
        this.content_type = content_type;
        this.endpoint = endpoint;
        this.method = method;
        // Set socket permissions programmatically
//        setSocketPermissions();
    }


    String jsonString = "{\n" +
    "  \"text\": \"Japanese pitcher and freshly minted Dodgers team member Shohei Ohtani’s decade-long $700 million contract with the team — already a subject of interest in the sports and tax worlds for its structure — has now drawn the ire of California’s controller.\",\n" +
    "  \"question\": \"\",\n" +
    "  \"context\": \"\",\n" +
    "  \"instruction\": \"Extract entities from the model's output\"\n" +
    "}";


    private String MakeRestCall(String endpoint, String method, String body, String authorization, String content_type) {
        StringBuilder response = new StringBuilder();
        try {
            // Create the URL
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", content_type);
            conn.setRequestProperty("Authorization", authorization);
            conn.setDoOutput(true);

            // Write the JSON data to the output stream
            byte[] postData = jsonString.getBytes(StandardCharsets.UTF_8);
            conn.getOutputStream().write(postData);

            // Read the response
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Print the response
            System.out.println("Response: " + response.toString());

            // Close the connection
            conn.disconnect();
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
            String response = MakeRestCall(this.endpoint, this.method, value, this.authorization, this.content_type);
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
                String processorTag, String description, Map<String, Object> config) throws IOException {

            String field = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "field",
                    "default_field_name");
            String targetField = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "target_field",
                    "default_target_field_name");
            String model_id = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "model_id",
                    "7e009044-17cd-4132-b43f-cfd5cc5c61fd");
            String task = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "task",
                    "named_entity_recognition");
            String authorization = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "authorization",
                    "Basic bWljaGFlbC5jaXptYXJAbWNwbHVzYS5jb206bWljaGFlbA==");
            String content_type = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "content_type",
                    "application/json");
            String endpoint = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "endpoint",
                    "https://control-plane-gateway-44gp1iu3.uc.gateway.dev/mcplusa/models/predict");
            String method = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "method",
                    "POST");
            
            return new IngestRestProcessor(processorTag, description, field, targetField, model_id, task, authorization,
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