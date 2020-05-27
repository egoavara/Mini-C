import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UnicodeTable {
    // http://unicode.org/charts/PDF/U0000.pdf
    public static final UnicodeTable ASCII_DIGITS = new UnicodeTable(0x0030, 0x0039);
    public static final UnicodeTable UPPERCASE_LATIN_ALPHABET = new UnicodeTable(0x0041, 0x005A);
    public static final UnicodeTable LOWERCASE_LATIN_ALPHABET = new UnicodeTable(0x0061, 0x007A);
    public static final UnicodeTable LATIN_ALPHABET = UnicodeTable.builder().add(UPPERCASE_LATIN_ALPHABET).add(LOWERCASE_LATIN_ALPHABET).build();

    public static class UnicodeTableBuilder {
        List<Integer> arr;

        UnicodeTableBuilder(List<Integer> arr) {
            this.arr = arr;
        }

        public UnicodeTableBuilder add(char c) {
            return add((int)c);
        }
        public UnicodeTableBuilder add(int c) {
            arr.add(c);
            arr.add(c);
            return this;
        }


        public UnicodeTableBuilder add(int start, int end) {
            arr.add(start);
            arr.add(end);
            return this;
        }

        public UnicodeTableBuilder add(UnicodeTable utb) {
            for (int i = 0; i < utb.ranges.length; i += 2) {
                add(utb.ranges[i], utb.ranges[i + 1]);
            }
            return this;
        }

        public UnicodeTable build() {
            return new UnicodeTable(arr.stream().mapToInt(Integer::intValue).toArray());
        }
    }

    public static UnicodeTableBuilder builder() {
        return new UnicodeTableBuilder(new ArrayList<>());
    }

    public static UnicodeTableBuilder builder(UnicodeTable from) {
        return new UnicodeTableBuilder(Arrays.stream(from.ranges).boxed().collect(Collectors.toList()));
    }

    // ranges의 길이는 반드시 2의 배수이다.
    private final int[] ranges;


    public UnicodeTable() {
        ranges = new int[]{};
    }

    public UnicodeTable(char c) {
        ranges = new int[]{(int)c, (int)c};
    }
    public UnicodeTable(int start, int end) {
        ranges = new int[]{start, end};
    }

    protected UnicodeTable(int[] ranges) {
        this.ranges = ranges;
    }

    public boolean contain(int c) {
        for (int i = 0; i < ranges.length; i += 2) {
            if (ranges[i] <= c && c <= ranges[i + 1]) {
                return true;
            }
        }
        return false;
    }

    public boolean contain(char c) {
        return contain((int) c);
    }
}
