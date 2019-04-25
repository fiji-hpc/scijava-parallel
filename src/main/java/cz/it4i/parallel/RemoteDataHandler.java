
package cz.it4i.parallel;

import java.nio.file.Path;

public interface RemoteDataHandler {

	Object importData(Path filePath);

	void exportData(Object data, Path filePath);

	void deleteData(Object ds);

}
