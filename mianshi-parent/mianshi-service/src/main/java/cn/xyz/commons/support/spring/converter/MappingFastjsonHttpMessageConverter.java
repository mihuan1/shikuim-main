package cn.xyz.commons.support.spring.converter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import org.bson.types.ObjectId;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class MappingFastjsonHttpMessageConverter extends
		AbstractHttpMessageConverter<Object> {

	public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

	private SerializerFeature[] serializerFeatures = new SerializerFeature[] {
	// SerializerFeature.WriteMapNullValue,
	// SerializerFeature.WriteNullListAsEmpty,
	// SerializerFeature.WriteNullNumberAsZero,
	// SerializerFeature.WriteNullStringAsEmpty,
	// SerializerFeature.QuoteFieldNames,
	// SerializerFeature.WriteDateUseDateFormat

	};
	// private SerializerFeature[] serializerFeatures = new SerializerFeature[]
	// {
	// SerializerFeature.WriteMapNullValue,
	// SerializerFeature.WriteNullListAsEmpty,
	// SerializerFeature.WriteNullNumberAsZero,
	// SerializerFeature.WriteNullStringAsEmpty,
	// SerializerFeature.QuoteFieldNames
	// };

	private SerializeConfig config;

	public SerializerFeature[] getSerializerFeatures() {
		return serializerFeatures;
	}

	public void setSerializerFeatures(SerializerFeature[] serializerFeature) {

		this.serializerFeatures = serializerFeature;
	}

	public MappingFastjsonHttpMessageConverter() {
		super(new MediaType("application", "json", DEFAULT_CHARSET));

		config = new SerializeConfig();
		config.put(ObjectId.class, new ObjectIdSerializer());
	}

	@Override
	public boolean canRead(Class<?> clazz, MediaType mediaType) {
		return true;
	}

	@Override
	public boolean canWrite(Class<?> clazz, MediaType mediaType) {
		return true;
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		int i;
		while ((i = inputMessage.getBody().read()) != -1) {
			byteArrayOutputStream.write(i);
		}
		return JSON.parseArray(byteArrayOutputStream.toString(), clazz);
	}

	@Override
	protected void writeInternal(Object object, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		HttpHeaders headers = outputMessage.getHeaders();
		headers.add("Access-Control-Allow-Origin", "*");
		headers.add("Access-Control-Allow-Methods",
				"POST, GET, OPTIONS, DELETE");
		headers.add("Access-Control-Max-Age", "3600");
		headers.add("Access-Control-Allow-Headers", "x-requested-with");
		
		String text = JSON.toJSONString(object, config, serializerFeatures);
		byte[] b = text.getBytes(DEFAULT_CHARSET);
		OutputStream out = outputMessage.getBody();
		out.write(b);
		out.flush();
		out.close();
	}

	public static class ObjectIdSerializer implements ObjectSerializer {

		/*@Override
		public void write(JSONSerializer serializer, Object object,
				Object fieldName, Type fieldType) throws IOException {
			SerializeWriter out = serializer.getWriter();
			if (object == null) {
				serializer.getWriter().writeNull();
				return;
			}
			out.write("\"" + ((ObjectId) object).toString() + "\"");
		}*/

		@Override
		public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features)
				throws IOException {
			SerializeWriter out = serializer.getWriter();
			if (object == null) {
				serializer.getWriter().writeNull();
				return;
			}
			out.write("\"" + ((ObjectId) object).toString() + "\"");
			
		}

	}

}