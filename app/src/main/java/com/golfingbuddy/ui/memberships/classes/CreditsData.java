package com.golfingbuddy.ui.memberships.classes;

import com.google.gson.annotations.SerializedName;

/**
 * Created by jk on 2/23/15.
 */
public class CreditsData {
    @SerializedName("creditsActive")
    public String active;
    private String balance;
    private CreditPack[] packs;
    private CreditAction[] spendingActions;
    private CreditAction[] earningActions;

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public CreditPack[] getPacks() {
        return packs;
    }

    public void setPacks(CreditPack[] packs) {
        this.packs = packs;
    }

    public CreditAction[] getSpendingActions() {
        return spendingActions;
    }

    public void setSpendingActions(CreditAction[] spendingActions) {
        this.spendingActions = spendingActions;
    }

    public CreditAction[] getEarningActions() {
        return earningActions;
    }

    public void setEarningActions(CreditAction[] earningActions) {
        this.earningActions = earningActions;
    }
}
