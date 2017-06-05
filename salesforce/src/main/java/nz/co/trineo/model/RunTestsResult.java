package nz.co.trineo.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "runTestsResult")
@JsonInclude(Include.NON_DEFAULT)
public class RunTestsResult {
	@Id
	@GeneratedValue
	private int dbId;
	@Column
	private String apexLogId;
	@OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
	@JoinColumn(name = "resultId")
	private List<CodeCoverageResult> codeCoverage = new ArrayList<>();
	@OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
	@JoinColumn(name = "resultId")
	private List<CodeCoverageWarning> codeCoverageWarnings = new ArrayList<>();
	@OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
	@JoinColumn(name = "resultId")
	private List<RunTestFailure> failures = new ArrayList<>();
	@Column
	private int numFailures;
	@Column
	private int numTestsRun;
	@OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
	@JoinColumn(name = "resultId")
	private List<RunTestSuccess> successes = new ArrayList<>();
	@Column
	private double totalTime;
	@ManyToOne
	@JoinColumn(name = "orgTest")
	private Organization organization;

	@JsonProperty
	public String getApexLogId() {
		return apexLogId;
	}

	@JsonProperty
	public List<CodeCoverageResult> getCodeCoverage() {
		return codeCoverage;
	}

	@JsonProperty
	public List<CodeCoverageWarning> getCodeCoverageWarnings() {
		return codeCoverageWarnings;
	}

	@JsonProperty
	public List<RunTestFailure> getFailures() {
		return failures;
	}

	public int getId() {
		return dbId;
	}

	@JsonProperty
	public int getNumFailures() {
		return numFailures;
	}

	@JsonProperty
	public int getNumTestsRun() {
		return numTestsRun;
	}

	@JsonProperty
	public List<RunTestSuccess> getSuccesses() {
		return successes;
	}

	@JsonProperty
	public double getTotalTime() {
		return totalTime;
	}

	public void setApexLogId(final String apexLogId) {
		this.apexLogId = apexLogId;
	}

	public void setCodeCoverage(final List<CodeCoverageResult> codeCoverage) {
		this.codeCoverage = codeCoverage;
	}

	public void setCodeCoverageWarnings(final List<CodeCoverageWarning> codeCoverageWarnings) {
		this.codeCoverageWarnings = codeCoverageWarnings;
	}

	public void setFailures(final List<RunTestFailure> failures) {
		this.failures = failures;
	}

	public void setNumFailures(final int numFailures) {
		this.numFailures = numFailures;
	}

	public void setNumTestsRun(final int numTestsRun) {
		this.numTestsRun = numTestsRun;
	}

	public void setSuccesses(final List<RunTestSuccess> successes) {
		this.successes = successes;
	}

	public void setTotalTime(final double totalTime) {
		this.totalTime = totalTime;
	}
}
