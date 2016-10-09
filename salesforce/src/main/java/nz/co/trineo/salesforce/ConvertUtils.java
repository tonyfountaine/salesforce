package nz.co.trineo.salesforce;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sforce.soap.partner.sobject.SObject;

import nz.co.trineo.salesforce.model.CodeCoverageResult;
import nz.co.trineo.salesforce.model.CodeCoverageWarning;
import nz.co.trineo.salesforce.model.CodeLocation;
import nz.co.trineo.salesforce.model.Organization;
import nz.co.trineo.salesforce.model.RunTestFailure;
import nz.co.trineo.salesforce.model.RunTestSuccess;
import nz.co.trineo.salesforce.model.RunTestsResult;

public class ConvertUtils {
	private static final Log log = LogFactory.getLog(ConvertUtils.class);

	public static RunTestsResult toRunTestsResult(final com.sforce.soap.apex.RunTestsResult result) {
		log.info(result);
		if (result == null) {
			return null;
		}
		final RunTestsResult runTestsResult = new RunTestsResult();
		runTestsResult.setApexLogId(result.getApexLogId());
		runTestsResult.setNumFailures(result.getNumFailures());
		runTestsResult.setNumTestsRun(result.getNumTestsRun());
		runTestsResult.setTotalTime(result.getTotalTime());
		for (final com.sforce.soap.apex.CodeCoverageResult coverageResult : result.getCodeCoverage()) {
			runTestsResult.getCodeCoverage().add(toCodeCoverageResult(coverageResult));
		}
		for (final com.sforce.soap.apex.CodeCoverageWarning coverageWarning : result.getCodeCoverageWarnings()) {
			runTestsResult.getCodeCoverageWarnings().add(toCodeCoverageWarning(coverageWarning));
		}
		for (final com.sforce.soap.apex.RunTestFailure testFailure : result.getFailures()) {
			runTestsResult.getFailures().add(toRunTestFailure(testFailure));
		}
		for (final com.sforce.soap.apex.RunTestSuccess testSuccess : result.getSuccesses()) {
			runTestsResult.getSuccesses().add(toRunTestSuccess(testSuccess));
		}
		return runTestsResult;
	}

	public static CodeCoverageResult toCodeCoverageResult(final com.sforce.soap.apex.CodeCoverageResult result) {
		final CodeCoverageResult coverageResult = new CodeCoverageResult();
		coverageResult.setId(result.getId());
		for (final com.sforce.soap.apex.CodeLocation codeLocation : result.getLocationsNotCovered()) {
			coverageResult.getLocationsNotCovered().add(toCodeLocation(codeLocation));
		}
		coverageResult.setName(result.getName());
		coverageResult.setNamespace(result.getNamespace());
		coverageResult.setNumLocations(result.getNumLocations());
		coverageResult.setNumLocationsNotCovered(result.getNumLocationsNotCovered());
		coverageResult.setType(result.getType());
		return coverageResult;
	}

	public static CodeCoverageWarning toCodeCoverageWarning(final com.sforce.soap.apex.CodeCoverageWarning warning) {
		final CodeCoverageWarning coverageWarning = new CodeCoverageWarning();
		coverageWarning.setId(warning.getId());
		coverageWarning.setMessage(warning.getMessage());
		coverageWarning.setName(warning.getName());
		coverageWarning.setNamespace(warning.getNamespace());
		return coverageWarning;
	}

	public static CodeLocation toCodeLocation(final com.sforce.soap.apex.CodeLocation location) {
		final CodeLocation codeLocation = new CodeLocation();
		codeLocation.setColumn(location.getColumn());
		codeLocation.setLine(location.getLine());
		codeLocation.setNumExecutions(location.getNumExecutions());
		codeLocation.setTime(location.getTime());
		return codeLocation;
	}

	public static RunTestFailure toRunTestFailure(final com.sforce.soap.apex.RunTestFailure failure) {
		final RunTestFailure runTestFailure = new RunTestFailure();
		runTestFailure.setId(failure.getId());
		runTestFailure.setMessage(failure.getMessage());
		runTestFailure.setMethodName(failure.getMethodName());
		runTestFailure.setName(failure.getName());
		runTestFailure.setNamespace(failure.getNamespace());
		runTestFailure.setSeeAllData(failure.getSeeAllData());
		runTestFailure.setStackTrace(failure.getStackTrace());
		runTestFailure.setTime(failure.getTime());
		runTestFailure.setType(failure.getType());
		return runTestFailure;
	}

	public static RunTestSuccess toRunTestSuccess(final com.sforce.soap.apex.RunTestSuccess success) {
		final RunTestSuccess runTestSuccess = new RunTestSuccess();
		runTestSuccess.setId(success.getId());
		runTestSuccess.setMethodName(success.getMethodName());
		runTestSuccess.setName(success.getName());
		runTestSuccess.setNamespace(success.getNamespace());
		runTestSuccess.setSeeAllData(success.getSeeAllData());
		runTestSuccess.setTime(success.getTime());
		return runTestSuccess;
	}

	public static Organization toOrganization(final SObject sObject) {
		log.info(sObject);
		final Organization organization = new Organization();
		organization.setId(sObject.getId());
		organization.setName((String) sObject.getField("Name"));
		organization.setOrganizationType((String) sObject.getField("OrganizationType"));
		organization.setSandbox(Boolean.valueOf((String) sObject.getField("IsSandbox")));
		return organization;
	}
}
