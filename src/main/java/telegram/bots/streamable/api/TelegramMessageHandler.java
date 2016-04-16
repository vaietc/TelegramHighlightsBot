package telegram.bots.streamable.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.util.concurrent.MoreExecutors;
import com.mashape.unirest.http.exceptions.UnirestException;

import telegram.bots.streamable.api.TelegramMessage.Message;
import telegram.bots.streamable.api.TelegramMessage.TelegramUpdate;
import telegram.bots.streamable.api.streamable.StreamablePublisher;

/**
 * A handler which handles a batch of messages.
 */
public class TelegramMessageHandler {

	private final CompletionService<Long> completionService;
	private final TelegramConnector connector;
	private final Map<String, StreamablePublisher> streamablePublishers;
	private final Multimap<String, TelegramStreamableConsumer> streamableConsumers;
	private final Map<TelegramStreamableConsumer, StreamablePublisher> consumerPublisherMap;

	/**
	 * @param connector
	 *            Telegram connector
	 * @param numThreads
	 *            num threads to run
	 */
	public TelegramMessageHandler(TelegramConnector connector, int numThreads) {
		this.connector = connector;
		completionService = new ExecutorCompletionService<Long>(
				MoreExecutors.getExitingExecutorService((ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads)));
		streamablePublishers = new ConcurrentHashMap<>();
		streamableConsumers = Multimaps.synchronizedMultimap(HashMultimap.create());
		consumerPublisherMap = new ConcurrentHashMap<>();
	}

	/**
	 * 
	 * @param message
	 *            message to handle
	 * @return a future with the handled message
	 * @throws InterruptedException
	 *             on interruption to completion service
	 */
	public Long handleMessage(final TelegramMessage telegramMessage) throws InterruptedException {
		Long max = 0L;
		if (!telegramMessage.isOk()) {
			System.out.println("Not OK");
			return max;
		}
		if (telegramMessage.getUpdates() == null || telegramMessage.getUpdates().isEmpty()) {
			System.out.println("Nothing here");
			return max;
		}

		int submitted = telegramMessage.getUpdates().size();
		for (final TelegramUpdate update : telegramMessage.getUpdates()) {
			final Message message = update.getMessage();
			completionService.submit(new Callable<Long>() {

				public Long call() throws Exception {
					if (message != null) {
						String chatId = message.getChat().getChatId().toString();
						String text = message.getText();
						if (text.startsWith("/help")) {
							printHelp(chatId);

						} else if (text.startsWith("/start ")) {
							String[] parts = text.split(" ");
							if (parts.length < 2) {
								printHelp(chatId);
							} else {
								String topic = parts[1];
								List<String> keywords = new ArrayList<>();
								for (int i = 2; i < parts.length; i++) {
									keywords.add(parts[i]);
								}

								TelegramStreamableConsumer consumer = new TelegramStreamableConsumer(connector, chatId,
										keywords);
								if (!streamablePublishers.containsKey(topic)) {
									StreamablePublisher publisher = new StreamablePublisher(topic);
									publisher.publish();
									streamablePublishers.put(topic, publisher);
								}
								StreamablePublisher publisher = streamablePublishers.get(topic);
								streamablePublishers.get(topic).addConsumer(consumer);
								consumerPublisherMap.put(consumer, publisher);
								streamableConsumers.put(chatId, consumer);
								connector.sendMessage(chatId, "Subscribed to updates from #" + topic
										+ " matching keywords " + Joiner.on(",").join(keywords));
							}

						} else if (text.startsWith("/stop")) {
							if (streamableConsumers.containsKey(chatId)) {
								for (TelegramStreamableConsumer consumer : streamableConsumers.get(chatId)) {
									if (consumerPublisherMap.containsKey(consumer)) {
										StreamablePublisher publisher = consumerPublisherMap.remove(consumer);
										if (publisher.getNumConsumers() == 0) {
											streamablePublishers.remove(publisher.getTopic());
										}
									}
								}
								streamableConsumers.removeAll(chatId);
								connector.sendMessage(chatId, "Unsubscribed from all updates");
							}
						} else {
							printHelp(chatId);
						}
					}
					return update.getUpdateId();
				}

				private void printHelp(String chatId) throws IOException, UnirestException {
					connector.sendMessage(chatId,
							"Available commands:\n /start <hashtag> <space separated keywords> (optional) \n /stop");

				}
			});
		}

		int received = 0;
		while (received < submitted) {
			Future<Long> finishedUpdate = completionService.take();
			try {
				Long updateId = finishedUpdate.get();
				max = Math.max(max, updateId);
			} catch (ExecutionException e) {
				System.out.println("Something went wrong" + e);
			}
			received++;
		}
		return max;
	}
}
