package com.example.app_doan;

import android.content.Context;
import android.util.Log;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;

/**
 * MQTTManager - Singleton quản lý MQTT và gửi text về Activity
 *
 * - Singleton: giữ 1 instance duy nhất trong toàn app, tránh mất kết nối khi chuyển Activity
 * - MQTT client: dùng HiveMQ MQTT 5 Async Client
 * - TextListener: gửi thông báo trạng thái hoặc dữ liệu về MainActivity
 */
public class MQTTManager {

    // -------------------------------
    // 1. Singleton instance
    // -------------------------------
    private static MQTTManager instance;       // instance duy nhất
    private Mqtt5AsyncClient client;           // MQTT client
    private final String TAG = "MQTTManager";  // Tag log

    // -------------------------------
    // 2. Thông tin broker MQTT
    // -------------------------------
    private final String host = "5f14affda214460285ae6e28e8a5727b.s1.eu.hivemq.cloud"; // MQTT broker host
    private final int port = 8883;            // Port (SSL)
    private final String clientId = "AndroidClient"; // Client ID
    private final String username = "Duong";  // Username broker
    private final String password = "Duong123"; // Password broker
    private final String subTopic = "esp32/sensor"; // Topic subscribe
    private final String pubTopic = "esp32/led";    // Topic publish

    // -------------------------------
    // 3. Listener gửi status về Activity
    // -------------------------------
    public interface DataListener {
        void onDataReceived(String data);
    }

    private DataListener dataListener;

    public void setDataListener(DataListener listener) {
        this.dataListener = listener;
    }
    // -------------------------------
    public interface StatusListener {
        void onStatusReceived(String status); // callback gửi status về Activity
    }

    private StatusListener statusListener; // Biến lưu listener hiện tại

    // Gán listener từ Activity
    public void setStatusListener(StatusListener listener) {
        this.statusListener = listener;
    }

    // -------------------------------
    // 4. Private constructor (Singleton)
    // -------------------------------
    private MQTTManager(Context context) {
        // Tạo MQTT client
        client = MqttClient.builder()
                .useMqttVersion5()       // Dùng MQTT 5
                .identifier(clientId)    // Client ID
                .serverHost(host)        // Địa chỉ broker
                .serverPort(port)        // Port
                .sslWithDefaultConfig()  // Dùng SSL mặc định
                .automaticReconnectWithDefaultConfig()
                .addDisconnectedListener(context1 -> {
                    Log.e("MQTT", "❌ MQTT Disconnected");

                    if (statusListener != null)
                        statusListener.onStatusReceived("MQTT disconnected - reconnecting...");

                    connect(); // ép reconnect (backup)
                })// AUTO reconnect
                .buildAsync();           // Xây dựng async client



        connect(); // Kết nối MQTT ngay sau khi tạo client
    }

    // -------------------------------
    // 5. Lấy instance Singleton
    // -------------------------------
    public static MQTTManager getInstance(Context context) {
        if (instance == null) {
            // Nếu chưa có instance, tạo mới
            instance = new MQTTManager(context.getApplicationContext());
        }
        return instance;
    }

    // -------------------------------
    // 6. Kết nối MQTT
    // -------------------------------
    //  CHỈ CONNECT KHI ĐƯỢC GỌI
    public void connect() {
        client.connectWith()
                .simpleAuth()
                .username(username)
                .password(password.getBytes())
                .applySimpleAuth()
                .keepAlive(30)   // ping mỗi 30 giây
                .send()
                .thenAccept(connAck -> {
                    Log.d(TAG, "MQTT connected");
                    if (statusListener != null)
                        statusListener.onStatusReceived("MQTT connected");
                    subscribe();
                })
                .exceptionally(ex -> {
                    Log.e(TAG, ex.getMessage());
                    if (statusListener != null)
                        statusListener.onStatusReceived(ex.getMessage());                    return null;
                });
    }

    // -------------------------------
    // 7. Subscribe topic
    // -------------------------------
    private void subscribe() {
        client.subscribeWith()
                .topicFilter(subTopic)          // Topic muốn subscribe
                .qos(MqttQos.AT_LEAST_ONCE)    // QoS 1
                .callback(msg -> {              // Khi nhận message
                    String message = new String(msg.getPayloadAsBytes()); // Lấy payload
                    Log.d(TAG,  message);
                    // Gửi text về MainActivity nếu listener đã gán
                    if (dataListener != null)
                        dataListener.onDataReceived(message);
                })
                .send(); // Gửi yêu cầu subscribe
    }

    // -------------------------------
    // 8. Publish JSON dữ liệu
    // -------------------------------
    public void publishJSON(String json) {
        client.publishWith()
                .topic(pubTopic)               // Topic publish
                .payload(json.getBytes())      // Nội dung
                .qos(MqttQos.AT_LEAST_ONCE)   // QoS 1
                .send();                       // Gửi dữ liệu
    }

    // -------------------------------
    // 9. Ngắt kết nối MQTT
    // -------------------------------
    public void disconnect() {
        if (client != null) {
            client.disconnect();            // Ngắt kết nối
        }
    }
}
