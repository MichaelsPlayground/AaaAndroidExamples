package de.androidcrypto.aaaandroidexamples;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcV;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.payneteasy.tlv.BerTagFactory;
import com.payneteasy.tlv.BerTlv;
import com.payneteasy.tlv.BerTlvParser;
import com.payneteasy.tlv.BerTlvs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

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
    Tag nfcTag;

    final String TechNfcA = "android.nfc.tech.NfcA";
    final String TechNfcB = "android.nfc.tech.NfcB";
    final String TechNfcV = "android.nfc.tech.NfcV";
    final String TechNdef = "android.nfc.tech.Ndef";
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
//showAlertDialog();
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

        nfcTag = tag;
        //showNfcTechnologyChoice();

        // the next steps depend on the TechList found on the device
        for (int i = 0; i < techList.length; i++) {
            String tech = techList[i];
            writeToUiAppend(etLog, "");
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
                    readMifareUltralight(tag);
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
                    readIsoDep(tag);
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
            /*
            runOnUiThread(() -> {
                //etLog.setText("");
                etData.setText("");
            });

             */
            maxTransceiveLength = nfc.getMaxTransceiveLength();
            atqa = nfc.getAtqa();
            sak = nfc.getSak();
            timeout = nfc.getTimeout();
            StringBuilder sb = new StringBuilder();
            sb.append("TechParameter") . append("\n");
            sb.append("maxTransceiveLength: ") . append(String.valueOf(maxTransceiveLength)).append("\n");
            if (atqa != null) sb.append("atqa: ") . append(BinaryUtils.bytesToHex(atqa)).append("\n");
            sb.append("sak: ") . append(String.valueOf(sak)).append("\n");
            sb.append("timeout: ") . append(String.valueOf(timeout)).append("\n");
            writeToUiAppend(etLog, sb.toString());
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
                    writeToUiAppend(etData, "NfcA data for block 00");
                    writeToUiAppend(etData, responseString);

                    byte[] referenceBlock = readOneBlockNfcA(nfc, 0);
                    System.out.println("* i: " + 0 + " data length: " + referenceBlock.length + " data: " + BinaryUtils.bytesToHex(referenceBlock));
                    for (int i = 1; i < 500; i++) {
                        byte[] data = readOneBlockNfcA(nfc, (i * 4));
                        if (data != null) {
                            System.out.println("* i: " + i + " data length: " + data.length + " data: " + BinaryUtils.bytesToHex(data));
                        }
                    }

                    //res = readOneBlockNfcA(nfc, 1);
                    //res = readOneBlockNfcA(nfc, 2);

                    nfc.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error on connecting to tag: " + e.getMessage());
                //writeToUiToast("Error on connecting to tag: " + e.getMessage());
                writeToUiAppend(etLog, "can not read block 00 with NfcA technology");
                //throw new RuntimeException(e);
            }
            try {
                nfc.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /** read an NfcA tag
     * @param nfcA
     * @param blockNumber
     * @return 16 bytes of data = 4 blocks of 4 bytes each
     */
    private byte[] readOneBlockNfcA(NfcA nfcA, int blockNumber) {
        byte[] cmd = new byte[]{
                    (byte) 0x30, // READ
                (byte)((blockNumber) & 0x0ff)  // page address
            };

        byte[] RESPONSE_OK = new byte[]{
                (byte) 0x00
        };

        try {
            return nfcA.transceive(cmd);
            //System.out.println("*** nfcA blockNumber: " + blockNumber);
            //System.out.println("*** response length: " + response.length + " data: " + BinaryUtils.bytesToHex(response));

        } catch (IOException e) {
            //throw new RuntimeException(e);
            return null;
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
            StringBuilder sb = new StringBuilder();
            sb.append("TechParameter") . append("\n");
            sb.append("dsfId: ") . append(String.valueOf(dsfId)).append("\n");
            sb.append("responseFlags: ") . append(String.valueOf(responseFlags)).append("\n");
            writeToUiAppend(etLog, sb.toString());
            // try to connect to the tag
            try {
                nfc.connect();
                writeToUiAppend(etLog, "connected to the tag");
                // inventory
                byte[] UIDFrame = new byte[] { (byte) 0x26, (byte) 0x01, (byte) 0x00 };
                byte[] responseInventory = nfc.transceive(UIDFrame);
                String responseInventoryString = BinaryUtils.bytesToHex(responseInventory);
                writeToUiAppend(etLog, "responseInventory: " + responseInventoryString);
                System.out.println("responseInventory: " + responseInventoryString);

                byte[] GetSystemInfoFrame1bytesAddress = new byte[] { (byte) 0x02, (byte) 0x2B };
                byte[] responseGetSystemInfoFrame1bytesAddress = nfc.transceive(GetSystemInfoFrame1bytesAddress);
                String responseGetSystemInfoFrame1bytesAddressString = BinaryUtils.bytesToHex(responseGetSystemInfoFrame1bytesAddress);
                writeToUiAppend(etLog, "responseGetSystemInfoFrame1bytesAddress: " + responseGetSystemInfoFrame1bytesAddressString);

                writeToUiAppend(etLog, "Trying to read 52 blocks of data from the tag. This function may run on a special tag only !");
                writeToUiAppend(etLog, "tested with Tag EM4x3x Skipass Oetztal Gurgl");
                // read the tags 52 blocks separately
                int NUMBER_OF_BLOCKS = 52;
                int NUMBER_OF_BYTES_IN_BLOCK = 4;
                byte[] responseComplete = new byte[(NUMBER_OF_BLOCKS * NUMBER_OF_BYTES_IN_BLOCK)];
                for (int i = 0; i < NUMBER_OF_BLOCKS; i++) {
                    //byte[] responseBlock = readOneBlockMultiple(nfcV, tagId, i);
                    byte[] responseBlock = readOneBlockNfcV(nfc, tagId, i);
                    if (responseBlock != null) {
                        // copy the new bytes to responseComplete
                        System.arraycopy(responseBlock, 0, responseComplete, (i * NUMBER_OF_BYTES_IN_BLOCK), NUMBER_OF_BYTES_IN_BLOCK);
                        writeToUiAppend(etLog, "processing block: " + i);
                        //String dumpBlock = HexDumpOwn.prettyPrint(responseBlock, 16);
                        //writeToUiAppend(readResult, dumpBlock);
                    } else {
                        writeToUiAppend(etLog, "error on reading block " + i);
                    }
                }
                writeToUiAppend(etLog, "complete content");
                String dumpComplete = HexDumpUtil.prettyPrint(responseComplete, 0);
                writeToUiAppend(etData, dumpComplete);

            } catch (IOException e) {
                Log.e(TAG, "Error on connecting to tag: " + e.getMessage());
                //writeToUiToast("Error on connecting to tag: " + e.getMessage());
                writeToUiAppend(etLog, "can not connect or read block with NfcV technology");
            }

        }
    }

    private byte[] readOneBlockNfcV(NfcV nfcV, byte[] tagId, int blockNumber) {
        byte[] RESPONSE_OK = new byte[]{
                (byte) 0x00
        };
        byte[] cmd = new byte[] {
                /* FLAGS   */ (byte)0x20,
                /* COMMAND */ (byte)0x20, // command read single block
                /* UID     */ (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                /* OFFSET  */ (byte)0x00
        };
        System.arraycopy(tagId, 0, cmd, 2, 8); // copy tagId to UID
        cmd[10] = (byte)((blockNumber) & 0x0ff); // copy block number
        try {
            byte[] response = nfcV.transceive(cmd);
            byte[] responseByte = getResponseByte(response);
            if (Arrays.equals(responseByte, RESPONSE_OK)) {
                return trimFirstByte(response);
            } else {
                return null;
            }
        } catch (IOException e) {
            //throw new RuntimeException(e);
            return null;
        }
    }

    private byte[] getResponseByte(byte[] input) {
        return Arrays.copyOfRange(input, 0, 1);
    }

    private byte[] getResponseBytes(byte[] input) {
        return Arrays.copyOfRange(input, 0, 2);
    }

    private byte[] trimFirstByte(byte[] input) {
        return Arrays.copyOfRange(input, 1, (input.length));
    }

    private void clearAllData() {
        maxTransceiveLength = 0;
        atqa = null;
        sak = 0;
        dsfId = 0;
        responseFlags = 0;
    }


    private void readMifareUltralight(Tag tag) {
        Log.i(TAG, "read a tag with Mifare Ultralight technology");
        MifareUltralight nfc = null;
        nfc = MifareUltralight.get(tag);
        if (nfc != null) {
            maxTransceiveLength = nfc.getMaxTransceiveLength();
            int type = nfc.getType();
            StringBuilder sb = new StringBuilder();
            sb.append("TechParameter") . append("\n");
            sb.append("maxTransceiveLength: ") . append(String.valueOf(maxTransceiveLength)).append("\n");
            sb.append("type: ") . append(String.valueOf(type)).append("\n");
            writeToUiAppend(etLog, sb.toString());

            try {
                nfc.connect();
                int numberOfPages = 0; // number of pages, each 4 byte long
                int numberOfPagesRead = 0; // number of pages to read with readPages (=number of pages / 4) as it returns 16 byte
                if (type == MifareUltralight.TYPE_ULTRALIGHT) {
                    numberOfPages = 16; // 64 bytes complete memory, 48 bytes user memory
                    numberOfPagesRead = 4;
                    writeToUiAppend(etLog, "type of card: Mifare Ultralight (memory 64 bytes)");
                }
                if (type == MifareUltralight.TYPE_ULTRALIGHT_C) {
                    numberOfPages = 44; // 192 bytes complete memory, 144 bytes user accessible, 16 bytes password data (not readable)
                    numberOfPagesRead = 11;
                    writeToUiAppend(etLog, "type of card: Mifare Ultralight-C (memory 192 bytes)");
                }
                for (int i = 0; i < numberOfPagesRead; i++) {
                    // a readPages returns not 1 page but 4 pages = 16 bytes of data
                    byte[] pageData = nfc.readPages(i * 4);
                    writeToUiAppend(etData, "MiUl page " + i + " " + BinaryUtils.bytesToHex(pageData));
                }

            } catch (IOException e) {
                Log.e(TAG, "Error on connecting to tag: " + e.getMessage());
                writeToUiAppend(etLog, "can not read block 00 with NfcA technology");
                //throw new RuntimeException(e);
            }
            try {
                nfc.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * section for IsoDep
     */


    private void readIsoDep(Tag tag) {
        Log.i(TAG, "read a tag with IsoDep technology");
        IsoDep nfc = null;
        nfc = IsoDep.get(tag);
        if (nfc != null) {
            maxTransceiveLength = nfc.getMaxTransceiveLength();
            int timeout = nfc.getTimeout();
            byte[] historicalBytes = nfc.getHistoricalBytes(); // on tags based on NfcA it is filled, otherwise null
            byte[] hiLayerResponse = nfc.getHiLayerResponse(); // on tags based on NfcB it is filled, otherwise null
            boolean extendedLengthApduSupport = nfc.isExtendedLengthApduSupported();
            StringBuilder sb = new StringBuilder();
            sb.append("TechParameter") . append("\n");
            sb.append("maxTransceiveLength: ") . append(String.valueOf(maxTransceiveLength)).append("\n");
            sb.append("timeout: ") . append(String.valueOf(timeout)).append("\n");
            if (historicalBytes != null) sb.append("historicalBytes: ") . append(BinaryUtils.bytesToHex(historicalBytes)).append("\n");
            if (hiLayerResponse != null) sb.append("hiLayerResponse: ") . append(BinaryUtils.bytesToHex(hiLayerResponse)).append("\n");
            sb.append("extendedLengthApduSupport: ") . append(String.valueOf(extendedLengthApduSupport)).append("\n");
            writeToUiAppend(etLog, sb.toString());

            try {
                nfc.connect();
                writeToUiAppend(etLog, "try to read a payment card with PSE and PPSE");
                byte[] PSE = "1PAY.SYS.DDF01".getBytes(StandardCharsets.UTF_8); // PSE
                byte[] PPSE = "2PAY.SYS.DDF01".getBytes(StandardCharsets.UTF_8); // PPSE
                byte[] RESULT_FAILUE = BinaryUtils.hexToBytes("6A82");
                byte[] command = selectApduIsoDep(PSE);
                byte[] responsePse = nfc.transceive(command);
                System.out.println("* selectPse response: " + BinaryUtils.bytesToHex(responsePse));
                if (responsePse == null) {
                    writeToUiAppend(etLog, "selectApdu with PSE fails (null)");
                } else {
                    if (Arrays.equals(responsePse, RESULT_FAILUE)) {
                        writeToUiAppend(etLog, "selectApdu with PSE fails (not allowed)");
                    } else {
                        writeToUiAppend(etLog, "responsePse length: " + responsePse.length + " data: " + BinaryUtils.bytesToHex(responsePse));
                        //System.out.println("pse: " + bytesToHex(responsePse));
                    }
                }
                command = selectApduIsoDep(PPSE);
                byte[] responsePpse = nfc.transceive(command);
                System.out.println("* selectPpse response: " + BinaryUtils.bytesToHex(responsePpse));
                if (responsePpse == null) {
                    writeToUiAppend(etLog, "selectApdu with PPSE fails (null)");
                } else {
                    if (Arrays.equals(responsePpse, RESULT_FAILUE)) {
                        writeToUiAppend(etLog, "selectApdu with PPSE fails (not allowed)");
                    } else {
                        writeToUiAppend(etLog, "responsePpse length: " + responsePpse.length + " data: " + BinaryUtils.bytesToHex(responsePpse));
                        //System.out.println("pse: " + bytesToHex(responsePse));
                    }
                }

                if (responsePpse != null) {
                    //final BerTlvLoggerSlf4j LOG = new BerTlvLoggerSlf4j();
                    BerTlvParser parser = new BerTlvParser();
                    BerTlvs tlvs = parser.parse(responsePpse, 0, responsePpse.length);
                    List<BerTlv> tlv = tlvs.getList();
                    for (int i = 0; i < tlv.size(); i++) {
                        System.out.println("ppse i " + i + " " + tlv.get(i).toString());
                    }
                }






                writeToUiAppend(etLog, "try to read a nPA (national ID-card Germany)");
                // https://github.com/PersoApp/import/blob/1e255d54cf2260e39c2dd911079da5fd0b35c980/PersoApp-Core/src/de/persoapp/core/card/ICardHandler.java

                /**
	            * application identifier for BSI TR-03110 eID application, oid =
	            * 0.4.0.127.0.7.3.2
	            */
                final String	AID_NPA		= "E80704007F00070302";

                /**
                 * application identifier for ICAO 9303 MRTD application
                 */
                final String	AID_ICAO	= "A0000002471001";

                /**
                 * application identifier for CEN 14890 DF.eSign
                 */
                final String	AID_eSign	= "A000000167455349474E";

                //byte[] npaAid = BinaryUtils.hexToBytes("6F048400A500"); // default AID
                //byte[] npaAid = BinaryUtils.hexToBytes("6F088404524F4F54A500"); // masterfile AID
                //byte[] npaAid = BinaryUtils.hexToBytes(AID_NPA);
                byte[] npaAid = BinaryUtils.hexToBytes(AID_ICAO);
                command = selectApduIsoDep(npaAid);
                byte[] responseNpaAid = nfc.transceive(command);
                writeToUiAppend(etLog, "responseNpaAid: " + BinaryUtils.bytesToHex(responseNpaAid));
                if (responseNpaAid == null) {
                    writeToUiAppend(etLog, "selectApdu with npaAid fails (null)");
                } else {
                    if (Arrays.equals(responseNpaAid, RESULT_FAILUE)) {
                        writeToUiAppend(etLog, "selectApdu with npaAid fails (not allowed)");
                    } else {
                        writeToUiAppend(etLog, "responseNpaAid length: " + responseNpaAid.length + " data: " + BinaryUtils.bytesToHex(responseNpaAid));
                        System.out.println("used AID: " + AID_NPA);
                        System.out.println("responseNpaAid length: " + responseNpaAid.length + " data: " + BinaryUtils.bytesToHex(responseNpaAid));
                        // manual parsing: https://github.com/evsinev/ber-tlv
                        // https://emvlab.org/tlvutils/
                        // using E80704007F00070302
                        // 6f0d8409e80704007f00070302a5009000
                        /*
                            6F File Control Information (FCI) Template
 	                            84 Dedicated File (DF) Name
 	 	                            E80704007F00070302
 	                            A5 File Control Information (FCI) Proprietary Template
                            90 Issuer Public Key Certificate
                         */
                        // using A0000002471001
                        // 6f0b8407a0000002471001a5009000
                        /*
                        6F File Control Information (FCI) Template
 	                        84 Dedicated File (DF) Name
 	 	                    A0000002471001
 	                    A5 File Control Information (FCI) Proprietary Template
                        90 Issuer Public Key Certificate
                         */
                    }
                }

                String dedicatedFilename = "E80704007F00070302"; // Dedicated File (DF) Name


            } catch (IOException e) {
                Log.e(TAG, "IsoDep Error on connecting to card: " + e.getMessage());
                //throw new RuntimeException(e);
            }
            try {
                nfc.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    // https://stackoverflow.com/a/51338700/8166854
    private byte[] selectApduIsoDep(byte[] aid) {
        byte[] commandApdu = new byte[6 + aid.length];
        commandApdu[0] = (byte) 0x00;  // CLA
        commandApdu[1] = (byte) 0xA4;  // INS
        commandApdu[2] = (byte) 0x04;  // P1
        commandApdu[3] = (byte) 0x00;  // P2
        commandApdu[4] = (byte) (aid.length & 0x0FF);       // Lc
        System.arraycopy(aid, 0, commandApdu, 5, aid.length);
        commandApdu[commandApdu.length - 1] = (byte) 0x00;  // Le
        return commandApdu;
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
            for (int sector = 0; sector < (sectorCount); ++sector) {
                boolean authenticated = authenticateSectorMifareClassic(nfc, sector);
                if (authenticated) {
                    String result = readBlockDataMifareClassic(nfc, sector);
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
    private boolean authenticateSectorMifareClassic(MifareClassic mfc, int sector) {

        boolean authenticated = false;
        Log.i(TAG, "Authenticating Sector: " + sector + " It contains Blocks: " + mfc.getBlockCountInSector(sector));

        //https://developer.android.com/reference/android/nfc/tech/MifareClassic.html#authenticateSectorWithKeyA(int,%20byte[])
        try {
            if (mfc.authenticateSectorWithKeyA(sector, MifareClassic.KEY_DEFAULT)) {
                authenticated = true;
                Log.w(TAG, "Authenticated!!! ");
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
    private String readBlockDataMifareClassic(MifareClassic mfc, int sector) {
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
            if (TextUtils.isEmpty(textView.getText().toString())) {
                textView.setText(message);
            } else {
                String newString = textView.getText().toString() + "\n" + message;
                textView.setText(newString);
            }
        });
    }

    private void writeToUiAppendReverse(TextView textView, String message) {
        runOnUiThread(() -> {
            if (TextUtils.isEmpty(textView.getText().toString())) {
                textView.setText(message);
            } else {
                String newString = message + "\n" + textView.getText().toString();
                textView.setText(newString);
            }
        });
    }

    private void writeToUiToast(String message) {
        runOnUiThread(() -> {
            Toast.makeText(getApplicationContext(),
                    message,
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void showNfcTechnologyChoice() {
        runOnUiThread(() -> {
            showAlertDialog();
        });
    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(NfcActivity.this);
        alertDialog.setTitle("AlertDialog");
        String[] items = {"NfcA","NfcV","Data Structures","HTML","CSS"};
        int checkedItem = 1;
        alertDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Toast.makeText(NfcActivity.this, "Clicked on NfcA", Toast.LENGTH_LONG).show();
                        readNfcA(nfcTag);
                        dialog.dismiss();
                        break;
                    case 1:
                        Toast.makeText(NfcActivity.this, "Clicked on android", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        break;
                    case 2:
                        Toast.makeText(NfcActivity.this, "Clicked on Data Structures", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        break;
                    case 3:
                        Toast.makeText(NfcActivity.this, "Clicked on HTML", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        break;
                    case 4:
                        Toast.makeText(NfcActivity.this, "Clicked on CSS", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        break;
                }
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

}