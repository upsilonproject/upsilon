package upsilon;

import java.util.Collections;
import java.util.Vector;

import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import upsilon.dataStructures.StructureService;
import upsilon.util.GlobalConstants;
import upsilon.util.Util;

public class DaemonQueueMaintainer extends Daemon {
	private final Vector<StructureService> queue = new Vector<StructureService>();

	private boolean run = true;

	private transient static final Logger LOG = LoggerFactory.getLogger(DaemonQueueMaintainer.class);

	public DaemonQueueMaintainer() {
		Main.instance.queueMaintainer = this;
	}

	private void checkUpdateDelay() {
		if (Configuration.instance.services.isEmpty()) {
			return;
		}

		final Duration suggestedQueueMaintainerDelay = Duration.standardSeconds(Configuration.instance.services.size() / 10);

		if (!suggestedQueueMaintainerDelay.isShorterThan(GlobalConstants.DEF_TIMER_QUEUE_MAINTAINER_DELAY) && Configuration.instance.queueMaintainerDelay.isLongerThan(suggestedQueueMaintainerDelay)) {
			LOG.warn("The queue maintainer delay is quite long for the amount of services that are configured. Suggested value is:" + suggestedQueueMaintainerDelay + ", the actual value is: " + Configuration.instance.queueMaintainerDelay);
		}
	}

	public boolean isEmpty() {
		return this.size() == 0;
	}

	public StructureService poll() {
		if (this.queue.isEmpty()) {
			return null;
		} else {
			StructureService s = this.queue.get(this.queue.size() - 1);
			this.queue.remove(this.queue.size() - 1);

			Collections.shuffle(this.queue); // Stops bad services holding up
												// other services.

			return s;
		}
	}

	private void queueServices() {
		for (StructureService service : Configuration.instance.services) {
			this.setStatus("Service being checked for queue: " + service.getIdentifier());
			Util.lazySleep(Configuration.instance.queueMaintainerDelay);

			if (service.isReadyToBeChecked()) {
				if (this.queue.contains(service)) {
					DaemonQueueMaintainer.LOG.warn("service check required but it's already in the queue. Executor queue too long?: " + this.queue.size() + " items.");
				} else {
					this.queue.add(service);
				}
			}
		}
	}

	public void queueUrgent(StructureService ss) throws IllegalStateException {
		if (!Configuration.instance.services.contains(ss)) {
			throw new IllegalStateException("Tried to urgently queue a service that is not registered globally. Ignoring.");
		}

		this.queue.add(0, ss);
	}

	@Override
	public void run() {
		this.checkUpdateDelay();

		// Now go to normal queueing
		while (this.run) {
			this.setStatus("Sleeping before next execution: " + Configuration.instance.queueMaintainerDelay);
			Util.lazySleep(Configuration.instance.queueMaintainerDelay);

			this.setStatus("Queueing services");
			this.queueServices();
		}

		DaemonQueueMaintainer.LOG.warn("Queue maintenance thread shutdown.");
	}

	public int size() {
		return this.queue.size();
	}

	@Override
	public void stop() {
		this.run = false;
	}

	@Override
	public String toString() {
		return "ServiceCheckQueue: " + this.queue.size() + " services";
	}
}
