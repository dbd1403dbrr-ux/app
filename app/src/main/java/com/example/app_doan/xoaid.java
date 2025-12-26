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

public class xoaid extends AppCompatActivity {
    EditText idxoavtay;
    Button nut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_xoaid);
        idxoavtay=findViewById(R.id.id_xoavantay);
        MQTTManager mqttManager = MQTTManager.getInstance(this);
        nut=findViewById(R.id.bnt_xoavantayd);
        nut.setOnClickListener(v -> {
            String layid;
            layid=idxoavtay.getText().toString().trim();


            if(layid.isEmpty()  )
            {
                Toast.makeText(this, "Khong dc de trong thong tin", Toast.LENGTH_SHORT).show();
                return;
            }
            int idxoa;
            idxoa=Integer.parseInt(layid);


            if ( idxoa>99 || idxoa<1) {
                Toast.makeText(this, "Khong hop le", Toast.LENGTH_SHORT).show();
                idxoavtay.setText("");
                return;

            }
            mqttManager.publishJSON("{\"xoaid\":\"" + idxoa +"\"}");


            idxoavtay.setText("");

            Toast.makeText(this, "Da gui du lieu", Toast.LENGTH_SHORT).show();
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main2), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}