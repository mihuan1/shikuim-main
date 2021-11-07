package cn.xyz.commons.support.jackson;

import java.io.IOException;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.TextNode;

public class ObjectIdDeserializer extends JsonDeserializer<ObjectId> {

	@Override
	public ObjectId deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
		TextNode node = (TextNode) parser.getCodec().readTree(parser);

		return new ObjectId(node.asText());
	}

}
