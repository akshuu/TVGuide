package com.akshat.peel.tvguide.data;


public class Schedule {

	private String dStartTime;
	private String dStartDate;
	private String tDuration;
	private Program program;
	
	public String getStartTime() {
		return dStartTime;
	}

	public void setStartTime(String dStartTime) {
		this.dStartTime = dStartTime;
	}

	public String getStartDate() {
		return dStartDate;
	}

	public void setStartDate(String dStartDate) {
		this.dStartDate = dStartDate;
	}

	public String getDuration() {
		return tDuration;
	}

	public void setDuration(String tDuration) {
		this.tDuration = tDuration;
	}

	public Program getProgram() {
		return program;
	}

	public void setPrograms(Program program) {
		this.program = program;
	}

	public String toString(){
		if(program != null){
			return "Program name " + program.getTitle() + " of type " + program.getType() + " starts at : " + tDuration
					+ " ends at : " + dStartDate + " and is of total " + getDuration() + " hours";
		}
		return "No details about the program";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((dStartDate == null) ? 0 : dStartDate.hashCode());
		result = prime * result
				+ ((dStartTime == null) ? 0 : dStartTime.hashCode());
		result = prime * result + ((program == null) ? 0 : program.hashCode());
		result = prime * result
				+ ((tDuration == null) ? 0 : tDuration.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Schedule other = (Schedule) obj;
		if (dStartDate == null) {
			if (other.dStartDate != null)
				return false;
		} else if (!dStartDate.equals(other.dStartDate))
			return false;
		if (dStartTime == null) {
			if (other.dStartTime != null)
				return false;
		} else if (!dStartTime.equals(other.dStartTime))
			return false;
		if (program == null) {
			if (other.program != null)
				return false;
		} else if (!program.equals(other.program))
			return false;
		if (tDuration == null) {
			if (other.tDuration != null)
				return false;
		} else if (!tDuration.equals(other.tDuration))
			return false;
		return true;
	}
	
	

}
