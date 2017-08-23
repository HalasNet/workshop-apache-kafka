package no.sysco.middleware.workshops.kafka.schemas;

import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/** */
public class AvroSpecificSerializer<T extends SpecificRecordBase> {
  public byte[] serialize(T record) {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);
      new SpecificDatumWriter<>(record.getSchema()).write(record, encoder);
      encoder.flush();
      return out.toByteArray();
    } catch (IOException ex) {
      throw new RuntimeException("Error serializing event", ex);
    }
  }
}
