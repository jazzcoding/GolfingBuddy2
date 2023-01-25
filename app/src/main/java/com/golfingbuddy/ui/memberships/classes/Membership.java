package com.golfingbuddy.ui.memberships.classes;

/**
 * Created by jk on 2/23/15.
 */
public class Membership {
    private String id;
    private String roleId;
    private String label;
    private Plan[] plans;
    private Benefit[] benefits;

    public Benefit[] getBenefits() {
        return benefits;
    }

    public void setBenefits(Benefit[] benefits) {
        this.benefits = benefits;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Plan[] getPlans() {
        return plans;
    }

    public void setPlans(Plan[] plans) {
        this.plans = plans;
    }
}
