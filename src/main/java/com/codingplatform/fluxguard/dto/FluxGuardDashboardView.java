package com.codingplatform.fluxguard.dto;

import java.util.ArrayList;
import java.util.List;

import com.codingplatform.fluxguard.model.FluxGuardRequestLog;

public class FluxGuardDashboardView {

    private String username = "Guest";
    private String currentIp = "Unknown";
    private String browser = "Unknown";
    private String operatingSystem = "Unknown";
    private String loginTime = "—";
    private String sessionDuration = "—";
    private int securityScore = 100;
    private long totalRequests;
    private long todayRequests;
    private long successfulRequests;
    private long failedRequests;
    private double averageResponseTime;
    private String mostVisitedEndpoint = "N/A";
    private List<FluxGuardRequestLog> recentActivity = new ArrayList<>();
    private List<String> securityEvents = new ArrayList<>();
    private List<String> devices = new ArrayList<>();
    private List<FluxGuardRequestLog> loginHistory = new ArrayList<>();

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCurrentIp() {
        return currentIp;
    }

    public void setCurrentIp(String currentIp) {
        this.currentIp = currentIp;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(String loginTime) {
        this.loginTime = loginTime;
    }

    public String getSessionDuration() {
        return sessionDuration;
    }

    public void setSessionDuration(String sessionDuration) {
        this.sessionDuration = sessionDuration;
    }

    public int getSecurityScore() {
        return securityScore;
    }

    public void setSecurityScore(int securityScore) {
        this.securityScore = securityScore;
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public long getTodayRequests() {
        return todayRequests;
    }

    public void setTodayRequests(long todayRequests) {
        this.todayRequests = todayRequests;
    }

    public long getSuccessfulRequests() {
        return successfulRequests;
    }

    public void setSuccessfulRequests(long successfulRequests) {
        this.successfulRequests = successfulRequests;
    }

    public long getFailedRequests() {
        return failedRequests;
    }

    public void setFailedRequests(long failedRequests) {
        this.failedRequests = failedRequests;
    }

    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    public void setAverageResponseTime(double averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
    }

    public String getMostVisitedEndpoint() {
        return mostVisitedEndpoint;
    }

    public void setMostVisitedEndpoint(String mostVisitedEndpoint) {
        this.mostVisitedEndpoint = mostVisitedEndpoint;
    }

    public List<FluxGuardRequestLog> getRecentActivity() {
        return recentActivity;
    }

    public void setRecentActivity(List<FluxGuardRequestLog> recentActivity) {
        this.recentActivity = recentActivity;
    }

    public List<String> getSecurityEvents() {
        return securityEvents;
    }

    public void setSecurityEvents(List<String> securityEvents) {
        this.securityEvents = securityEvents;
    }

    public List<String> getDevices() {
        return devices;
    }

    public void setDevices(List<String> devices) {
        this.devices = devices;
    }

    public List<FluxGuardRequestLog> getLoginHistory() {
        return loginHistory;
    }

    public void setLoginHistory(List<FluxGuardRequestLog> loginHistory) {
        this.loginHistory = loginHistory;
    }
}
