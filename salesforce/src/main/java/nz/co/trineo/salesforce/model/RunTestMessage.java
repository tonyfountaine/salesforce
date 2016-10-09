package nz.co.trineo.salesforce.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import com.fasterxml.jackson.annotation.JsonProperty;

@MappedSuperclass
public abstract class RunTestMessage implements Comparable<RunTestMessage> {
	@Id
	@GeneratedValue
	private int dbId;
	@Column
	private String id;
	@Column
	private String methodName;
	@Column
	private String name;
	@Column
	private String namespace;
	@Column
	private boolean seeAllData;
	@Column
	private double time;

	@JsonProperty
	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	@JsonProperty
	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(final String methodName) {
		this.methodName = methodName;
	}

	@JsonProperty
	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@JsonProperty
	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(final String namespace) {
		this.namespace = namespace;
	}

	@JsonProperty
	public boolean isSeeAllData() {
		return seeAllData;
	}

	public void setSeeAllData(final boolean seeAllData) {
		this.seeAllData = seeAllData;
	}

	@JsonProperty
	public double getTime() {
		return time;
	}

	public void setTime(final double time) {
		this.time = time;
	}

	private int compareString(final String str1, final String str2) {
		if (str1 == null && str2 == null) {
			return 0;
		}
		if (str1 == null && str2 != null) {
			return -1;
		}
		if (str1 != null && str2 == null) {
			return 1;
		}
		return str1.compareTo(str2);
	}

	@Override
	public int compareTo(final RunTestMessage that) {
		int compare = compareString(namespace, that.namespace);
		if (compare == 0) {
			compare = compareString(name, that.name);
		}
		if (compare == 0) {
			compare = compareString(methodName, that.methodName);
		}
		return compare;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dbId;
		result = prime * result + (id == null ? 0 : id.hashCode());
		result = prime * result + (methodName == null ? 0 : methodName.hashCode());
		result = prime * result + (name == null ? 0 : name.hashCode());
		result = prime * result + (namespace == null ? 0 : namespace.hashCode());
		result = prime * result + (seeAllData ? 1231 : 1237);
		long temp;
		temp = Double.doubleToLongBits(time);
		result = prime * result + (int) (temp ^ temp >>> 32);
		return result;
	}

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
		final RunTestMessage other = (RunTestMessage) obj;
		if (dbId != other.dbId) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (methodName == null) {
			if (other.methodName != null) {
				return false;
			}
		} else if (!methodName.equals(other.methodName)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (namespace == null) {
			if (other.namespace != null) {
				return false;
			}
		} else if (!namespace.equals(other.namespace)) {
			return false;
		}
		if (seeAllData != other.seeAllData) {
			return false;
		}
		if (Double.doubleToLongBits(time) != Double.doubleToLongBits(other.time)) {
			return false;
		}
		return true;
	}
}