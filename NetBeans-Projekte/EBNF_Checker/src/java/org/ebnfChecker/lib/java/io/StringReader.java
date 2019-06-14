/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package java.io;

/**
 *
 * @author Admin
 */
import java.io.IOException;

public class StringReader extends Reader {

    /**
     * The text to read.
     */
    private final String text;

    /**
     * The next position to read.
     */
    private int position;

    /**
     * Constructor.
     *
     * @param text The source text to read.
     */
    public StringReader(String text) {
        this.text = text;
        this.position = 0;
    }

    /**
     * Reads the next character in the source text.
     *
     * @return The next character or -1 if end of text is reached.
     */
    public int read() throws IOException {
        return (this.position == this.text.length()) ? -1 : this.text
                .charAt(this.position++);
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if (position >= text.length()) {
            return -1;
        }
        int n = Math.min(text.length() - position, len);
        text.getChars(position, position + n, cbuf, off);
        position += n;
        return n;
    }

}
