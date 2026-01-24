package top.harrylei.community.core.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JSON 工具类测试
 *
 * @author harry
 */
@DisplayName("JsonUtil 测试")
class JsonUtilTest {

    @Nested
    @DisplayName("toJson 方法测试")
    class ToJsonTest {

        @Test
        @DisplayName("普通对象应正确序列化为 JSON")
        void shouldSerializeObjectToJson() {
            TestUser user = new TestUser("John", 25);

            String json = JsonUtil.toJson(user);

            assertThat(json).contains("\"name\":\"John\"");
            assertThat(json).contains("\"age\":25");
        }

        @Test
        @DisplayName("null 对象应返回 null")
        void shouldReturnNullForNullObject() {
            String json = JsonUtil.toJson(null);

            assertThat(json).isNull();
        }

        @Test
        @DisplayName("List 对象应正确序列化")
        void shouldSerializeListToJson() {
            List<String> list = List.of("a", "b", "c");

            String json = JsonUtil.toJson(list);

            assertThat(json).isEqualTo("[\"a\",\"b\",\"c\"]");
        }

        @Test
        @DisplayName("Map 对象应正确序列化")
        void shouldSerializeMapToJson() {
            Map<String, Integer> map = Map.of("one", 1, "two", 2);

            String json = JsonUtil.toJson(map);

            assertThat(json).contains("\"one\":1");
            assertThat(json).contains("\"two\":2");
        }

        @Test
        @DisplayName("Date 对象应格式化为标准格式")
        void shouldFormatDateCorrectly() {
            TestDateObject obj = new TestDateObject();
            obj.setDate(new Date(1704067200000L)); // 2024-01-01 00:00:00 UTC

            String json = JsonUtil.toJson(obj);

            // 日期应格式化为 yyyy-MM-dd HH:mm:ss
            assertThat(json).matches(".*\"date\":\"\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\".*");
        }
    }

    @Nested
    @DisplayName("fromJson 方法测试")
    class FromJsonTest {

