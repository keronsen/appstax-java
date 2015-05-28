package com.appstax;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import static org.junit.Assert.*;

public class AxFileTest extends AxTest {

    @org.junit.Test
    public void testFileUploadSuccess() throws Exception {
        MockWebServer server = createMockWebServer();

        server.enqueue(new MockResponse().setBody(getResource("save-object-success.json")));
        server.enqueue(new MockResponse().setBody(""));

        String filename = "file-example-image.jpg";
        AxObject object = new AxObject(COLLECTION_2);
        AxFile file = new AxFile(filename, getResource(filename));
        object.put("image", file);
        Ax.save(object);

        RecordedRequest req1 = server.takeRequest();
        String body = "{\"image\":{\"sysDatatype\":\"file\",\"filename\":\"file-example-image.jpg\"}}";
        assertEquals("POST", req1.getMethod());
        assertEquals(body, req1.getBody().readUtf8());

        RecordedRequest req2 = server.takeRequest();
        assertEquals("PUT", req2.getMethod());
        assertTrue(req2.getPath().indexOf("/files/") > -1);
        assertTrue(req2.getHeader("Content-Type").startsWith("multipart"));
        assertTrue(req2.getBody().readUtf8().indexOf("filedata") > -1);

        server.shutdown();
    }

    @org.junit.Test
    public void testMultiFileUploadSuccess() throws Exception {
        MockWebServer server = createMockWebServer();
        server.enqueue(new MockResponse().setBody(getResource("save-object-success.json")));
        server.enqueue(new MockResponse().setBody(""));
        server.enqueue(new MockResponse().setBody(""));
        server.enqueue(new MockResponse().setBody(""));

        String filename = "file-example-image.jpg";
        AxObject object = new AxObject(COLLECTION_2);

        object.put("image1", new AxFile("file-example-image-1.jpg", getResource(filename)));
        object.put("image2", new AxFile("file-example-image-2.jpg", getResource(filename)));
        object.put("image3", new AxFile("file-example-image-3.jpg", getResource(filename)));

        Ax.save(object);

        RecordedRequest req1 = server.takeRequest();
        RecordedRequest req2 = server.takeRequest();
        RecordedRequest req3 = server.takeRequest();
        RecordedRequest req4 = server.takeRequest();

        assertEquals("POST", req1.getMethod());
        assertEquals("PUT", req2.getMethod());
        assertEquals("PUT", req3.getMethod());
        assertEquals("PUT", req4.getMethod());

        server.shutdown();
    }

    @org.junit.Test
    public void testFileGetSuccess() throws Exception {
        MockWebServer server = createMockWebServer();
        server.enqueue(new MockResponse().setBody(getResource("find-file-success.json")));

        AxObject object = Ax.find(COLLECTION_1, "123");
        AxFile file = object.getFile("file");

        RecordedRequest req = server.takeRequest();
        assertEquals("GET", req.getMethod());
        assertEquals("file-example-image.jpg", file.getName());
        assertEquals("files/FileCollection/opgklDfL0Y4Q/file/file-example-image.jpg", file.getUrl());
        assertNull(file.getData());

        server.enqueue(new MockResponse().setBody(getResource("file-example-image.jpg")));
        file.load();
        assertNotNull(file.getData());

        req = server.takeRequest();
        assertEquals("GET", req.getMethod());
        assertEquals(Ax.getAppKey(), req.getHeader("x-appstax-appkey"));

        server.shutdown();
    }

    @org.junit.Test(expected=AxException.class)
    public void testFileGetError() throws Exception {
        MockWebServer server = createMockWebServer();
        server.enqueue(new MockResponse().setBody(getResource("find-file-success.json")));

        AxObject object = Ax.find(COLLECTION_1, "123");
        AxFile file = object.getFile("file");
        assertNull(file.getData());

        server.enqueue(new MockResponse().setBody(getResource("find-object-error.json")).setResponseCode(400));

        file.load();
        server.shutdown();
    }

}