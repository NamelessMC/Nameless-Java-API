package com.namelessmc.java_api;

public class Announcement {

    private final String content;
    private final String[] displayPages;
    private final String[] displayRanks;

    Announcement(final String content, final String[] displayPages, final String[] displayRanks) {
        this.content = content;
        this.displayPages = displayPages;
        this.displayRanks = displayRanks;
    }

    public String getContent() {
        return this.content;
    }

    public String[] getDisplayPages() {
        return this.displayPages;
    }

    public String[] getDisplayRanks() {
        return this.displayRanks;
    }

}
