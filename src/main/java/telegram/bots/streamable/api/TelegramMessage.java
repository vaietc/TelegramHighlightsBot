package telegram.bots.streamable.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Telegram Message object.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramMessage {
	private final Boolean ok;
	private final List<TelegramUpdate> updates;

	/**
	 * @param ok
	 *            true if the request was successful
	 * @param updates
	 *            list of updates to process
	 */
	@JsonCreator
	public TelegramMessage(@JsonProperty("ok") Boolean ok, @JsonProperty("result") List<TelegramUpdate> updates) {
		this.ok = ok;
		this.updates = updates;
	}

	/**
	 * @return ok.
	 */
	public Boolean isOk() {
		return ok;
	}

	/**
	 * @return updates
	 */
	public List<TelegramUpdate> getUpdates() {
		return updates;
	}

	/**
	 * A Telegram update
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class TelegramUpdate {
		private final Long updateId;
		private final Message message;

		/**
		 * @param updateId
		 *            updateId
		 * @param message
		 *            message
		 */
		@JsonCreator
		public TelegramUpdate(@JsonProperty("update_id") Long updateId, @JsonProperty("message") Message message) {
			this.updateId = updateId;
			this.message = message;
		}

		/**
		 * @return the updateID
		 */
		public Long getUpdateId() {
			return updateId;
		}

		/**
		 * @return the message
		 */
		public Message getMessage() {
			return message;
		}
	}

	/**
	 * Received message
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Message {
		private final Long messageId;
		private final Chat chat;
		private final String text;

		/**
		 * @param messageId
		 *            messageId
		 * @param chat
		 *            chat
		 * @param text
		 *            text
		 */
		@JsonCreator
		public Message(@JsonProperty("message_id") Long messageId, @JsonProperty("chat") Chat chat,
				@JsonProperty("text") String text) {
			this.messageId = messageId;
			this.chat = chat;
			this.text = text;
		}

		/**
		 * @return the messageId
		 */
		public Long getMessageId() {
			return messageId;
		}

		/**
		 * @return the chat details
		 */
		public Chat getChat() {
			return chat;
		}

		/**
		 * @return the text
		 */
		public String getText() {
			return text;
		}
	}

	/**
	 * Chat details
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Chat {

		private final Long chatId;
		private final String firstName;

		/**
		 * @param chatId
		 *            the chatId
		 * @param firstName
		 *            the firstName of the sender
		 */
		@JsonCreator
		public Chat(@JsonProperty("id") Long chatId, @JsonProperty("first_name") String firstName) {
			this.chatId = chatId;
			this.firstName = firstName;
		}

		/**
		 * @return the chatId
		 */
		public Long getChatId() {
			return chatId;
		}

		/**
		 * @return the first name of the sender
		 */
		public String getFirstName() {
			return firstName;
		}
	}
}
