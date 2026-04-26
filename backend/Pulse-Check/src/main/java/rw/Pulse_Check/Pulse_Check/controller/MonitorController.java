package rw.Pulse_Check.Pulse_Check.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import rw.Pulse_Check.Pulse_Check.dto.request.MonitorRegistrationRequest;
import rw.Pulse_Check.Pulse_Check.dto.response.MonitorActionResponse;
import rw.Pulse_Check.Pulse_Check.dto.response.MonitorResponse;
import rw.Pulse_Check.Pulse_Check.service.MonitorService;

@RestController
@RequestMapping("/api/v1/monitors")
public class MonitorController {

    private final MonitorService monitorService;

    public MonitorController(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @PostMapping
    public ResponseEntity<MonitorActionResponse> registerMonitor(
            @Valid @RequestBody MonitorRegistrationRequest request) {
        MonitorActionResponse response = monitorService.registerMonitor(request);
        URI location = URI.create("/api/v1/monitors/" + response.monitor().id());

        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/{id}/heartbeat")
    public ResponseEntity<MonitorActionResponse> heartbeat(@PathVariable String id) {
        return ResponseEntity.ok(monitorService.heartbeat(id));
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<MonitorActionResponse> pause(@PathVariable String id) {
        return ResponseEntity.ok(monitorService.pause(id));
    }

    @GetMapping
    public ResponseEntity<List<MonitorResponse>> getAllMonitors() {
        return ResponseEntity.ok(monitorService.getAllMonitors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MonitorResponse> getMonitor(@PathVariable String id) {
        return ResponseEntity.ok(monitorService.getMonitor(id));
    }
}
