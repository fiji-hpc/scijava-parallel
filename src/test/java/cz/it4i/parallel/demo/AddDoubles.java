
package cz.it4i.parallel.demo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.imagej.ops.math.PrimitiveMath;

import org.scijava.Context;
import org.scijava.parallel.ParallelizationParadigm;

import cz.it4i.parallel.utils.TestParadigm;

public class AddDoubles
{

	public static void main(String[] args)
	{
		Context context = new Context();
		try (ParallelizationParadigm paradigm = TestParadigm.localImageJServer(
			Config.getFijiExecutable(), context))
		{

			List<Map<String, Object>> inputs = new LinkedList<>();
			for (int i = 0; i < 100; i++) {
				Map<String, Object> params = new HashMap<>();
				params.put("a", 10 * i);
				params.put("b", 20 * i);
				inputs.add(params);
			}

			List<Map<String, Object>> result = paradigm.runAll(
				PrimitiveMath.DoubleMultiply.class, inputs);
			System.out.println("result: " + result);
		}
	}


}
