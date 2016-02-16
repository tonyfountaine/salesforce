package nz.co.trineo.diff;

import static org.apache.commons.io.FilenameUtils.getName;
import static org.eclipse.jgit.diff.RawTextComparator.WS_IGNORE_ALL;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.HistogramDiff;
import org.eclipse.jgit.diff.RawText;

import nz.co.trineo.diff.model.Diff;

public class DiffService {
	private static final Log log = LogFactory.getLog(DiffService.class);

	private final DiffDAO dao;

	public DiffService(final DiffDAO dao) {
		super();
		this.dao = dao;
	}

	public Diff doDiff() throws DiffException {
		try (final ByteArrayOutputStream out = new ByteArrayOutputStream();
				final DiffFormatter diffFormatter = new DiffFormatter(out);) {
			final HistogramDiff histogramDiff = new HistogramDiff();
			final File filea = new File("/tmp/git/ccl/src/pages/CPOrderExtras.page");
			final RawText a = new RawText(filea);
			log.info(a.getString(0));
			final File fileb = new File("/tmp/salesforce/00DO00000004HhWMAU/unpackaged/pages/CPOrderExtras.page");
			final RawText b = new RawText(fileb);
			log.info(b.getString(0));
			final EditList editList = histogramDiff.diff(WS_IGNORE_ALL, a, b);
			log.info(editList.size());
			log.info(editList.get(0).toString());
			diffFormatter.format(editList, a, b);
			final String string = out.toString();
			log.info(string);
			final Diff diff = new Diff();
			diff.getModified().put(getName(filea.getName()), string);
			dao.persist(diff);
			return diff;
		} catch (final IOException e) {
			throw new DiffException(e);
		}
	}
}
