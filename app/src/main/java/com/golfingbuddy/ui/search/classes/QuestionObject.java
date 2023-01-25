package com.golfingbuddy.ui.search.classes;

import com.google.gson.annotations.SerializedName;

public class QuestionObject {
    private String id;
    private String name;
    private String label;
    private String value;
    private CustomOptions custom;
    private String section;
    private String required;
    private String presentation;
    private QuestionOption[] options;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public CustomOptions getCustom() {
        return custom;
    }

    public void setCustom(CustomOptions custom) {
        this.custom = custom;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getPresentation() {
        return presentation;
    }

    public void setPresentation(String presentation) {
        this.presentation = presentation;
    }

    public QuestionOption[] getOptions() {
        return options;
    }

    public void setOptions(QuestionOption[] options) {
        this.options = options;
    }

    public String getRequired() {
        return required;
    }

    public void setRequired(String required) {
        this.required = required;
    }

    public static class QuestionOption {
        private String label;
        private String value;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class CustomOptions {
        @SerializedName("year_range")
        private YearRange yearRange;

        public YearRange getYearRange() {
            return yearRange;
        }

        public void setYearRange(YearRange yearRange) {
            this.yearRange = yearRange;
        }
    }

    public static class YearRange {
        private String from;
        private String to;

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }
    }
}
