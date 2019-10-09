package qunar.tc.qmq.consumer.pull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qmq.CompositePullClient;
import qunar.tc.qmq.ConsumeStrategy;
import qunar.tc.qmq.PullClient;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author zhenwei.liu
 * @since 2019-09-30
 */
public abstract class AbstractCompositePullClient<T extends PullClient> extends AbstractPullClient implements CompositePullClient<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCompositePullClient.class);

    private List<T> components;

    public AbstractCompositePullClient(String subject, String consumerGroup, String partitionName, String brokerGroup, String consumerId, ConsumeStrategy consumeStrategy, int version, boolean isBroadcast, boolean isOrdered, long consumptionExpiredTime, List<T> components) {
        super(subject, consumerGroup, partitionName, brokerGroup, consumerId, consumeStrategy, version, isBroadcast, isOrdered, consumptionExpiredTime);
        this.components = components;
    }


    @Override
    public List<T> getComponents() {
        return components;
    }

    @Override
    public void setComponents(List<T> components) {
        this.components = components;
    }

    @Override
    public void startPull(ExecutorService executor) {
        for (T component : components) {
            doIgnoreException(() -> component.startPull(executor));
        }
    }

    @Override
    public void stopPull() {
        for (T component : components) {
            doIgnoreException(component::stopPull);
        }
    }

    @Override
    public void destroy() {
        for (T component : components) {
            doIgnoreException(component::destroy);
        }
    }

    @Override
    public void offline() {
        for (T component : components) {
            doIgnoreException(component::offline);
        }
    }

    @Override
    public void setVersion(int version) {
        for (T component : components) {
            component.setVersion(version);
        }
    }

    @Override
    public void setConsumptionExpiredTime(long timestamp) {
        for (T component : components) {
            component.setConsumptionExpiredTime(timestamp);
        }
    }

    @Override
    public void setConsumeStrategy(ConsumeStrategy consumeStrategy) {
        for (T component : components) {
            component.setConsumeStrategy(consumeStrategy);
        }
    }

    private void doIgnoreException(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            LOGGER.error("error ", t);
        }
    }
}