package com.all.ultrapeer;

import static com.all.ultrapeer.messages.UltrapeerMessages.Types.START_ULTRAPEER_SERVICES_TYPE;
import static com.all.ultrapeer.messages.UltrapeerMessages.Types.STOP_ULTRAPEER_SERVICES_TYPE;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.all.commons.IncrementalNamedThreadFactory;
import com.all.messengine.MessageMethod;
import com.sun.management.OperatingSystemMXBean;

@SuppressWarnings("restriction")
@Component
public class UltrapeerMonitor {

	private static final String MONITOR_DELAY_KEY = "ultrapeer_monitor_delay";

	private static final String CPU_TRESHOLD_KEY = "ultrapeer_cpu_treshold";

	private static final String HEAP_TRESHOLD_KEY = "ultrapeer_heap_treshold";

	private final Log log = LogFactory.getLog(this.getClass());

	private final ScheduledExecutorService monitor = Executors
			.newSingleThreadScheduledExecutor(new IncrementalNamedThreadFactory("Ultrapeer-Monitor"));

	private final Runnable monitoringTask = new ResourceMonitorTask();

	@Autowired
	private UltrapeerConfig ultrapeerConfig;

	private final HealthStatus currentStatus = new HealthStatus();

	@MessageMethod(START_ULTRAPEER_SERVICES_TYPE)
	public void start() {
		monitor.scheduleWithFixedDelay(monitoringTask, 0, ultrapeerConfig.getTypedProperty(MONITOR_DELAY_KEY, Long.class),
				TimeUnit.SECONDS);
	}

	@MessageMethod(STOP_ULTRAPEER_SERVICES_TYPE)
	public void stop() {
		monitor.shutdownNow();
	}

	public boolean isHealthy() {
		return currentStatus.isHealthy();
	}

	private double calculateCpuUsage() throws InterruptedException {
		OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		int numCpus = operatingSystemMXBean.getAvailableProcessors();
		long prevUpTime = runtimeMXBean.getUptime();
		long prevProcessCpuTime = operatingSystemMXBean.getProcessCpuTime();
		Thread.sleep(500);
		long upTime = runtimeMXBean.getUptime();
		long processCpuTime = operatingSystemMXBean.getProcessCpuTime();
		if (prevUpTime > 0L && upTime > prevUpTime) {
			long elapsedCpu = processCpuTime - prevProcessCpuTime;
			long elapsedTime = upTime - prevUpTime;
			return Math.min(99F, elapsedCpu / (elapsedTime * 10000F * numCpus));
		}
		return 0.001;
	}

	private double getHeapUsage() {
		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
		MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
		return ((double) heapMemoryUsage.getUsed()) / ((double) heapMemoryUsage.getMax()) * 100.0;
	}

	private final class ResourceMonitorTask implements Runnable {
		@Override
		public void run() {
			try {
				currentStatus.updateCpuUsage(calculateCpuUsage());
				currentStatus.updateHeapUsage(getHeapUsage());
				log.info(currentStatus);
			} catch (Exception e) {
				log.error("Unexpected exception monitoring ultrapeer resources.", e);
			}
		}
	}

	private final class HealthStatus {

		private double cpuCurrentUsage;

		private double cpuPreviousUsage;

		private double heapCurrentUsage;

		private double heapPreviousUsage;

		public void updateCpuUsage(double lastCpuUsage) {
			cpuPreviousUsage = cpuCurrentUsage;
			cpuCurrentUsage = lastCpuUsage;
		}

		public void updateHeapUsage(double lastHeapUsage) {
			heapPreviousUsage = heapCurrentUsage;
			heapCurrentUsage = lastHeapUsage;
		}

		public boolean isHealthy() {
			return isHeapOk() && isCpuOk();
		}

		private boolean isHeapOk() {
			double treshold = ultrapeerConfig.getTypedProperty(HEAP_TRESHOLD_KEY, Double.class);
			if (heapCurrentUsage < treshold) {
				return true;
			}
			System.gc();
			if (heapPreviousUsage < treshold) {
				return true;
			}
			return false;
		}

		private boolean isCpuOk() {
			double treshold = ultrapeerConfig.getTypedProperty(CPU_TRESHOLD_KEY, Double.class);
			if (cpuCurrentUsage < treshold || cpuPreviousUsage < treshold) {
				return true;
			}
			return false;
		}

		@Override
		public String toString() {
			String status = isHealthy() ? "OK" : "WARN";
			return new StringBuilder("HealthStatus[CPU:").append(cpuCurrentUsage).append("][HEAP:").append(heapCurrentUsage)
					.append("] ----> ").append(status).toString();
		}
	}
}
