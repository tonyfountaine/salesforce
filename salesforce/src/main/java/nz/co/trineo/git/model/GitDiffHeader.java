package nz.co.trineo.git.model;

public class GitDiffHeader {
	private int start;
	private int end;

	public int getEnd() {
		return end;
	}

	public int getStart() {
		return start;
	}

	public void setEnd(final int end) {
		this.end = end;
	}

	public void setStart(final int start) {
		this.start = start;
	}

	@Override
	public String toString() {
		switch (end) {
		case 0:
			return start - 1 + ",0";
		case 1:
			return String.valueOf(start);

		default:
			return start + "," + end;
		}
	}
}
