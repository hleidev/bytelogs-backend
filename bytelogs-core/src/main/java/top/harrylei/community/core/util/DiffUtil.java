package top.harrylei.community.core.util;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 文本差异对比工具类
 *
 * @author harry
 */
public class DiffUtil {

    private static final String INSERT_PREFIX = "<span class='diff-insert'>";
    private static final String DELETE_PREFIX = "<span class='diff-delete'>";
    private static final String SPAN_SUFFIX = "</span>";

    // 文本长度限制，避免性能问题
    private static final int MAX_DIFF_LENGTH = 50000;

    // 分行正则，兼容不同操作系统的换行符
    private static final Pattern LINE_PATTERN = Pattern.compile("\\r?\\n");

    /**
     * 对比两个文本，返回HTML格式的差异
     *
     * @param text1 原始文本
     * @param text2 修改后的文本
     * @return HTML格式的差异展示，如果没有差异返回原文本
     */
    public static String diff(String text1, String text2) {
        if (Objects.equals(text1, text2)) {
            return text2;
        }

        if (text1 == null) {
            text1 = "";
        }
        if (text2 == null) {
            text2 = "";
        }

        // 性能保护：文本过长时返回提示信息
        if (text1.length() > MAX_DIFF_LENGTH || text2.length() > MAX_DIFF_LENGTH) {
            return "文本过长，无法进行详细对比。建议下载版本文件进行本地对比。";
        }

        // 简单文本（无换行）使用字符级对比
        if (!text1.contains("\n") && !text2.contains("\n") && text1.length() < 200 && text2.length() < 200) {
            return diffInline(text1, text2);
        }

        // 复杂文本使用行级对比
        return diffLines(text1, text2);
    }

    /**
     * 行级差异对比（适用于长文本）
     */
    private static String diffLines(String text1, String text2) {
        String[] lines1 = LINE_PATTERN.split(text1);
        String[] lines2 = LINE_PATTERN.split(text2);

        List<DiffLine> diffResult = computeLineDiff(lines1, lines2);

        return buildHtmlFromLineDiff(diffResult);
    }

    /**
     * 字符级差异对比（适用于短文本）
     */
    private static String diffInline(String text1, String text2) {
        if (text1.isEmpty()) {
            return INSERT_PREFIX + escapeHtml(text2) + SPAN_SUFFIX;
        }
        if (text2.isEmpty()) {
            return DELETE_PREFIX + escapeHtml(text1) + SPAN_SUFFIX;
        }

        // 使用LCS算法计算最长公共子序列
        return computeInlineDiff(text1, text2);
    }

    /**
     * 计算行级差异
     */
    private static List<DiffLine> computeLineDiff(String[] lines1, String[] lines2) {
        int m = lines1.length;
        int n = lines2.length;

        // 使用动态规划计算LCS
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (lines1[i].equals(lines2[j])) {
                    dp[i + 1][j + 1] = dp[i][j] + 1;
                } else {
                    dp[i + 1][j + 1] = Math.max(dp[i][j + 1], dp[i + 1][j]);
                }
            }
        }

        // 回溯构建差异结果
        LinkedList<DiffLine> result = new LinkedList<>();
        int i = m, j = n;

        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && lines1[i - 1].equals(lines2[j - 1])) {
                result.addFirst(new DiffLine(DiffType.EQUAL, lines1[i - 1]));
                i--;
                j--;
            } else if (j > 0 && (i == 0 || dp[i][j - 1] >= dp[i - 1][j])) {
                result.addFirst(new DiffLine(DiffType.INSERT, lines2[j - 1]));
                j--;
            } else {
                result.addFirst(new DiffLine(DiffType.DELETE, lines1[i - 1]));
                i--;
            }
        }

        return result;
    }

    /**
     * 计算字符级差异
     */
    private static String computeInlineDiff(String text1, String text2) {
        char[] chars1 = text1.toCharArray();
        char[] chars2 = text2.toCharArray();

        int m = chars1.length;
        int n = chars2.length;

        // 简化版LCS for字符
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (chars1[i] == chars2[j]) {
                    dp[i + 1][j + 1] = dp[i][j] + 1;
                } else {
                    dp[i + 1][j + 1] = Math.max(dp[i][j + 1], dp[i + 1][j]);
                }
            }
        }

        // 构建结果
        int i = m, j = n;

        LinkedList<DiffChar> diffs = new LinkedList<>();

        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && chars1[i - 1] == chars2[j - 1]) {
                diffs.addFirst(new DiffChar(DiffType.EQUAL, chars1[i - 1]));
                i--;
                j--;
            } else if (j > 0 && (i == 0 || dp[i][j - 1] >= dp[i - 1][j])) {
                diffs.addFirst(new DiffChar(DiffType.INSERT, chars2[j - 1]));
                j--;
            } else {
                diffs.addFirst(new DiffChar(DiffType.DELETE, chars1[i - 1]));
                i--;
            }
        }

        // 合并连续的同类型操作
        return buildHtmlFromCharDiff(diffs);
    }

    /**
     * 从行差异构建HTML
     */
    private static String buildHtmlFromLineDiff(List<DiffLine> diffs) {
        StringBuilder html = new StringBuilder();

        for (DiffLine diff : diffs) {
            switch (diff.type) {
                case EQUAL:
                    html.append(escapeHtml(diff.content)).append("\n");
                    break;
                case INSERT:
                    html.append(INSERT_PREFIX)
                            .append(escapeHtml(diff.content))
                            .append(SPAN_SUFFIX)
                            .append("\n");
                    break;
                case DELETE:
                    html.append(DELETE_PREFIX)
                            .append(escapeHtml(diff.content))
                            .append(SPAN_SUFFIX)
                            .append("\n");
                    break;
            }
        }

        return html.toString().trim();
    }

    /**
     * 从字符差异构建HTML
     */
    private static String buildHtmlFromCharDiff(List<DiffChar> diffs) {
        StringBuilder html = new StringBuilder();
        StringBuilder currentSegment = new StringBuilder();
        DiffType currentType = null;

        for (DiffChar diff : diffs) {
            if (currentType != diff.type) {
                // 输出当前段落
                if (currentType != null) {
                    appendSegment(html, currentSegment.toString(), currentType);
                    currentSegment.setLength(0);
                }
                currentType = diff.type;
            }
            currentSegment.append(diff.character);
        }

        // 输出最后一个段落
        if (currentType != null) {
            appendSegment(html, currentSegment.toString(), currentType);
        }

        return html.toString();
    }

    /**
     * 添加段落到HTML
     */
    private static void appendSegment(StringBuilder html, String content, DiffType type) {
        switch (type) {
            case EQUAL:
                html.append(escapeHtml(content));
                break;
            case INSERT:
                html.append(INSERT_PREFIX)
                        .append(escapeHtml(content))
                        .append(SPAN_SUFFIX);
                break;
            case DELETE:
                html.append(DELETE_PREFIX)
                        .append(escapeHtml(content))
                        .append(SPAN_SUFFIX);
                break;
        }
    }

    /**
     * HTML转义
     */
    private static String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    /**
     * 差异类型枚举
     */
    private enum DiffType {
        EQUAL,
        INSERT,
        DELETE
    }

    /**
     * 行差异数据结构
     */
    private record DiffLine(DiffType type, String content) {
    }

    /**
     * 字符差异数据结构
     */
    private record DiffChar(DiffType type, char character) {
    }
}