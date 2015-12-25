package bloomberg.presto.accumulo.model;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;

import com.facebook.presto.spi.block.ArrayBlock;
import com.facebook.presto.spi.block.Block;
import com.facebook.presto.spi.type.DateType;
import com.facebook.presto.spi.type.StandardTypes;
import com.facebook.presto.spi.type.TimeType;
import com.facebook.presto.spi.type.TimestampType;
import com.facebook.presto.spi.type.Type;
import com.facebook.presto.spi.type.VarbinaryType;

import bloomberg.presto.accumulo.Types;

public class Field {
    private Object value;
    private Type type;

    public Field(Object v, Type t) {
        this.value = Field.cleanObject(v, t);
        this.type = t;
    }

    public Field(Field f) {
        this.type = f.type;

        if (Types.isArrayType(this.type)) {
            this.value = f.value;
            return;
        }

        switch (type.getDisplayName()) {
        case StandardTypes.BIGINT:
            this.value = new Long(f.getBigInt());
            break;
        case StandardTypes.BOOLEAN:
            this.value = new Boolean(f.getBoolean());
            break;
        case StandardTypes.DATE:
            this.value = new Date(f.getDate().getTime());
            break;
        case StandardTypes.DOUBLE:
            this.value = new Double(f.getDouble());
            break;
        case StandardTypes.TIME:
            this.value = new Time(f.getTime().getTime());
            break;
        case StandardTypes.TIMESTAMP:
            this.value = new Timestamp(f.getTimestamp().getTime());
            break;
        case StandardTypes.VARBINARY:
            this.value = Arrays.copyOf(f.getVarbinary(),
                    f.getVarbinary().length);
            break;
        case StandardTypes.VARCHAR:
            this.value = new String(f.getVarchar());
            break;
        default:
            throw new UnsupportedOperationException("Unsupported type " + type);
        }
    }

    public Type getType() {
        return type;
    }

    public Block getArray() {
        return (Block) value;
    }

    public Long getBigInt() {
        return (Long) value;
    }

    public Block getBlock() {
        return (Block) value;
    }

    public Boolean getBoolean() {
        return (Boolean) value;
    }

    public Date getDate() {
        return (Date) value;
    }

    public void setDate(long value) {
        ((Date) this.value).setTime(value);
    }

    public Double getDouble() {
        return (Double) value;
    }

    public Object getIntervalDatToSecond() {
        throw new UnsupportedOperationException();
    }

    public Object getIntervalYearToMonth() {
        throw new UnsupportedOperationException();
    }

    public Object getObject() {
        return value;
    }

    public Timestamp getTimestamp() {
        return (Timestamp) value;
    }

    public Object getTimestampWithTimeZone() {
        throw new UnsupportedOperationException();
    }

    public Time getTime() {
        return (Time) value;
    }

    public Object getTimeWithTimeZone() {
        throw new UnsupportedOperationException();
    }

    public byte[] getVarbinary() {
        return (byte[]) value;
    }

    public String getVarchar() {
        return (String) value;
    }

    public static Object cleanObject(Object v, Type t) {
        if (v == null) {
            return v;
        }

        if (Types.isArrayType(t)) {
            if (!(v instanceof Block))
                throw new RuntimeException(
                        "Object is not a Block, but " + v.getClass());
            return v;
        }

        // Validate the object is the given type
        switch (t.getDisplayName()) {

        case StandardTypes.BIGINT:
            // Auto-convert integers to Longs
            if (v instanceof Integer)
                return new Long((Integer) v);
            if (!(v instanceof Long))
                throw new RuntimeException(
                        "Object is not a Long, but " + v.getClass());
            break;
        case StandardTypes.BOOLEAN:
            if (!(v instanceof Boolean))
                throw new RuntimeException(
                        "Object is not a Boolean, but " + v.getClass());
            break;
        case StandardTypes.DATE:
            if (v instanceof Long)
                return new Date((Long) v);

            if (!(v instanceof Date))
                throw new RuntimeException(
                        "Object is not a Date, but " + v.getClass());
            break;
        case StandardTypes.DOUBLE:
            if (!(v instanceof Double))
                throw new RuntimeException(
                        "Object is not a Double, but " + v.getClass());
            break;
        case StandardTypes.TIME:
            if (v instanceof Long)
                return new Time((Long) v);

            if (!(v instanceof Time))
                throw new RuntimeException(
                        "Object is not a Time, but " + v.getClass());
            break;
        case StandardTypes.TIMESTAMP:
            if (v instanceof Long)
                return new Timestamp((Long) v);

            if (!(v instanceof Timestamp))
                throw new RuntimeException(
                        "Object is not a Timestamp, but " + v.getClass());
            break;
        case StandardTypes.VARBINARY:
            if (!(v instanceof byte[]))
                throw new RuntimeException(
                        "Object is not a byte[], but " + v.getClass());
            break;
        case StandardTypes.VARCHAR:
            if (!(v instanceof String))
                throw new RuntimeException(
                        "Object is not a String, but " + v.getClass());
            break;
        default:
            throw new RuntimeException("Unsupported PrestoType " + t);
        }

        return v;
    }

    @Override
    public boolean equals(Object obj) {
        boolean retval = true;
        if (obj instanceof Field) {
            Field f = (Field) obj;
            if (type.equals(f.getType())) {
                if (type.equals(VarbinaryType.VARBINARY)) {
                    // special case for byte arrays
                    // aren't they so fancy
                    retval = Arrays.equals((byte[]) value,
                            (byte[]) f.getObject());
                } else if (type.equals(DateType.DATE)
                        || type.equals(TimeType.TIME)
                        || type.equals(TimestampType.TIMESTAMP)) {
                    retval = value.toString().equals(f.getObject().toString());
                } else {
                    if (value instanceof Block) {
                        retval = equals((Block) value, (Block) f.getObject());
                    } else {
                        retval = value.equals(f.getObject());
                    }
                }
            }
        }
        return retval;
    }

    private boolean equals(Block b1, Block b2) {
        boolean retval = b1.getPositionCount() == b2.getPositionCount();
        for (int i = 0; i < b1.getPositionCount() && retval; ++i) {
            if (b1 instanceof ArrayBlock && b2 instanceof ArrayBlock) {
                retval = equals(b1.getObject(i, Block.class),
                        b2.getObject(i, Block.class));
            } else {
                retval = b1.compareTo(i, 0, b1.getLength(i), b2, i, 0,
                        b2.getLength(i)) == 0;
            }
        }
        return retval;
    }

    @Override
    public String toString() {
        return value == null ? "null" : value.toString();
    }
}
