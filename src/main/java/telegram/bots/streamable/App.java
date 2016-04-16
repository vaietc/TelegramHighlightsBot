package telegram.bots.streamable;

import telegram.bots.streamable.api.TelegramConnector;
import telegram.bots.streamable.api.TelegramMessageHandler;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) throws Exception {
		TelegramConnector connector = new TelegramConnector();
		TelegramMessageHandler handler = new TelegramMessageHandler(connector, 10);

		long lastUpdateId = 0;
		while (true) {
			System.out.println("Polling for requests...");
			Long maxUpdateId = handler.handleMessage(connector.getUpdates(lastUpdateId));
			lastUpdateId = Math.max(lastUpdateId, maxUpdateId);
		}
	}
}
