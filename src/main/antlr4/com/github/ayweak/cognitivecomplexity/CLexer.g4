lexer grammar CLexer;

options {
    language = Java;
}

channels {
    PREPROCESSOR_CHANNEL
}

@members {

boolean afterNewline;
boolean inPp;
int ppBackslashNewlineSequenceLength;
Token lastToken;

@Override
public void emit(Token token) {
    super.emit(token);
    lastToken = token;
    afterNewline = false;
}

@Override
public Token emit() {
    if (inPp) {
        setChannel(PREPROCESSOR_CHANNEL);
    }
    return super.emit();
}

}

// Keyword

Auto : 'auto';
Break : 'break';
Case : 'case';
Char : 'char';
Const : 'const';
Continue : 'continue';
Default : 'default';
Do : 'do';
Double : 'double';
Else : 'else';
Enum : 'enum';
Extern : 'extern';
Float : 'float';
For : 'for';
Goto : 'goto';
If : 'if';
Inline : 'inline';
Int : 'int';
Long : 'long';
Register : 'register';
Restrict : 'restrict';
Return : 'return';
Short : 'short';
Signed : 'signed';
Sizeof : 'sizeof';
Static : 'static';
Struct : 'struct';
Switch : 'switch';
Typedef : 'typedef';
Union : 'union';
Unsigned : 'unsigned';
Void : 'void';
Volatile : 'volatile';
While : 'while';

Alignas : '_Alignas';
Alignof : '_Alignof';
Atomic : '_Atomic';
Bool : '_Bool';
Complex : '_Complex';
Generic : '_Generic';
Imaginary : '_Imaginary';
Noreturn : '_Noreturn';
StaticAssert : '_Static_assert';
ThreadLocal : '_Thread_local';

// Extension keyword

ExtKwExtension : '__extension__';
ExtKwBuiltinVaArg : '__builtin_va_arg';
ExtKwBuiltinOffsetof : '__builtin_offsetof';
ExtKwM128 : '__m128';
ExtKwM128d : '__m128d';
ExtKwM128i : '__m128i';
ExtKwTypeof : '__typeof__';
ExtKwInline : '__inline__';
ExtKwStdcall : '__stdcall';
ExtKwDeclspec : '__declspec';
ExtKwAsm1 : '__asm';
ExtKwAsm2 : '__asm__';
ExtKwAttribute : '__attribute__';
ExtKwVolatile : '__volatile__';

// Punctuator

LeftParen : '(';
RightParen : ')';
LeftBracket : '[';
RightBracket : ']';
LeftBrace : '{';
RightBrace : '}';

Less : '<';
LessEqual : '<=';
Greater : '>';
GreaterEqual : '>=';
LeftShift : '<<';
RightShift : '>>';

Plus : '+';
PlusPlus : '++';
Minus : '-';
MinusMinus : '--';
Star : '*';
Div : '/';
Mod : '%';

And : '&';
Or : '|';
AndAnd : '&&';
OrOr : '||';
Caret : '^';
Not : '!';
Tilde : '~';

Question : '?';
Colon : ':';
Semi : ';';
Comma : ',';

Assign : '=';
// '*=' | '/=' | '%=' | '+=' | '-=' | '<<=' | '>>=' | '&=' | '^=' | '|='
StarAssign : '*=';
DivAssign : '/=';
ModAssign : '%=';
PlusAssign : '+=';
MinusAssign : '-=';
LeftShiftAssign : '<<=';
RightShiftAssign : '>>=';
AndAssign : '&=';
XorAssign : '^=';
OrAssign : '|=';

Equal : '==';
NotEqual : '!=';

Arrow : '->';
Dot : '.';
Ellipsis : '...';

PpHash
    :   Hash
        { afterNewline || lastToken == null || lastToken.getType() == Newline }?
        { inPp = true; }
        -> mode(Preprocessor)
    ;

Hash : '#';
HashHash : '##';

