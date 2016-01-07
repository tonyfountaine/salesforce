package nz.co.trineo.diff;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.HistogramDiff;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;

public class DiffService {
	private static final Log log = LogFactory.getLog(DiffService.class);

	public String doDiff() throws DiffException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (DiffFormatter diffFormatter = new DiffFormatter(out);) {
			HistogramDiff histogramDiff = new HistogramDiff();
			RawText a = new RawText(
					new File(
							"D:\\tmp\\backup\\2015-12-31-23-11-42\\unpackaged\\classes\\testClass.cls"));
			log.info(a.getString(0));
			RawText b = new RawText(new File(
					"D:\\tmp\\salesforce\\unpackaged\\classes\\testClass.cls"));
			log.info(b.getString(0));
			EditList editList = histogramDiff.diff(RawTextComparator.DEFAULT,
					a, b);
			log.info(editList.size());
			log.info(editList.get(0).toString());
			diffFormatter.format(editList, a, b);
			String string = out.toString();
			log.info(string);
			return string;
		} catch (IOException e) {
			throw new DiffException(e);
		}
	}
}
