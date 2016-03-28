package nz.co.trineo.git;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.internal.JGitText;

import com.google.common.html.HtmlEscapers;

import nz.co.trineo.git.model.GitDiff;
import nz.co.trineo.git.model.GitDiffHeader;
import nz.co.trineo.git.model.GitDiffLine;

public class GitDiffFormatter extends DiffFormatter {
	private final List<GitDiff> entries = new ArrayList<>();
	private int context = 3;
	private String newPath;
	private String oldPath;

	public GitDiffFormatter(final OutputStream out) {
		super(out);
		setContext(5);
		setNewPrefix("");
		setOldPrefix("");
	}

	@Override
	public void setContext(final int lineCount) {
		if (lineCount < 0) {
			throw new IllegalArgumentException(JGitText.get().contextMustBeNonNegative);
		}
		context = lineCount;
	}

	private int findCombinedEnd(final List<Edit> edits, final int i) {
		int end = i + 1;
		while (end < edits.size() && (combineA(edits, end) || combineB(edits, end))) {
			end++;
		}
		return end - 1;
	}

	private boolean combineA(final List<Edit> e, final int i) {
		return e.get(i).getBeginA() - e.get(i - 1).getEndA() <= 2 * context;
	}

	private boolean combineB(final List<Edit> e, final int i) {
		return e.get(i).getBeginB() - e.get(i - 1).getEndB() <= 2 * context;
	}

	private static boolean end(final Edit edit, final int a, final int b) {
		return edit.getEndA() <= a && edit.getEndB() <= b;
	}

	public void setPaths(final String newPath, final String oldPath) {
		this.newPath = newPath;
		this.oldPath = oldPath;
	}

	@Override
	public void format(final EditList edits, final RawText a, final RawText b) throws IOException {
		for (int curIdx = 0; curIdx < edits.size();) {
			final GitDiff di = new GitDiff();
			Edit curEdit = edits.get(curIdx);
			final int endIdx = findCombinedEnd(edits, curIdx);
			final Edit endEdit = edits.get(endIdx);

			di.setPathA(oldPath);
			di.setPathB(newPath);

			int aCur = (int) Math.max(0, (long) curEdit.getBeginA() - context);
			int bCur = (int) Math.max(0, (long) curEdit.getBeginB() - context);
			final int aEnd = (int) Math.min(a.size(), (long) endEdit.getEndA() + context);
			final int bEnd = (int) Math.min(b.size(), (long) endEdit.getEndB() + context);

			final GitDiffHeader headerA = createHeader(aCur, aEnd);
			final GitDiffHeader headerB = createHeader(bCur, bEnd);
			di.setHeaderA(headerA);
			di.setHeaderB(headerB);

			while (aCur < aEnd || bCur < bEnd) {
				if (aCur < curEdit.getBeginA() || endIdx + 1 < curIdx) {
					final GitDiffLine line = createContextLine(a, aCur, bCur);
					di.getLines().add(line);
					aCur++;
					bCur++;
				} else if (aCur < curEdit.getEndA()) {
					final GitDiffLine line = createRemovedLine(a, aCur);
					di.getLines().add(line);
					aCur++;
				} else if (bCur < curEdit.getEndB()) {
					final GitDiffLine line = createAddedLine(b, bCur);
					di.getLines().add(line);
					bCur++;
				}

				if (end(curEdit, aCur, bCur) && ++curIdx < edits.size()) {
					curEdit = edits.get(curIdx);
				}
			}
			entries.add(di);
		}
	}

	protected GitDiffHeader createHeader(final int startLine, final int endLine) {
		final GitDiffHeader header = new GitDiffHeader();
		header.setStart(startLine + 1);
		header.setEnd(endLine - startLine);
		return header;
	}

	protected GitDiffLine createContextLine(final RawText text, final int lineA, final int lineB) throws IOException {
		final GitDiffLine line = new GitDiffLine();
		line.setLineNumA(lineA);
		line.setLineNumB(lineB);
		line.setLine(createLine(text, lineA));
		return line;
	}

	protected GitDiffLine createAddedLine(final RawText text, final int lineNum) throws IOException {
		final GitDiffLine line = new GitDiffLine();
		line.setLineNumB(lineNum);
		line.setLine(createLine(text, lineNum));
		line.setAdded(true);
		return line;
	}

	protected GitDiffLine createRemovedLine(final RawText text, final int lineNum) throws IOException {
		final GitDiffLine line = new GitDiffLine();
		line.setLineNumA(lineNum);
		line.setLine(createLine(text, lineNum));
		line.setRemoved(true);
		return line;
	}

	private String createLine(final RawText text, final int cur) throws IOException {
		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		text.writeLine(stream, cur);
		final String escape = HtmlEscapers.htmlEscaper().escape(stream.toString());
		return escape;
	}

	public List<GitDiff> getEntries() {
		return entries;
	}
}
