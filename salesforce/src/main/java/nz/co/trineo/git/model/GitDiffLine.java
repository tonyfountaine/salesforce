package nz.co.trineo.git.model;

public class GitDiffLine {
	private int lineNumA;
	private int lineNumB;
	private boolean added;
	private boolean removed;
	private String line;

	public String getLine() {
		return line;
	}

	public int getLineNumA() {
		return lineNumA;
	}

	public int getLineNumB() {
		return lineNumB;
	}

	public boolean isAdded() {
		return added;
	}

	public boolean isRemoved() {
		return removed;
	}

	public void setAdded(final boolean added) {
		this.added = added;
	}

	public void setLine(final String line) {
		this.line = line;
	}

	public void setLineNumA(final int lineNumA) {
		this.lineNumA = lineNumA;
	}

	public void setLineNumB(final int lineNumB) {
		this.lineNumB = lineNumB;
	}

	public void setRemoved(final boolean removed) {
		this.removed = removed;
	}
}
