package com.appstax;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public final class AxObject {

    private static final String KEY_CREATED = "sysCreated";
    private static final String KEY_UPDATED = "sysUpdated";
    private static final String KEY_ID = "sysObjectId";
    private static final String KEY_TYPE = "sysDatatype";
    private static final String KEY_FILE = "filename";
    private static final String KEY_DATA = "filedata";
    private static final String KEY_GRANTS = "grants";
    private static final String KEY_REVOKES = "revokes";
    private static final String KEY_USER = "username";
    private static final String KEY_PERMISSIONS = "permissions";
    private static final String TYPE_FILE = "file";

    private String collection;
    private JSONObject properties;
    private JSONObject access;
    private Map<String, AxFile> files;

    public AxObject(String collection) {
        this(collection, new JSONObject());
    }

    public AxObject(String collection, JSONObject properties) {
        this.collection = collection;
        this.properties = properties;
        this.access = new JSONObject();
        this.access.put(KEY_GRANTS, new JSONArray());
        this.access.put(KEY_REVOKES, new JSONArray());
        this.files = new HashMap<String, AxFile>();
    }

    public String getCollection() {
        return this.collection;
    }

    public Object get(String key) {
        return this.properties.has(key) ?
                this.properties.get(key) :
                null;
    }

    public String getId() {
        return this.properties.has(KEY_ID) ?
            this.properties.getString(KEY_ID) :
            null;
    }

    public AxFile getFile(String key) {
        JSONObject meta = this.properties.getJSONObject(key);
        return new AxFile(meta);
    }

    public void put(String key, Object val) {
        this.properties.put(key, val);
    }

    public void put(String key, AxFile file) {
        JSONObject meta = new JSONObject();
        meta.put(KEY_TYPE, TYPE_FILE);
        meta.put(KEY_FILE, file.getName());
        this.files.put(key, file);
        this.put(key, meta);
    }

    public AxObject grantPublic(String... permissions) {
        return this.permission(KEY_GRANTS, "*", permissions);
    }

    public AxObject grant(String username, String... permissions) {
        return this.permission(KEY_GRANTS, username, permissions);
    }

    public AxObject revokePublic(String... permissions) {
        return this.permission(KEY_REVOKES, "*", permissions);
    }

    public AxObject revoke(String username, String... permissions) {
        return this.permission(KEY_REVOKES, username, permissions);
    }

    protected AxObject save() {
        saveObject();
        saveAccess();
        saveFiles();
        return this;
    }

    protected AxObject remove() {
        String path = AxPaths.object(this.getCollection(), this.getId());
        this.properties = AxClient.request(AxClient.Method.DELETE, path);
        return this;
    }

    protected AxObject refresh() {
        String path = AxPaths.object(this.getCollection(), this.getId());
        this.properties = AxClient.request(AxClient.Method.GET, path);
        return this;
    }

    private void saveObject() {
        if (this.getId() == null) {
            this.createObject();
        } else {
            this.updateObject();
        }
    }

    private void saveAccess() {
        if (this.hasAccess()) {
            String path = AxPaths.permissions();
            AxClient.request(AxClient.Method.POST, path, this.access);
        }
    }

    private boolean hasAccess() {
        return (
            this.access.getJSONArray(KEY_GRANTS).length() > 0 ||
            this.access.getJSONArray(KEY_REVOKES).length() > 0
        );
    }

    private AxObject createObject() {
        String path = AxPaths.collection(this.getCollection());
        JSONObject meta = AxClient.request(AxClient.Method.POST, path, this.properties);
        this.put(KEY_CREATED, meta.get(KEY_CREATED));
        this.put(KEY_UPDATED, meta.get(KEY_UPDATED));
        this.put(KEY_ID, meta.get(KEY_ID));
        return this;
    }

    private AxObject updateObject() {
        String path = AxPaths.object(this.getCollection(), this.getId());
        JSONObject meta = AxClient.request(AxClient.Method.PUT, path, this.properties);
        this.put(KEY_UPDATED, meta.get(KEY_UPDATED));
        return this;
    }

    private AxObject permission(String type, String username, String[] permissions) {
        if (permissions.length > 0) {
            JSONObject grant = new JSONObject();
            grant.put(KEY_ID, this.getId());
            grant.put(KEY_USER, username);
            grant.put(KEY_PERMISSIONS, new JSONArray(permissions));
            this.access.getJSONArray(type).put(grant);
        }
        return this;
    }

    private void saveFiles() {
        for (Map.Entry<String, AxFile> item : this.files.entrySet()) {
            String key = item.getKey();
            String name = item.getValue().getName();
            String data = item.getValue().getData();
            String path = AxPaths.file(this.getCollection(), this.getId(), key, name);

            Map<String, String> form = new HashMap<String, String>();
            form.put(KEY_DATA, data);
            AxClient.form(AxClient.Method.PUT, path, form);
        }
    }

}