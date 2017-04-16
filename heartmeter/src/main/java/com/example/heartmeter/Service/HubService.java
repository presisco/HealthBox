package com.example.heartmeter.Service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.heartmeter.Data.SensorData;
import com.presisco.shared.service.BaseBluetoothService;
import com.presisco.shared.service.BaseHubService;
import com.presisco.shared.utils.ByteUtils;
import com.presisco.shared.utils.LCAT;

import java.util.ArrayList;
import java.util.concurrent.Executors;

public class HubService extends BaseHubService implements BaseBluetoothService.PacketReceivedListener {

    public static final String KEY_INSTRUCTION = "INSTRUCTION";
    public static final String KEY_SAMPLE_RATE = "SAMPLE_RATE";
    public static final String KEY_FILTER = "FILTER";

    public static final String ACTION_HEARTRATE_REDUCED = "HEARTRATE_REDUCED";
    public static final String ACTION_HEARTRATE_VOLUME = "HEARTRATE_VOLUMN";
    public static final String ACTION_ECG = "ECG";
    public static final int TYPE_HEARTRATE = 0;
    public static final int TYPE_ECG = 1;
    public static final byte SAMPLE_RATE_100HZ = 0x01;
    public static final byte SAMPLE_RATE_250HZ = 0x02;
    public static final byte SAMPLE_RATE_500HZ = 0x03;
    public static final byte FILTER_OFF = 0x01;
    public static final byte FILTER_ON = 0x02;
    public static final byte SEND_START = (byte) 0x91;
    public static final byte CHANGE_SAMPLE_RATE = (byte) 0x87;
    public static final byte CHANGE_FILTER = (byte) 0x85;
    public static final byte SEND_STOP = (byte) 0x93;
    public static final byte UNKNOWN = 0x00;
    private static final int MAX_SAMPLE_RATE = 500;
    private static final int HEAD_DATA = 0xA0;
    private static final int HEAD_RESPONSE = 0xC0;

    int mInstructionIdCounter = 1;
    int mSampleRate = 100;
    BTServiceConnection mConnection = new BTServiceConnection();
    BaseBluetoothService mBTService;
    private SensorData[] raw_data = new SensorData[MAX_SAMPLE_RATE];
    private int[] raw_heartrate = new int[MAX_SAMPLE_RATE];
    private int[] raw_ecg = new int[MAX_SAMPLE_RATE];
    private int data_packet_counter = 0;

    public HubService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        //registerLocalReceiver(new BTServiceReceiver(), new IntentFilter(BaseBluetoothService.ACTION_TARGET_DATA_RECEIVED));
        registerLocalReceiver(new HubHostReceiver(), new IntentFilter(ACTION_SEND_INSTRUCTION));
        bindService(new Intent(this, BTService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        unbindService(mConnection);
        super.onDestroy();
    }

    /**
     * 生成设备控制指令
     *
     * @param intent 指令数据
     */
    private void sendInstruction(Intent intent) {
        byte[] instruction = new byte[7];
        instruction[0] = (byte) (0x80 | mInstructionIdCounter);
        instruction[1] = 0x01;
        instruction[2] = 0x00;
        instruction[3] = 0x02;
        instruction[4] = intent.getByteExtra(KEY_INSTRUCTION, UNKNOWN);
        switch (instruction[4]) {
            case SEND_START:
            case SEND_STOP:
                instruction[5] = 0x00;
                break;
            case CHANGE_SAMPLE_RATE:
                instruction[5] = intent.getByteExtra(KEY_SAMPLE_RATE, SAMPLE_RATE_100HZ);
                switch (intent.getByteExtra(KEY_SAMPLE_RATE, SAMPLE_RATE_100HZ)) {
                    case SAMPLE_RATE_100HZ:
                        mSampleRate = 100;
                        break;
                    case SAMPLE_RATE_250HZ:
                        mSampleRate = 250;
                        break;
                    case SAMPLE_RATE_500HZ:
                        mSampleRate = 500;
                        break;
                }
                break;
            case CHANGE_FILTER:
                instruction[5] = intent.getByteExtra(KEY_FILTER, FILTER_ON);
                break;
            default:
                return;
        }
        instruction[6] = ByteUtils.getXOR(instruction, 6);

        sendInstruction(instruction);

        if (mInstructionIdCounter == 0x0F) {
            mInstructionIdCounter = 0;
        } else {
            mInstructionIdCounter++;
        }
    }

    private boolean isValidData(SensorData data) {
        return data.status == SensorData.STATUS_NORMAL;
    }

    private boolean isValidHeartrate(SensorData data) {
        if (data.heart_rate < 20) {
            return false;
        }
        return data.heart_rate <= 180;
    }

    private int reduceHeartrate(ArrayList<Integer> data) {
        int sum = 0;
        for (Integer point : data) {
            sum += point;
        }
        if (data.size() == 0) {
            return -1;
        } else {
            return sum / data.size();
        }
    }

    @Override
    public void onReceived(byte[] packet) {
        if (ByteUtils.byteHighMatch(packet[0], HEAD_RESPONSE)) {
            LCAT.d(this, "received response packet: " + ByteUtils.bytes2hex(packet));
        } else {
            if (data_packet_counter >= mSampleRate) {
                broadcast(ACTION_HEARTRATE_VOLUME, raw_heartrate);
                broadcast(ACTION_ECG, raw_ecg);
                broadcast(ACTION_HEARTRATE_REDUCED, raw_data[0].heart_rate);

                data_packet_counter = 0;
            }
            SensorData data = SensorData.parseDataPacket(packet);

            raw_data[data_packet_counter] = data;
            raw_heartrate[data_packet_counter] = data.heart_rate;
            raw_ecg[data_packet_counter] = data.ecg;
            data_packet_counter++;
        }
    }

    private class HubHostReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LCAT.d(this, "broadcast received: " + intent.getAction());
            if (intent.getAction() == ACTION_SEND_INSTRUCTION) {
                if (intent.getByteExtra(KEY_INSTRUCTION, UNKNOWN) == SEND_START) {
                    new StartMeasureTask().executeOnExecutor(Executors.newSingleThreadExecutor());
                } else {
                    sendInstruction(intent);
                }
            }
        }
    }

    private class StartMeasureTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                sendInstruction(new Intent()
                        .putExtra(KEY_INSTRUCTION, CHANGE_SAMPLE_RATE)
                        .putExtra(KEY_SAMPLE_RATE, SAMPLE_RATE_100HZ));
                Thread.sleep(500);
                sendInstruction(new Intent()
                        .putExtra(KEY_INSTRUCTION, CHANGE_FILTER)
                        .putExtra(KEY_FILTER, FILTER_ON));
                Thread.sleep(500);
                sendInstruction(new Intent()
                        .putExtra(KEY_INSTRUCTION, SEND_START));
                data_packet_counter = 0;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class BTServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBTService = ((BaseBluetoothService.BaseBTServiceBinder) service).getService();
            mBTService.setPacketReceivedListener(HubService.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBTService = null;
        }
    }

}
