package top.harrylei.community.api.enums.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import top.harrylei.community.api.enums.article.CollectionStatusEnum;
import top.harrylei.community.api.enums.comment.CommentStatusEnum;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 操作类型枚举测试
 *
 * @author harry
 */
@DisplayName("OperateTypeEnum 测试")
class OperateTypeEnumTest {

    @Nested
    @DisplayName("fromCode 方法测试")
    class FromCodeTest {

        @ParameterizedTest(name = "code={0} 应返回 {1}")
        @CsvSource({
                "0, EMPTY",
                "1, READ",
                "2, COMMENT",
                "3, PRAISE",
                "4, COLLECTION",
                "5, DELETE_COMMENT",
                "6, CANCEL_PRAISE",
                "7, CANCEL_COLLECTION"
        })
        @DisplayName("有效操作码应返回对应枚举")
        void shouldReturnEnumForValidCode(Integer code, String expectedName) {
            OperateTypeEnum result = OperateTypeEnum.fromCode(code);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo(expectedName);
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("null 应返回 null")
        void shouldReturnNullForNullCode(Integer code) {
            OperateTypeEnum result = OperateTypeEnum.fromCode(code);

            assertThat(result).isNull();
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 8, 100, 999})
        @DisplayName("无效操作码应返回 null")
        void shouldReturnNullForInvalidCode(Integer code) {
            OperateTypeEnum result = OperateTypeEnum.fromCode(code);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("getStatus 方法测试")
    class GetStatusTest {

        @Test
        @DisplayName("READ 应返回 ReadStatusEnum.READ")
        void readShouldReturnReadStatus() {
            Enum<?> status = OperateTypeEnum.READ.getStatus();

            assertThat(status).isEqualTo(ReadStatusEnum.READ);
        }

        @Test
        @DisplayName("PRAISE 应返回 PraiseStatusEnum.PRAISE")
        void praiseShouldReturnPraiseStatus() {
            Enum<?> status = OperateTypeEnum.PRAISE.getStatus();

            assertThat(status).isEqualTo(PraiseStatusEnum.PRAISE);
        }

        @Test
        @DisplayName("COLLECTION 应返回 CollectionStatusEnum.COLLECTION")
        void collectionShouldReturnCollectionStatus() {
            Enum<?> status = OperateTypeEnum.COLLECTION.getStatus();

            assertThat(status).isEqualTo(CollectionStatusEnum.COLLECTION);
        }

        @Test
        @DisplayName("CANCEL_PRAISE 应返回 PraiseStatusEnum.NOT_PRAISE")
        void cancelPraiseShouldReturnNotPraiseStatus() {
            Enum<?> status = OperateTypeEnum.CANCEL_PRAISE.getStatus();

            assertThat(status).isEqualTo(PraiseStatusEnum.NOT_PRAISE);
        }

        @Test
        @DisplayName("CANCEL_COLLECTION 应返回 CollectionStatusEnum.NOT_COLLECTION")
        void cancelCollectionShouldReturnNotCollectionStatus() {
            Enum<?> status = OperateTypeEnum.CANCEL_COLLECTION.getStatus();

            assertThat(status).isEqualTo(CollectionStatusEnum.NOT_COLLECTION);
        }

        @Test
        @DisplayName("COMMENT 应返回 CommentStatusEnum.COMMENT")
        void commentShouldReturnCommentStatus() {
            Enum<?> status = OperateTypeEnum.COMMENT.getStatus();

            assertThat(status).isEqualTo(CommentStatusEnum.COMMENT);
        }

        @Test
        @DisplayName("DELETE_COMMENT 应返回 CommentStatusEnum.NOT_COMMENT")
        void deleteCommentShouldReturnNotCommentStatus() {
            Enum<?> status = OperateTypeEnum.DELETE_COMMENT.getStatus();

            assertThat(status).isEqualTo(CommentStatusEnum.NOT_COMMENT);
        }

        @Test
        @DisplayName("EMPTY 应返回 null")
        void emptyShouldReturnNull() {
            Enum<?> status = OperateTypeEnum.EMPTY.getStatus();

            assertThat(status).isNull();
        }
    }

    @Nested
    @DisplayName("isPraiseOrCollection 方法测试")
    class IsPraiseOrCollectionTest {

        @ParameterizedTest
        @ValueSource(strings = {"PRAISE", "CANCEL_PRAISE", "COLLECTION", "CANCEL_COLLECTION"})
        @DisplayName("点赞收藏操作应返回 true")
        void shouldReturnTrueForPraiseOrCollectionOperations(String enumName) {
            OperateTypeEnum operateType = OperateTypeEnum.valueOf(enumName);

            assertThat(operateType.isPraiseOrCollection()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"EMPTY", "READ", "COMMENT", "DELETE_COMMENT"})
        @DisplayName("非点赞收藏操作应返回 false")
        void shouldReturnFalseForNonPraiseOrCollectionOperations(String enumName) {
            OperateTypeEnum operateType = OperateTypeEnum.valueOf(enumName);

            assertThat(operateType.isPraiseOrCollection()).isFalse();
        }
    }

    @Nested
    @DisplayName("isPraise 方法测试")
    class IsPraiseTest {

        @ParameterizedTest
        @ValueSource(strings = {"PRAISE", "CANCEL_PRAISE"})
        @DisplayName("点赞相关操作应返回 true")
        void shouldReturnTrueForPraiseOperations(String enumName) {
            OperateTypeEnum operateType = OperateTypeEnum.valueOf(enumName);

            assertThat(operateType.isPraise()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"EMPTY", "READ", "COMMENT", "COLLECTION", "DELETE_COMMENT", "CANCEL_COLLECTION"})
        @DisplayName("非点赞操作应返回 false")
        void shouldReturnFalseForNonPraiseOperations(String enumName) {
            OperateTypeEnum operateType = OperateTypeEnum.valueOf(enumName);

            assertThat(operateType.isPraise()).isFalse();
        }
    }

    @Nested
    @DisplayName("getCode 和 getLabel 方法测试")
    class GetterTest {

        @Test
        @DisplayName("PRAISE 的 code 应为 3")
        void praiseShouldHaveCode3() {
            assertThat(OperateTypeEnum.PRAISE.getCode()).isEqualTo(3);
        }

        @Test
        @DisplayName("PRAISE 的 label 应为 '点赞'")
        void praiseShouldHaveCorrectLabel() {
            assertThat(OperateTypeEnum.PRAISE.getLabel()).isEqualTo("点赞");
        }

        @Test
        @DisplayName("所有枚举值应有唯一的 code")
        void allEnumsShouldHaveUniqueCode() {
            OperateTypeEnum[] values = OperateTypeEnum.values();

            long distinctCodeCount = java.util.Arrays.stream(values)
                    .map(OperateTypeEnum::getCode)
                    .distinct()
                    .count();

            assertThat(distinctCodeCount).isEqualTo(values.length);
        }
    }
}
