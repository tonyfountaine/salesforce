package nz.co.trineo.git.model;

public class GitDiffLine {
	private int lineNumA;
	private int lineNumB;
	private boolean added;
	private boolean removed;
	private String line;

	public int getLineNumA() {
		return lineNumA;
	}

	public void setLineNumA(final int lineNumA) {
		this.lineNumA = lineNumA;
	}

	public int getLineNumB() {
		return lineNumB;
	}

	public void setLineNumB(final int lineNumB) {
		this.lineNumB = lineNumB;
	}

	public boolean isAdded() {
		return added;
	}

	public void setAdded(final boolean added) {
		this.added = added;
	}

	public boolean isRemoved() {
		return removed;
	}

	public void setRemoved(final boolean removed) {
		this.removed = removed;
	}

	public String getLine() {
		return line;
	}

	public void setLine(final String line) {
		this.line = line;
	}
}
