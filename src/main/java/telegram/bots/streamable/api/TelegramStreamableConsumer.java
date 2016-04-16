package telegram.bots.streamable.api;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import telegram.bots.streamable.api.streamable.StreamableConsumer;
import telegram.bots.streamable.api.streamable.StreamableMessage;

public class TelegramStreamableConsumer implements StreamableConsumer {

	private static final String VIDEO_DETAILS = "%s\n%s";

	private final TelegramConnector connector;
	private final String chatId;
	private final List<String> keywords;

	public TelegramStreamableConsumer(TelegramConnector connector, String chatId, List<String> keywords) {
		this.connector = connector;
		this.chatId = chatId;
		this.keywords = keywords;
	}

	@Override
	public void handleMessage(StreamableMessage message) throws Exception {
		if (message.getVideoUrl() != null) {
			if (keywords.isEmpty()) {
				sendMessage(String.format(VIDEO_DETAILS,
						StringUtils.isEmpty(message.getTitle()) ? message.getRedditTitle() : message.getTitle(),
						message.getVideoUrl()));
			} else {
				for (String keyword : keywords) {
					if (hasKeyword(message.getTitle(), keyword) || hasKeyword(message.getRedditTitle(), keyword)
							|| hasKeyword(message.getRedditUrl(), keyword)) {
						sendMessage(String.format(VIDEO_DETAILS,
								StringUtils.isEmpty(message.getTitle()) ? message.getRedditTitle() : message.getTitle(),
								message.getVideoUrl()));
						break;
					}
				}
			}
		}
	}

	private void sendMessage(String message) throws Exception {
		connector.sendMessage(chatId, message);
	}

	private boolean hasKeyword(String title, String keyword) {
		if (title == null) {
			return false;
		}
		return title.toLowerCase().contains(keyword.toLowerCase());
	}

}
