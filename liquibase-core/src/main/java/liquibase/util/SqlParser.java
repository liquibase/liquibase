package liquibase.util;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.grammar.CharStream;
import liquibase.util.grammar.SimpleSqlGrammar;
import liquibase.util.grammar.SimpleSqlGrammarConstants;
import liquibase.util.grammar.Token;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class SqlParser {

    public static StringClauses parse(String sqlBlock) {
        return parse(sqlBlock, false, false);
    }

    public static StringClauses parse(String sqlBlock, boolean preserveWhitespace, boolean preserveComments) {
        StringClauses clauses = new StringClauses(preserveWhitespace?"":" ");

        SimpleSqlGrammar t = new SimpleSqlGrammar(new SqlCharStream(new StringReader(sqlBlock)));
        try {
            Token token = t.getNextToken();
            while (!token.toString().equals("")) {
                if (token.kind == SimpleSqlGrammarConstants.WHITESPACE) {
                    if (preserveWhitespace) {
                        clauses.append(new StringClauses.Whitespace(token.image));
                    }
                } else if (token.kind == SimpleSqlGrammarConstants.LINE_COMMENT || token.kind == SimpleSqlGrammarConstants.MULTI_LINE_COMMENT) {
                    if (preserveComments) {
                        String comment = token.image;
                        if (!preserveWhitespace && token.kind == SimpleSqlGrammarConstants.LINE_COMMENT) {
                            if (!comment.endsWith("\n")) {
                                comment = comment + "\n";
                            }
                        }
                        clauses.append(new StringClauses.Comment(comment));
                    }
                } else {
                    clauses.append(token.image);
                }
                token = t.getNextToken();
            }

        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
        return clauses;
    }

    public static class SqlCharStream implements CharStream {
        char[] buffer = null;

        int bufferLength = 0;				  // end of valid chars
        int bufferPosition = 0;			  // next char to read

        int tokenStart = 0;				  // offset in buffer
        int bufferStart = 0;				  // position in file of buffer

        Reader input;					  // source of chars

        /** Constructs from a Reader. */
        public SqlCharStream(Reader r) {
            input = r;
        }

        public final char readChar() throws IOException {
            if (bufferPosition >= bufferLength)
                refill();
            return buffer[bufferPosition++];
        }

        private final void refill() throws IOException {
            int newPosition = bufferLength - tokenStart;

            if (tokenStart == 0) {			  // token won't fit in buffer
                if (buffer == null) {			  // first time: alloc buffer
                    buffer = new char[2048];
                } else if (bufferLength == buffer.length) { // grow buffer
                    char[] newBuffer = new char[buffer.length*2];
                    System.arraycopy(buffer, 0, newBuffer, 0, bufferLength);
                    buffer = newBuffer;
                }
            } else {					  // shift token to front
                System.arraycopy(buffer, tokenStart, buffer, 0, newPosition);
            }

            bufferLength = newPosition;			  // update state
            bufferPosition = newPosition;
            bufferStart += tokenStart;
            tokenStart = 0;

            int charsRead =				  // fill space in buffer
                    input.read(buffer, newPosition, buffer.length-newPosition);
            if (charsRead == -1)
                throw new IOException("read past eof");
            else
                bufferLength += charsRead;
        }

        public final char BeginToken() throws IOException {
            tokenStart = bufferPosition;
            return readChar();
        }

        public final void backup(int amount) {
            bufferPosition -= amount;
        }

        public final String GetImage() {
            return new String(buffer, tokenStart, bufferPosition - tokenStart);
        }

        public final char[] GetSuffix(int len) {
            char[] value = new char[len];
            System.arraycopy(buffer, bufferPosition - len, value, 0, len);
            return value;
        }

        public final void Done() {
            try {
                input.close();
            } catch (IOException e) {
                System.err.println("Caught: " + e + "; ignoring.");
            }
        }

        public final int getColumn() {
            return bufferStart + bufferPosition;
        }
        public final int getLine() {
            return 1;
        }
        public final int getEndColumn() {
            return bufferStart + bufferPosition;
        }
        public final int getEndLine() {
            return 1;
        }
        public final int getBeginColumn() {
            return bufferStart + tokenStart;
        }
        public final int getBeginLine() {
            return 1;
        }
    }
}
