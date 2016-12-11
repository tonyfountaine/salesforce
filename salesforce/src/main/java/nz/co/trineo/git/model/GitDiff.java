package nz.co.trineo.git.model;

import java.util.ArrayList;
import java.util.List;

public class GitDiff {
	private GitDiffHeader headerA;
	private GitDiffHeader headerB;
	private String pathA;
	private String pathB;
	private final List<GitDiffLine> lines = new ArrayList<>();

	public GitDiffHeader getHeaderA() {
		return headerA;
	}

	public GitDiffHeader getHeaderB() {
		return headerB;
	}

	public List<GitDiffLine> getLines() {
		return lines;
	}

	public String getPathA() {
		return pathA;
	}

	public String getPathB() {
		return pathB;
	}

	public void setHeaderA(final GitDiffHeader aHeader) {
		headerA = aHeader;
	}

	public void setHeaderB(final GitDiffHeader bHeader) {
		headerB = bHeader;
	}

	public void setPathA(final String pathA) {
		this.pathA = pathA;
	}

	public void setPathB(final String pathB) {
		this.pathB = pathB;
	}
}
