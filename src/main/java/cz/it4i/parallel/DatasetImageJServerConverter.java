
package cz.it4i.parallel;

import static cz.it4i.parallel.Routines.castTo;
import static cz.it4i.parallel.Routines.getSuffix;

import io.scif.services.DatasetIOService;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import net.imagej.Dataset;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = ParallelizationParadigmConverter.class)
public class DatasetImageJServerConverter extends
	AbstractParallelizationParadigmConverter<Dataset> implements Closeable
{

	private static final String NAME_FOR_EXPORT = "export";

	private static final String PREFIX_OF_TEMPDIR_FOR_EXPORT = "scijava-parallel";

	private static final String DEFAULT_SUFFIX = ".tif";
	@Parameter
	private DatasetIOService ioService;

	private RemoteDataHandler parallelWorker;

	private Dataset workingDataSet;
	private Object workingDataSetID;

	public DatasetImageJServerConverter() {
		super(Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
			ImageJServerParadigm.class))), Dataset.class);
	}

	@Override
	public ParallelizationParadigmConverter<Dataset> cloneForWorker(
		RemoteDataHandler worker)
	{
		// Remark: better use ThreadLocal in this class, than
		// explicitly calling cloneForWorker.
		DatasetImageJServerConverter result =
			new DatasetImageJServerConverter();
		result.ioService = ioService;
		result.parallelWorker = worker;
		return result;
	}

	@Override
	public <T> T convert(Object src, Class<T> dest) {
		if (dest == Object.class) {
			return castTo(convert2Paradigm(src));
		}
		return castTo(convert2Local(src));
	}
	
	@Override
	public void close() throws IOException {
		// ignore this
	}

	private Object convert2Paradigm(Object input) {
		if (input instanceof Path) {
			throw new UnsupportedOperationException(
				"Using path instead of Dataset is not supported.");
		}
		else if (input instanceof Dataset) {
			workingDataSet = (Dataset) input;
			Path tempFileForWorkingDataSet = Routines.getTempFileForSuffix(getSuffix(
				workingDataSet.getName()));
			try {
				Routines.runWithExceptionHandling(() -> ioService.save((Dataset) input,
					tempFileForWorkingDataSet.toString()));
				workingDataSetID = parallelWorker.importData(tempFileForWorkingDataSet);
				return workingDataSetID;
			}
			finally {
				Routines.runWithExceptionHandling(() -> Files.deleteIfExists(
					tempFileForWorkingDataSet));
			}
		}
		throw new IllegalArgumentException("cannot convert from " + input
			.getClass());
	}

	private Object convert2Local(Object input) {
		// Remark: The download shouldn't depend on how the upload happend before.
		// This connection between upload and download is artificial, and
		// makes the download rather unstable.
		final Path tempFileForWorkingDataSet = Routines.supplyWithExceptionHandling(
			() -> Files.createTempDirectory(PREFIX_OF_TEMPDIR_FOR_EXPORT).resolve(
				NAME_FOR_EXPORT + getSuffixForExport()));
		try {
			parallelWorker.exportData(input, tempFileForWorkingDataSet);
			parallelWorker.deleteData(input);
					Dataset tempDataset = Routines.supplyWithExceptionHandling(() -> ioService.open(
				tempFileForWorkingDataSet.toString()));
			if (workingDataSet != null && input.equals(workingDataSetID)) {
				tempDataset.copyInto(workingDataSet);
				return workingDataSet;
			}
			return tempDataset;

		}
		finally {
			Routines.runWithExceptionSuppress(() -> Files.deleteIfExists(
				tempFileForWorkingDataSet));
			Routines.runWithExceptionSuppress(() -> Files.deleteIfExists(
				tempFileForWorkingDataSet.getParent()));
		}
	}

	private String getSuffixForExport() {
		if (workingDataSet != null) {
			return getSuffix(workingDataSet.getName());
		}
		return DEFAULT_SUFFIX;
	}

}
