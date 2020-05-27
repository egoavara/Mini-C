
public enum TokenType {
    Const, Void, Else, If, Int, While, Return,
    // 확장된 토큰 타입
    Char, Double, String, For, Switch, Case, Default, Continue, Break,

    Eof, 
    LeftBrace, RightBrace, LeftBracket, RightBracket,
    LeftParen, RightParen, Semicolon, Comma, Assign, AddAssign, SubAssign, MultAssign, DivAssign, RemAssign,
    Equals, Less, LessEqual, Greater, GreaterEqual,
    Not, NotEqual, Plus, Minus, Multiply, Reminder,
    Increment, Decrement,
    Divide, And, Or, Identifier, IntLiteral,
    // 확장된 주석 타입
    SingleLineComment, MultiLineComment, DocumentedComment
}
