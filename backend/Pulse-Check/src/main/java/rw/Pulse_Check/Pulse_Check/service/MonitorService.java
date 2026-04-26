package rw.Pulse_Check.Pulse_Check.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import rw.Pulse_Check.Pulse_Check.dto.request.MonitorRegistrationRequest;
import rw.Pulse_Check.Pulse_Check.dto.response.MonitorActionResponse;
import rw.Pulse_Check.Pulse_Check.dto.response.MonitorResponse;
import rw.Pulse_Check.Pulse_Check.exception.DuplicateMonitorException;
import rw.Pulse_Check.Pulse_Check.exception.MonitorNotFoundException;
import rw.Pulse_Check.Pulse_Check.model.Monitor;

@Service
public class MonitorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorService.class);

    private final Map<String, Monitor> monitors = new ConcurrentHashMap<>();

    public MonitorActionResponse registerMonitor(MonitorRegistrationRequest request) {
        MonitorRegistrationRequest validatedRequest = Objects.requireNonNull(request, "request must not be null");
        Monitor monitor = Monitor.create(
                validatedRequest.id(),
                validatedRequest.timeout(),
                validatedRequest.alertEmail());

        Monitor existingMonitor = monitors.putIfAbsent(monitor.getId(), monitor);
        if (existingMonitor != null) {
            throw new DuplicateMonitorException("A monitor with id '" + monitor.getId() + "' already exists.");
        }

        return new MonitorActionResponse("Monitor registered successfully.", MonitorResponse.from(monitor));
    }

    public MonitorActionResponse heartbeat(String monitorId) {
        Monitor monitor = getRequiredMonitor(monitorId);
        monitor.heartbeat();

        return new MonitorActionResponse("Heartbeat received. Monitor timer reset.", MonitorResponse.from(monitor));
    }

    public MonitorActionResponse pause(String monitorId) {
        Monitor monitor = getRequiredMonitor(monitorId);
        monitor.pause();

        return new MonitorActionResponse("Monitor paused successfully.", MonitorResponse.from(monitor));
    }

    public MonitorResponse getMonitor(String monitorId) {
        return MonitorResponse.from(getRequiredMonitor(monitorId));
    }

    public List<MonitorResponse> getAllMonitors() {
        return monitors.values()
                .stream()
                .map(MonitorResponse::from)
                .collect(Collectors.toList());
    }

    public void processExpiredMonitors() {
        monitors.values().forEach(this::markDownAndAlertIfExpired);
    }

    private void markDownAndAlertIfExpired(Monitor monitor) {
        if (!monitor.isExpired() || monitor.isDown()) {
            return;
        }

        monitor.markDown();
        String alertLog = String.format(
                "{\"ALERT\":\"%s\",\"time\":\"%s\",\"alert_email\":\"%s\"}",
                monitor.getAlertMessage(),
                Instant.now(),
                monitor.getAlertEmail());
        LOGGER.error(alertLog);
    }

    private Monitor getRequiredMonitor(String monitorId) {
        String normalizedId = normalizeMonitorId(monitorId);
        Monitor monitor = monitors.get(normalizedId);

        if (monitor == null) {
            throw new MonitorNotFoundException("Monitor with id '" + normalizedId + "' was not found.");
        }

        return monitor;
    }

    private String normalizeMonitorId(String monitorId) {
        if (monitorId == null || monitorId.isBlank()) {
            throw new IllegalArgumentException("monitorId must not be blank.");
        }

        return monitorId.trim();
    }
}
