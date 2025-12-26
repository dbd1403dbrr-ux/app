package com.example.app_doan;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import org.json.JSONException;


public class MainActivity extends AppCompatActivity {

    // Các View
    private TextView  txtStatus,txtgiatri1,txtgiatri2,trangthaigas,trangthailua;
    private Button khoa, vantay_them, vantay_xoaa, vantay_xoatheoid;

    // Singleton MQTT
    private MQTTManager mqttManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Khởi tạo View

        txtStatus = findViewById(R.id.txtStatus);
        txtgiatri1=findViewById(R.id.txtgiatri1);
        txtgiatri2=findViewById(R.id.txtgiatri2);
        trangthaigas=findViewById(R.id.txtTrangThaiGas);
        trangthailua=findViewById(R.id.txtTrangThaiLua);

        khoa = findViewById(R.id.khoa);
        vantay_them = findViewById(R.id.vantay_them);
        vantay_xoaa = findViewById(R.id.vantay_xoaa);
        vantay_xoatheoid = findViewById(R.id.vantay_xoaid);




        // Lấy instance Singleton MQTT
        MQTTManager mqttManager = MQTTManager.getInstance(this);

        // Gán listener để nhận text từ Singleton
        mqttManager.setStatusListener(text ->
                runOnUiThread(() -> txtStatus.setText(text))
        );
        mqttManager.setDataListener(text -> {
            try {
                JSONObject json = new JSONObject(text);

                final int gas = json.optInt("gas");
                final int flame = json.optInt("flame");



                // Cập nhật UI trên Luồng Chính
                runOnUiThread(() -> {
                    txtgiatri1.setText("Gas: " + gas);
                    txtgiatri2.setText("Flame: " + flame);
                    if (gas>3000)
                        trangthaigas.setText("CÓ KHÍ GAS RÒ RỈ");
                    else
                        trangthaigas.setText("AN TOÀN");

                    if(flame==0)
                        trangthailua.setText("CÓ LỬA");
                    else
                        trangthailua.setText("KHÔNG CÓ LỬA");

                });

            } catch (JSONException e) {
                e.printStackTrace();

            }
        });



        //  Gán hành động cho các nút
        khoa.setOnClickListener(v -> mqttManager.publishJSON("{\"khoa\":\"mo\"}"));
        //vantay_them.setOnClickListener(v -> mqttManager.publishJSON("{\"vantay\":\"1\"}"));
        vantay_them.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Nhapthongtin.class);
            startActivity(intent);
        } );
        vantay_xoaa.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa toàn bộ vân tay không?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Xác nhận", (dialog, which) -> {
                        // Thực hiện xóa toàn bộ vân tay
                        mqttManager.publishJSON("{\"xoavantayall\":\"ok\"}");

                    })
                    .setNegativeButton("Hủy", (dialog, which) -> {
                        // Không làm gì, chỉ đóng dialog
                        dialog.dismiss();
                    })
                    .setCancelable(false) // không cho đóng bằng nút Back
                    .show();
        });
        vantay_xoatheoid.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, xoaid.class);
            startActivity(intent);
        } );

    }

    // Không cần gọi disconnect nữa vì Singleton giữ kết nối
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Nếu muốn hủy MQTT khi app thoát hẳn, có thể gọi:
        mqttManager.disconnect();
    }
}
