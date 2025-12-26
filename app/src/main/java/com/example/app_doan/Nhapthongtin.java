package com.example.app_doan;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class Nhapthongtin extends AppCompatActivity {
    EditText id_vantay;
    Button bnt_vantay;
    String id_gui;
    int ktra;
    String ten;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nhapthongtin);
        id_vantay=findViewById(R.id.id_vantay);
        bnt_vantay=findViewById(R.id.bnt_themvantay);

        MQTTManager mqttManager = MQTTManager.getInstance(this);
        bnt_vantay.setOnClickListener(v ->
        {

            id_gui=id_vantay.getText().toString().trim();


            if(id_gui.isEmpty()  )
            {
                Toast.makeText(this, "Khong dc de trong thong tin", Toast.LENGTH_SHORT).show();
                return;
            }
            ktra=Integer.parseInt(id_vantay.getText().toString().trim());

            if ( ktra>99 || ktra<1 ) {
                Toast.makeText(this, "Khong hop le", Toast.LENGTH_SHORT).show();
                return;

            }
            mqttManager.publishJSON("{\"iddk\":\"" + id_gui + "\"}");

            id_vantay.setText("");
            Toast.makeText(this, "Da gui du lieu", Toast.LENGTH_SHORT).show();


        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}