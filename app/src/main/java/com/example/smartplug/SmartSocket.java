package com.example.smartplug;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class SmartSocket {
    private String id;
    private SwitchMaterial sw;
    private EditText et;
    private Button btn;
    private SocketHandler handler;

    public interface SocketHandler {
        void onAction(String path, String message);
    }

    public SmartSocket(String id, View root, int swId, int etId, int btnId, SocketHandler handler) {
        this.id = id;
        this.handler = handler;
        this.sw = root.findViewById(swId);
        this.et = root.findViewById(etId);
        this.btn = root.findViewById(btnId);

        // UPDATE: Kirim pesan Toast saat Switch ditekan
        sw.setOnClickListener(v -> {
            String state = sw.isChecked() ? " dinyalakan" : " dimatikan";
            handler.onAction(id + (sw.isChecked() ? "/ON" : "/OFF"), id + state);
        });

        // UPDATE: Kirim pesan Toast saat Timer diatur
        btn.setOnClickListener(v -> {
            String m = et.getText().toString();
            if (!m.isEmpty()) {
                int sec = Integer.parseInt(m) * 60;
                String msg = "Timer " + id + " diatur: " + m + " menit";
                handler.onAction(id + "/TIMER?sec=" + sec, msg);
                et.setText("");
            }
        });
    }

    public void updateStateSilently(boolean isOn) {
        // Menghapus listener sementara agar tidak mengirim perintah balik ke Wemos
        sw.setOnCheckedChangeListener(null);
        sw.setChecked(isOn);
        // Pasang listener lagi (sama seperti di atas)
        sw.setOnCheckedChangeListener((btn, isChecked) -> {
            String state = isChecked ? " dinyalakan" : " dimatikan";
            handler.onAction(id + (isChecked ? "/ON" : "/OFF"), id + state);
        });
    }
}