import ai.tecton.client.TectonClient;
import ai.tecton.client.request.GetFeaturesBatchRequest;
import ai.tecton.client.request.GetFeaturesRequestData;
import ai.tecton.client.response.GetFeaturesBatchResponse;
import ai.tecton.client.response.GetFeaturesResponse;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/**
 * A GetFeaturesBatch request with default TectonClientOptions and default microBatchSize (1)
 */
public class GetFeaturesBatchDemo {
	private static final String WORKSPACE_NAME = "prod";
	private static final String FEATURE_SERVICE_NAME = "fraud_detection_feature_service";

	public static void main(String[] args) throws IOException, InterruptedException {

		//Read URL and api key from tecton.properties file
		Properties properties = new Properties();
		properties.load(new FileInputStream("tecton.properties"));
		String url = properties.getProperty("url");
		String apiKey = properties.getProperty("apiKey");


		// Create Default Tecton Client
		TectonClient tectonClient = new TectonClient(url, apiKey);

		List<GetFeaturesRequestData> getFeaturesRequestDataList = generateFraudRequestDataFromFile("input.csv");

		//Create GetFeaturesBatchRequest with 100 request data and default microBatchSize(1)
		GetFeaturesBatchRequest batchRequest = new GetFeaturesBatchRequest(WORKSPACE_NAME, FEATURE_SERVICE_NAME, getFeaturesRequestDataList);

		//Call getFeaturesBatch using the tectonClient
		GetFeaturesBatchResponse batchResponse = tectonClient.getFeaturesBatch(batchRequest);
		System.out.println("Request Duration: " + batchResponse.getRequestLatency().toMillis());

		List<GetFeaturesResponse> responseList = batchResponse.getBatchResponseList();
		System.out.println("Total Responses: " + responseList.size());

		//REFER TO GetFeaturesDemo on how to access each GetFeaturesResponse in the responseList
		/*
		responseList.forEach(getFeaturesResponse -> {

			//Get each Feature Vector as List
			List<FeatureValue> featureValueList = getFeaturesResponse.getFeatureValues();

			//Get Feature Vector as Map
			Map<String, FeatureValue> featureValues = getFeaturesResponse.getFeatureValuesAsMap();

			//Access Individual Feature Names and values
			FeatureValue sampleFeatureValue = featureValues.get("user_transaction_amount_metrics.amt_mean_1d_10m");
			String featureNamespace = sampleFeatureValue.getFeatureNamespace();
			String featureName = sampleFeatureValue.getFeatureName();
			ValueType valueType = sampleFeatureValue.getValueType();
			Double value = sampleFeatureValue.float64Value();
		});
		*/

		tectonClient.close();
	}


	public static List<GetFeaturesRequestData> generateFraudRequestDataFromFile(String filePath)
			throws IOException {
		List<GetFeaturesRequestData> requestDataList = new ArrayList<>();
		File file = new File(GetFeaturesBatchDemo.class.getClassLoader().getResource(filePath).getFile());
		String content = new String(Files.readAllBytes(file.toPath()));
		Arrays.asList(StringUtils.split(content, "\n")).subList(0, 100)
				.forEach(
						row -> {
							String[] vals = StringUtils.split(row, ",");
							requestDataList.add(
									new GetFeaturesRequestData()
											.addJoinKey("user_id", vals[0])
											.addJoinKey("merchant", vals[2])
											.addRequestContext("amt", Double.parseDouble(vals[1])));
						});
		return requestDataList;
	}
}
