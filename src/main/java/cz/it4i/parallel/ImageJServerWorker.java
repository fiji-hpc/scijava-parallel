
package cz.it4i.parallel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.ws.rs.core.Response;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.AbstractContentBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.scijava.Context;
import org.scijava.plugin.SciJavaPlugin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImageJServerWorker implements ParallelWorker {

	private static final String HTTP_PROTOCOL = "http://";
	private final String hostName;
	private final int port;

	private static final Set<String> supportedImageTypes = Collections
		.unmodifiableSet(new HashSet<>(Arrays.asList("png", "jpg", "tif")));

	private final Map<String, PRemoteObject> id2importedData =
		new HashMap<>();

	ImageJServerWorker(final String hostName, final int port) {
		this.hostName = hostName;
		this.port = port;
	}

	public String getHostName() {
		return hostName;
	}

	public int getPort() {
		return port;
	}

	// -- ParallelWorker methods --

	@Override
	public Object importData(final Path path) {

		final String filePath = path.toAbsolutePath().toString();
		final String fileName = path.getFileName().toString();
		return importData(new FileBody(new File(filePath), ContentType.create(
			getContentType(filePath)), fileName));
	}

	public Object importData(final String fileName, long length,
		Consumer<OutputStream> osConsumer)
	{

		return importData(new AbstractContentBody(ContentType.create(getContentType(
			fileName)))
		{

			@Override
			public String getTransferEncoding() {
				return MIME.ENC_BINARY;
			}

			@Override
			public long getContentLength() {
				return length;
			}

			@Override
			public void writeTo(OutputStream out) throws IOException {
				osConsumer.accept(out);
			}

			@Override
			public String getFilename() {
				return fileName;
			}
		});
	}

	@Override
	public void exportData(final Object dataset, final Path p) {

		final String filePath = p.toString();
		final String fileName = p.getFileName().toString();
		try (OutputStream os = new BufferedOutputStream(new FileOutputStream(
			new File(filePath))))
		{
			exportData(dataset, fileName, t -> {

				int inByte;
				try {
					while ((inByte = t.read()) != -1) {
						os.write(inByte);
					}
				}
				catch (IOException exc) {
					log.error("", exc);
				}
			});
		}
		catch (final Exception e) {
			log.error("", e);
		}
	}

	@Override
	public void deleteData(final Object dataset) {

		final String objectId = getObjectId(dataset);

		try {
			final String postUrl = HTTP_PROTOCOL + hostName + ":" + port +
				"/objects/" + objectId;
			final HttpDelete httpDelete = new HttpDelete(postUrl);
			HttpClientBuilder.create().build().execute(httpDelete);
		}
		catch (final Exception e) {
			throw new SciJavaParallelRuntimeException(e);
		}
	}

	/**
	 * @throws RuntimeException if response from the ImageJ server is not successful, or json cannot be parsed properly.
	 */
	@Override
	public Map<String, Object> executeCommand(final String commandTypeName,
		final Map<String, ?> inputs)
	{
		final Map<String, ?> wrappedInputs = wrapInputMap(inputs);
		return unwrapOutputMap(doRequest(commandTypeName, wrappedInputs));
	}

	@Override
	public List<Map<String, Object>> executeCommand(final String commandTypeName,
		final List<Map<String, Object>> inputs)
	{
		if (inputs.isEmpty()) {
			return Collections.emptyList();
		}
		else if (inputs.size() == 1) {
			return Collections.singletonList(executeCommand(commandTypeName, inputs
				.get(0)));
		}

		Map<String, Object> inputForExecution = new HashMap<>();
		inputForExecution.put("moduleId", commandTypeName);
		inputForExecution.put("inputs", inputs.stream().map(this::wrapInputMap)
			.collect(Collectors.toList()));
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> output = (List<Map<String, Object>>) doRequest(
			"cz.it4i.parallel.plugins.ThreadRunner", inputForExecution).get(
				"outputs");
		return output.stream().map(this::unwrapOutputMap).collect(Collectors
			.toList());

	}

	// -- Helper methods --

	private Map<String, Object> doRequest(final String commandTypeName,
		final Map<String, ?> wrappedInputs)
	{
		try {
	
			final JSONObject inputJson = toJson(wrappedInputs);
			final String postUrl = HTTP_PROTOCOL + hostName + ":" + port +
				"/modules/" + "command:" + commandTypeName;
			final HttpPost httpPost = new HttpPost(postUrl);
			httpPost.setEntity(new StringEntity(inputJson.toString()));
			httpPost.setHeader("Content-type", "application/json");
	
			final HttpResponse response = HttpClientBuilder.create().build().execute(
				httpPost);
	
			int statusCode = response.getStatusLine().getStatusCode();
			boolean success = Response.Status.fromStatusCode(statusCode).getFamily() == Response.Status.Family.SUCCESSFUL;
			if ( !success ) {
				throw new SciJavaParallelRuntimeException("Command cannot be executed" +
					response.getStatusLine() + " " + response.getEntity());
			}
	
			String json = EntityUtils.toString( response.getEntity() );
			final org.json.JSONObject jsonObj = new org.json.JSONObject(json);
	
			return jsonToMap(jsonObj);
		}
		catch ( IOException e )
		{
			throw new SciJavaParallelRuntimeException(e);
		}
	}

	private void exportData(final Object dataset, final String filePath,
		Consumer<InputStream> isConsumer) throws IOException
	{

		final String objectId = getObjectId(dataset);
		final String getUrl = HTTP_PROTOCOL + hostName + ":" + port +
			"/objects/" + objectId + "/" + getImageType(filePath);
		final HttpGet httpGet = new HttpGet(getUrl);
	
		final HttpEntity entity = HttpClientBuilder.create().build().execute(
			httpGet).getEntity();
		if (entity != null) {
			try (BufferedInputStream bis = new BufferedInputStream(entity
				.getContent()))
			{
				isConsumer.accept(bis);
			}
		}
	}

	private String getObjectId(final Object dataset) {
		if (dataset instanceof String) {
			return (String) dataset;
		}
		return ((PRemoteObject) dataset).getId();
	}

	private Map<String, Object> jsonToMap(final org.json.JSONObject jsonObj) {
		final Map<String, Object> rawOutputs = new HashMap<>();
		jsonObj.keys().forEachRemaining(key -> rawOutputs.put(key, restobj2localobj(
			jsonObj.get(key))));
		return rawOutputs;
	}

	private Object restobj2localobj(Object object) {
		if (object instanceof JSONArray) {
			JSONArray array = (JSONArray) object;
			return StreamSupport.stream(array.spliterator(), false).map(
				this::restobj2localobj).collect(Collectors.toList());
		}
		else if (object instanceof org.json.JSONObject) {
			org.json.JSONObject jsonObject = (org.json.JSONObject) object;
			return jsonToMap(jsonObject);
		}
		return object;
	}

	@SuppressWarnings("unchecked")
	private JSONObject toJson(Map<String, ?> inputs) {
		JSONObject result = new JSONObject();
	
		for (final Map.Entry<String, ?> pair : inputs.entrySet()) {
			result.put(pair.getKey(), pair.getValue());
		}
		return result;
	}

	private PRemoteObject importData(ContentBody contentBody) {
		return Routines.supplyWithExceptionHandling(() -> {

			final String postUrl = HTTP_PROTOCOL + hostName + ":" + port +
				"/objects/upload";
			final HttpPost httpPost = new HttpPost(postUrl);

			final HttpEntity entity = MultipartEntityBuilder.create().addPart("file",
				contentBody).build();
			httpPost.setEntity(entity);

			final HttpResponse response = HttpClientBuilder.create().build().execute(
				httpPost);

			final String json = EntityUtils.toString(response.getEntity());
			org.json.JSONObject result = new org.json.JSONObject(json);
			return indexImported(new PRemoteObject(result));
		});
	}

	private PRemoteObject indexImported(PRemoteObject obj) {
		id2importedData.put(obj.getId(), obj);
		return obj;
	}

	// TODO: support another types
	private String getContentType(final String path) {
		return "image/" + getImageType(path);
	}

	private String getImageType(final String path) {
		for (final String type : supportedImageTypes) {
			if (path.endsWith("." + type)) {
				return type;
			}
		}

		throw new UnsupportedOperationException("Only " + supportedImageTypes +
			" image files supported");
	}

	private Map<String, Object> wrapInputMap(final Map<String, ?> map) {
		return new PInputOutputValueConvertor(ImageJServerWorker::isEntryResolvable,
			this::wrapValue).convertMap(map);
	}

	private Map<String, Object> unwrapOutputMap(
		final Map<String, Object> map)
	{
		return new PInputOutputValueConvertor(ImageJServerWorker::isEntryResolvable,
			this::unwrapValue).convertMap(map);
	}

	private Object wrapValue(Object value) {
		if (value instanceof PRemoteObject) {
			final PRemoteObject obj = (PRemoteObject) value;
			return obj.getId();
		}
		return value;
	}

	private Object unwrapValue(Object value) {
		final Object obj = id2importedData.get(value);
		if (obj != null) {
			value = obj;
		}
		return value;
	}

	/**
	 * Determines whether an entry is resolvable from the SciJava Context
	 */
	private static boolean isEntryResolvable(final Map.Entry<String, ?> entry) {
		return entry.getValue() != null && !(entry
			.getValue() instanceof SciJavaPlugin) && !(entry
				.getValue() instanceof Context);
	}

	private class PRemoteObject {

		@Getter
		final String id;

		public PRemoteObject(org.json.JSONObject jsonObj) {
			id = jsonObj.getString("id");
		}

	}

	@AllArgsConstructor
	private class PInputOutputValueConvertor {

		/**
		 * a filter to be applied on all map entries prior the actual conversion
		 */
		final Function<Map.Entry<String, ?>, Boolean> filter;

		/**
		 * a conversion applied to all values
		 */
		final Function<Object, Object> conversion;

		/**
		 * Converts an input map into an output map
		 * 
		 * @param map - an input map
		 * @return a converted map
		 */
		Map<String, Object> convertMap(final Map<String, ?> map)
		{
			return map.entrySet().stream().filter(filter::apply).map(
				entry -> new SimpleImmutableEntry<>(entry.getKey(), convertObject(entry
					.getValue()))).collect(Collectors.toMap(
						SimpleImmutableEntry<String, Object>::getKey,
						SimpleImmutableEntry<String, Object>::getValue));
		}

		Object convertObject(Object value) {
			if (value instanceof Map) {
				@SuppressWarnings({ "unchecked" })
				Map<String, Object> map = (Map<String, Object>) value;
				return convertMap(map);
			}
			else if (value instanceof List) {
				@SuppressWarnings("unchecked")
				List<Object> list = (List<Object>) value;
				return convertList(list);
			}
			return conversion.apply(value);
		}

		Object convertList(List<Object> list) {
			return list.stream().map(this::convertObject).collect(Collectors
				.toList());
		}
	}
}
