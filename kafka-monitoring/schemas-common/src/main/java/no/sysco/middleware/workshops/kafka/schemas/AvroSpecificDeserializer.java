package no.sysco.middleware.workshops.kafka.schemas;

import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;

import java.io.IOException;

/** */
public class AvroSpecificDeserializer<T extends SpecificRecordBase> {

  private final Class<T> type;

  public AvroSpecificDeserializer(Class<T> type) {
    this.type = type;
  }

  public T deserialize(byte[] recordSerialized) {
    try {
      return new SpecificDatumReader<>(type)
          .read(null, DecoderFactory.get().binaryDecoder(recordSerialized, null));
    } catch (IOException ex) {
      throw new RuntimeException("Error deserializing event", ex);
    }
  }
}
