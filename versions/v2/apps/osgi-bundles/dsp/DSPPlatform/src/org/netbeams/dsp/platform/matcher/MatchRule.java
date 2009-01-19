package org.netbeams.dsp.platform.matcher;


public class MatchRule implements Comparable<MatchRule>{
	
	private int priority;

	private String ruleID;
	private MatchCriteria criteria;
	private MatchTarget target;
	
	public MatchRule(String ruleID, MatchCriteria criteria, MatchTarget target) {
		super();
		resetPriority();
		this.ruleID = ruleID;
		this.criteria = criteria;
		this.target = target;
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
	
	/**
	 * @Override
	 */
	public int compareTo(MatchRule o) {
		return priority - o.priority;
	}	
	
	public int getPriority() {
		if(priority == -1){
			generatePriority();
		}
		return priority;
	}

	private void resetPriority() {
		priority= -1;
	}

	private void generatePriority() {
		priority = 1;
		if(criteria.getComponentType().indexOf('*') > -1){
			priority = 10;
		}		
	}
	
}
