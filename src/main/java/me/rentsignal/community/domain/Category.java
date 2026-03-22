package me.rentsignal.community.domain;

public enum Category {
    RECOMMEND("추천"),
    QUESTION("질문"),
    REVIEW("거주리뷰");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}