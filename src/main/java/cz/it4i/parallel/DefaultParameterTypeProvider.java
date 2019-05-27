package cz.it4i.parallel;

import static cz.it4i.parallel.Routines.supplyWithExceptionHandling;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DefaultParameterTypeProvider implements
	ParameterTypeProvider
{

	final private Map<String, Map<String, String>> mappedTypes = new HashMap<>();

	private int port;
	private String host;

	@Override
	public String provideParameterTypeName(String commandName,
		String parameterName)
	{
		Map<String, String> paramToClass = mappedTypes.computeIfAbsent(
			commandName, c -> obtainCommandInfo(c));
		return paramToClass.get(parameterName);
	}

	private Map<String, String> obtainCommandInfo(String commandTypeName) {
		Map<String, String> result = new HashMap<>();
		final String getUrl = "http://" + host + ":" + port + "/modules/" +
			"command:" + commandTypeName;
		final HttpGet httpGet = new HttpGet(getUrl);
		final HttpResponse response = supplyWithExceptionHandling(
			() -> HttpClientBuilder.create().build().execute(httpGet));
		org.json.JSONObject json = supplyWithExceptionHandling(
			() -> new org.json.JSONObject(EntityUtils.toString(response
				.getEntity())));

		processParamaters(result, json, "inputs");
		processParamaters(result, json, "outputs");

		return result;
	}

	private void processParamaters(Map<String, String> result,
		org.json.JSONObject json, String direction)
	{
		org.json.JSONArray inputs = (org.json.JSONArray) json.get(direction);
		Iterator<?> iter = inputs.iterator();
		while (iter.hasNext()) {
			org.json.JSONObject param = (org.json.JSONObject) iter.next();
			String typeName = ((String) param.get("genericType")).trim();
			typeName = clearTypeName(typeName);
			if (Character.isLowerCase(typeName.charAt(0)) && !typeName.contains(
				"."))
			{
				typeName = "java.lang." + Character.toUpperCase(typeName.charAt(0)) +
					typeName.substring(1);
			}
			result.put((String) param.get("name"), typeName);
		}
	}

	private String clearTypeName(String typeName) {
		String[] prefixes = { "class", "interface" };
		for (String prefix : prefixes) {
			if (typeName.startsWith(prefix)) {
				typeName = typeName.substring(prefix.length()).trim();
			}
		}
		typeName = typeName.replaceAll("<[^>]*>", "");
		return typeName;
	}

}