package com.golfingbuddy.ui.memberships.classes;

/**
 * Created by jk on 2/23/15.
 */
public class Benefit {
    private String label;
    private BenefitActions[] actions;
    private String name;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public BenefitActions[] getActions() {
        return actions;
    }

    public void setActions(BenefitActions[] actions) {
        this.actions = actions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
