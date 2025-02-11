package com.message.message_manipulation.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.message.message_manipulation.service.SettingsService;
import com.message.message_manipulation.model.Settings;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public List<Settings> getAll() {
        return settingsService.getAll();    
    }

    @GetMapping("/{propKey}")
    public String getValue(@PathVariable String propKey) {
        return settingsService.getValue(propKey);
    }

    @PostMapping("/{propKey}")
    public void setValue(@PathVariable String propKey, @RequestBody Map<String, String> body) {
        String newValue = body.get("value");
        settingsService.setValue(propKey, newValue);
    }
}