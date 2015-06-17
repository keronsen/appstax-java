package com.appstax;

import org.json.JSONException;

import static org.junit.Assert.*;

public class AxObjectTest extends AxTest {

    @org.junit.Test
    public void testCollection() {
        AxObject object = new AxObject(COLLECTION_1);
        assertEquals(COLLECTION_1, object.getCollection());
    }

    @org.junit.Test
    public void testProperties() {
        AxObject object = new AxObject(COLLECTION_1);
        object.put(PROPERTY_1, 1);
        object.put(PROPERTY_2, 1.1);
        object.put(PROPERTY_3, "1");

        assertTrue(object.has(PROPERTY_1));
        assertFalse(object.has(PROPERTY_1 + "1"));

        assertEquals(1, object.get(PROPERTY_1));
        assertEquals(1.1, object.get(PROPERTY_2));
        assertEquals("1", object.get(PROPERTY_3));
        assertEquals("1", object.getString(PROPERTY_3));

        try {
            object.getString(PROPERTY_1);
        } catch (JSONException e) {
            assertNotNull(e);
        }
    }

}