// 

Identifier
    :   IdentifierNondigit
        (   IdentifierNondigit
        |   Digit
        )*
    ;

fragment
IdentifierNondigit
    :   Nondigit
    |   UniversalCharacterName
    //|   // other implementation-defined characters...
    ;

fragment
Nondigit
    :   [a-zA-Z_]
    ;

fragment
Digit
    :   [0-9]
    ;

fragment
UniversalCharacterName
    :   '\\u' HexQuad
    |   '\\U' HexQuad HexQuad
    ;

fragment
HexQuad
    :   HexadecimalDigit HexadecimalDigit HexadecimalDigit HexadecimalDigit
    ;

//

Constant
    :   IntegerConstant
    |   FloatingConstant
    //|   EnumerationConstant
    |   CharacterConstant
    ;

fragment
IntegerConstant
    :   DecimalConstant IntegerSuffix?
    |   OctalConstant IntegerSuffix?
    |   HexadecimalConstant IntegerSuffix?
    |   BinaryConstant
    ;

fragment
BinaryConstant
    :   '0' [bB] [0-1]+
    ;

fragment
DecimalConstant
    :   NonzeroDigit Digit*
    ;

fragment
OctalConstant
    :   '0' OctalDigit*
    ;

fragment
HexadecimalConstant
    :   HexadecimalPrefix HexadecimalDigit+
    ;

fragment
HexadecimalPrefix
    :   '0' [xX]
    ;

fragment
NonzeroDigit
    :   [1-9]
    ;

fragment
OctalDigit
    :   [0-7]
    ;

fragment
HexadecimalDigit
    :   [0-9a-fA-F]
    ;

fragment
IntegerSuffix
    :   UnsignedSuffix LongSuffix?
    |   UnsignedSuffix LongLongSuffix
    |   LongSuffix UnsignedSuffix?
    |   LongLongSuffix UnsignedSuffix?
    ;

fragment
UnsignedSuffix
    :   [uU]
    ;

fragment
LongSuffix
    :   [lL]
    ;

fragment
LongLongSuffix
    :   'll' | 'LL'
    ;

fragment
FloatingConstant
    :   DecimalFloatingConstant
    |   HexadecimalFloatingConstant
    ;

fragment
DecimalFloatingConstant
    :   FractionalConstant ExponentPart? FloatingSuffix?
    |   DigitSequence ExponentPart FloatingSuffix?
    ;

fragment
HexadecimalFloatingConstant
    :   HexadecimalPrefix (HexadecimalFractionalConstant | HexadecimalDigitSequence) BinaryExponentPart FloatingSuffix?
    ;

fragment
FractionalConstant
    :   DigitSequence? '.' DigitSequence
    |   DigitSequence '.'
    ;

fragment
ExponentPart
    :   [eE] Sign? DigitSequence
    ;

fragment
Sign
    :   [+-]
    ;

DigitSequence
    :   Digit+
    ;

fragment
HexadecimalFractionalConstant
    :   HexadecimalDigitSequence? '.' HexadecimalDigitSequence
    |   HexadecimalDigitSequence '.'
    ;

fragment
BinaryExponentPart
    :   [pP] Sign? DigitSequence
    ;

fragment
HexadecimalDigitSequence
    :   HexadecimalDigit+
    ;

fragment
FloatingSuffix
    :   [flFL]
    ;

fragment
CharacterConstant
    :   '\'' CCharSequence '\''
    |   'L\'' CCharSequence '\''
    |   'u\'' CCharSequence '\''
    |   'U\'' CCharSequence '\''
    ;

fragment
CCharSequence
    :   CChar+
    ;

