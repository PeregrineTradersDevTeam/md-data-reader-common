package com.peregrinetraders.configuration;

public class ETLConfigurationParameters {
	private String prefix;
	private String configuration;
	private Integer[] exclude;
	private Integer only;
	private String[] folders;
	
	
	public ETLConfigurationParameters() {
		super();
	}
	
	public ETLConfigurationParameters(String prefix, String configuration, Integer[] exclude, Integer only,
			String[] folders) {
		super();
		this.prefix = prefix;
		this.configuration = configuration;
		this.exclude = exclude;
		this.only = only;
		this.folders = folders;
	}
	/**
	 * Fill missing own properties from another object
	 * @param other
	 */
	public void fillInMissing(ETLConfigurationParameters other) {
		if (!this.hasPrefix()) {
			this.prefix = other.prefix;
		}
		if (!this.hasExclude()) {
			this.exclude = other.exclude;
		}
		if (!this.hasOnly()) {
			this.only = other.only;

		}
		if (!this.hasFolders()) {
			this.folders = other.folders;
		}
	}
	
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public boolean hasPrefix() {
		return this.prefix != null;
	}
	public String getConfiguration() {
		return configuration;
	}
	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}
	public boolean hasConfiguration() {
		return this.configuration != null;
	}
	public Integer[] getExclude() {
		return exclude;
	}
	public void setExclude(Integer[] exclude) {
		this.exclude = exclude;
	}
	public boolean hasExclude() {
		return this.exclude != null;
	}
	public Integer getOnly() {
		return only;
	}
	public void setOnly(Integer only) {
		this.only = only;
	}
	public boolean hasOnly() {
		return this.only != null;
	}
	public String[] getFolders() {
		return folders;
	}
	public void setFolders(String[] folders) {
		this.folders = folders;
	}
	public boolean hasFolders() {
		return this.folders != null && this.folders.length > 0;
	}
}