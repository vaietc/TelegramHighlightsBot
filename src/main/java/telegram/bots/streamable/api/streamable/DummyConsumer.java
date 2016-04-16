package telegram.bots.streamable.api.streamable;

public class DummyConsumer implements StreamableConsumer {

	@Override
	public void handleMessage(StreamableMessage message) {
		String title = message.getTitle();
		if (message.getTitle() != null && message.getRedditTitle() != null) {
			title = message.getTitle().length() >= message.getRedditTitle().length() ? message.getTitle()
					: message.getRedditTitle();
		} else if (message.getTitle() == null) {
			title = message.getRedditTitle();
		}
		System.out.println(message.getVideoUrl() + " " + title + " "
				+ (message.getRedditUrl() != null ? message.getRedditUrl() : ""));
	}

	public static void main(String[] args) throws Exception {
		StreamablePublisher publisher = new StreamablePublisher("soccer");
		publisher.addConsumer(new DummyConsumer());
		publisher.publish();
	}

}
