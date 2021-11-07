package tigase.shiku.utils;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;

public final class HttpUtil {

	public static class Request {
		private Map<String, Object> data = new HashMap<String, Object>();
		private RequestMethod method;
		private String spec;

		public Request() {
			super();
		}

		public Request(Map<String, Object> data, String spec) {
			super();
			this.data = data;
			this.spec = spec;
		}

		public Request(Map<String, Object> data, RequestMethod method, String spec) {
			super();
			this.data = data;
			this.method = method;
			this.spec = spec;
		}

		public Map<String, Object> getData() {
			return data;
		}

		public RequestMethod getMethod() {
			return method;
		}

		public String getSpec() {
			return spec;
		}

		public void setData(Map<String, Object> data) {
			this.data = data;
		}

		public void setMethod(RequestMethod method) {
			this.method = method;
		}

		public void setSpec(String spec) {
			this.spec = spec;
		}

	}

	public static enum RequestMethod {
		DELETE, GET, HEAD, OPTIONS, PATCH, POST, PUT, TRACE
	}

	private static byte[] getBytes(Map<String, Object> data) throws Exception {
		StringBuffer sb = new StringBuffer();
		for (String key : data.keySet())
			sb.append(key).append('=').append(data.get(key)).append('&');
		return sb.substring(0, sb.length() - 1).toString().getBytes("UTF-8");
	}

	public static String get(Request request) throws Exception {
		URL url = new URL(buildSpec(request));
		HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
		urlConn.setDoOutput(true);
		urlConn.setDoInput(true);
		urlConn.setRequestMethod("GET");

		return FileUtil.readAll(urlConn.getInputStream());
	}

	private static String buildSpec(Request request) {
		Map<String, Object> params = request.getData();
		StringBuffer sb = new StringBuffer();
		sb.append(request.getSpec());
		if (!params.isEmpty())
			sb.append("?");
		for (String key : params.keySet()) {
			sb.append(key).append('=').append(params.get(key)).append('&');
		}
		String spec = sb.substring(0, sb.length() - 1);
		System.out.println(spec);
		return spec;
	}

	public static String asString(Request request) throws Exception {
		URL url = new URL(request.getSpec());
		HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
		urlConn.setDoOutput(true);
		urlConn.setDoInput(true);
		// urlConn.setRequestMethod("POST");
		// urlConn.setRequestMethod(method);
		urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		urlConn.setUseCaches(false);
		urlConn.setInstanceFollowRedirects(true);
		if (null != request.getData() && !request.getData().isEmpty()) {
			OutputStream out = urlConn.getOutputStream();
			//URLEncoder.encode(, "utf-8");
			out.write(getBytes(request.getData()));
			out.flush();
			out.close();
		}

		return FileUtil.readAll(urlConn.getInputStream());
	}

	@SuppressWarnings("unchecked")
	public static <T> T asBean(Request request, Class<?> clazz) throws Exception {
		String text = asString(request);

		return (T) JSON.parseObject(text, clazz);
	}

	public static <T> T asBean(Map<String, Object> data, String spec, Class<?> clazz) throws Exception {
		return asBean(new Request(data, spec), clazz);
	}

	public static byte[] getBytes(Object object) {
		SerializeWriter out = new SerializeWriter();
		JSONSerializer.write(out, object);
		return out.toBytes("UTF-8");
	}
}
