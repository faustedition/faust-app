package com.bradmcevoy.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.io.StreamUtils;

/**
 *
 */
public class DebugFilter implements Filter{

    private static final Logger log = LoggerFactory.getLogger(DebugFilter.class);

    private static int counter = 0;

    public void process(FilterChain chain, Request request, Response response) {
        try {
            DebugRequest req2 = new DebugRequest(request);
            DebugResponse resp2 = new DebugResponse(response);
            chain.process(req2, resp2);
            record(req2,resp2);
            response.getOutputStream().write(resp2.out.toByteArray());
            response.getOutputStream().flush();
        } catch (IOException ex) {
            log.error("", ex);
        }
    }

    private static synchronized void record(DebugRequest req2, DebugResponse resp2) {
        counter++;
        FileOutputStream fout = null;
        try {
            File f = new File(System.getProperty("user.home"));
            f = new File(f, counter + "_" + req2.getMethod() + "_.req");
            fout = new FileOutputStream(f);
            req2.record(fout);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                fout.close();
            } catch (IOException ex) {
            }
        }

        try {
            File f = new File(System.getProperty("user.home"));
            f = new File(f, counter + "_" + req2.getMethod() + "_.resp");
            fout = new FileOutputStream(f);
            resp2.record(fout);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                fout.close();
            } catch (IOException ex) {
            }
        }

    }

    public class DebugResponse extends AbstractResponse {
        final Response r;
        final ByteArrayOutputStream out;

        public DebugResponse(Response r) {
            this.r = r;
            out = new ByteArrayOutputStream();
        }

        public Status getStatus() {
            return r.getStatus();
        }

        public void setStatus(Status status) {
            r.setStatus(status);
        }

        public void setNonStandardHeader(String code, String value) {
            r.setNonStandardHeader(code, value);
        }

        public String getNonStandardHeader(String code) {
            return r.getNonStandardHeader(code);
        }

        public OutputStream getOutputStream() {
            return out;
        }

        public  Map<String,String> getHeaders() {
            return r.getHeaders();
        }

        private void record(FileOutputStream fout) {
            try {
                PrintWriter writer = new PrintWriter(fout);
                writer.println("HTTP/1.1 " + getStatus().code);
                for (Map.Entry<String, String> header : this.getHeaders().entrySet()) {
                    writer.println(header.getKey() + ": " + header.getValue());
                }
                writer.flush();
                fout.write(out.toByteArray());
                fout.flush();
            } catch (IOException ex) {
                log.error("",ex);
            }
        }


    }

    public class DebugRequest extends AbstractRequest {
        final Request r;
        final byte[] contentBytes;
        final ByteArrayInputStream content;

        public DebugRequest(Request r) {
            this.r = r;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                StreamUtils.readTo(r.getInputStream(), out);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            this.contentBytes = out.toByteArray();
            this.content = new ByteArrayInputStream(this.contentBytes);
            log.debug(out.toString());
        }

        public Map<String, String> getHeaders() {
            return r.getHeaders();
        }

        @Override
        public String getRequestHeader(Header header) {
            return r.getRequestHeader(header);
        }

        public String getFromAddress() {
            return r.getFromAddress();
        }

        public Method getMethod() {
            return r.getMethod();
        }

        public Auth getAuthorization() {
            return r.getAuthorization();
        }

        public String getAbsoluteUrl() {
            return r.getAbsoluteUrl();
        }

        public InputStream getInputStream() throws IOException {
            return content;
        }

        public void parseRequestParameters(Map<String, String> params, Map<String, FileItem> files) throws RequestParseException {
            r.parseRequestParameters(params, files);
        }

        public void record(OutputStream out) {
            PrintWriter writer = new PrintWriter(out);
            writer.println(getMethod() + " " + getAbsolutePath() + " HTTP/1.1");
            for(Map.Entry<String,String> header : this.getHeaders().entrySet()) {
                writer.println(header.getKey() + ": " + header.getValue());
            }
            writer.flush();
            try {
                out.write(contentBytes);
            } catch (IOException ex) {
                log.error("",ex);
            }
        }

    }

}
