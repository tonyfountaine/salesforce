package nz.co.trineo.repo.view;

import com.fasterxml.jackson.annotation.JsonProperty;

import nz.co.trineo.model.Tag;

public class TagView {
	private long id;
	private String sha;
	private String name;
	private String url;
	private String tarballUrl;
	private String zipballUrl;

	public TagView(final Tag tag) {
		id = tag.getId();
		sha = tag.getSha();
		name = tag.getName();
		url = tag.getUrl();
		tarballUrl = tag.getTarballUrl();
		zipballUrl = tag.getZipballUrl();
	}

	@JsonProperty
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@JsonProperty
	public String getSha() {
		return sha;
	}

	public void setSha(String sha) {
		this.sha = sha;
	}

	@JsonProperty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@JsonProperty
	public String getTarballUrl() {
		return tarballUrl;
	}

	public void setTarballUrl(String tarballUrl) {
		this.tarballUrl = tarballUrl;
	}

	@JsonProperty
	public String getZipballUrl() {
		return zipballUrl;
	}

	public void setZipballUrl(String zipballUrl) {
		this.zipballUrl = zipballUrl;
	}
}
