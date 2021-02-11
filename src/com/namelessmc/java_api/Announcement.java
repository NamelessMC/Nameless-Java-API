package com.namelessmc.java_api;

public class Announcement {

    private final String content;
    private final String[] displayPages;
    private final String[] displayRanks;

    public Announcement(String content, String[] displayPages, String[] displayRanks) {
        this.content = content;
        this.displayPages = displayPages;
        this.displayRanks = displayRanks;
    }

    public String getContent() {
        return content;
    }

    public String[] getDisplayPages() {
        return displayPages;
    }

    public String[] getDisplayRanks() {
        return displayRanks;
    }

}
