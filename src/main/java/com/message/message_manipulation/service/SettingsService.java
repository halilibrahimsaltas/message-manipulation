package com.message.message_manipulation.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.message.message_manipulation.model.Settings;
import com.message.message_manipulation.repository.SettingsRepository;

@Service
public class SettingsService {

    private final SettingsRepository settingsRepository;

    public SettingsService(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public String getValue(String propKey) {
        return settingsRepository.findByPropKey(propKey)
                .map(Settings::getPropValue)
                .orElse(null);
    }

    public void setValue(String propKey, String newValue) {
        Settings setting = settingsRepository.findByPropKey(propKey)
                .orElseGet(Settings::new);

        setting.setPropKey(propKey);
        setting.setPropValue(newValue);
        settingsRepository.save(setting);
    }

    // Örneğin virgülle ayrılmış string'i List'e çevirme fonksiyonları vs. eklendi.
    public List<String> getListValue(String propKey) {
        return Arrays.asList(getValue(propKey).split(","));
    }
}
