package com.message.message_manipulation.service;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.springframework.stereotype.Service;

@Service
public class LinkConversionService {

    private final SettingsService settingsService;
    private static final Pattern REF_PATTERN = Pattern.compile("ref=\\w+");

    public LinkConversionService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public String convertLinks(String text) {
        if (text == null) return null;
        // DB’de tutulan "link.conversion.ref"
        String myRef = settingsService.getValue("link.conversion.ref");
        if (myRef == null) {
            myRef = "DEFAULT_REF"; // ya da null check
        }
        Matcher matcher = REF_PATTERN.matcher(text);
        return matcher.replaceAll("ref=" + myRef);
    }
}

