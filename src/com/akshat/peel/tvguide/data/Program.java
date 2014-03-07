package com.akshat.peel.tvguide.data;

/*
 * 
 * 1 - ID

 2 - type

 6 - ID2

 11 - title

 12 - description

 13 - genre

 14 - image

 */
public class Program {

	private String ID;
	private String ID2;
	private String sType;
	private String sTitle;
	private String sDescription;
	private String sGenre;
	private String sImageUrl;

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public String getID2() {
		return ID2;
	}

	public void setID2(String iD2) {
		ID2 = iD2;
	}

	public String getType() {
		return sType;
	}

	public void setType(String sType) {
		this.sType = sType;
	}

	public String getTitle() {
		return sTitle;
	}

	public void setTitle(String sTitle) {
		this.sTitle = sTitle;
	}

	public String getDescription() {
		return sDescription;
	}

	public void setDescription(String sDescription) {
		this.sDescription = sDescription;
	}

	public String getGenre() {
		return sGenre;
	}

	public void setGenre(String sGenre) {
		this.sGenre = sGenre;
	}

	public String getImageUrl() {
		return sImageUrl;
	}

	public void setImageUrl(String sImageUrl) {
		this.sImageUrl = sImageUrl;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ID == null) ? 0 : ID.hashCode());
		result = prime * result + ((ID2 == null) ? 0 : ID2.hashCode());
		result = prime * result
				+ ((sDescription == null) ? 0 : sDescription.hashCode());
		result = prime * result + ((sGenre == null) ? 0 : sGenre.hashCode());
		result = prime * result
				+ ((sImageUrl == null) ? 0 : sImageUrl.hashCode());
		result = prime * result + ((sTitle == null) ? 0 : sTitle.hashCode());
		result = prime * result + ((sType == null) ? 0 : sType.hashCode());
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
		Program other = (Program) obj;
		if (ID == null) {
			if (other.ID != null)
				return false;
		} else if (!ID.equals(other.ID))
			return false;
		if (ID2 == null) {
			if (other.ID2 != null)
				return false;
		} else if (!ID2.equals(other.ID2))
			return false;
		if (sDescription == null) {
			if (other.sDescription != null)
				return false;
		} else if (!sDescription.equals(other.sDescription))
			return false;
		if (sGenre == null) {
			if (other.sGenre != null)
				return false;
		} else if (!sGenre.equals(other.sGenre))
			return false;
		if (sImageUrl == null) {
			if (other.sImageUrl != null)
				return false;
		} else if (!sImageUrl.equals(other.sImageUrl))
			return false;
		if (sTitle == null) {
			if (other.sTitle != null)
				return false;
		} else if (!sTitle.equals(other.sTitle))
			return false;
		if (sType == null) {
			if (other.sType != null)
				return false;
		} else if (!sType.equals(other.sType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Program [ID=" + ID + ", ID2=" + ID2 + ", sType=" + sType
				+ ", sTitle=" + sTitle + ", sDescription=" + sDescription
				+ ", sGenre=" + sGenre + ", sImageUrl=" + sImageUrl + "]";
	}

	
}
