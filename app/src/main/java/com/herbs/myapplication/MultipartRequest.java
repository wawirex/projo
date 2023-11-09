package com.herbs.myapplication;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class MultipartRequest extends Request<NetworkResponse> {

    private final Response.Listener<NetworkResponse> mListener;
    private final Response.ErrorListener mErrorListener;
    private final Map<String, String> mStringParts;
    private final Map<String, byte[]> mByteParts;
    private final String boundary;

    public MultipartRequest(int method, String url, Response.Listener<NetworkResponse> listener,
                            Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
        mErrorListener = errorListener;
        mStringParts = new HashMap<>();
        mByteParts = new HashMap<>();
        boundary = "----" + System.currentTimeMillis(); // Set the boundary here
        setShouldCache(false);
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data; boundary=" + boundary;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        try {
            // Add string parameters
            for (Map.Entry<String, String> entry : mStringParts.entrySet()) {
                buildTextPart(dos, entry.getKey(), entry.getValue());
            }

            // Add binary parameters
            for (Map.Entry<String, byte[]> entry : mByteParts.entrySet()) {
                buildDataPart(dos, entry.getKey(), entry.getValue());
            }

            // End of multipart/form-data
            dos.writeBytes("--" + boundary + "--" + "\r\n");

            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void buildTextPart(DataOutputStream dataOutputStream, String parameterName, String parameterValue) throws IOException {
        dataOutputStream.writeBytes("--" + boundary + "\r\n");
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + parameterName + "\"\r\n\r\n");
        dataOutputStream.writeBytes(parameterValue + "\r\n");
    }

    private void buildDataPart(DataOutputStream dataOutputStream, String parameterName, byte[] data) throws IOException {
        dataOutputStream.writeBytes("--" + boundary + "\r\n");
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" +
                parameterName + "\"; filename=\"" + parameterName + "\"" + "\r\n");
        dataOutputStream.writeBytes("Content-Type: image/jpeg" + "\r\n\r\n");
        dataOutputStream.write(data);
        dataOutputStream.writeBytes("\r\n");
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }

    @Override
    public void deliverError(VolleyError error) {
        mErrorListener.onErrorResponse(error);
    }

    public void addStringPart(String key, String value) {
        mStringParts.put(key, value);
    }

    public void addBytePart(String key, byte[] value) {
        mByteParts.put(key, value);
    }
}
