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

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Board other = (Board) obj;
		if (account == null) {
			if (other.account != null) {
				return false;
			}
		} else if (!account.equals(other.account)) {
			return false;
		}
		if (client == null) {
			if (other.client != null) {
				return false;
			}
		} else if (!client.equals(other.client)) {
			return false;
		}
		if (closed != other.closed) {
			return false;
		}
		if (dateLastActivity == null) {
			if (other.dateLastActivity != null) {
				return false;
			}
		} else if (!dateLastActivity.equals(other.dateLastActivity)) {
			return false;
		}
		if (dateLastView == null) {
			if (other.dateLastView != null) {
				return false;
			}
		} else if (!dateLastView.equals(other.dateLastView)) {
			return false;
		}
		if (desc == null) {
			if (other.desc != null) {
				return false;
			}
		} else if (!desc.equals(other.desc)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (idOrganization == null) {
			if (other.idOrganization != null) {
				return false;
			}
		} else if (!idOrganization.equals(other.idOrganization)) {
			return false;
		}
		// if (labelNames == null) {
		// if (other.labelNames != null)
		// return false;
		// } else if (!labelNames.equals(other.labelNames))
		// return false;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (shortLink == null) {
			if (other.shortLink != null) {
				return false;
			}
		} else if (!shortLink.equals(other.shortLink)) {
			return false;
		}
		if (shortUrl == null) {
			if (other.shortUrl != null) {
				return false;
			}
		} else if (!shortUrl.equals(other.shortUrl)) {
			return false;
		}
		if (subscribed != other.subscribed) {
			return false;
		}
		if (url == null) {
			if (other.url != null) {
				return false;
			}
		} else if (!url.equals(other.url)) {
			return false;
		}
		return true;
	}

	@JsonProperty
	public ConnectedAccount getAccount() {
		return account;
	}

	@JsonProperty
	public Client getClient() {
		return client;
	}

	@JsonProperty
	public Date getDateLastActivity() {
		return dateLastActivity;
	}

	@JsonProperty
	public Date getDateLastView() {
		return dateLastView;
	}

	@JsonProperty
	public String getDesc() {
		return desc;
	}

	@JsonProperty
	public String getId() {
		return id;
	}

	@JsonProperty
	public String getIdOrganization() {
		return idOrganization;
	}

	@JsonProperty
	public String getName() {
		return name;
	}

	@JsonProperty
	public String getShortLink() {
		return shortLink;
	}

	@JsonProperty
	public String getShortUrl() {
		return shortUrl;
	}

	@JsonProperty
	public String getUrl() {
		return url;
	}

	// @JsonProperty
	// public Map<String, String> getLabelNames() {
	// return labelNames;
	// }
	//
	// public void setLabelNames(Map<String, String> labelNames) {
	// this.labelNames = labelNames;
	// }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (account == null ? 0 : account.hashCode());
		result = prime * result + (client == null ? 0 : client.hashCode());
		result = prime * result + (closed ? 1231 : 1237);
		result = prime * result + (dateLastActivity == null ? 0 : dateLastActivity.hashCode());
		result = prime * result + (dateLastView == null ? 0 : dateLastView.hashCode());
		result = prime * result + (desc == null ? 0 : desc.hashCode());
		result = prime * result + (id == null ? 0 : id.hashCode());
		result = prime * result + (idOrganization == null ? 0 : idOrganization.hashCode());
		// result = prime * result + ((labelNames == null) ? 0 :
		// labelNames.hashCode());
		result = prime * result + (name == null ? 0 : name.hashCode());
		result = prime * result + (shortLink == null ? 0 : shortLink.hashCode());
		result = prime * result + (shortUrl == null ? 0 : shortUrl.hashCode());
		result = prime * result + (subscribed ? 1231 : 1237);
		result = prime * result + (url == null ? 0 : url.hashCode());
		return result;
	}

	@JsonProperty
	public boolean isClosed() {
		return closed;
	}

	@JsonProperty
	public boolean isSubscribed() {
		return subscribed;
	}

	public void setAccount(final ConnectedAccount account) {
		this.account = account;
	}

	public void setClient(final Client client) {
		this.client = client;
	}

	public void setClosed(final boolean closed) {
		this.closed = closed;
	}

	public void setDateLastActivity(final Date dateLastActivity) {
		this.dateLastActivity = dateLastActivity;
	}

	public void setDateLastView(final Date dateLastView) {
		this.dateLastView = dateLastView;
	}

	public void setDesc(final String desc) {
		this.desc = desc;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public void setIdOrganization(final String idOrganization) {
		this.idOrganization = idOrganization;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setShortLink(final String shortLink) {
		this.shortLink = shortLink;
	}

	public void setShortUrl(final String shortUrl) {
		this.shortUrl = shortUrl;
	}

	public void setSubscribed(final boolean subscribed) {
		this.subscribed = subscribed;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