fragment
CChar
    :   ~['\\\r\n]
    |   EscapeSequence
    |   BackslashNewline
    ;

fragment
EscapeSequence
    :   SimpleEscapeSequence
    |   OctalEscapeSequence
    |   HexadecimalEscapeSequence
    |   UniversalCharacterName
    ;

fragment
SimpleEscapeSequence
    :   '\\' ['"?abfnrtv\\]
    ;

fragment
OctalEscapeSequence
    :   '\\' OctalDigit OctalDigit? OctalDigit?
    ;

fragment
HexadecimalEscapeSequence
    :   '\\x' HexadecimalDigit+
    ;

// 

StringLiteral
    :   EncodingPrefix? '"' SCharSequence? '"'
    ;

fragment
EncodingPrefix
    :   'u8'
    |   'u'
    |   'U'
    |   'L'
    ;

fragment
SCharSequence
    :   SChar+
    ;

fragment
SChar
    :   ~["\\\r\n]
    |   EscapeSequence
    |   '\\\n'   // Added line
    |   '\\\r\n' // Added line
    ;

// ignore the following asm blocks:
/*
    asm
    {
        mfspr x, 286;
    }
 */
AsmBlock
    :   'asm' ~'{'* '{' ~'}'* '}'
        -> skip
    ;

Whitespace
    :   [ \t]+
        -> skip
    ;

Newline
    :   '\r'? '\n'
        {
            if (inPp) {
                setChannel(PREPROCESSOR_CHANNEL);
                inPp = false;
            } else {
                skip();
                afterNewline = true;
            }
        }
    ;

BackslashNewline
    :   '\\' '\r'? '\n'
        -> skip
    ;

BlockComment
    :   '/*' .*? '*/'
        -> skip
    ;

LineComment
    :   '//' ~[\r\n]*
        -> skip
    ;

mode Preprocessor;

PpIf
    :   'if'
        { lastToken.getType() == PpHash }?
    ;

PpIfdef
    :   'ifdef'
        { lastToken.getType() == PpHash }?
    ;

PpIfndef
    :   'ifndef'
        { lastToken.getType() == PpHash }?
    ;

PpElif
    :   'elif'
        { lastToken.getType() == PpHash }?
    ;

PpElse
    :   'else'
        { lastToken.getType() == PpHash }?
    ;

PpEndif
    :   'endif'
        { lastToken.getType() == PpHash }?
    ;

PpInclude
    :   'include'
        { lastToken.getType() == PpHash }?
    ;

PpDefine
    :   'define'
        { lastToken.getType() == PpHash }?
    ;

PpUndef
    :   'undef'
        { lastToken.getType() == PpHash }?
    ;

PpLine
    :   'line'
        { lastToken.getType() == PpHash }?
    ;

PpError
    :   'error'
        { lastToken.getType() == PpHash }?
    ;

PpPragma
    :   'pragma'
        { lastToken.getType() == PpHash }?
    ;

HeaderName
    :   ( '<' ~[>\r\n]+ '>' | '"' ~["\r\n]+ '"' )
        { lastToken.getType() == PpInclude }?
    ;

MacroName
    :   Identifier
        { lastToken.getType() == PpDefine }?
        { ppBackslashNewlineSequenceLength = 0; }
    ;

MacroLeftParen
    :   LeftParen
        { lastToken.getType() == MacroName
        && lastToken.getStopIndex() == _tokenStartCharIndex - ppBackslashNewlineSequenceLength - 1 }?
    ;

PpWhitespace
    :   Whitespace
        -> skip
    ;

PpNewline
    :   Newline
        { inPp = false; }
        -> type(Newline), channel(PREPROCESSOR_CHANNEL), mode(DEFAULT_MODE)
    ;

PpBackslashNewline
    :   BackslashNewline
        { ppBackslashNewlineSequenceLength += getText().length(); }
        -> skip
    ;

PpBlockComment
    :   BlockComment
        -> skip
    ;

PpAny
    :   .
        {
            // seek で後戻りしても改行のカウントは戻らないので、改行は PpNewline で一致させる。
            _input.seek(_tokenStartCharIndex);
        }
        -> skip, mode(DEFAULT_MODE)
    ;
