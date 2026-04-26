package rw.Pulse_Check.Pulse_Check.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import rw.Pulse_Check.Pulse_Check.service.MonitorService;

@Component
public class MonitorScheduler {

    private final MonitorService monitorService;

    public MonitorScheduler(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @Scheduled(fixedDelayString = "${monitor.scan-interval-ms:1000}")
    public void scanExpiredMonitors() {
        monitorService.processExpiredMonitors();
    }
}
