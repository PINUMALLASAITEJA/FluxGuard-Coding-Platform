package com.codingplatform.fluxguard.service;

import java.time.Duration;

public interface RequestDeduplicationStore {

    boolean claim(String key, Duration protectionWindow);
}