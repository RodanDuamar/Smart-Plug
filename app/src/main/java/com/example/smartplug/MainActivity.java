package com.example.smartplug;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private String currentIP = "";

    private OkHttpClient client = new OkHttpClient();
    private List<SmartSocket> sockets = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SmartSocket.SocketHandler h = (path,msg) -> sendCommand(path, msg);

        // Menghubungkan UI dengan Objek Java
        sockets.add(new SmartSocket("R1", findViewById(android.R.id.content), R.id.sw1, R.id.et1, R.id.btn1, h));
        sockets.add(new SmartSocket("R2", findViewById(android.R.id.content), R.id.sw2, R.id.et2, R.id.btn2, h));
        sockets.add(new SmartSocket("R3", findViewById(android.R.id.content), R.id.sw3, R.id.et3, R.id.btn3, h));
        sockets.add(new SmartSocket("R4", findViewById(android.R.id.content), R.id.sw4, R.id.et4, R.id.btn4, h));

        startDiscovery(); // Auto-Discovery via UDP Broadcast
    }

    private void sendCommand(String path, String message) {
        if (currentIP.isEmpty()) {
            Toast.makeText(this, "Wemos belum ditemukan!", Toast.LENGTH_SHORT).show();
            return;
        }

        Request req = new Request.Builder().url("http://" + currentIP + "/" + path).build();
        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call c, IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Gagal terhubung ke Wemos", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call c, Response r) throws IOException {
                if (r.isSuccessful()) {
                    // Tampilkan Toast sukses di UI Thread
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                        syncStatus(); // Langsung sinkronkan status tampilan
                    });
                }
            }
        });
    }

    private void startDiscovery() {
        new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(8888)) {
                byte[] buf = new byte[1024];
                while (true) {
                    DatagramPacket p = new DatagramPacket(buf, buf.length);
                    socket.receive(p);
                    String msg = new String(p.getData(), 0, p.getLength());
                    if (msg.contains("SOCKET_HUB_IP:")) {
                        currentIP = msg.split(":")[1];
                        // PANGGIL DI SINI: Segera setelah IP ditemukan
                        runOnUiThread(() -> syncStatus());
                        break;
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void syncStatus() {
        if (currentIP.isEmpty()) return;

        Request request = new Request.Builder()
                .url("http://" + currentIP + "/status")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String res = response.body().string(); // Contoh respon: "1001"

                runOnUiThread(() -> {
                    for (int i = 0; i < 4; i++) {
                        // Jika karakter ke-i adalah '1', maka statusnya ON
                        boolean isOn = res.charAt(i) == '1';
                        sockets.get(i).updateStateSilently(isOn);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                // Log error jika gagal sinkronisasi
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Jika IP sudah pernah ditemukan sebelumnya, lakukan sinkronisasi ulang
        if (!currentIP.isEmpty()) {
            syncStatus();
        }
    }
}