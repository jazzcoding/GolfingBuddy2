package com.golfingbuddy.ui.memberships.classes;

import com.google.gson.annotations.SerializedName;

public class MembershipData {
        @SerializedName("membershipActive")
        private String active;
        @SerializedName("currentType")
        private String currentMembership;
        @SerializedName("types")
        private Membership[] membershipTypes;

        public String getActive() {
            return active;
        }

        public void setActive(String active) {
            this.active = active;
        }

        public String getCurrentMembership() {
            return currentMembership;
        }

        public void setCurrentMembership(String currentMembership) {
            this.currentMembership = currentMembership;
        }

        public Membership[] getMembershipTypes() {
            return membershipTypes;
        }

        public void setMembershipTypes(Membership[] membershipTypes) {
            this.membershipTypes = membershipTypes;
        }
    }