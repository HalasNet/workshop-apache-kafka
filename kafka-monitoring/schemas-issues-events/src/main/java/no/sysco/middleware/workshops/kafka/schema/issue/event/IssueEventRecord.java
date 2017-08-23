/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package no.sysco.middleware.workshops.kafka.schema.issue.event;

import org.apache.avro.specific.SpecificData;

@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class IssueEventRecord extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = -9212022439337516225L;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"IssueEventRecord\",\"namespace\":\"no.sysco.middleware.workshops.kafka.schema.issue.event\",\"fields\":[{\"name\":\"id\",\"type\":\"string\"},{\"name\":\"title\",\"type\":\"string\"},{\"name\":\"descripcion\",\"type\":[\"string\",\"null\"]},{\"name\":\"type\",\"type\":\"string\"}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  @Deprecated public java.lang.CharSequence id;
  @Deprecated public java.lang.CharSequence title;
  @Deprecated public java.lang.CharSequence descripcion;
  @Deprecated public java.lang.CharSequence type;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public IssueEventRecord() {}

  /**
   * All-args constructor.
   * @param id The new value for id
   * @param title The new value for title
   * @param descripcion The new value for descripcion
   * @param type The new value for type
   */
  public IssueEventRecord(java.lang.CharSequence id, java.lang.CharSequence title, java.lang.CharSequence descripcion, java.lang.CharSequence type) {
    this.id = id;
    this.title = title;
    this.descripcion = descripcion;
    this.type = type;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return id;
    case 1: return title;
    case 2: return descripcion;
    case 3: return type;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: id = (java.lang.CharSequence)value$; break;
    case 1: title = (java.lang.CharSequence)value$; break;
    case 2: descripcion = (java.lang.CharSequence)value$; break;
    case 3: type = (java.lang.CharSequence)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'id' field.
   * @return The value of the 'id' field.
   */
  public java.lang.CharSequence getId() {
    return id;
  }

  /**
   * Sets the value of the 'id' field.
   * @param value the value to set.
   */
  public void setId(java.lang.CharSequence value) {
    this.id = value;
  }

  /**
   * Gets the value of the 'title' field.
   * @return The value of the 'title' field.
   */
  public java.lang.CharSequence getTitle() {
    return title;
  }

  /**
   * Sets the value of the 'title' field.
   * @param value the value to set.
   */
  public void setTitle(java.lang.CharSequence value) {
    this.title = value;
  }

  /**
   * Gets the value of the 'descripcion' field.
   * @return The value of the 'descripcion' field.
   */
  public java.lang.CharSequence getDescripcion() {
    return descripcion;
  }

  /**
   * Sets the value of the 'descripcion' field.
   * @param value the value to set.
   */
  public void setDescripcion(java.lang.CharSequence value) {
    this.descripcion = value;
  }

  /**
   * Gets the value of the 'type' field.
   * @return The value of the 'type' field.
   */
  public java.lang.CharSequence getType() {
    return type;
  }

  /**
   * Sets the value of the 'type' field.
   * @param value the value to set.
   */
  public void setType(java.lang.CharSequence value) {
    this.type = value;
  }

  /**
   * Creates a new IssueEventRecord RecordBuilder.
   * @return A new IssueEventRecord RecordBuilder
   */
  public static no.sysco.middleware.workshops.kafka.schema.issue.event.IssueEventRecord.Builder newBuilder() {
    return new no.sysco.middleware.workshops.kafka.schema.issue.event.IssueEventRecord.Builder();
  }

  /**
   * Creates a new IssueEventRecord RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new IssueEventRecord RecordBuilder
   */
  public static no.sysco.middleware.workshops.kafka.schema.issue.event.IssueEventRecord.Builder newBuilder(no.sysco.middleware.workshops.kafka.schema.issue.event.IssueEventRecord.Builder other) {
    return new no.sysco.middleware.workshops.kafka.schema.issue.event.IssueEventRecord.Builder(other);
  }

  /**
   * Creates a new IssueEventRecord RecordBuilder by copying an existing IssueEventRecord instance.
   * @param other The existing instance to copy.
   * @return A new IssueEventRecord RecordBuilder
   */
  public static no.sysco.middleware.workshops.kafka.schema.issue.event.IssueEventRecord.Builder newBuilder(no.sysco.middleware.workshops.kafka.schema.issue.event.IssueEventRecord other) {
    return new no.sysco.middleware.workshops.kafka.schema.issue.event.IssueEventRecord.Builder(other);
  }

  /**
   * RecordBuilder for IssueEventRecord instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<IssueEventRecord>
    implements org.apache.avro.data.RecordBuilder<IssueEventRecord> {

    private java.lang.CharSequence id;
    private java.lang.CharSequence title;
    private java.lang.CharSequence descripcion;
    private java.lang.CharSequence type;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(no.sysco.middleware.workshops.kafka.schema.issue.event.IssueEventRecord.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.id)) {
        this.id = data().deepCopy(fields()[0].schema(), other.id);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.title)) {
        this.title = data().deepCopy(fields()[1].schema(), other.title);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.descripcion)) {
        this.descripcion = data().deepCopy(fields()[2].schema(), other.descripcion);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.type)) {
        this.type = data().deepCopy(fields()[3].schema(), other.type);
        fieldSetFlags()[3] = true;
      }
    }

    /**
     * Creates a Builder by copying an existing IssueEventRecord instance
     * @param other The existing instance to copy.
     */
    private Builder(no.sysco.middleware.workshops.kafka.schema.issue.event.IssueEventRecord other) {
            super(SCHEMA$);
      if (isValidValue(fields()[0], other.id)) {
        this.id = data().deepCopy(fields()[0].schema(), other.id);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.title)) {
        this.title = data().deepCopy(fields()[1].schema(), other.title);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.descripcion)) {
        this.descripcion = data().deepCopy(fields()[2].schema(), other.descripcion);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.type)) {
        this.type = data().deepCopy(fields()[3].schema(), other.type);
        fieldSetFlags()[3] = true;
      }
    }

    /**
      * Gets the value of the 'id' field.
      * @return The value.
      */
    public java.lang.CharSequence getId() {
      return id;
    }

    /**
      * Sets the value of the 'id' field.
      * @param value The value of 'id'.
      * @return This builder.
      */
    public no.sysco.middleware.workshops.kafka.schema.issue.event.IssueEventRecord.Builder setId(java.lang.CharSequence value) {
      validate(fields()[0], value);
      this.id = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'id' field has been set.
      * @return True if the 'id' field has been set, false otherwise.
      */
    public boolean hasId() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'id' field.
      * @return This builder.
      */
    public no.sysco.middleware.workshops.kafka.schema.issue.event.IssueEventRecord.Builder clearId() {
      id = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'title' field.
      * @return The value.
      */
    public java.lang.CharSequence getTitle() {
      return title;
    }

    /**
      * Sets the value of the 'title' field.
      * @param value The value of 'title'.
      * @return This builder.
      */
    public no.sysco.middleware.workshops.kafka.schema.issue.event.IssueEventRecord.Builder setTitle(java.lang.CharSequence value) {
      validate(fields()[1], value);
      this.title = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'title' field has been set.
      * @return True if the 'title' field has been set, false otherwise.
      */
    public boolean hasTitle() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'title' field.
      * @return This builder.
      */
    public no.sysco.middleware.workshops.kafka.schema.issue.event.IssueEventRecord.Builder clearTitle() {
      title = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /**
      * Gets the value of the 'descripcion' field.
      * @return The value.
      */
    public java.lang.CharSequence getDescripcion() {
      return descripcion;
    }

    /**
      * Sets the value of the 'descripcion' field.
      * @param value The value of 'descripcion'.
      * @return This builder.
      */
    public no.sysco.middleware.workshops.kafka.schema.issue.event.IssueEventRecord.Builder setDescripcion(java.lang.CharSequence value) {
      validate(fields()[2], value);
      this.descripcion = value;
      fieldSetFlags()[2] = true;
      return this;
    }

    /**
      * Checks whether the 'descripcion' field has been set.
      * @return True if the 'descripcion' field has been set, false otherwise.
      */
    public boolean hasDescripcion() {
      return fieldSetFlags()[2];
    }


    /**
      * Clears the value of the 'descripcion' field.
      * @return This builder.
      */
    public no.sysco.middleware.workshops.kafka.schema.issue.event.IssueEventRecord.Builder clearDescripcion() {
      descripcion = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    /**
      * Gets the value of the 'type' field.
      * @return The value.
      */
    public java.lang.CharSequence getType() {
      return type;
    }

    /**
      * Sets the value of the 'type' field.
      * @param value The value of 'type'.
      * @return This builder.
      */
    public no.sysco.middleware.workshops.kafka.schema.issue.event.IssueEventRecord.Builder setType(java.lang.CharSequence value) {
      validate(fields()[3], value);
      this.type = value;
      fieldSetFlags()[3] = true;
      return this;
    }

    /**
      * Checks whether the 'type' field has been set.
      * @return True if the 'type' field has been set, false otherwise.
      */
    public boolean hasType() {
      return fieldSetFlags()[3];
    }


    /**
      * Clears the value of the 'type' field.
      * @return This builder.
      */
    public no.sysco.middleware.workshops.kafka.schema.issue.event.IssueEventRecord.Builder clearType() {
      type = null;
      fieldSetFlags()[3] = false;
      return this;
    }

    @Override
    public IssueEventRecord build() {
      try {
        IssueEventRecord record = new IssueEventRecord();
        record.id = fieldSetFlags()[0] ? this.id : (java.lang.CharSequence) defaultValue(fields()[0]);
        record.title = fieldSetFlags()[1] ? this.title : (java.lang.CharSequence) defaultValue(fields()[1]);
        record.descripcion = fieldSetFlags()[2] ? this.descripcion : (java.lang.CharSequence) defaultValue(fields()[2]);
        record.type = fieldSetFlags()[3] ? this.type : (java.lang.CharSequence) defaultValue(fields()[3]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  private static final org.apache.avro.io.DatumWriter
    WRITER$ = new org.apache.avro.specific.SpecificDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  private static final org.apache.avro.io.DatumReader
    READER$ = new org.apache.avro.specific.SpecificDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

}
