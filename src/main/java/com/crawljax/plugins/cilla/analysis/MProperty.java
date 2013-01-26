package com.crawljax.plugins.cilla.analysis;

public class MProperty {

	private String name = "";
	private String value = "";
	private String status = "notset";
	private boolean effective;

	public boolean isEffective() {
		return effective;
	}

	public void setEffective(boolean effective) {
		this.effective = effective;
	}

	public MProperty(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub

		return "{ " + name + " : " + value + " " + (effective ? "Effective" : "Ineffective")
		        + " }";
	}
	public int getsize(){
		return (name.getBytes().length+value.getBytes().length);
	}
}
