package telegram.bots.streamable.api.streamable;

public interface StreamableConsumer {

	public void handleMessage(StreamableMessage message) throws Exception;

}
