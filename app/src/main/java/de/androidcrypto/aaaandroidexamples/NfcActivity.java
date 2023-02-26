package de.androidcrypto.aaaandroidexamples;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcV;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class NfcActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {

    private final String TAG = "NfcAct";

    Button btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9;
    TextView tv1;
    com.google.android.material.textfield.TextInputEditText etData, etLog;

    private NfcAdapter mNfcAdapter;
    private byte[] tagId;
    private int maxTransceiveLength = 0; // NfcA, NfcV
    byte[] atqa; // NfcA
    short sak; // NfcA
    int timeout;
    byte dsfId; // NfcV
    byte responseFlags; // NfcV

    final String TechNfcA = "android.nfc.tech.NfcA";
    final String TechNfcB = "android.nfc.tech.NfcB";
    final String TechNfcV = "android.nfc.tech.NfcV";
    final String TechNdef = "android.nfc.tech.NdefFormatable";
    final String TechMifareUltralight = "android.nfc.tech.MifareUltralight";
    final String TechMifareClassic = "android.nfc.tech.MifareClassic";
    final String TechIsoDep = "android.nfc.tech.IsoDep";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);

        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);
        btn4 = findViewById(R.id.btn4);
        btn5 = findViewById(R.id.btn5);
        btn6 = findViewById(R.id.btn6);
        btn7 = findViewById(R.id.btn7);
        btn8 = findViewById(R.id.btn8);
        btn9 = findViewById(R.id.btn9);
        tv1 = findViewById(R.id.tv1);
        etData = findViewById(R.id.etData);
        //etFilename = findViewById(R.id.etFilename);
        //etSubfolder = findViewById(R.id.etSubfolder);
        etLog = findViewById(R.id.etLog);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn1 back to main menu");
                Intent intent = new Intent(NfcActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * section for NFC
     */

    /**
     * This method is run in another thread when a card is discovered
     * This method cannot cannot direct interact with the UI Thread
     * Use `runOnUiThread` method to change the UI from this method
     * @param tag discovered tag
     */
    @Override
    public void onTagDiscovered(Tag tag) {
        runOnUiThread(() -> {
            etLog.setText("");
            etData.setText("");
        });
        // clear all data
        clearAllData();

        writeToUiAppend(etLog, "NFC tag discovered");
        // Make a Sound
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(150, 10));
        } else {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(200);
        }
        tagId = tag.getId();
        writeToUiAppend(etLog, "TagId: " + BinaryUtils.bytesToHex(tagId));
        String[] techList = tag.getTechList();
        writeToUiAppend(etLog, "TechList found with these entries:");
        for (int i = 0; i < techList.length; i++) {
            writeToUiAppend(etLog, techList[i]);
            System.out.println("TechList: " + techList[i]);
        }
        // the next steps depend on the TechList found on the device
        for (int i = 0; i < techList.length; i++) {
            String tech = techList[i];
            switch (tech) {
                case TechNfcA: {
                        writeToUiAppend(etLog, "*** Tech ***");
                        writeToUiAppend(etLog, "Technology NfcA");
                        readNfcA(tag);
                    break;
                }
                case TechNfcB: {
                    writeToUiAppend(etLog, "*** Tech ***");
                    writeToUiAppend(etLog, "Technology NfcB");
                    break;
                }
                case TechNfcV: {
                    writeToUiAppend(etLog, "*** Tech ***");
                    writeToUiAppend(etLog, "Technology NfcV");
                    readNfcV(tag);
                    break;
                }
                case TechMifareUltralight: {
                    writeToUiAppend(etLog, "*** Tech ***");
                    writeToUiAppend(etLog, "Technology Mifare Ultralight");
                    break;
                }
                case TechMifareClassic: {
                    writeToUiAppend(etLog, "*** Tech ***");
                    writeToUiAppend(etLog, "Technology Mifare Classic");
                    readMifareClassic(tag);
                    break;
                }
                case TechNdef: {
                    writeToUiAppend(etLog, "*** Tech ***");
                    writeToUiAppend(etLog, "Technology NdefFormatable");
                    break;
                }
                case TechIsoDep: {
                    writeToUiAppend(etLog, "*** Tech ***");
                    writeToUiAppend(etLog, "Technology IsoDep");
                    break;
                }
                default: {
                    writeToUiAppend(etLog, "*** Tech ***");
                    writeToUiAppend(etLog, "unknown tech: " + tech);
                    break;
                }
            }
        }

    }

    private void readNfcA(Tag tag) {
        Log.i(TAG, "read a tag with NfcA technology");
        NfcA nfc = null;
        nfc = NfcA.get(tag);
        if (nfc != null) {
            maxTransceiveLength = nfc.getMaxTransceiveLength();
            atqa = nfc.getAtqa();
            sak = nfc.getSak();
            timeout = nfc.getTimeout();
            writeTechParameter();
            // try to connect to the tag
            try {
                nfc.connect();
                if (nfc.isConnected()) {
                    writeToUiAppend(etLog, "connected to tag");
                    // read data from page 00 (should return 4 bytes)
                    byte[] response;
                    response = nfc.transceive(new byte[]{
                            (byte) 0x30, // READ
                            (byte) 0x00  // page address
                    });
                    String responseString = BinaryUtils.bytesToHex(response);
                    writeToUiAppend(etData, "data for block 00");
                    writeToUiAppend(etData, responseString);
                    nfc.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error on connecting to tag: " + e.getMessage());
                //writeToUiToast("Error on connecting to tag: " + e.getMessage());
                writeToUiAppend(etLog, "can not read block 00 with NfcA technology");
                //throw new RuntimeException(e);
                return;
            }
            try {
                nfc.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private void readNfcV(Tag tag) {
        Log.i(TAG, "read a tag with NfcV technology");
        NfcV nfc = null;
        nfc = NfcV.get(tag);
        if (nfc != null) {
            maxTransceiveLength = nfc.getMaxTransceiveLength();
            dsfId = nfc.getDsfId();
            responseFlags = nfc.getResponseFlags();
            writeTechParameter();
        }
    }
    private void writeTechParameter() {
        // todo print only parameters available for the technology
        StringBuilder sb = new StringBuilder();
        sb.append("TechParameter") . append("\n");
        sb.append("maxTransceiveLength: ") . append(String.valueOf(maxTransceiveLength)).append("\n");
        if (atqa != null) sb.append("atqa: ") . append(BinaryUtils.bytesToHex(atqa)).append("\n");
        sb.append("sak: ") . append(String.valueOf(sak)).append("\n");
        sb.append("dsfId: ") . append(String.valueOf(dsfId)).append("\n");
        sb.append("responseFlags: ") . append(String.valueOf(responseFlags)).append("\n");
        writeToUiAppend(etLog, sb.toString());
    }

    private void clearAllData() {
        maxTransceiveLength = 0;
        atqa = null;
        sak = 0;
        dsfId = 0;
        responseFlags = 0;
    }

    /**
     * section for MifareClassic
     */

    private void readMifareClassic(Tag tag) {
        Log.i(TAG, "read a tag with Mifare Classic technology");
        MifareClassic nfc = null;
        nfc = MifareClassic.get(tag);
        if (nfc != null) {
            maxTransceiveLength = nfc.getMaxTransceiveLength();
            int blockCount = nfc.getBlockCount();
            int sectorCount = nfc.getSectorCount();
            int type = nfc.getType();
            int size = nfc.getSize();
            StringBuilder sb = new StringBuilder();
            sb.append("TechParameter") . append("\n");
            sb.append("type: ") . append(String.valueOf(type)).append("\n");
            sb.append("size: ") . append(String.valueOf(size)).append("\n");
            sb.append("blockCount: ") . append(String.valueOf(blockCount)).append("\n");
            sb.append("sectorCount: ") . append(String.valueOf(sectorCount)).append("\n");
            writeToUiAppend(etLog, sb.toString());

            try {
                nfc.connect();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Loop through all sectors
            for (int sector = 0; sector < (nfc.getSectorCount()); ++sector) {
                boolean authenticated = autheticateSector(nfc, sector);
                if (authenticated) {
                    String result = readBlockData(nfc, sector);
                    writeToUiAppend(etData, "sector " + String.valueOf(sector) + " " + result);
                } else {
                    writeToUiAppend(etData, "sector " + String.valueOf(sector) + " not authenticated");
                }
            }

        }
    }

    /**
     * Authenticate the sector with default key
     * OR the key that you used to write the card
     * (if you didn't then use the default one!)
     *
     * @param sector current sector
     * @return Boolean authenticated? true:false
     */
    private boolean autheticateSector(MifareClassic mfc, int sector) {

        boolean authenticated = false;
        Log.i(TAG, "Authenticating Sector: " + sector + " It contains Blocks: " + mfc.getBlockCountInSector(sector));

        //https://developer.android.com/reference/android/nfc/tech/MifareClassic.html#authenticateSectorWithKeyA(int,%20byte[])
        try {
            if (mfc.authenticateSectorWithKeyA(sector, MifareClassic.KEY_DEFAULT)) {
                authenticated = true;
                Log.w(TAG, "Authenticated!!! ");
                //rawData += ("Authenticated!!! \n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!authenticated) {
            Log.e(TAG, "authenticateSector: Authenticating with key B");
            try {
                if (mfc.authenticateSectorWithKeyB(sector, MifareClassic.KEY_DEFAULT)) {
                    authenticated = true;
                    Log.w(TAG, "Authenticated!!! ");
                    //rawData += ("Authenticated!!! \n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return authenticated;
    }

    /**
     * reads the binary data in blocks
     *
     * @param sector current sector
     * @return hex string of the read data
     */
    // source: https://github.com/tarun-nain/RFIDReader/blob/master/app/src/main/java/com/codes29/rfidreader/MainActivity.java
    private String readBlockData(MifareClassic mfc, int sector) {
        String blockvalues = "";
        // Read all blocks in sector
        for (int block = 0; (block < mfc.getBlockCountInSector(sector)); ++block) {
            // Get block number for sector + block
            int blockIndex = (mfc.sectorToBlock(sector) + block);
            try {
                // Create a string of bits from block data and fix endianness
                // http://en.wikipedia.org/wiki/Endianness

                if (sector <= 15 && block < 3) {
                    // Read block data from block index
                    byte[] data = mfc.readBlock(blockIndex);
                    if (!(sector == 0 && block == 0)) {
                        String temp = BinaryUtils.bytesToHex(data);
                        blockvalues += temp;
                        Log.i(TAG, "Block " + blockIndex + " : " + temp);
                        //rawData += ("Block " + blockIndex + " : " + temp + "\n");
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Exception occurred  " + e.getLocalizedMessage());
            }
        }
        return blockvalues.trim();
    }


    /**
     * section for activity workflow - important is the disabling of the ReaderMode when activity is pausing
     */

    private void showWirelessSettings() {
        Toast.makeText(this, "You need to enable NFC", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mNfcAdapter != null) {

            if (!mNfcAdapter.isEnabled())
                showWirelessSettings();

            Bundle options = new Bundle();
            // Work around for some broken Nfc firmware implementations that poll the card too fast
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250);

            // Enable ReaderMode for all types of card and disable platform sounds
            // the option NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK is NOT set
            // to get the data of the tag afer reading
            mNfcAdapter.enableReaderMode(this,
                    this,
                    NfcAdapter.FLAG_READER_NFC_A |
                            NfcAdapter.FLAG_READER_NFC_B |
                            NfcAdapter.FLAG_READER_NFC_F |
                            NfcAdapter.FLAG_READER_NFC_V |
                            NfcAdapter.FLAG_READER_NFC_BARCODE |
                            NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                    options);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null)
            mNfcAdapter.disableReaderMode(this);
    }

    /**
     * section for UI
     */

    private void writeToUiAppend(TextView textView, String message) {
        runOnUiThread(() -> {
            String newString = textView.getText().toString() + "\n" + message;
            textView.setText(newString);
        });
    }

    private void writeToUiAppendReverse(TextView textView, String message) {
        runOnUiThread(() -> {
            String newString = message + "\n" + textView.getText().toString();
            textView.setText(newString);
        });
    }

    private void writeToUiToast(String message) {
        runOnUiThread(() -> {
            Toast.makeText(getApplicationContext(),
                    message,
                    Toast.LENGTH_SHORT).show();
        });
    }


}