package de.androidcrypto.aaaandroidexamples;

import static de.androidcrypto.aaaandroidexamples.BinaryUtils.bytesToHex;
import static de.androidcrypto.aaaandroidexamples.BinaryUtils.hexToBytes;

import android.content.Context;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.payneteasy.tlv.BerTag;
import com.payneteasy.tlv.BerTlv;
import com.payneteasy.tlv.BerTlvParser;
import com.payneteasy.tlv.BerTlvs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import de.androidcrypto.aaaandroidexamples.nfccreditcards.TagValues;

public class NfcCreditCardActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {

    private final String TAG = "NfcCreditCardAct";

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
        setContentView(R.layout.activity_nfc_creditcard);

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
                Intent intent = new Intent(NfcCreditCardActivity.this, MainActivity.class);
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
        writeToUiAppend(etLog, "TagId: " + bytesToHex(tagId));
        String[] techList = tag.getTechList();
        writeToUiAppend(etLog, "TechList found with these entries:");
        for (int i = 0; i < techList.length; i++) {
            writeToUiAppend(etLog, techList[i]);
            System.out.println("TechList: " + techList[i]);
        }
        // the next steps depend on the TechList found on the device
        for (int i = 0; i < techList.length; i++) {
            String tech = techList[i];
            writeToUiAppend(etLog, "");
            switch (tech) {
                /*
                case TechNfcA: {
                        writeToUiAppend(etLog, "*** Tech ***");
                        writeToUiAppend(etLog, "Technology NfcA");
                        //readNfcA(tag);
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
                    //readNfcV(tag);
                    break;
                }
                case TechMifareUltralight: {
                    writeToUiAppend(etLog, "*** Tech ***");
                    writeToUiAppend(etLog, "Technology Mifare Ultralight");
                    //readMifareUltralight(tag);
                    break;
                }
                case TechMifareClassic: {
                    writeToUiAppend(etLog, "*** Tech ***");
                    writeToUiAppend(etLog, "Technology Mifare Classic");
                    //readMifareClassic(tag);
                    break;
                }
                case TechNdef: {
                    writeToUiAppend(etLog, "*** Tech ***");
                    writeToUiAppend(etLog, "Technology NdefFormatable");
                    break;
                }
                 */
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
            if (historicalBytes != null) sb.append("historicalBytes: ") . append(bytesToHex(historicalBytes)).append("\n");
            if (hiLayerResponse != null) sb.append("hiLayerResponse: ") . append(bytesToHex(hiLayerResponse)).append("\n");
            sb.append("extendedLengthApduSupport: ") . append(String.valueOf(extendedLengthApduSupport)).append("\n");
            writeToUiAppend(etLog, sb.toString());

            try {
                nfc.connect();
                writeToUiAppend(etLog, "try to read a payment card with PSE and PPSE");

                byte[] command;

                writeToUiAppend(etLog,"");
                writeToUiAppend(etLog, "01 select PSE");
                byte[] PSE = "1PAY.SYS.DDF01".getBytes(StandardCharsets.UTF_8); // PSE
                command = selectApdu(PSE);
                byte[] responsePse = nfc.transceive(command);
                writeToUiAppend(etLog, "01 select PSE response length " + responsePse.length + " data: " + bytesToHex(responsePse));
                boolean responsePseNotAllowed = responseNotAllowed(responsePse);
                if (responsePseNotAllowed) {
                    writeToUiAppend(etLog, "01 selecting PSE is not allowed on card");
                }

                writeToUiAppend(etLog,"");
                writeToUiAppend(etLog, "02 select PPSE");
                byte[] PPSE = "2PAY.SYS.DDF01".getBytes(StandardCharsets.UTF_8); // PPSE
                command = selectApdu(PPSE);
                byte[] responsePpse = nfc.transceive(command);
                writeToUiAppend(etLog, "02 select PPSE response length " + responsePpse.length + " data: " + bytesToHex(responsePpse));
                boolean responsePpseNotAllowed = responseNotAllowed(responsePpse);
                if (responsePpseNotAllowed) {
                    writeToUiAppend(etLog, "02 selecting PPSE is not allowed on card");
                }

                if (responsePseNotAllowed && responsePpseNotAllowed) {
                    writeToUiAppend(etLog,"");
                    writeToUiAppend(etLog,"The card is not a credit card, reading aborted");
                    try {
                        nfc.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return;
                }

                writeToUiAppend(etLog,"");
                writeToUiAppend(etLog, "04 analyze PPSE");
                TagValues tv = new TagValues();
                System.out.println("get tv: " + tv.getEmvTagList());

                byte[] responseOk = checkResponse(responsePpse);
                if (responseOk != null) {
                    System.out.println("# selectPpse response:" + bytesToHex(responseOk));
/*
MC AAB: response:6f3c840e325041592e5359532e4444463031a52abf0c2761254f07a000000004101050104465626974204d6173746572436172648701019f0a0400010101
6F File Control Information (FCI) Template
 	84 Dedicated File (DF) Name
 	 	325041592E5359532E4444463031
 	A5 File Control Information (FCI) Proprietary Template
 	 	BF0C File Control Information (FCI) Issuer Discretionary Data
 	 	 	61 Application Template
 	 	 	 	4F Application Identifier (AID) – card
 	 	 	 	 	A0000000041010
 	 	 	 	50 Application Label
 	 	 	 	 	D e b i t M a s t e r C a r d
 	 	 	 	87 Application Priority Indicator
 	 	 	 	 	01
 	 	 	 	9F0A Unknown tag
 	 	 	 	 	00010101
 */
/*
Lloyds # selectPpse response:6f2b840e325041592e5359532e4444463031a519bf0c1661144f07a00000000310109f0a080001050100000000
6F File Control Information (FCI) Template
 	84 Dedicated File (DF) Name
 	 	325041592E5359532E4444463031
 	A5 File Control Information (FCI) Proprietary Template
 	 	BF0C File Control Information (FCI) Issuer Discretionary Data
 	 	 	61 Application Template
 	 	 	 	4F Application Identifier (AID) – card
 	 	 	 	 	A0000000031010
 	 	 	 	9F0A Unknown tag
 	 	 	 	 	0001050100000000
 */
/*
Voba RF # selectPpse response:6f67840e325041592e5359532e4444463031a555bf0c5261194f09a000000059454301008701019f0a080001050100000000611a4f0aa00000035910100280018701019f0a08000105010000000061194f09d276000025474101008701019f0a080001050100000000
6F File Control Information (FCI) Template
 	84 Dedicated File (DF) Name
 	 	325041592E5359532E4444463031
 	A5 File Control Information (FCI) Proprietary Template
 	 	BF0C File Control Information (FCI) Issuer Discretionary Data
 	 	 	61 Application Template
 	 	 	 	4F Application Identifier (AID) – card
 	 	 	 	 	A00000005945430100
 	 	 	 	87 Application Priority Indicator
 	 	 	 	 	01
 	 	 	 	9F0A Unknown tag
 	 	 	 	 	0001050100000000
 	 	 	61 Application Template
 	 	 	 	4F Application Identifier (AID) – card
 	 	 	 	 	A0000003591010028001
 	 	 	 	87 Application Priority Indicator
 	 	 	 	 	01
 	 	 	 	9F0A Unknown tag
 	 	 	 	 	0001050100000000
 	 	 	61 Application Template
 	 	 	 	4F Application Identifier (AID) – card
 	 	 	 	 	D27600002547410100
 	 	 	 	87 Application Priority Indicator
 	 	 	 	 	01
 	 	 	 	9F0A Unknown tag
 	 	 	 	 	0001050100000000
 */

                    BerTlvParser parser = new BerTlvParser();
                    BerTlvs tlvs = parser.parse(responseOk);
                    List<BerTlv> tlvList = tlvs.getList();
                    int tlvListLength = tlvList.size();
                    writeToUiAppend(etLog, "tlvListLength length: " + tlvListLength);
                    for (int i = 0; i < tlvListLength; i++) {
                        BerTlv tlv = tlvList.get(i);
                        BerTag berTag = tlv.getTag();
                        boolean tagIsConstructed = berTag.isConstructed();
                        writeToUiAppend(etLog, "BerTag: " + berTag.toString());
                        writeToUiAppend(etLog, "BerTag name: " + tv.getEmvTagName(berTag.bytes).getTagName());
                        writeToUiAppend(etLog, "BerTag is constructed: " + tagIsConstructed);
                        // we need to get tag 6F that is constructed
                        if (Arrays.equals(berTag.bytes, new byte[]{(byte) 0x6f})) {
                            BerTlv tag6f = tlvs.find(new BerTag(0x6F));
                            //writeToUiAppend(readResult, "tag6f is constructed: " + tag6f.isConstructed());
                            //byte[] tag6fValue = tag6f.getBytesValue(); // gives error, tag6f is constructed
                            //writeToUiAppend(readResult, "tag6fValue length: " + tag6fValue.length + " data: " + bytesToHex(tag6fValue));
                            List<BerTlv> tag6fVals = tag6f.getValues();
                            int tag6fValLength = tag6fVals.size();
                            writeToUiAppend(etLog, "tag6fValLength length: " + tag6fValLength);
                            for (int i2 = 0; i2 < tag6fValLength; i2++) {
                                BerTlv tlv2 = tag6fVals.get(i2);
                                BerTag berTag2 = tlv2.getTag();
                                //writeToUiAppend(etLog, "BerTag: " + berTag2.toString());
                                tagIsConstructed = berTag2.isConstructed();
                                writeToUiAppend(etLog, "BerTag: " + berTag2.toString());
                                writeToUiAppend(etLog, "BerTag name: " + tv.getEmvTagName(berTag2.bytes).getTagName());
                                writeToUiAppend(etLog, "BerTag is constructed: " + tagIsConstructed);
                                // need to get tag a5 that is constructed
                                if (Arrays.equals(berTag.bytes, new byte[]{(byte) 0xa5})) {
                                    BerTlv tagA5 = tlvs.find(new BerTag(0xA5));
                                    //writeToUiAppend(readResult, "tag6f is constructed: " + tag6f.isConstructed());
                                    //byte[] tag6fValue = tag6f.getBytesValue(); // gives error, tag6f is constructed
                                    //writeToUiAppend(readResult, "tag6fValue length: " + tag6fValue.length + " data: " + bytesToHex(tag6fValue));
                                    List<BerTlv> tagA5Vals = tagA5.getValues();
                                    int tagA5ValLength = tagA5Vals.size();
                                    writeToUiAppend(etLog, "tagA5ValLength length: " + tagA5ValLength);
                                    for (int i3 = 0; i3 < tagA5ValLength; i3++) {
                                        BerTlv tlv3 = tagA5Vals.get(i3);
                                        BerTag berTag3 = tlv3.getTag();
                                        //writeToUiAppend(etLog, "BerTag: " + berTag2.toString());
                                        tagIsConstructed = berTag3.isConstructed();
                                        writeToUiAppend(etLog, "BerTag: " + berTag3.toString());
                                        writeToUiAppend(etLog, "BerTag name: " + tv.getEmvTagName(berTag3.bytes).getTagName());
                                        writeToUiAppend(etLog, "BerTag is constructed: " + tagIsConstructed);

                                    }

                                }
                            }
                            // here we are shortening the parsing through the tree and try to find the AID(s) on the card
                            List<BerTlv> tag4fList = tlvs.findAll(new BerTag(0x4F));
                            if (tag4fList.size() < 1) {
                                writeToUiAppend(etLog, "there is no tag 4f available, stopping here");
                                try {
                                    nfc.close();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                return;
                            }
                            for (int i4f = 0; i4f < tag4fList.size(); i4f++) {
                                BerTlv tlv4f = tag4fList.get(i4f);
                                BerTag berTag4f = tlv4f.getTag();
                                byte[] tlv4fBytes = tlv4f.getBytesValue();
                                writeToUiAppend(etLog, "BerTag: " + berTag4f.toString());
                                writeToUiAppend(etLog, "BerTag name: " + tv.getEmvTagName(berTag4f.bytes).getTagName());
                                writeToUiAppend(etLog, "BerTag value: " + bytesToHex(tlv4fBytes));
                            }

                        }
                    }
                }                /*

                if (responsePse == null) {
                    writeToUiAppend(etLog, "selectApdu with PSE fails (null)");
                } else {
                    if (Arrays.equals(responsePse, RESULT_FAILUE)) {
                        writeToUiAppend(etLog, "selectApdu with PSE fails (not allowed)");
                    } else {
                        writeToUiAppend(etLog, "responsePse length: " + responsePse.length + " data: " + bytesToHex(responsePse));
                        //System.out.println("pse: " + bytesToHex(responsePse));
                    }
                }
                command = selectApdu(PPSE);
                byte[] responsePpse = nfc.transceive(command);
                if (responsePpse == null) {
                    writeToUiAppend(etLog, "selectApdu with PPSE fails (null)");
                } else {
                    if (Arrays.equals(responsePpse, RESULT_FAILUE)) {
                        writeToUiAppend(etLog, "selectApdu with PPSE fails (not allowed)");
                    } else {
                        writeToUiAppend(etLog, "responsePpse length: " + responsePpse.length + " data: " + bytesToHex(responsePpse));
                        //System.out.println("pse: " + bytesToHex(responsePse));
                    }
                }

                writeToUiAppend(etLog, "try to read a nPA (national ID-card Germany)");
                // https://github.com/PersoApp/import/blob/1e255d54cf2260e39c2dd911079da5fd0b35c980/PersoApp-Core/src/de/persoapp/core/card/ICardHandler.java
*/
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
/*
                //byte[] npaAid = hexToBytes("6F048400A500"); // default AID
                //byte[] npaAid = hexToBytes("6F088404524F4F54A500"); // masterfile AID
                //byte[] npaAid = hexToBytes(AID_NPA);
                byte[] npaAid = hexToBytes(AID_ICAO);
                command = selectApdu(npaAid);
                byte[] responseNpaAid = nfc.transceive(command);
                writeToUiAppend(etLog, "responseNpaAid: " + bytesToHex(responseNpaAid));
                if (responseNpaAid == null) {
                    writeToUiAppend(etLog, "selectApdu with npaAid fails (null)");
                } else {
                    if (Arrays.equals(responseNpaAid, RESULT_FAILUE)) {
                        writeToUiAppend(etLog, "selectApdu with npaAid fails (not allowed)");
                    } else {
                        writeToUiAppend(etLog, "responseNpaAid length: " + responseNpaAid.length + " data: " + bytesToHex(responseNpaAid));
                        System.out.println("used AID: " + AID_NPA);
                        System.out.println("responseNpaAid length: " + responseNpaAid.length + " data: " + bytesToHex(responseNpaAid));
 */
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
                    //}
                //}

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

    private byte[] checkResponse(byte[] data) {
        if (data.length < 5) return null; // not ok
        int status = ((0xff & data[data.length - 2]) << 8) | (0xff & data[data.length - 1]);
        if (status != 0x9000) {
            return null;
        } else {
            return Arrays.copyOfRange(data, 0, data.length - 2);
        }
    }


    private boolean responseNotAllowed(byte[] data) {
        byte[] RESULT_FAILUE = hexToBytes("6a82");
        if (data.equals(RESULT_FAILUE)) {
            return true;
        } else {
            return false;
        }
    }

    // https://stackoverflow.com/a/51338700/8166854
    private byte[] selectApdu(byte[] aid) {
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
    
    
    
    private void readNfcA(Tag tag) {
        Log.i(TAG, "read a tag with NfcA technology");
        NfcA nfc = null;
        nfc = NfcA.get(tag);
        if (nfc != null) {
            maxTransceiveLength = nfc.getMaxTransceiveLength();
            atqa = nfc.getAtqa();
            sak = nfc.getSak();
            timeout = nfc.getTimeout();
            StringBuilder sb = new StringBuilder();
            sb.append("TechParameter") . append("\n");
            sb.append("maxTransceiveLength: ") . append(String.valueOf(maxTransceiveLength)).append("\n");
            if (atqa != null) sb.append("atqa: ") . append(bytesToHex(atqa)).append("\n");
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
                    String responseString = bytesToHex(response);
                    writeToUiAppend(etData, "NfcA data for block 00");
                    writeToUiAppend(etData, responseString);
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
        }
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
                    writeToUiAppend(etData, "MiUl page " + i + " " + bytesToHex(pageData));
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
                boolean authenticated = authenticateSector(nfc, sector);
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
    private boolean authenticateSector(MifareClassic mfc, int sector) {

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
                        String temp = bytesToHex(data);
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


}