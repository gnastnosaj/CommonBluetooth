package io.jasontsang.commonbluetooth.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import io.jasontsang.commonbluetooth.CommonBluetooth;
import io.jasontsang.commonbluetooth.Data;
import io.jasontsang.commonbluetooth.Device;
import io.jasontsang.commonbluetooth.event.CommonBluetoothSubscription;
import io.jasontsang.commonbluetooth.event.DataEvent;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jason on 12/4/2015.
 */
public class DemoActivity extends AppCompatActivity {

    public final static String EXTRA_DEVICE = "device";
    public final static String EXTRA_DATATYPE = "dataType";
    public final static String EXTRA_SERVICE_UUID = "serviceUUID";
    public final static String EXTRA_CHAR_UUID_WRITE = "charUUIDWrite";
    public final static String EXTRA_CHAR_UUID_READ = "charUUIDRead";
    public final static String EXTRA_DES_UUID = "desUUID";

    private Device device;
    private int dataType;
    private String serviceUUID;
    private String charUUIDWrite;
    private String charUUIDRead;
    private String desUUID;

    private EditText send;
    private Button reconnect;
    private Button writeData;
    private TextView receive;

    private List<String> commands = new ArrayList<>();
    private List cache = new ArrayList<>();

    private CommonBluetoothSubscription dataSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        send = (EditText) findViewById(R.id.send);
        writeData = (Button) findViewById(R.id.write);
        receive = (TextView) findViewById(R.id.receive);
        writeData.setOnClickListener(view -> {
            receive.setText("");
            commands.clear();
            commands.addAll(Arrays.asList(send.getText().toString().split("\n")));
            cache.clear();
            if(commands.size() > 0) {
                Data data = new Data();
                data.setDevice(device);
                data.setData(HexUtils.hexStringToBytes(commands.remove(0)));
                Bundle bundle = new Bundle();
                bundle.putInt(Data.EXTRA_DATATYPE, Data.CHAR_DATA);
                bundle.putString(Data.EXTRA_SERVICE_UUID, serviceUUID);
                bundle.putString(Data.EXTRA_CHAR_UUID, charUUIDWrite);
                bundle.putString(Data.EXTRA_DES_UUID, desUUID);
                data.setBundle(bundle);
                CommonBluetooth.send(data);
            }
        });
        Intent intent = getIntent();
        device = intent.getParcelableExtra(EXTRA_DEVICE);
        dataType = intent.getIntExtra(EXTRA_DATATYPE, 0);
        serviceUUID = intent.getStringExtra(EXTRA_SERVICE_UUID);
        charUUIDWrite = intent.getStringExtra(EXTRA_CHAR_UUID_WRITE);
        charUUIDRead = intent.getStringExtra(EXTRA_CHAR_UUID_READ);
        desUUID = intent.getStringExtra(EXTRA_DES_UUID);
    }

    @Override
    protected void onStart() {
        super.onStart();
        dataSubscription = CommonBluetooth.subscribeDataEvent(event -> {
            DataEvent dataEvent = (DataEvent) event;
            if (dataEvent.getType() == DataEvent.DATA_RECEIVED) {
                List tmp = new ArrayList();
                List bak = new ArrayList();
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    tmp.addAll(cache);
                    cache.clear();
                    byte[] bytes = dataEvent.getBundle().getByteArray(DataEvent.EXTRA_DATA);
                    for (int i = 0; i < bytes.length; i++) {
                        tmp.add(bytes[i]);
                    }
                    bak.addAll(tmp);
                    byte[] buf = new byte[1];
                    buf[0] = (byte) tmp.remove(0);
                    if (buf[0] != 0x02) {
                        throw new Exception("STX NOT 0x02");
                    }
                    baos.write(buf);

                    //dataLen
                    buf = new byte[2];
                    buf[0] = (byte) tmp.remove(0);
                    buf[1] = (byte) tmp.remove(0);
                    baos.write(buf);

                    int dataLen = HexUtils.byteToInt(buf) - 5;
                    //data
                    buf = new byte[dataLen];
                    for (int i = 0; i < dataLen; i++) {
                        buf[i] = (byte) tmp.remove(0);
                    }
                    baos.write(buf);

                    //end
                    buf = new byte[2];
                    buf[0] = (byte) tmp.remove(0);
                    buf[1] = (byte) tmp.remove(0);
                    baos.write(buf);

                    baos.close();
                    byte[] rebuf = baos.toByteArray();

                    //checkSum
                    int checkSum = rebuf[0];
                    for (int i = 1; i < rebuf.length - 2; i++) {
                        checkSum ^= rebuf[i];
                    }

                    //replace checkSum
                    rebuf[rebuf.length - 2] = (byte) (checkSum & 0xFF);

                    if (rebuf[rebuf.length - 1] != 0x03) {
                        throw new Exception("ETX NOT 0x03");
                    }
                    cache.addAll(tmp);
                    receive.setText(receive.getText() + HexUtils.bytes2HexString(rebuf) + "\n");
                    if (commands.size() > 0) {
                        Data data = new Data();
                        data.setData(HexUtils.hexStringToBytes(commands.remove(0)));
                        data.setDevice(device);
                        Bundle bundle = new Bundle();
                        bundle.putInt(Data.EXTRA_DATATYPE, Data.CHAR_DATA);
                        bundle.putString(Data.EXTRA_SERVICE_UUID, serviceUUID);
                        bundle.putString(Data.EXTRA_CHAR_UUID, charUUIDWrite);
                        bundle.putString(Data.EXTRA_DES_UUID, desUUID);
                        data.setBundle(bundle);
                        CommonBluetooth.send(data);
                    }
                } catch (Exception e) {
                    cache.addAll(bak);
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        dataSubscription.unsubscribe();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonBluetooth.disconnect();
    }
}
