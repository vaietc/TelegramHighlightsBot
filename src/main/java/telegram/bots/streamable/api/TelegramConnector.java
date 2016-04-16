package telegram.bots.streamable.api;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class TelegramConnector {
	private static final String BASE_URL = "https://api.telegram.org/<bot_key>";
	private static final String SEND_MESSAGE = "sendMessage";
	private static final String GET_UPDATES = "getUpdates";

	private final ObjectMapper mapper;
	private final RateLimiter sendLimiter;
	private final RateLimiter updateLimiter;

	public TelegramConnector() {
		this.mapper = new ObjectMapper();
		this.sendLimiter = RateLimiter.create(1);
		this.updateLimiter = RateLimiter.create(1);
	}

	/**
	 * @param chatId
	 *            chatId of recipient
	 * @param text
	 *            Text to send
	 * @throws IOException
	 *             on failure
	 * @throws UnirestException
	 *             on non-200 response
	 */
	public void sendMessage(String chatId, String text) throws IOException, UnirestException {
		sendLimiter.acquire();
		HttpResponse<JsonNode> response = Unirest.post(BASE_URL + SEND_MESSAGE).field("chat_id", chatId)
				.field("text", text).asJson();
		if (response.getStatus() != 200) {
			throw new UnirestException("Failed to get a 200 response, got " + response.getStatus());
		}
	}

	/**
	 * @param lastUpdateId
	 *            last updateId
	 * @return TelegramMessage with updates
	 * @throws IOException
	 *             on failure
	 * @throws UnirestException
	 *             on non-200 response
	 */
	public TelegramMessage getUpdates(Long lastUpdateId) throws IOException, UnirestException {
		updateLimiter.acquire();
		HttpResponse<JsonNode> response = Unirest.post(BASE_URL + GET_UPDATES)
				.field("offset", Long.toString(lastUpdateId + 1)).asJson();
		if (response.getStatus() != 200) {
			throw new UnirestException("Failed to get a 200 response, got " + response.getStatus());
		}

		return mapper.readValue(response.getRawBody(), TelegramMessage.class);
	}

}
