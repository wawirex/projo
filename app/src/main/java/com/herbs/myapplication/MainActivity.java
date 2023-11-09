package com.herbs.myapplication;

import android.Manifest;
import android.app.Activity;
import android.graphics.Color;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Locale;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private ImageView imageView;
    private Button captureButton;
    private Button predictButton;
    private TextView predictionTextView;
    private byte[] encodedImageBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        captureButton = findViewById(R.id.captureButton);
        predictButton = findViewById(R.id.predictButton);
        predictionTextView = findViewById(R.id.predictionTextView);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
                } else {
                    openCamera();
                }
            }
        });

        predictButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendImageForPrediction();
            }
        });
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);

            encodeImageToBytes(imageBitmap);
        }
    }

    private void encodeImageToBytes(Bitmap imageBitmap) {
        int targetWidth = 256;
        int targetHeight = 256;
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, targetWidth, targetHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        encodedImageBytes = byteArrayOutputStream.toByteArray();
    }

    private void sendImageForPrediction() {
        String predictionUrl = "http://127.0.0.1:7000/predict?";

        MultipartRequest request = new MultipartRequest(Request.Method.POST, predictionUrl,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        // Handle the response
                        try {
                            // Parse the response data
                            JSONObject jsonResponse = new JSONObject(new String(response.data));
                            String predictedClass = jsonResponse.getString("class");
                            double confidence = jsonResponse.getDouble("confidence");

                            // Convert confidence to percentage
                            int confidencePercentage = (int) (confidence * 100);

                            // Determine color based on confidence level
                            int color = getColorBasedOnConfidence(confidence);

                            // Display the results
                            String resultText = String.format(Locale.getDefault(),
                                    "Class: %s\nConfidence: %d%%", predictedClass, confidencePercentage);
                            predictionTextView.setText(resultText);
                            predictionTextView.setTextColor(color);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            predictionTextView.setText("Error Obtaining Data");
                        // ...
                        String result = new String(response.data);
                        predictionTextView.setText(result);
                    }
                }

                //color manenos

                //color manenos
    },
            new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            // Handle errors
            predictionTextView.setText("Error: " + error.getMessage());
        }
    }
        );

        // Add the file part with the correct parameter name
        request.addBytePart("file", encodedImageBytes);

        // Add the request to the queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);

    }
    private int getColorBasedOnConfidence(double confidence) {
        // Customize this method to define color thresholds based on confidence levels
        if (confidence >= 0.90) {
            return Color.GREEN;  // High confidence, green color
        } else if (confidence >= 0.70) {
            return Color.YELLOW; // Moderate confidence, yellow color
        } else {
            return Color.RED;    // Low confidence, red color
        }
    }
}




