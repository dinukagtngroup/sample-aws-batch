package org.example.awslogger;

import software.amazon.awssdk.services.batch.BatchClient;
import software.amazon.awssdk.services.batch.model.DescribeJobsRequest;
import software.amazon.awssdk.services.batch.model.DescribeJobsResponse;
import software.amazon.awssdk.services.batch.model.JobDetail;
import software.amazon.awssdk.services.batch.model.TagResourceRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class AWSLogger {
	private final BatchClient batchClient;


	private static AWSLogger instance;


	private AWSLogger() {
		this.batchClient = BatchClient.create();
	}

	public static AWSLogger getInstance() {
		if (instance == null)
			instance = new AWSLogger();
		return instance;
	}

	public void tagJobMultiple(String jobARN, Map<String, String> tags) {
		Consumer<TagResourceRequest.Builder> tagResourceRequestConsumer = request -> request.resourceArn(jobARN).tags(tags);
		System.out.println(this.batchClient.tagResource(tagResourceRequestConsumer));
	}

	public void tagJobSingle(String jobARN, String tagKey, String tagValue) {
		Map<String, String> map = new HashMap<>();
		map.put(tagKey, tagValue);
		this.tagJobMultiple(jobARN, map);
	}

	public void tagJob(String tagValue) {
		String jobID = getJobIDFromEnvironment();
		String attemptNo = getJobAttemptNumberFromEnvironment();
		String jobARN = retrieveJobARNFromJobID(jobID);

		tagJobSingle(jobARN, "job_status_" + attemptNo, tagValue);
	}

	private String retrieveJobARNFromJobID(String jobID) throws RuntimeException {
		Consumer<DescribeJobsRequest.Builder> builderConsumer = request -> request.jobs(jobID);
		DescribeJobsResponse describeJobsResponse = this.batchClient.describeJobs(builderConsumer);

		if (!describeJobsResponse.hasJobs() || describeJobsResponse.jobs().size() != 1) {
			throw new RuntimeException("No results returned for the given Job ID.");
		}

		JobDetail jobDetail = describeJobsResponse.jobs().get(0);
		return jobDetail.jobArn();

	}

	private String getJobIDFromEnvironment() throws RuntimeException {
		String jobID = System.getenv("AWS_BATCH_JOB_ID");
		if (jobID == null) {
			throw new RuntimeException("Unable to read Job ID from environment variables.");
		}
		return jobID;
	}

	private String getJobAttemptNumberFromEnvironment() {
		String attemptNumber = System.getenv("AWS_BATCH_JOB_ATTEMPT");
		if (attemptNumber == null) {
			throw new RuntimeException("Unable to read Job Attempt Number from environment variables.");
		}
		return attemptNumber;
	}
}
