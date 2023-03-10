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
	private String jobARN;


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
		if (isAnArrayJob()) {
			System.out.println("WARN: Tagging is not supported in Array Jobs.");
			jobARN = extractParentJobARNFromChildJobARN(jobARN, getArrayJobIndex());
		}

		final String modifiedJobARN = jobARN;
		Consumer<TagResourceRequest.Builder> tagResourceRequestConsumer = request -> request.resourceArn(modifiedJobARN).tags(tags);
		this.batchClient.tagResource(tagResourceRequestConsumer);
	}

	public void tagJobMultiple(Map<String, String> tags) {
		String jobID = getJobIDFromEnvironment();
		String jobARN = retrieveJobARNFromJobID(jobID);

		tagJobMultiple(jobARN, tags);
	}

	public void tagJobSingle(String jobARN, String tagKey, String tagValue) {
		Map<String, String> map = new HashMap<>();
		map.put(tagKey, tagValue);
		this.tagJobMultiple(jobARN, map);
	}

	public void tagJob(String status, String statusReason) {
		Map<String, String> tags = new HashMap<>();
		tags.put("job_status", status);
		if (statusReason != null)
			tags.put("job_status_reason", statusReason);

		tagJobMultiple(tags);
	}

	public void tagJob(String status) {
		tagJob(status, null);
	}

	public void tagJobSuccess() {
		tagJob("SUCCESS");
	}

	public void tagJobFailure(String failReason) {
		tagJob("FAILED", failReason);
	}


	private String retrieveJobARNFromJobID(String jobID) throws RuntimeException {
		if (this.jobARN != null) return this.jobARN;

		Consumer<DescribeJobsRequest.Builder> builderConsumer = request -> request.jobs(jobID);
		DescribeJobsResponse describeJobsResponse = this.batchClient.describeJobs(builderConsumer);

		if (!describeJobsResponse.hasJobs() || describeJobsResponse.jobs().size() != 1) {
			throw new RuntimeException("No results returned for the given Job ID.");
		}

		JobDetail jobDetail = describeJobsResponse.jobs().get(0);
		this.jobARN = jobDetail.jobArn();
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

	private boolean isAnArrayJob() {
		return System.getenv("AWS_BATCH_JOB_ARRAY_INDEX") != null;
	}

	private String getArrayJobIndex() {
		String attemptNumber = System.getenv("AWS_BATCH_JOB_ARRAY_INDEX");
		if (attemptNumber == null) {
			throw new RuntimeException("Unable to read Array Job Index from environment variables.");
		}
		return attemptNumber;
	}

	private String extractParentJobARNFromChildJobARN(String childJobARN, String jobIndex) {
		return childJobARN.substring(0, childJobARN.length() - jobIndex.length() -1);
	}
}
