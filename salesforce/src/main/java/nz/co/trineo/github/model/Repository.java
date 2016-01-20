package nz.co.trineo.github.model;

import java.net.URI;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_DEFAULT)
public class Repository {
	private URI url;
	private int id;
	private String description;
	private URI homepage;
	private String name;
	private User owner;
	private String fullName;
	private URI htmlUrl; // this is the UI
	private String gitUrl, sshUrl, cloneUrl, svnUrl, mirrorUrl;
	private boolean issues, wiki, fork, downloads;
	private boolean _private;
	private int watchers, forks, openIssues, size, networkCount, subscribersCount;
	private Date pushedAt;
	private String defaultBranch, language;

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
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@JsonProperty
	public URI getHomepage() {
		return homepage;
	}

	public void setHomepage(URI homepage) {
		this.homepage = homepage;
	}

	@JsonProperty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty
	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	@JsonProperty
	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	@JsonProperty
	public URI getHtmlUrl() {
		return htmlUrl;
	}

	public void setHtmlUrl(URI htmlUrl) {
		this.htmlUrl = htmlUrl;
	}

	@JsonProperty
	public String getGitUrl() {
		return gitUrl;
	}

	public void setGitUrl(String gitUrl) {
		this.gitUrl = gitUrl;
	}

	@JsonProperty
	public String getSshUrl() {
		return sshUrl;
	}

	public void setSshUrl(String sshUrl) {
		this.sshUrl = sshUrl;
	}

	@JsonProperty
	public String getCloneUrl() {
		return cloneUrl;
	}

	public void setCloneUrl(String cloneUrl) {
		this.cloneUrl = cloneUrl;
	}

	@JsonProperty
	public String getSvnUrl() {
		return svnUrl;
	}

	public void setSvnUrl(String svnUrl) {
		this.svnUrl = svnUrl;
	}

	@JsonProperty
	public String getMirrorUrl() {
		return mirrorUrl;
	}

	public void setMirrorUrl(String mirrorUrl) {
		this.mirrorUrl = mirrorUrl;
	}

	@JsonProperty
	public boolean isIssues() {
		return issues;
	}

	public void setIssues(boolean issues) {
		this.issues = issues;
	}

	@JsonProperty
	public boolean isWiki() {
		return wiki;
	}

	public void setWiki(boolean wiki) {
		this.wiki = wiki;
	}

	@JsonProperty
	public boolean isFork() {
		return fork;
	}

	public void setFork(boolean fork) {
		this.fork = fork;
	}

	@JsonProperty
	public boolean isDownloads() {
		return downloads;
	}

	public void setDownloads(boolean downloads) {
		this.downloads = downloads;
	}

	@JsonProperty
	public boolean isPrivate() {
		return _private;
	}

	public void setPrivate(boolean _private) {
		this._private = _private;
	}

	@JsonProperty
	public int getWatchers() {
		return watchers;
	}

	public void setWatchers(int watchers) {
		this.watchers = watchers;
	}

	@JsonProperty
	public int getForks() {
		return forks;
	}

	public void setForks(int forks) {
		this.forks = forks;
	}

	@JsonProperty
	public int getOpenIssues() {
		return openIssues;
	}

	public void setOpenIssues(int openIssues) {
		this.openIssues = openIssues;
	}

	@JsonProperty
	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	@JsonProperty
	public int getNetworkCount() {
		return networkCount;
	}

	public void setNetworkCount(int networkCount) {
		this.networkCount = networkCount;
	}

	@JsonProperty
	public int getSubscribersCount() {
		return subscribersCount;
	}

	public void setSubscribersCount(int subscribersCount) {
		this.subscribersCount = subscribersCount;
	}

	@JsonProperty
	public Date getPushedAt() {
		return pushedAt;
	}

	public void setPushedAt(Date pushedAt) {
		this.pushedAt = pushedAt;
	}

	@JsonProperty
	public String getDefaultBranch() {
		return defaultBranch;
	}

	public void setDefaultBranch(String defaultBranch) {
		this.defaultBranch = defaultBranch;
	}

	@JsonProperty
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
}
