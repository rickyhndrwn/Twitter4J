/*
Copyright (c) 2007-2011, Yusuke Yamamoto
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the Yusuke Yamamoto nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY Yusuke Yamamoto ``AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL Yusuke Yamamoto BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package twitter4j.internal.http.alternative;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import twitter4j.internal.http.HttpClientConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.2
 */
final class ApacheHttpClientHttpResponseImpl extends twitter4j.internal.http.HttpResponse {
    private HttpResponse res;

    ApacheHttpClientHttpResponseImpl(HttpResponse res, HttpClientConfiguration conf) throws IOException {
        super(conf);
        this.res = res;
        is = res.getEntity().getContent();
        statusCode = res.getStatusLine().getStatusCode();
        if (null != is && "gzip".equals(getResponseHeader("Content-Encoding"))) {
            // the response is gzipped
            is = new GZIPInputStream(is);
        }
    }

    /**
     * {@inheritDoc}
     */
    public final String getResponseHeader(String name) {
        Header[] headers = res.getHeaders(name);
        if (null != headers && headers.length > 0) {
            return headers[0].getValue();
        } else {
            return null;
        }
    }

    @Override
    public Map<String, List<String>> getResponseHeaderFields() {
        Header[] headers = res.getAllHeaders();
        Map<String, List<String>> maps = new HashMap<String, List<String>>();
        for (Header header : headers) {
            HeaderElement[] elements = header.getElements();
            for (HeaderElement element : elements) {
                List<String> values;
                if (null == (values = maps.get(element.getName()))) {
                    values = new ArrayList<String>(1);
                    maps.put(element.getName(), values);
                }
                values.add(element.getValue());
            }
        }
        return maps;
    }

    /**
     * {@inheritDoc}
     */
    public void disconnect() throws IOException {
        if (null != res) {
            res.getEntity().consumeContent();
        }
    }
}
