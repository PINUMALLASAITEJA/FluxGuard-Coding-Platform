package com.codingplatform.dto;

import java.util.List;

public class DashboardView {

    private String userName;
    private String greeting;
    private String heroSubtitle;

    private int problemsSolved;
    private int contestsJoined;
    private int submissions;
    private String currentRank;

    private String activeContestName;
    private String activeContestStartTime;
    private String activeContestDuration;

    private List<String> recentActivity;
    private List<String> upcomingContests;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGreeting() {
        return greeting;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }

    public String getHeroSubtitle() {
        return heroSubtitle;
    }

    public void setHeroSubtitle(String heroSubtitle) {
        this.heroSubtitle = heroSubtitle;
    }

    public int getProblemsSolved() {
        return problemsSolved;
    }

    public void setProblemsSolved(int problemsSolved) {
        this.problemsSolved = problemsSolved;
    }

    public int getContestsJoined() {
        return contestsJoined;
    }

    public void setContestsJoined(int contestsJoined) {
        this.contestsJoined = contestsJoined;
    }

    public int getSubmissions() {
        return submissions;
    }

    public void setSubmissions(int submissions) {
        this.submissions = submissions;
    }

    public String getCurrentRank() {
        return currentRank;
    }

    public void setCurrentRank(String currentRank) {
        this.currentRank = currentRank;
    }

    public String getActiveContestName() {
        return activeContestName;
    }

    public void setActiveContestName(String activeContestName) {
        this.activeContestName = activeContestName;
    }

    public String getActiveContestStartTime() {
        return activeContestStartTime;
    }

    public void setActiveContestStartTime(String activeContestStartTime) {
        this.activeContestStartTime = activeContestStartTime;
    }

    public String getActiveContestDuration() {
        return activeContestDuration;
    }

    public void setActiveContestDuration(String activeContestDuration) {
        this.activeContestDuration = activeContestDuration;
    }

    public List<String> getRecentActivity() {
        return recentActivity;
    }

    public void setRecentActivity(List<String> recentActivity) {
        this.recentActivity = recentActivity;
    }

    public List<String> getUpcomingContests() {
        return upcomingContests;
    }

    public void setUpcomingContests(List<String> upcomingContests) {
        this.upcomingContests = upcomingContests;
    }
}
