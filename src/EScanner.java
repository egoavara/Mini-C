import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class EScanner implements Iterator<Token> {
    public static final UnicodeTable WS = UnicodeTable.builder().add(' ').add('\n').add('\t').build();
    public static final UnicodeTable IDENTIFIER_BEGIN = UnicodeTable.builder().add('_').add(UnicodeTable.LATIN_ALPHABET).build();
    public static final UnicodeTable IDENTIFIER_LOOP = UnicodeTable.builder().add('_').add(UnicodeTable.LATIN_ALPHABET).add(UnicodeTable.ASCII_DIGITS).build();
    public static final UnicodeTable NUMERIC = UnicodeTable.ASCII_DIGITS;

    //    private int lineNo;
//    private int columnNo;
//    private int countNo;
    private final String original;
    private CharSequence cs;

    public EScanner(String original) {
//        this.lineNo = 0;
//        this.columnNo = 0;
//        this.countNo = 0;
        this.original = original;
        this.cs = original;
    }

    //
    Token temporal = null;

    @Override
    public boolean hasNext() {
        if (temporal == Token.eofTok) {
            return false;
        }
        if (this.cs.length() == 0) {
            temporal = Token.eofTok;
            return true;
        }
        this.cs = consumeWS(this.cs);
        // 다음에 찾아지는 토큰이
        // 주석
        // 연산자, 특수문자
        // 키워드, 식별자
        // 숫자
        // 모두 해당하지 않는 경우
        // EOF 토큰
        temporal = optOr(
                this::tryComment,
                this::tryConsumeOperator,
                this::tryConsumeKeyword,
                this::tryConsumeNumeric
        ).orElse(Token.eofTok);
        return true;
    }

    @Override
    public Token next() {
        return temporal;
    }

    @SafeVarargs
    private static Optional<Token> optOr(Supplier<Optional<Token>>... suppliers) {
        for (Supplier<Optional<Token>> supplier : suppliers) {
            Optional<Token> tmp = supplier.get();
            if (tmp.isPresent()) {
                return tmp;
            }
        }
        return Optional.empty();
    }

    private static CharSequence consumeWS(CharSequence cs) {
        if (cs.length() == 0) {
            return cs;
        }
        int counter = 0;
        while (counter < cs.length() && WS.contain(cs.charAt(counter))) {
            counter++;
        }
        return cs.subSequence(counter, cs.length());
    }

    // 주석 타입인지 확인
    private Optional<Token> tryComment() {
        return optOr(this::tryDocumentedComment, this::tryMultiComment, this::trySingleComment);
    }

    private Optional<Token> trySingleComment() {
        if (cs.length() < 2) {
            return Optional.empty();
        }
        // //로 시작하는 문자열부터 \n까지 분석
        if (cs.subSequence(0, 2).equals("//")) {
            //
            int counter = 2;
            for (; counter < cs.length(); counter++) {
                if (cs.charAt(counter) == '\n') {
                    break;
                }
            }
            String catched = cs.subSequence(2, counter).toString();
            cs = cs.subSequence(counter + 1, cs.length());
            return Optional.of(Token.mkSingleComment(catched));
        }
        return Optional.empty();
    }

    private Optional<Token> tryMultiComment() {
        if (cs.length() < 2) {
            return Optional.empty();
        }
        if (cs.subSequence(0, 2).equals("/*")) {
            //
            int counter = 2;
            for (; counter < cs.length() - 1; counter++) {
                if (cs.subSequence(counter, counter + 2).equals("*/")) {
                    break;
                }
            }
            String catched = cs.subSequence(2, counter).toString();
            cs = cs.subSequence(counter + 2, cs.length());
            return Optional.of(Token.mkMultiComment(catched));
        }
        return Optional.empty();
    }

    private Optional<Token> tryDocumentedComment() {
        if (cs.length() < 3) {
            return Optional.empty();
        }
        if (cs.subSequence(0, 3).equals("/**")) {
            //
            int counter = 3;
            for (; counter < cs.length() - 1; counter++) {
                if (cs.subSequence(counter, counter + 2).equals("*/")) {
                    break;
                }
            }
            String catched = cs.subSequence(2, counter).toString();
            cs = cs.subSequence(counter + 2, cs.length());
            return Optional.of(Token.mkDocumentedComment(catched));
        }
        return Optional.empty();
    }
    // 키워드 혹은 식별자인지 확인
    private Optional<Token> tryConsumeKeyword() {
        int counter = 0;
        while (counter < cs.length() && IDENTIFIER_BEGIN.contain(cs.charAt(counter))) {
            counter++;
        }
        if (counter <= 0) {
            return Optional.empty();
        }
        while (counter < cs.length() && IDENTIFIER_LOOP.contain(cs.charAt(counter))) {
            counter++;
        }
        String catched = cs.subSequence(0, counter).toString();
        cs = cs.subSequence(counter, cs.length());
        return Optional.of(Token.keyword(catched));
    }

    // 숫자 문자열인지 확인
    private Optional<Token> tryConsumeNumeric() {
        int counter = 0;
        while (counter < cs.length() && NUMERIC.contain(cs.charAt(counter))) {
            counter++;
        }
        if (counter <= 0) {
            return Optional.empty();
        }
        String catched = cs.subSequence(0, counter).toString();
        cs = cs.subSequence(counter, cs.length());
        return Optional.of(Token.mkIntLiteral(catched));
    }

    // 연산자, 특수기호인지 확인
    private Optional<Token> tryConsumeOperator() {
        Token tk = null;
        if (this.cs.length() >= 2) {
            String op = this.cs.subSequence(0, 2).toString();
            switch (op) {
//
                case "==":
                    tk = Token.eqeqTok;
                    break;
                case "!=":
                    tk = Token.noteqTok;
                    break;
                case ">=":
                    tk = Token.gteqTok;
                    break;
                case "<=":
                    tk = Token.lteqTok;
                    break;
//
                case "+=":
                    tk = Token.addAssignTok;
                    break;
                case "-=":
                    tk = Token.subAssignTok;
                    break;
                case "*=":
                    tk = Token.multAssignTok;
                    break;
                case "/=":
                    tk = Token.divAssignTok;
                    break;
                case "%=":
                    tk = Token.remAssignTok;
                    break;
                case "++":
                    tk = Token.incrementTok;
                    break;
                case "--":
                    tk = Token.decrementTok;
                    break;
                case "&&":
                    tk = Token.andTok;
                    break;
                case "||":
                    tk = Token.orTok;
                    break;
            }
            if (tk != null) {
                this.cs = this.cs.subSequence(2, cs.length());
                return Optional.of(tk);
            }
        }
        if (this.cs.length() >= 1) {
            String op = this.cs.subSequence(0, 1).toString();
            switch (op) {
                case "{":
                    tk = Token.leftBraceTok;
                    break;
                case "}":
                    tk = Token.rightBraceTok;
                    break;
                case "[":
                    tk = Token.leftBracketTok;
                    break;
                case "]":
                    tk = Token.rightBracketTok;
                    break;
                case "(":
                    tk = Token.leftParenTok;
                    break;
                case ")":
                    tk = Token.rightParenTok;
                    break;
                case ";":
                    tk = Token.semicolonTok;
                    break;
                case ",":
                    tk = Token.commaTok;
                    break;
                case "=":
                    tk = Token.assignTok;
                    break;

                case ">":
                    tk = Token.gtTok;
                    break;
                case "<":
                    tk = Token.ltTok;
                    break;
                case "!":
                    tk = Token.notTok;
                    break;

                case "+":
                    tk = Token.plusTok;
                    break;
                case "-":
                    tk = Token.minusTok;
                    break;
                case "*":
                    tk = Token.multiplyTok;
                    break;
                case "/":
                    tk = Token.divideTok;
                    break;
                case "%":
                    tk = Token.reminderTok;
                    break;
            }
            if (tk != null) {
                this.cs = this.cs.subSequence(1, cs.length());
                return Optional.of(tk);
            }
        }
        return Optional.empty();
    }

    public static void main(String[] args) {
        // 파일명을 매개변수로 받아서 파일의 토큰들을 출력한다.
        try {
            if (args.length < 1) {
                System.out.println("출력할 파일이 없습니다.");
                return;
            }
            // 파일의 모든 줄을 읽어 온다.
            String src = String.join("\n", Files.readAllLines(Paths.get(args[0])));
            // 모든 토큰을 출력한다.
            for (EScanner it = new EScanner(src); it.hasNext(); ) {
                Token tk = it.next();
                System.out.printf("Token(%20s, '%s')\n", tk.type(), tk.value());
            }
        } catch (IOException e) {
            System.out.println("파일을 찾을 수 없습니다.");
        }
    }
}
