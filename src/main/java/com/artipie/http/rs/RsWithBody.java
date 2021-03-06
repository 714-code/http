/*
 * MIT License
 *
 * Copyright (c) 2020 Artipie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.artipie.http.rs;

import com.artipie.http.Connection;
import com.artipie.http.Response;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map.Entry;
import java.util.concurrent.Flow.Publisher;
import wtf.g4s8.jflows.PubSingle;

/**
 * Response with body.
 * @since 0.3
 * @todo #29:30min Create a unit test for this class
 *  and hamcrest matcher to verify response body (similar to RsHasStatus).
 *  The test should verify response body content created from byte buffer
 *  and from string with charset encoding.
 */
public final class RsWithBody implements Response {

    /**
     * Origin response.
     */
    private final Response origin;

    /**
     * Body buffer.
     */
    private final ByteBuffer body;

    /**
     * Decorates response with new text body.
     * @param origin Response to decorate
     * @param body Text body
     * @param charset Encoding
     */
    public RsWithBody(final Response origin, final String body, final Charset charset) {
        this(origin, ByteBuffer.wrap(body.getBytes(charset)));
    }

    /**
     * Creates new response with text body.
     * @param body Text body
     * @param charset Encoding
     */
    public RsWithBody(final String body, final Charset charset) {
        this(ByteBuffer.wrap(body.getBytes(charset)));
    }

    /**
     * Creates new response from byte buffer.
     * @param buf Buffer body
     */
    public RsWithBody(final ByteBuffer buf) {
        this(Response.EMPTY, buf);
    }

    /**
     * Overrides origin response body with byte buffer.
     * @param origin Response
     * @param buf Body buffer
     */
    public RsWithBody(final Response origin, final ByteBuffer buf) {
        this.origin = origin;
        this.body = buf;
    }

    @Override
    public void send(final Connection con) {
        this.origin.send(new RsWithBody.ConWithBody(con, new PubSingle<>(this.body)));
    }

    /**
     * Connection with body publisher.
     * @since 0.3
     */
    private static final class ConWithBody implements Connection {

        /**
         * Origin connection.
         */
        private final Connection origin;

        /**
         * Body publisher.
         */
        private final Publisher<ByteBuffer> body;

        /**
         * Ctor.
         * @param origin Connection
         * @param body Publisher
         */
        ConWithBody(final Connection origin, final Publisher<ByteBuffer> body) {
            this.origin = origin;
            this.body = body;
        }

        @Override
        public void accept(final int code, final Iterable<Entry<String, String>> headers,
            final Publisher<ByteBuffer> none) {
            this.origin.accept(code, headers, this.body);
        }
    }
}
