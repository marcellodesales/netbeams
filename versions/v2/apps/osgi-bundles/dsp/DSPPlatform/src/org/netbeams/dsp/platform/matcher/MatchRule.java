package org.netbeams.dsp.platform.matcher;

public class MatchRule {

    boolean isDefault;
    private String ruleID;
    private MatchCriteria criteria;
    private MatchTarget target;

    public MatchRule(String ruleID, boolean isDefault, MatchCriteria criteria, MatchTarget target) {
        super();
        this.ruleID = ruleID;
        this.isDefault = isDefault;
        this.criteria = criteria;
        this.target = target;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof MatchRule)) {
            return false;
        }
        return this.ruleID.equals(((MatchRule) obj).ruleID) && this.criteria.equals(((MatchRule) obj).criteria)
                && this.target.equals(((MatchRule) obj).target) && this.isDefault == ((MatchRule) obj).isDefault;
    }

    public int hashCode() {
        return 10 * this.ruleID.hashCode() + 20 * this.criteria.hashCode()
                + (this.target != null ? 30 * this.target.hashCode() : 0) + (this.isDefault ? -1 : 1);
    }

    public String getRuleID() {
        return ruleID;
    }

    public MatchCriteria getCriteria() {
        return criteria;
    }

    public void setCriteria(MatchCriteria criteria) {
        this.criteria = criteria;
    }

    public MatchTarget getTarget() {
        return target;
    }

    public void setTarget(MatchTarget target) {
        this.target = target;
    }

    public boolean isDefault() {
        return this.isDefault;
    }
}
