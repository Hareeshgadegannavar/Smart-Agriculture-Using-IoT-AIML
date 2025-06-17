package com.example.test1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.test1.ml.DataModel;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class SensorPredict extends AppCompatActivity {


    Button b1;
    TextView t1;
    private Interpreter tflite;

    String temp,hum;
    String[] cropNames = {
            "Apple", "Banana", "Blackgram", "Chickpea", "Coconut", "Coffee", "Cotton",
            "Grapes", "Jute", "Kidneybeans", "Lentil", "Maize", "Mango", "Mothbean",
            "Mungbean", "Muskmelon", "Orange", "Papaya", "Pigeonpeas", "Pomogranate",
            "Rice", "Watermilon"
    };
    private static final int numClasses = 22;

    // Mean and standard deviation values extracted from Python
    float[] mean_values = {(float) 77.082847, (float) 49.582586, (float) 48.149867, (float) 25.616243, (float) 71.481039, (float) 103.463745, (float) 6.4694805};
    float[] std_dev_values = {(float) 36.522455, (float) 36.985465, (float) 50.648283, (float) 7.387625, (float) 28.163111, (float) 54.958389, (float) 1.7173705};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_predict);


       t1=findViewById(R.id.cr_result);
       temp=getIntent().getStringExtra("temp");
       hum=getIntent().getStringExtra("humidity");
       predictCrop();
    }

    private void predictCrop() {
        try {
            // Initialize the TensorFlow Lite interpreter
            tflite = new Interpreter(loadModelFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        float[] inputValues = {
                1100.25f,
                45.85f,
                18.26f,
                Float.parseFloat(temp),
                Float.parseFloat(hum),
               90.853f,
                7.8f
        };

        // Apply preprocessing steps
        for (int i = 0; i < inputValues.length; i++) {
            inputValues[i] = (inputValues[i] - mean_values[i]) / std_dev_values[i];
        }

        // Convert input data to ByteBuffer
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(inputValues.length * Float.BYTES);
        inputBuffer.order(ByteOrder.nativeOrder());
        for (float value : inputValues) {
            inputBuffer.putFloat(value);
        }

        // Run inference
        float[][] outputData = new float[1][numClasses];
        tflite.run(inputBuffer, outputData);

        // Process the output data
        int predictedLabel = argmax(outputData[0]);
        String predictedCropName = cropNames[predictedLabel];
        t1.setText("Predicted crop: " + predictedCropName);
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = getAssets().openFd("test_new_model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private int argmax(float[] array) {
        int maxIndex = 0;
        float maxValue = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > maxValue) {
                maxValue = array[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }
}
