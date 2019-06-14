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

public abstract class Reader {

    public abstract void close() throws IOException;

    public abstract int read() throws IOException;

    public int read(char[] cbuf) throws IOException {
        return read(cbuf, 0, cbuf.length);
    }

    public abstract int read(char[] cbuf, int off, int len) throws IOException;
}
