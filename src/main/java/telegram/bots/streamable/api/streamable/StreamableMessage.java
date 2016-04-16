package telegram.bots.streamable.api.streamable;

public class StreamableMessage {
	private final String videoUrl;
	private final String title;
	private final String redditTitle;
	private final String redditUrl;

	public StreamableMessage(String videoUrl, String title, String redditTitle, String redditUrl) {
		this.videoUrl = videoUrl;
		this.title = title;
		this.redditTitle = redditTitle;
		this.redditUrl = redditUrl;
	}

	public String getVideoUrl() {
		return videoUrl;
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
}
