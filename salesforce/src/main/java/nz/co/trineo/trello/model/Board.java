package nz.co.trineo.trello.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import nz.co.trineo.common.model.Client;
import nz.co.trineo.common.model.ConnectedAccount;

@Entity
@Table(name = "board")
@JsonInclude(Include.NON_DEFAULT)
public class Board {
	@Id
	private String id;
	@Column
	private String name;
	@Column
	private String desc;
	@Column
	private boolean closed;
	@Column
	private String idOrganization;
	@Column
	private String url;
	// @Column
	// private Map<String, String> labelNames;
	@Column
	private String shortUrl;
	@Column
	private boolean subscribed;
	@Column
	private Date dateLastActivity;
	@Column
	private Date dateLastView;
	@Column
	private String shortLink;
	@OneToOne
	private ConnectedAccount account;
	@ManyToOne
	@JoinColumn(name = "CLIENT_ID")
	private Client client;

	@JsonProperty
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@JsonProperty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty
	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	@JsonProperty
	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	@JsonProperty
	public String getIdOrganization() {
		return idOrganization;
	}

	public void setIdOrganization(String idOrganization) {
		this.idOrganization = idOrganization;
	}

	@JsonProperty
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	// @JsonProperty
	// public Map<String, String> getLabelNames() {
	// return labelNames;
	// }
	//
	// public void setLabelNames(Map<String, String> labelNames) {
	// this.labelNames = labelNames;
	// }

	@JsonProperty
	public String getShortUrl() {
		return shortUrl;
	}

	public void setShortUrl(String shortUrl) {
		this.shortUrl = shortUrl;
	}

	@JsonProperty
	public boolean isSubscribed() {
		return subscribed;
	}

	public void setSubscribed(boolean subscribed) {
		this.subscribed = subscribed;
	}

	@JsonProperty
	public Date getDateLastActivity() {
		return dateLastActivity;
	}

	public void setDateLastActivity(Date dateLastActivity) {
		this.dateLastActivity = dateLastActivity;
	}

	@JsonProperty
	public Date getDateLastView() {
		return dateLastView;
	}

	public void setDateLastView(Date dateLastView) {
		this.dateLastView = dateLastView;
	}

	@JsonProperty
	public String getShortLink() {
		return shortLink;
	}

	public void setShortLink(String shortLink) {
		this.shortLink = shortLink;
	}

	@JsonProperty
	public ConnectedAccount getAccount() {
		return account;
	}

	public void setAccount(ConnectedAccount account) {
		this.account = account;
	}

	@JsonProperty
	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((account == null) ? 0 : account.hashCode());
		result = prime * result + ((client == null) ? 0 : client.hashCode());
		result = prime * result + (closed ? 1231 : 1237);
		result = prime * result + ((dateLastActivity == null) ? 0 : dateLastActivity.hashCode());
		result = prime * result + ((dateLastView == null) ? 0 : dateLastView.hashCode());
		result = prime * result + ((desc == null) ? 0 : desc.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((idOrganization == null) ? 0 : idOrganization.hashCode());
		// result = prime * result + ((labelNames == null) ? 0 : labelNames.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((shortLink == null) ? 0 : shortLink.hashCode());
		result = prime * result + ((shortUrl == null) ? 0 : shortUrl.hashCode());
		result = prime * result + (subscribed ? 1231 : 1237);
		result = prime * result + ((url == null) ? 0 : url.hashCode());
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
		Board other = (Board) obj;
		if (account == null) {
			if (other.account != null)
				return false;
		} else if (!account.equals(other.account))
			return false;
		if (client == null) {
			if (other.client != null)
				return false;
		} else if (!client.equals(other.client))
			return false;
		if (closed != other.closed)
			return false;
		if (dateLastActivity == null) {
			if (other.dateLastActivity != null)
				return false;
		} else if (!dateLastActivity.equals(other.dateLastActivity))
			return false;
		if (dateLastView == null) {
			if (other.dateLastView != null)
				return false;
		} else if (!dateLastView.equals(other.dateLastView))
			return false;
		if (desc == null) {
			if (other.desc != null)
				return false;
		} else if (!desc.equals(other.desc))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (idOrganization == null) {
			if (other.idOrganization != null)
				return false;
		} else if (!idOrganization.equals(other.idOrganization))
			return false;
		// if (labelNames == null) {
		// if (other.labelNames != null)
		// return false;
		// } else if (!labelNames.equals(other.labelNames))
		// return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (shortLink == null) {
			if (other.shortLink != null)
				return false;
		} else if (!shortLink.equals(other.shortLink))
			return false;
		if (shortUrl == null) {
			if (other.shortUrl != null)
				return false;
		} else if (!shortUrl.equals(other.shortUrl))
			return false;
		if (subscribed != other.subscribed)
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
