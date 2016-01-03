package bloomberg.presto.accumulo;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import com.facebook.presto.spi.type.BigintType;
import com.facebook.presto.spi.type.BooleanType;
import com.facebook.presto.spi.type.DateType;
import com.facebook.presto.spi.type.DoubleType;
import com.facebook.presto.spi.type.TimeType;
import com.facebook.presto.spi.type.TimestampType;
import com.facebook.presto.spi.type.Type;
import com.facebook.presto.spi.type.VarbinaryType;
import com.facebook.presto.spi.type.VarcharType;
import com.facebook.presto.type.ArrayType;
import com.facebook.presto.type.MapType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import bloomberg.presto.accumulo.benchmark.QueryDriver;
import bloomberg.presto.accumulo.model.Row;
import bloomberg.presto.accumulo.model.RowSchema;
import bloomberg.presto.accumulo.serializers.AccumuloRowSerializer;

public class PredicatePushdownTest {

    public static final QueryDriver HARNESS;

    static {
        try {
            AccumuloConfig config = new AccumuloConfig();
            config.setInstance("default");
            config.setZooKeepers("localhost:2181");
            config.setUsername("root");
            config.setPassword("secret");
            HARNESS = new QueryDriver(config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @After
    public void cleanup() throws Exception {
        HARNESS.cleanup();
    }

    @Test
    @Ignore
    public void testSelectArray() throws Exception {
        Type elementType = VarcharType.VARCHAR;
        ArrayType arrayType = new ArrayType(elementType);
        RowSchema schema = RowSchema.newInstance().addRowId()
                .addColumn("senders", "metadata", "senders", arrayType);

        Row r1 = Row.newInstance().addField("row1", VarcharType.VARCHAR)
                .addField(AccumuloRowSerializer.getBlockFromArray(elementType,
                        ImmutableList.of("a", "b", "c")), arrayType);
        Row r2 = Row.newInstance().addField("row2", VarcharType.VARCHAR)
                .addField(AccumuloRowSerializer.getBlockFromArray(elementType,
                        ImmutableList.of("d", "e", "f")), arrayType);

        HARNESS.withHost("localhost").withPort(8080).withSchema("default")
                .withTable("testmytable").withQuery("SELECT * FROM testmytable")
                .withInputSchema(schema).withInput(r1, r2).withOutput(r1, r2)
                .runTest();
    }

    @Test
    @Ignore
    public void testSelectNestedArray() throws Exception {
        ArrayType nestedArrayType = new ArrayType(VarcharType.VARCHAR);
        ArrayType arrayType = new ArrayType(nestedArrayType);
        RowSchema schema = RowSchema.newInstance().addRowId()
                .addColumn("senders", "metadata", "senders", arrayType);

        Row r1 = Row.newInstance().addField("row1", VarcharType.VARCHAR)
                .addField(
                        AccumuloRowSerializer.getBlockFromArray(nestedArrayType,
                                ImmutableList.of(
                                        ImmutableList.of("a", "b", "c"),
                                        ImmutableList.of("d", "e", "f"),
                                        ImmutableList.of("g", "h", "i"))),
                        arrayType);
        Row r2 = Row.newInstance().addField("row2", VarcharType.VARCHAR)
                .addField(
                        AccumuloRowSerializer.getBlockFromArray(nestedArrayType,
                                ImmutableList.of(
                                        ImmutableList.of("j", "k", "l"),
                                        ImmutableList.of("m", "n", "o"),
                                        ImmutableList.of("p", "q", "r"))),
                        arrayType);

        HARNESS.withHost("localhost").withPort(8080).withSchema("default")
                .withTable("testmytable").withQuery("SELECT * FROM testmytable")
                .withInputSchema(schema).withInput(r1, r2).withOutput(r1, r2)
                .runTest();
    }

    @Test
    @Ignore
    public void testSelectVeryNestedArray() throws Exception {
        ArrayType veryNestedArrayType = new ArrayType(VarcharType.VARCHAR);
        ArrayType nestedArrayType = new ArrayType(veryNestedArrayType);
        ArrayType arrayType = new ArrayType(nestedArrayType);

        RowSchema schema = RowSchema.newInstance().addRowId()
                .addColumn("senders", "metadata", "senders", arrayType);

        // @formatter:off
        Row r1 = Row.newInstance().addField("row1", VarcharType.VARCHAR)
                .addField(
                        AccumuloRowSerializer.getBlockFromArray(nestedArrayType,
                        		ImmutableList.of(
                                    ImmutableList.of(
                                            ImmutableList.of("a", "b", "c"),
                                            ImmutableList.of("d", "e", "f"),
                                            ImmutableList.of("g", "h", "i")),
                                    ImmutableList.of(
                                            ImmutableList.of("j", "k", "l"),
                                            ImmutableList.of("m", "n", "o"),
                                            ImmutableList.of("p", "q", "r")),
                                    ImmutableList.of(
                                            ImmutableList.of("s", "t", "u"),
                                            ImmutableList.of("v", "w", "x"),
                                            ImmutableList.of("y", "z", "aa")))),
                        arrayType);
        // @formatter:on

        HARNESS.withHost("localhost").withPort(8080).withSchema("default")
                .withTable("testmytable").withQuery("SELECT * FROM testmytable")
                .withInputSchema(schema).withInput(r1).withOutput(r1).runTest();
    }

    @Test
    @Ignore
    public void testSelectUberNestedArray() throws Exception {
        ArrayType uberNestedArrayType = new ArrayType(VarcharType.VARCHAR);
        ArrayType veryNestedArrayType = new ArrayType(uberNestedArrayType);
        ArrayType nestedArrayType = new ArrayType(veryNestedArrayType);
        ArrayType arrayType = new ArrayType(nestedArrayType);

        RowSchema schema = RowSchema.newInstance().addRowId()
                .addColumn("senders", "metadata", "senders", arrayType);

        // @formatter:off
        Row r1 = Row.newInstance().addField("row1", VarcharType.VARCHAR)
                .addField(
                        AccumuloRowSerializer.getBlockFromArray(nestedArrayType,
                            ImmutableList.of(
                                ImmutableList.of(
                                    ImmutableList.of(
                                            ImmutableList.of("a", "b", "c"),
                                            ImmutableList.of("d", "e", "f"),
                                            ImmutableList.of("g", "h", "i")),
                                    ImmutableList.of(
                                            ImmutableList.of("j", "k", "l"),
                                            ImmutableList.of("m", "n", "o"),
                                            ImmutableList.of("p", "q", "r")),
                                    ImmutableList.of(
                                            ImmutableList.of("s", "t", "u"),
                                            ImmutableList.of("v", "w", "x"),
                                            ImmutableList.of("y", "z", "aa"))),
                                ImmutableList.of(
                                    ImmutableList.of(
                                            ImmutableList.of("a", "b", "c"),
                                            ImmutableList.of("d", "e", "f"),
                                            ImmutableList.of("g", "h", "i")),
                                    ImmutableList.of(
                                            ImmutableList.of("j", "k", "l"),
                                            ImmutableList.of("m", "n", "o"),
                                            ImmutableList.of("p", "q", "r")),
                                    ImmutableList.of(
                                            ImmutableList.of("s", "t", "u"),
                                            ImmutableList.of("v", "w", "x"),
                                            ImmutableList.of("y", "z", "aa"))),
                                ImmutableList.of(
                                    ImmutableList.of(
                                            ImmutableList.of("a", "b", "c"),
                                            ImmutableList.of("d", "e", "f"),
                                            ImmutableList.of("g", "h", "i")),
                                    ImmutableList.of(
                                            ImmutableList.of("j", "k", "l"),
                                            ImmutableList.of("m", "n", "o"),
                                            ImmutableList.of("p", "q", "r")),
                                    ImmutableList.of(
                                            ImmutableList.of("s", "t", "u"),
                                            ImmutableList.of("v", "w", "x"),
                                            ImmutableList.of("y", "z", "aa"))))),
                        arrayType);
        // @formatter:on

        HARNESS.withHost("localhost").withPort(8080).withSchema("default")
                .withTable("testmytable").withQuery("SELECT * FROM testmytable")
                .withInputSchema(schema).withInput(r1).withOutput(r1).runTest();
    }

    @Test
    public void testSelectBigIntNoWhere() throws Exception {
        RowSchema schema = RowSchema.newInstance().addRowId().addColumn("age",
                "metadata", "age", BigintType.BIGINT);

        Row r1 = Row.newInstance().addField("row1", VarcharType.VARCHAR)
                .addField(new Long(0), BigintType.BIGINT);
        Row r2 = Row.newInstance().addField("row2", VarcharType.VARCHAR)
                .addField(new Long(15), BigintType.BIGINT);
        Row r3 = Row.newInstance().addField("row3", VarcharType.VARCHAR)
                .addField(new Long(30), BigintType.BIGINT);

        // no constraints on predicate pushdown
        HARNESS.withHost("localhost").withPort(8080).withSchema("default")
                .withTable("testmytable").withQuery("SELECT * FROM testmytable")
                .withInputSchema(schema).withInput(r1, r2, r3)
                .withOutput(r1, r2, r3).runTest();
    }

    @Test
    public void testSelectBigIntLess() throws Exception {
        RowSchema schema = RowSchema.newInstance().addRowId().addColumn("age",
                "metadata", "age", BigintType.BIGINT);

        Row r1 = Row.newInstance().addField("row1", VarcharType.VARCHAR)
                .addField(new Long(0), BigintType.BIGINT);
        Row r2 = Row.newInstance().addField("row2", VarcharType.VARCHAR)
                .addField(new Long(15), BigintType.BIGINT);
        Row r3 = Row.newInstance().addField("row3", VarcharType.VARCHAR)
                .addField(new Long(30), BigintType.BIGINT);

        HARNESS.withHost("localhost").withPort(8080).withSchema("default")
                .withTable("testmytable")
                .withQuery("SELECT * FROM testmytable WHERE age < 15")
                .withInputSchema(schema).withInput(r1, r2, r3).withOutput(r1)
                .runTest();
    }

    @Test
    public void testSelectBigIntLessOrEqual() throws Exception {
        RowSchema schema = RowSchema.newInstance().addRowId().addColumn("age",
                "metadata", "age", BigintType.BIGINT);

        Row r1 = Row.newInstance().addField("row1", VarcharType.VARCHAR)
                .addField(new Long(0), BigintType.BIGINT);
        Row r2 = Row.newInstance().addField("row2", VarcharType.VARCHAR)
                .addField(new Long(15), BigintType.BIGINT);
        Row r3 = Row.newInstance().addField("row3", VarcharType.VARCHAR)
                .addField(new Long(30), BigintType.BIGINT);

        HARNESS.withHost("localhost").withPort(8080).withSchema("default")
                .withTable("testmytable")
                .withQuery("SELECT * FROM testmytable WHERE age <= 15")
                .withInputSchema(schema).withInput(r1, r2, r3)
                .withOutput(r1, r2).runTest();
    }

    @Test
    public void testSelectBigIntEqual() throws Exception {
        RowSchema schema = RowSchema.newInstance().addRowId().addColumn("age",
                "metadata", "age", BigintType.BIGINT);

        Row r1 = Row.newInstance().addField("row1", VarcharType.VARCHAR)
                .addField(new Long(0), BigintType.BIGINT);
        Row r2 = Row.newInstance().addField("row2", VarcharType.VARCHAR)
                .addField(new Long(15), BigintType.BIGINT);
        Row r3 = Row.newInstance().addField("row3", VarcharType.VARCHAR)
                .addField(new Long(30), BigintType.BIGINT);

        HARNESS.withHost("localhost").withPort(8080).withSchema("default")
                .withTable("testmytable")
                .withQuery("SELECT * FROM testmytable WHERE age = 15")
                .withInputSchema(schema).withInput(r1, r2, r3).withOutput(r2)
                .runTest();

        HARNESS.withHost("localhost").withPort(8080).withSchema("default")
                .withTable("testmytable")
                .withQuery(
                        "SELECT * FROM testmytable WHERE age = 15 AND age IS NOT NULL")
                .withInputSchema(schema).withInput(r1).withInput(r2)
                .withInput(r3).withOutput(r2).runTest();

        HARNESS.withHost("localhost").withPort(8080).withSchema("default")
                .withTable("testmytable")
                .withQuery(
                        "SELECT * FROM testmytable WHERE age = 15 OR age IS NULL")
                .withInputSchema(schema).withInput(r1, r2, r3).withOutput(r2)
                .runTest();
    }

    @Test
    public void testSelectBigIntGreater() throws Exception {
        RowSchema schema = RowSchema.newInstance().addRowId().addColumn("age",
                "metadata", "age", BigintType.BIGINT);

        Row r1 = Row.newInstance().addField("row1", VarcharType.VARCHAR)
                .addField(new Long(0), BigintType.BIGINT);
        Row r2 = Row.newInstance().addField("row2", VarcharType.VARCHAR)
                .addField(new Long(15), BigintType.BIGINT);
        Row r3 = Row.newInstance().addField("row3", VarcharType.VARCHAR)
                .addField(new Long(30), BigintType.BIGINT);
        HARNESS.withHost("localhost").withPort(8080).withSchema("default")
                .withTable("testmytable")
                .withQuery("SELECT * FROM testmytable WHERE age > 15")
                .withInputSchema(schema).withInput(r1, r2, r3).withOutput(r3)
                .runTest();
    }

    @Test
    public void testSelectBigIntGreaterOrEqual() throws Exception {
        RowSchema schema = RowSchema.newInstance().addRowId().addColumn("age",
                "metadata", "age", BigintType.BIGINT);

        Row r1 = Row.newInstance().addField("row1", VarcharType.VARCHAR)
                .addField(new Long(0), BigintType.BIGINT);
        Row r2 = Row.newInstance().addField("row2", VarcharType.VARCHAR)
                .addField(new Long(15), BigintType.BIGINT);
        Row r3 = Row.newInstance().addField("row3", VarcharType.VARCHAR)
                .addField(new Long(30), BigintType.BIGINT);

        HARNESS.withHost("localhost").withPort(8080).withSchema("default")
                .withTable("testmytable")
                .withQuery("SELECT * FROM testmytable WHERE age >= 15")
                .withInputSchema(schema).withInput(r1, r2, r3)
                .withOutput(r2, r3).runTest();
    }

    @Test
    public void testSelectBigIntLessAndGreater() throws Exception {
        RowSchema schema = RowSchema.newInstance().addRowId().addColumn("age",
                "metadata", "age", BigintType.BIGINT);

        Row r1 = Row.newInstance().addField("row1", VarcharType.VARCHAR)
                .addField(new Long(0), BigintType.BIGINT);
        Row r2 = Row.newInstance().addField("row2", VarcharType.VARCHAR)
                .addField(new Long(15), BigintType.BIGINT);
        Row r3 = Row.newInstance().addField("row3", VarcharType.VARCHAR)
                .addField(new Long(30), BigintType.BIGINT);

        HARNESS.withHost("localhost").withPort(8080).withSchema("default")
                .withTable("testmytable")
                .withQuery(
                        "SELECT * FROM testmytable WHERE age < 30 AND age > 0")
                .withInputSchema(schema).withInput(r1, r2, r3).withOutput(r2)
                .runTest();
    }

    @Test
    public void testSelectBigIntLessEqualAndGreater() throws Exception {
        RowSchema schema = RowSchema.newInstance().addRowId().addColumn("age",
                "metadata", "age", BigintType.BIGINT);

        Row r1 = Row.newInstance().addField("row1", VarcharType.VARCHAR)
                .addField(new Long(0), BigintType.BIGINT);
        Row r2 = Row.newInstance().addField("row2", VarcharType.VARCHAR)
                .addField(new Long(15), BigintType.BIGINT);
        Row r3 = Row.newInstance().addField("row3", VarcharType.VARCHAR)
                .addField(new Long(30), BigintType.BIGINT);
        Row r4 = Row.newInstance().addField("row4", VarcharType.VARCHAR)
                .addField(new Long(-15), BigintType.BIGINT);
        Row r5 = Row.newInstance().addField("row5", VarcharType.VARCHAR)
                .addField(new Long(45), BigintType.BIGINT);

        HARNESS.withHost("localhost").withPort(8080).withSchema("default")
                .withTable("testmytable")
                .withQuery(
                        "SELECT * FROM testmytable WHERE age <= 30 AND age > 0")
                .withInputSchema(schema).withInput(r1, r2, r3, r4, r5)
                .withOutput(r2, r3).runTest();
    }

    @Test
    public void testSelectBigIntLessAndGreaterEqual() throws Exception {
        RowSchema schema = RowSchema.newInstance().addRowId().addColumn("age",
                "metadata", "age", BigintType.BIGINT);

        Row r1 = Row.newInstance().addField("row1", VarcharType.VARCHAR)
                .addField(new Long(0), BigintType.BIGINT);
        Row r2 = Row.newInstance().addField("row2", VarcharType.VARCHAR)
                .addField(new Long(15), BigintType.BIGINT);
        Row r3 = Row.newInstance().addField("row3", VarcharType.VARCHAR)
                .addField(new Long(30), BigintType.BIGINT);
        Row r4 = Row.newInstance().addField("row4", VarcharType.VARCHAR)
                .addField(new Long(-15), BigintType.BIGINT);
        Row r5 = Row.newInstance().addField("row5", VarcharType.VARCHAR)
                .addField(new Long(45), BigintType.BIGINT);

        HARNESS.withHost("localhost").withPort(8080).withSchema("default")
                .withTable("testmytable")
                .withQuery(
                        "SELECT * FROM testmytable WHERE age < 30 AND age >= 0")
                .withInputSchema(schema).withInput(r1, r2, r3, r4, r5)
                .withOutput(r1, r2).runTest();
    }

    @Test
    public void testSelectBigIntLessEqualAndGreaterEqual() throws Exception {
        RowSchema schema = RowSchema.newInstance().addRowId().addColumn("age",
                "metadata", "age", BigintType.BIGINT);

        Row r1 = Row.newInstance().addField("row1", VarcharType.VARCHAR)
                .addField(new Long(0), BigintType.BIGINT);
        Row r2 = Row.newInstance().addField("row2", VarcharType.VARCHAR)
                .addField(new Long(15), BigintType.BIGINT);
        Row r3 = Row.newInstance().addField("row3", VarcharType.VARCHAR)
                .addField(new Long(30), BigintType.BIGINT);
        Row r4 = Row.newInstance().addField("row4", VarcharType.VARCHAR)
                .addField(new Long(-15), BigintType.BIGINT);
        Row r5 = Row.newInstance().addField("row5", VarcharType.VARCHAR)
                .addField(new Long(45), BigintType.BIGINT);

        HARNESS.withHost("localhost").withPort(8080).withSchema("default")
                .withTable("testmytable")
                .withQuery(
                        "SELECT * FROM testmytable WHERE age <= 30 AND age >= 0")
                .withInputSchema(schema).withInput(r1, r2, r3, r4, r5)
                .withOutput(r1, r2, r3).runTest();
    }

    @Test
    public void testSelectBigIntWithOr() throws Exception {
        RowSchema schema = RowSchema.newInstance().addRowId().addColumn("age",
                "metadata", "age", BigintType.BIGINT);

        Row r1 = Row.newInstance().addField("row1", VarcharType.VARCHAR)
                .addField(new Long(0), BigintType.BIGINT);
        Row r2 = Row.newInstance().addField("row2", VarcharType.VARCHAR)
                .addField(new Long(15), BigintType.BIGINT);
        Row r3 = Row.newInstance().addField("row3", VarcharType.VARCHAR)
                .addField(new Long(30), BigintType.BIGINT);
        Row r4 = Row.newInstance().addField("row4", VarcharType.VARCHAR)
                .addField(new Long(-15), BigintType.BIGINT);
        Row r5 = Row.newInstance().addField("row5", VarcharType.VARCHAR)
                .addField(new Long(45), BigintType.BIGINT);
        Row r6 = Row.newInstance().addField("row6", VarcharType.VARCHAR)
                .addField(new Long(60), BigintType.BIGINT);

        HARNESS.withHost("localhost").withPort(8080).withSchema("default")
                .withTable("testmytable")
                .withQuery(
                        "SELECT * FROM testmytable WHERE (age <= 30 AND age >= 0) OR (age > 45 AND age < 70)")
                .withInputSchema(schema).withInput(r1, r2, r3, r4, r5, r6)
                .withOutput(r1, r2, r3, r6).runTest();
    }

    @Test
    public void testSelectBigIntWithComplexWhereing() throws Exception {
        RowSchema schema = RowSchema.newInstance().addRowId()
                .addColumn("age", "metadata", "age", BigintType.BIGINT)
                .addColumn("fav_num", "metadata", "fav_num", BigintType.BIGINT);

        Row r1 = Row.newInstance().addField("row1", VarcharType.VARCHAR)
                .addField(new Long(0), BigintType.BIGINT)
                .addField(new Long(0), BigintType.BIGINT);
        Row r2 = Row.newInstance().addField("row2", VarcharType.VARCHAR)
                .addField(new Long(15), BigintType.BIGINT)
                .addField(new Long(15), BigintType.BIGINT);
        Row r3 = Row.newInstance().addField("row3", VarcharType.VARCHAR)
                .addField(new Long(30), BigintType.BIGINT)
                .addField(new Long(30), BigintType.BIGINT);
        Row r4 = Row.newInstance().addField("row4", VarcharType.VARCHAR)
                .addField(new Long(-15), BigintType.BIGINT)
                .addField(new Long(-15), BigintType.BIGINT);
        Row r5 = Row.newInstance().addField("row5", VarcharType.VARCHAR)
                .addField(new Long(45), BigintType.BIGINT)
                .addField(new Long(45), BigintType.BIGINT);
        Row r6 = Row.newInstance().addField("row6", VarcharType.VARCHAR)
                .addField(new Long(60), BigintType.BIGINT)
                .addField(new Long(60), BigintType.BIGINT);
        Row r7 = Row.newInstance().addField("row7", VarcharType.VARCHAR)
                .addField(new Long(90), BigintType.BIGINT)
                .addField(new Long(90), BigintType.BIGINT);

        HARNESS.withHost("localhost").withPort(8080).withSchema("default")
                .withTable("testmytable")
                .withQuery("SELECT * FROM testmytable WHERE (("
                        + "age <= 30 AND age >= 0) OR ("
                        + "age > 45 AND age < 70) OR (age > 80)) AND (("
                        + "fav_num <= 30 AND fav_num > 15) OR ("
                        + "fav_num >= 45 AND fav_num < 60) OR (fav_num = 90))")
                .withInputSchema(schema).withInput(r1, r2, r3, r4, r5, r6, r7)
                .withOutput(r3, r7).runTest();
    }

    @Test
    @Ignore
    public void testSelectBoolean() throws Exception {
        RowSchema schema = RowSchema.newInstance().addRowId().addColumn("age",
                "metadata", "male", BooleanType.BOOLEAN);

        Row r1 = Row.newInstance().addField("row1", VarcharType.VARCHAR)
                .addField(new Boolean(true), BooleanType.BOOLEAN);
        Row r2 = Row.newInstance().addField("row2", VarcharType.VARCHAR)
                .addField(new Boolean(false), BooleanType.BOOLEAN);

        HARNESS.withHost("localhost").withPort(8080).withSchema("default")
                .withTable("testmytable").withQuery("SELECT * FROM testmytable")
                .withInputSchema(schema).withInput(r1, r2).withOutput(r1, r2)
                .runTest();
    }

    @Test
    @Ignore
    public void testSelectDate() throws Exception {
        RowSchema schema = RowSchema.newInstance().addRowId().addColumn("age",
                "metadata", "start_date", DateType.DATE);

        Row r1 = Row.newInstance().addField("row1", VarcharType.VARCHAR)
                .addField(new Date(new GregorianCalendar(2015, 12, 14).getTime()
                        .getTime()), DateType.DATE);
        Row r2 = Row.newInstance().addField("row2", VarcharType.VARCHAR)
                .addField(new Date(new GregorianCalendar(2015, 12, 15).getTime()
                        .getTime()), DateType.DATE);

        HARNESS.withHost("localhost").withPort(8080).withSchema("default")
                .withTable("testmytable").withQuery("SELECT * FROM testmytable")
                .withInputSchema(schema).withInput(r1, r2).withOutput(r1, r2)
                .runTest();
    }

    @Test
    @Ignore
    public void testSelectDouble() throws Exception {
        RowSchema schema = RowSchema.newInstance().addRowId().addColumn("age",
                "metadata", "rate", DoubleType.DOUBLE);

        Row r1 = Row.newInstance().addField("row1", VarcharType.VARCHAR)
                .addField(new Double(28.1234), DoubleType.DOUBLE);

        Row r2 = Row.newInstance().addField("row2", VarcharType.VARCHAR)
                .addField(new Double(-123.1234), DoubleType.DOUBLE);

        HARNESS.withHost("localhost").withPort(8080).withSchema("default")
                .withTable("testmytable").withQuery("SELECT * FROM testmytable")
                .withInputSchema(schema).withInput(r1, r2).withOutput(r1, r2)
                .runTest();
    }

    @Test
    @Ignore
    public void testSelectMap() throws Exception {
        Type keyType = VarcharType.VARCHAR;
        Type valueType = BigintType.BIGINT;
        MapType mapType = new MapType(keyType, valueType);
        RowSchema schema = RowSchema.newInstance().addRowId()
                .addColumn("peopleages", "metadata", "peopleages", mapType);

        Row r1 = Row.newInstance().addField("row1", VarcharType.VARCHAR)
                .addField(
                        AccumuloRowSerializer.getBlockFromMap(mapType,
                                ImmutableMap.of("a", 1, "b", 2, "c", 3)),
                        mapType);
        Row r2 = Row.newInstance().addField("row2", VarcharType.VARCHAR)
                .addField(
                        AccumuloRowSerializer.getBlockFromMap(mapType,
                                ImmutableMap.of("d", 4, "e", 5, "f", 6)),
                        mapType);

        HARNESS.withHost("localhost").withPort(8080).withSchema("default")
                .withTable("testmytable").withQuery("SELECT * FROM testmytable")
                .withInputSchema(schema).withInput(r1, r2).withOutput(r1, r2)
                .runTest();
    }

    @Test(expected = SQLException.class)
    @Ignore
    public void testSelectMapOfArrays() throws Exception {
        Type elementType = BigintType.BIGINT;
        Type keyMapType = new ArrayType(elementType);
        Type valueMapType = new ArrayType(elementType);
        MapType mapType = new MapType(keyMapType, valueMapType);
        RowSchema schema = RowSchema.newInstance().addRowId().addColumn("foo",
                "metadata", "foo", mapType);

        // @formatter:off
        Row r1 = Row.newInstance().addField("row1", VarcharType.VARCHAR)
                .addField(
                        AccumuloRowSerializer.getBlockFromMap(mapType,
                                ImmutableMap.of(
                                        ImmutableList.of(1, 2, 3),
                                        ImmutableList.of(1, 2, 3))
                                ),
                        mapType);
        Row r2 = Row.newInstance().addField("row2", VarcharType.VARCHAR)
                .addField(
                        AccumuloRowSerializer.getBlockFromMap(mapType,
                                ImmutableMap.of(
                                        ImmutableList.of(4, 5, 6),
                                        ImmutableList.of(4, 5, 6))
                                ),
                        mapType);
        // @formatter:on

        HARNESS.withHost("localhost").withPort(8080).withSchema("default")
                .withTable("testmytable").withQuery("SELECT * FROM testmytable")
                .withInputSchema(schema).withInput(r1, r2).withOutput(r1, r2)
                .runTest();
    }

    @Test(expected = SQLException.class)
    @Ignore
    public void testSelectMapOfMaps() throws Exception {
        Type keyType = VarcharType.VARCHAR;
        Type valueType = BigintType.BIGINT;
        Type keyMapType = new MapType(keyType, valueType);
        Type valueMapType = new MapType(keyType, valueType);
        MapType mapType = new MapType(keyMapType, valueMapType);
        RowSchema schema = RowSchema.newInstance().addRowId().addColumn("foo",
                "metadata", "foo", mapType);

        // @formatter:off
        Row r1 = Row.newInstance().addField("row1", VarcharType.VARCHAR)
                .addField(
                        AccumuloRowSerializer.getBlockFromMap(mapType,
                                ImmutableMap.of(
                                        ImmutableMap.of("a", 1, "b", 2, "c", 3),
                                        ImmutableMap.of("a", 1, "b", 2, "c", 3))
                                ),
                        mapType);
        Row r2 = Row.newInstance().addField("row2", VarcharType.VARCHAR)
                .addField(
                        AccumuloRowSerializer.getBlockFromMap(mapType,
                                ImmutableMap.of(
                                        ImmutableMap.of("d", 4, "e", 5, "f", 6),
                                        ImmutableMap.of("d", 4, "e", 5, "f", 6))
                                ),
                        mapType);
        // @formatter:on

        HARNESS.withHost("localhost").withPort(8080).withSchema("default")
                .withTable("testmytable").withQuery("SELECT * FROM testmytable")
                .withInputSchema(schema).withInput(r1, r2).withOutput(r1, r2)
                .runTest();
    }

    @Test
    @Ignore
    public void testSelectTime() throws Exception {
        RowSchema schema = RowSchema.newInstance().addRowId().addColumn("age",
                "metadata", "last_login", TimeType.TIME);

        Calendar cal = new GregorianCalendar();
        Row r1 = Row.newInstance().addField("row1", VarcharType.VARCHAR)
                .addField(new Time(cal.getTimeInMillis()), TimeType.TIME);

        cal.add(Calendar.MINUTE, 5);
        Row r2 = Row.newInstance().addField("row2", VarcharType.VARCHAR)
                .addField(new Time(cal.getTimeInMillis()), TimeType.TIME);

        HARNESS.withHost("localhost").withPort(8080).withSchema("default")
                .withTable("testmytable").withQuery("SELECT * FROM testmytable")
                .withInputSchema(schema).withInput(r1, r2).withOutput(r1, r2)
                .runTest();
    }

    @Test
    @Ignore
    public void testSelectTimestamp() throws Exception {
        RowSchema schema = RowSchema.newInstance().addRowId().addColumn("age",
                "metadata", "last_login", TimestampType.TIMESTAMP);

        Calendar cal = new GregorianCalendar();
        Row r1 = Row.newInstance().addField("row1", VarcharType.VARCHAR)
                .addField(new Timestamp(cal.getTimeInMillis()),
                        TimestampType.TIMESTAMP);

        cal.add(Calendar.MINUTE, 5);
        Row r2 = Row.newInstance().addField("row2", VarcharType.VARCHAR)
                .addField(new Timestamp(cal.getTimeInMillis()),
                        TimestampType.TIMESTAMP);

        HARNESS.withHost("localhost").withPort(8080).withSchema("default")
                .withTable("testmytable").withQuery("SELECT * FROM testmytable")
                .withInputSchema(schema).withInput(r1, r2).withOutput(r1, r2)
                .runTest();
    }

    @Test
    @Ignore
    public void testSelectVarbinary() throws Exception {
        RowSchema schema = RowSchema.newInstance().addRowId().addColumn("age",
                "metadata", "bytes", VarbinaryType.VARBINARY);

        Row r1 = Row.newInstance().addField("row1", VarcharType.VARCHAR)
                .addField("Check out all this data!".getBytes(),
                        VarbinaryType.VARBINARY);

        Row r2 = Row.newInstance().addField("row2", VarcharType.VARCHAR)
                .addField("Check out all this other data!".getBytes(),
                        VarbinaryType.VARBINARY);

        HARNESS.withHost("localhost").withPort(8080).withSchema("default")
                .withTable("testmytable").withQuery("SELECT * FROM testmytable")
                .withInputSchema(schema).withInput(r1, r2).withOutput(r1, r2)
                .runTest();
    }

    @Test
    @Ignore
    public void testSelectVarchar() throws Exception {
        RowSchema schema = RowSchema.newInstance().addRowId().addColumn("age",
                "metadata", "name", VarcharType.VARCHAR);

        Row r1 = Row.newInstance().addField("row1", VarcharType.VARCHAR)
                .addField("Alice", VarcharType.VARCHAR);
        Row r2 = Row.newInstance().addField("row2", VarcharType.VARCHAR)
                .addField("Bob", VarcharType.VARCHAR);
        Row r3 = Row.newInstance().addField("row3", VarcharType.VARCHAR)
                .addField("Carol", VarcharType.VARCHAR);

        HARNESS.withHost("localhost").withPort(8080).withSchema("default")
                .withTable("testmytable").withQuery("SELECT * FROM testmytable")
                .withInputSchema(schema).withInput(r1, r2, r3)
                .withOutput(r1, r2, r3).runTest();
    }
}
