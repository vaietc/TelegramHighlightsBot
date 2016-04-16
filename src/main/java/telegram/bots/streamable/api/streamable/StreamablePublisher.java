package telegram.bots.streamable.api.streamable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

public class StreamablePublisher {

	private static final String TOPIC_URL = "https://streamable.com/ajax/stream/%s";
	private final Set<StreamableConsumer> streamableConsumers;
	private final ExecutorService pollingThread;
	private final ObjectMapper mapper;
	private final String topicUrl;
	private final String topic;

	public StreamablePublisher(String topic) {
		this.topic = topic;
		this.topicUrl = String.format(TOPIC_URL, topic);
		streamableConsumers = Sets.newConcurrentHashSet();
		pollingThread = Executors.newSingleThreadExecutor();
		mapper = new ObjectMapper();
	}

	public void publish() throws Exception {
		pollingThread.submit(new Runnable() {

			@Override
			public void run() {
				try {
					String lastSeen = null;
					while (true) {
						List<StreamableMessage> newMessages = null;
						if (lastSeen == null) {
							newMessages = getLastNMessages(5);
						} else {
							List<StreamableMessage> allMessages = getLastNMessages(25);
							int lastIndex = 0;
							for (; lastIndex < allMessages.size(); lastIndex++) {
								if (allMessages.get(lastIndex).getVideoUrl().equals(lastSeen)) {
									break;
								}
							}
							if (lastIndex == 0) {
								newMessages = Collections.emptyList();
							} else {
								newMessages = allMessages.subList(0, lastIndex);
							}
						}
						if (!newMessages.isEmpty()) {
							lastSeen = newMessages.get(0).getVideoUrl();
							for (StreamableConsumer consumer : streamableConsumers) {
								for (StreamableMessage message : newMessages) {
									consumer.handleMessage(message);
								}
							}
						}
						Thread.sleep(5000);
					}
				} catch (Exception e) {
					throw new RuntimeException("Polling thread crashed!", e);
				}
			}
		});
	}

	public void addConsumer(StreamableConsumer consumer) {
		streamableConsumers.add(consumer);
	}

	public void removeConsumer(StreamableConsumer consumer) {
		streamableConsumers.remove(consumer);
	}

	public int getNumConsumers() {
		return streamableConsumers.size();
	}

	public String getTopic() {
		return topic;
	}

	private List<StreamableMessage> getLastNMessages(int n) throws Exception {
		System.out.println("Polling Streamable for last " + n + " messages");
		List<StreamableMessage> messages = new ArrayList<>();
		HttpResponse<JsonNode> response = Unirest.get(topicUrl).queryString("sort", "new").queryString("count", n)
				.asJson();
		if (response.getStatus() != 200) {
			System.err.println("Failed to poll Streamable...");
		}

		StreamableRawMessage rawMessage = mapper.readValue(response.getRawBody(), StreamableRawMessage.class);

		for (Video video : rawMessage.getVideos()) {
			if (video.getFileId() != null) {
				messages.add(new StreamableMessage(video.getStreamableUrl(), video.getTitle(), video.getRedditTitle(),
						video.getRedditUrl()));
			}
		}
		return messages;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class StreamableRawMessage {
		private List<Video> videos;

		@JsonCreator
		public StreamableRawMessage(@JsonProperty("videos") List<Video> videos) {
			this.videos = videos;
		}

		public List<Video> getVideos() {
			return videos;
		}

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Video {
		private static final String VIDEO_URL = "https://streamable.com/%s";
		private String fileId;
		private String title;
		private String redditTitle;
		private String redditUrl;

		public Video(@JsonProperty("file_id") String fileId, @JsonProperty("title") String title,
				@JsonProperty("reddit_title") String redditTitle, @JsonProperty("reddit_url") String redditUrl) {
			this.fileId = fileId;
			this.title = title;
			this.redditTitle = redditTitle;
			this.redditUrl = redditUrl;
		}

		public String getFileId() {
			return fileId;
		}

		public String getTitle() {
			return title;
		}

		public String getRedditTitle() {
			return redditTitle;
		}

		public String getRedditUrl() {
			return redditUrl;
		}

		@JsonIgnore
		public String getStreamableUrl() {
			return String.format(VIDEO_URL, fileId);
		}

	}

}
