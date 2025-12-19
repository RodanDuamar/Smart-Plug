package com.example.smartplug;

import android.os.Bundle;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    // 1. Masukkan IP Address Wemos yang muncul di Serial Monitor Arduino
    private final String WEMOS_IP = "192.168.2.119";
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 2. Inisialisasi Switch dari Layout
        SwitchMaterial sw1 = findViewById(R.id.switch1);
        SwitchMaterial sw2 = findViewById(R.id.switch2);
        SwitchMaterial sw3 = findViewById(R.id.switch3);
        SwitchMaterial sw4 = findViewById(R.id.switch4);

        // 3. Set Listener untuk masing-masing switch
        sw1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String path = isChecked ? "R1/ON" : "R1/OFF";
            sendCommand(path, "Soket 1");
        });

        sw2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String path = isChecked ? "R2/ON" : "R2/OFF";
            sendCommand(path, "Soket 2");
        });

        sw3.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String path = isChecked ? "R3/ON" : "R3/OFF";
            sendCommand(path, "Soket 3");
        });

        sw4.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String path = isChecked ? "R4/ON" : "R4/OFF";
            sendCommand(path, "Soket 4");
        });
    }

    // 4. Fungsi untuk mengirim perintah HTTP ke Wemos
    private void sendCommand(String path, String deviceName) {
        String url = "http://" + WEMOS_IP + "/" + path;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Jika gagal (Misal: Wemos mati atau HP tidak di WiFi yang sama)
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Gagal mengontrol " + deviceName, Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Jika berhasil, munculkan pesan singkat
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, deviceName + " berhasil diubah", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
}