        @Test
        @DisplayName("有效 JSON 应正确反序列化为对象")
        void shouldDeserializeJsonToObject() {
            String json = "{\"name\":\"Jane\",\"age\":30}";

            TestUser user = JsonUtil.fromJson(json, TestUser.class);

            assertThat(user).isNotNull();
            assertThat(user.getName()).isEqualTo("Jane");
            assertThat(user.getAge()).isEqualTo(30);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("空或 null JSON 应返回 null")
        void shouldReturnNullForBlankJson(String json) {
            TestUser user = JsonUtil.fromJson(json, TestUser.class);

            assertThat(user).isNull();
        }

        @Test
        @DisplayName("目标类型为 null 应返回 null")
        void shouldReturnNullForNullClass() {
            Class<TestUser> nullClass = null;
            TestUser user = JsonUtil.fromJson("{}", nullClass);

            assertThat(user).isNull();
        }

        @Test
        @DisplayName("无效 JSON 应返回 null")
        void shouldReturnNullForInvalidJson() {
            TestUser user = JsonUtil.fromJson("{invalid json}", TestUser.class);

            assertThat(user).isNull();
        }

        @Test
        @DisplayName("忽略未知属性")
        void shouldIgnoreUnknownProperties() {
            String json = "{\"name\":\"Test\",\"age\":20,\"unknown\":\"value\"}";

            TestUser user = JsonUtil.fromJson(json, TestUser.class);

            assertThat(user).isNotNull();
            assertThat(user.getName()).isEqualTo("Test");
            assertThat(user.getAge()).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("fromJson (TypeReference) 方法测试")
    class FromJsonTypeReferenceTest {

        @Test
        @DisplayName("泛型 List 应正确反序列化")
        void shouldDeserializeGenericList() {
            String json = "[{\"name\":\"A\",\"age\":1},{\"name\":\"B\",\"age\":2}]";

            List<TestUser> users = JsonUtil.fromJson(json, new TypeReference<>() {
            });

            assertThat(users).hasSize(2);
            assertThat(users.get(0).getName()).isEqualTo("A");
            assertThat(users.get(1).getName()).isEqualTo("B");
        }

        @Test
        @DisplayName("泛型 Map 应正确反序列化")
        void shouldDeserializeGenericMap() {
            String json = "{\"user1\":{\"name\":\"A\",\"age\":1},\"user2\":{\"name\":\"B\",\"age\":2}}";

            Map<String, TestUser> users = JsonUtil.fromJson(json, new TypeReference<>() {
            });

            assertThat(users).hasSize(2);
            assertThat(users.get("user1").getName()).isEqualTo("A");
            assertThat(users.get("user2").getName()).isEqualTo("B");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("空或 null JSON 应返回 null")
        void shouldReturnNullForBlankJson(String json) {
            List<TestUser> users = JsonUtil.fromJson(json, new TypeReference<>() {
            });

            assertThat(users).isNull();
        }

        @Test
        @DisplayName("TypeReference 为 null 应返回 null")
        void shouldReturnNullForNullTypeReference() {
            TypeReference<List<TestUser>> nullRef = null;
            List<TestUser> users = JsonUtil.fromJson("[{}]", nullRef);

            assertThat(users).isNull();
        }
    }

    @Nested
    @DisplayName("toBytes 方法测试")
    class ToBytesTest {

        @Test
        @DisplayName("对象应正确转换为字节数组")
        void shouldConvertObjectToBytes() {
            TestUser user = new TestUser("Test", 25);

            byte[] bytes = JsonUtil.toBytes(user);

            assertThat(bytes).isNotNull();
            String json = new String(bytes, StandardCharsets.UTF_8);
            assertThat(json).contains("\"name\":\"Test\"");
        }

        @Test
        @DisplayName("null 对象应返回 null")
        void shouldReturnNullForNullObject() {
            byte[] bytes = JsonUtil.toBytes(null);

            assertThat(bytes).isNull();
        }
    }

    @Nested
    @DisplayName("fromBytes 方法测试")
    class FromBytesTest {

        @Test
        @DisplayName("有效字节数组应正确反序列化为对象")
        void shouldDeserializeBytesToObject() {
            String json = "{\"name\":\"ByteTest\",\"age\":35}";
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

            TestUser user = JsonUtil.fromBytes(bytes, TestUser.class);

            assertThat(user).isNotNull();
            assertThat(user.getName()).isEqualTo("ByteTest");
            assertThat(user.getAge()).isEqualTo(35);
        }

        @Test
        @DisplayName("null 字节数组应返回 null")
        void shouldReturnNullForNullBytes() {
            TestUser user = JsonUtil.fromBytes(null, TestUser.class);

            assertThat(user).isNull();
        }

        @Test
        @DisplayName("空字节数组应返回 null")
        void shouldReturnNullForEmptyBytes() {
            TestUser user = JsonUtil.fromBytes(new byte[0], TestUser.class);

            assertThat(user).isNull();
        }

        @Test
        @DisplayName("目标类型为 null 应返回 null")
        void shouldReturnNullForNullClass() {
            byte[] bytes = "{}".getBytes(StandardCharsets.UTF_8);

            Class<TestUser> nullClass = null;
            TestUser user = JsonUtil.fromBytes(bytes, nullClass);

            assertThat(user).isNull();
        }
    }

    @Nested
    @DisplayName("fromBytes (TypeReference) 方法测试")
    class FromBytesTypeReferenceTest {

        @Test
        @DisplayName("泛型类型应正确反序列化")
        void shouldDeserializeBytesToGenericType() {
            String json = "[{\"name\":\"A\",\"age\":1}]";
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

            List<TestUser> users = JsonUtil.fromBytes(bytes, new TypeReference<>() {
            });

            assertThat(users).hasSize(1);
            assertThat(users.get(0).getName()).isEqualTo("A");
        }

        @Test
        @DisplayName("null 字节数组应返回 null")
        void shouldReturnNullForNullBytes() {
            List<TestUser> users = JsonUtil.fromBytes(null, new TypeReference<>() {
            });

            assertThat(users).isNull();
        }

        @Test
        @DisplayName("TypeReference 为 null 应返回 null")
        void shouldReturnNullForNullTypeReference() {
            byte[] bytes = "[]".getBytes(StandardCharsets.UTF_8);

            TypeReference<List<TestUser>> nullRef = null;
            List<TestUser> users = JsonUtil.fromBytes(bytes, nullRef);

            assertThat(users).isNull();
        }
    }

    @Nested
    @DisplayName("parseToNode 方法测试")
    class ParseToNodeTest {

        @Test
        @DisplayName("有效 JSON 应正确解析为 JsonNode")
        void shouldParseValidJsonToNode() {
            String json = "{\"name\":\"Node\",\"value\":123}";

            JsonNode node = JsonUtil.parseToNode(json);

            assertThat(node).isNotNull();
            assertThat(node.get("name").asText()).isEqualTo("Node");
            assertThat(node.get("value").asInt()).isEqualTo(123);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("空或 null JSON 应返回 null")
        void shouldReturnNullForBlankJson(String json) {
            JsonNode node = JsonUtil.parseToNode(json);

            assertThat(node).isNull();
        }

        @Test
        @DisplayName("无效 JSON 应返回 null")
        void shouldReturnNullForInvalidJson() {
            JsonNode node = JsonUtil.parseToNode("{invalid}");

            assertThat(node).isNull();
        }

        @Test
        @DisplayName("数组 JSON 应正确解析")
        void shouldParseArrayJson() {
            String json = "[1,2,3]";

            JsonNode node = JsonUtil.parseToNode(json);

            assertThat(node).isNotNull();
            assertThat(node.isArray()).isTrue();
            assertThat(node.size()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("序列化和反序列化一致性测试")
    class RoundTripTest {

        @Test
        @DisplayName("对象序列化后反序列化应保持一致")
        void shouldMaintainConsistencyAfterRoundTrip() {
            TestUser original = new TestUser("RoundTrip", 42);

            String json = JsonUtil.toJson(original);
            TestUser restored = JsonUtil.fromJson(json, TestUser.class);

            assertThat(restored).isNotNull();
            assertThat(restored.getName()).isEqualTo(original.getName());
            assertThat(restored.getAge()).isEqualTo(original.getAge());
        }

        @Test
        @DisplayName("字节数组序列化后反序列化应保持一致")
        void shouldMaintainConsistencyAfterByteRoundTrip() {
            TestUser original = new TestUser("ByteTrip", 33);

            byte[] bytes = JsonUtil.toBytes(original);
            TestUser restored = JsonUtil.fromBytes(bytes, TestUser.class);

            assertThat(restored).isNotNull();
            assertThat(restored.getName()).isEqualTo(original.getName());
            assertThat(restored.getAge()).isEqualTo(original.getAge());
        }
    }

    // 测试用内部类
    static class TestUser {
        private String name;
        private int age;

        public TestUser() {
        }

        public TestUser(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    static class TestDateObject {
        private Date date;

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }
}
