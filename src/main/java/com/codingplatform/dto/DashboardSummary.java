package com.codingplatform.dto;

public class DashboardSummary {

    private String welcomeMessage;
    private int problemsSolved;
    private int contestsJoined;
    private int submissions;
    private int currentRank;
    private String activeContestTitle;
    private String activeContestDescription;
    private String activeContestStatus;

    public DashboardSummary() {
    }

    public DashboardSummary(String welcomeMessage, int problemsSolved, int contestsJoined,
                            int submissions, int currentRank,
                            String activeContestTitle, String activeContestDescription,
                            String activeContestStatus) {
        this.welcomeMessage = welcomeMessage;
        this.problemsSolved = problemsSolved;
        this.contestsJoined = contestsJoined;
        this.submissions = submissions;
        this.currentRank = currentRank;
        this.activeContestTitle = activeContestTitle;
        this.activeContestDescription = activeContestDescription;
        this.activeContestStatus = activeContestStatus;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
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

    public int getCurrentRank() {
        return currentRank;
    }

    public void setCurrentRank(int currentRank) {
        this.currentRank = currentRank;
    }

    public String getActiveContestTitle() {
        return activeContestTitle;
    }

    public void setActiveContestTitle(String activeContestTitle) {
        this.activeContestTitle = activeContestTitle;
    }

    public String getActiveContestDescription() {
        return activeContestDescription;
    }

    public void setActiveContestDescription(String activeContestDescription) {
        this.activeContestDescription = activeContestDescription;
    }

    public String getActiveContestStatus() {
        return activeContestStatus;
    }

    public void setActiveContestStatus(String activeContestStatus) {
        this.activeContestStatus = activeContestStatus;
    }
}
