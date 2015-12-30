package nz.co.trineo.github.model;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
	private URI url;
	private int id;
	private String login;
	private String location;
	private URI blog;
	private String email;
	private String name;
	private String company;

	@JsonProperty
	public URI getUrl() {
		return url;
	}

	public void setUrl(URI url) {
		this.url = url;
	}

	@JsonProperty
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@JsonProperty
	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	@JsonProperty
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	@JsonProperty
	public URI getBlog() {
		return blog;
	}

	public void setBlog(URI blog) {
		this.blog = blog;
	}

	@JsonProperty
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@JsonProperty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty
	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}
}
