import Interfaces.LLMApiService;
import Interfaces.LoggerService;
import enums.Mutators;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.concurrent.atomic.AtomicBoolean;

public class OpenAIService implements LLMApiService {

    private final String API_KEY;
    private final String MODEL_NAME;
    private double TOTAL_COST;
    private final String API_URL;
    private final LoggerService logger;

    public OpenAIService(String modelName, String apiKey, String apiUrl, LoggerService logger) {
        this.MODEL_NAME = modelName;
        this.TOTAL_COST = 0.0;
        this.logger = logger;
        this.API_KEY = apiKey;
        this.API_URL = apiUrl;
    }

    @Override
    public String askOpenAI(String javaCode) {
        try {
            logger.log("LLM Mutation started...");

            //generate prompt
            String prompt = generatePrompt(javaCode);

            //Sending request
            JSONObject responseJson = sendAPIRequest(prompt);

            return processResponse(responseJson);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String generatePrompt(String javaCode) {
        logger.log("Generating Prompt...");

        StringBuilder mutators = new StringBuilder();
        for (Mutators mutator : Mutators.values()) {
            mutators.append("- ").append(mutator.getReadableName()).append("\n");
        }

        return """
           I want to perform mutation testing on the following Java code. \
           Generate mutants and return the entire mutated Java class to save as a new file.
           Use the following mutation operators (wherever applicable):
           """ + mutators + "\n\n" + javaCode;

    }

    private JSONObject sendAPIRequest(String prompt) throws Exception {

        HttpClient client = HttpClients.createDefault();
        HttpPost request = new HttpPost(API_URL);

        logger.log("Sending API request to " + MODEL_NAME + "...");

        request.setHeader("Authorization", "Bearer " + API_KEY);
        request.setHeader("Content-Type", "application/json");

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "system").put("content", "You are a helpful assistant."));
        messages.put(new JSONObject().put("role", "user").put("content", prompt));

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", MODEL_NAME);
        requestBody.put("messages", messages);

        request.setEntity(new StringEntity(requestBody.toString()));

        AtomicBoolean running = new AtomicBoolean(true);;
        Thread spinner = new Thread(() -> {
            String animation = "|/-\\";
            int i = 0;
            while (running.get()) {
                System.out.print("\r" + animation.charAt(i++ % animation.length()));
                try {
                    Thread.sleep(250);
                } catch (InterruptedException ignored) {}
            }
            System.out.print("\r");
        });
        spinner.start();

        String responseString = EntityUtils.toString(client.execute(request).getEntity());

        running.set(false);
        spinner.join();
        return new JSONObject(responseString);
    }

    private String processResponse(JSONObject responseJson) {
        StringBuilder responseBuilder = new StringBuilder();
        String responseText = "";

        if (responseJson.has("choices")) {
            responseText = responseJson.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content").trim();
        }

        if(responseText.isEmpty()){
            responseText = "No response from the model.";
            logger.error(responseText);
        }
        else {
            logger.log("API response received...");

            if (responseJson.has("usage")) {
                int promptTokens = responseJson.getJSONObject("usage").getInt("prompt_tokens");
                int completionTokens = responseJson.getJSONObject("usage").getInt("completion_tokens");
                int totalTokens = responseJson.getJSONObject("usage").getInt("total_tokens");

                double inputCostPerToken = 0.150 / 1000000;
                double outputCostPerToken = 0.600 / 1000000;
                double inputCost = promptTokens * inputCostPerToken;
                double outputCost = completionTokens * outputCostPerToken;
                double totalCost = inputCost + outputCost;
                TOTAL_COST = TOTAL_COST + totalCost;

                responseBuilder.append("---- Token Usage ----\n");
                responseBuilder.append("Prompt Tokens: ").append(promptTokens).append("\n");
                responseBuilder.append("Completion Tokens: ").append(completionTokens).append("\n");
                responseBuilder.append("Total Tokens: ").append(totalTokens).append("\n");
                responseBuilder.append(String.format("Estimated Cost: $%.6f", totalCost)).append("\n");
                logger.log(responseBuilder.toString());
                logger.log(String.format("Combined Overall Token Cost: $%.6f", TOTAL_COST));
            }
        }

        return responseText;
    }

}
