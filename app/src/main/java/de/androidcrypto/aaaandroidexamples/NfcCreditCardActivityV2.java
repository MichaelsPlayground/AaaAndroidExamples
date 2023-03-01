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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.androidcrypto.aaaandroidexamples.nfccreditcards.TagValues;

public class NfcCreditCardActivityV2 extends AppCompatActivity implements NfcAdapter.ReaderCallback {

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
                Intent intent = new Intent(NfcCreditCardActivityV2.this, MainActivity.class);
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
                // todo The card must not have a PSE or PPSE, then try with known AIDs
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
                            /*
                            // here we are shortening the parsing through the tree and try to find the AID(s) on the card
                            // by searching for tag 4f
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
                            ArrayList<byte[]> aidList = new ArrayList<>();
                            for (int i4f = 0; i4f < tag4fList.size(); i4f++) {
                                BerTlv tlv4f = tag4fList.get(i4f);
                                BerTag berTag4f = tlv4f.getTag();
                                byte[] tlv4fBytes = tlv4f.getBytesValue();
                                aidList.add(tlv4fBytes);
                                writeToUiAppend(etLog, "BerTag: " + berTag4f.toString());
                                writeToUiAppend(etLog, "BerTag name: " + tv.getEmvTagName(berTag4f.bytes).getTagName());
                                writeToUiAppend(etLog, "BerTag value: " + bytesToHex(tlv4fBytes));
                            }
*/
                        }
                    }
                    // here we are shortening the parsing through the tree and try to find the AID(s) on the card
                    // by searching for tag 4f
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
                    writeToUiAppend(etLog, "tag 4f found: " + tag4fList.size() + " aid(s)");
                    ArrayList<byte[]> aidList = new ArrayList<>();
                    for (int i4f = 0; i4f < tag4fList.size(); i4f++) {
                        BerTlv tlv4f = tag4fList.get(i4f);
                        BerTag berTag4f = tlv4f.getTag();
                        byte[] tlv4fBytes = tlv4f.getBytesValue();
                        aidList.add(tlv4fBytes);
                        writeToUiAppend(etLog, "BerTag: " + berTag4f.toString());
                        writeToUiAppend(etLog, "BerTag name: " + tv.getEmvTagName(berTag4f.bytes).getTagName());
                        writeToUiAppend(etLog, "BerTag value: " + bytesToHex(tlv4fBytes));
                    }
                    writeToUiAppend(etLog, "tag 4f found: " + tag4fList.size() + " aid(s)");
                    // step 02: iterating through aidList
                    for (int aidNumber = 0; aidNumber < tag4fList.size(); aidNumber++) {
                        byte[] aidSelected = aidList.get(aidNumber);
                        writeToUiAppend(etLog, "************************************");
                        writeToUiAppend(etLog, "analyzing aidNumber " + aidNumber + " (AID: " + bytesToHex(aidSelected) + ")");
                        command = selectApdu(aidSelected);
                        byte[] responseSelectedAid = nfc.transceive(command);
                        writeToUiAppend(etLog, "04 select AID response length " + responseSelectedAid.length + " data: " + bytesToHex(responseSelectedAid));
                        boolean responseSelectAidNotAllowed = responseNotAllowed(responseSelectedAid);
                        if (responseSelectAidNotAllowed) {
                            writeToUiAppend(etLog, "04 selecting AID is not allowed on card");
                            writeToUiAppend(etLog,"");
                            writeToUiAppend(etLog,"The card is not a credit card, reading aborted");
                            try {
                                nfc.close();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            return;
                        }
                        responseOk = checkResponse(responseSelectedAid);
                        if (responseOk != null) {
                            System.out.println("# selectAid response:" + bytesToHex(responseOk));
                            BerTlvs tlvsAid = parser.parse(responseOk);
                            List<BerTlv> tlvListAid = tlvsAid.getList();
                            int tlvListAidLength = tlvListAid.size();
                            writeToUiAppend(etLog, "tlvListAidLength length: " + tlvListAidLength);
                            // note: different behaviour between Visa and Mastercard
                            // Mastercard has NO PDOL, Visa gives PDOL in tag 9F38
                            // tag 50 and/or tag 9F12 has an application label or application name
                            // nex step: search for tag 9F38 Processing Options Data Object List (PDOL)
                            BerTlv tag9f38 = tlvsAid.find(new BerTag(0x9F, 0x38));
                            // tag9f38 is null when not found
                            if (tag9f38 != null) {
                                // this is mainly for Visa cards
                                byte[] pdolValue = tag9f38.getBytesValue();
                                writeToUiAppend(etLog, "PDOL found: " + bytesToHex(pdolValue));
                                System.out.println("pdolValue: " + bytesToHex(pdolValue));
/*
Voba 6f4a8409d27600002547410100a53d50086769726f636172648701019f38099f33029f35019f40015f2d046465656ebf0c1a9f4d02190a9f6e07028000003030009f0a080001050100000000
6F File Control Information (FCI) Template
 	84 Dedicated File (DF) Name
 	 	D27600002547410100
 	A5 File Control Information (FCI) Proprietary Template
 	 	50 Application Label
 	 	 	g i r o c a r d
 	 	87 Application Priority Indicator
 	 	 	01
 	 	9F38 Processing Options Data Object List (PDOL)
 	 	 	9F33029F35019F4001
 	 	5F2D Language Preference
 	 	 	d e e n
 	 	BF0C File Control Information (FCI) Issuer Discretionary Data
 	 	 	9F4D Log Entry
 	 	 	 	190A
 	 	 	9F6E Unknown tag
 	 	 	 	02800000303000
 	 	 	9F0A Unknown tag
 	 	 	 	0001050100000000
 */
/*
Visa: 9f66049f02069f03069f1a0295055f2a029a039c019f3704
 */
                                // we are using a generalized selectGpo command
                                String pdolWithCountryCode = "80A80000238321A0000000000000000001000000000000084000000000000840070203008017337000";
                                byte[] pdol = hexToBytes(pdolWithCountryCode);
                                System.out.println("#*# parse with pdol: " + bytesToHex(pdol));
                                byte[] gpo;
                                byte[] responsePdol = nfc.transceive(pdol);
                                System.out.println("responsePdol: " + bytesToHex(responsePdol));
                                byte[] responsePdolOk = checkResponse(responsePdol);
                                if (responsePdolOk != null) {
                                    writeToUiAppend(etLog, "respondePdol: " + bytesToHex(responsePdolOk));
                                    System.out.println("responsePdol: " + bytesToHex(responsePdolOk));
                                    // now we are searching for tag 57 = Track 2 Equivalent Data, the first 16 bytes are the cc number
                                    BerTlvs tlvsPdol = parser.parse(responsePdolOk);
                                    BerTlv tag57 = tlvsPdol.find(new BerTag(0x57));
                                    if (tag57 != null) {
                                        byte[] track2Data = tag57.getBytesValue();
                                        // 4930005025003985 D 2609 2012166408100000F
                                        // pan              separator
                                        //                    expiration date yymm
                                        String track2DataString = bytesToHex(track2Data);
                                        int posSeparator = track2DataString.toUpperCase().indexOf("D");
                                        String pan = track2DataString.substring(0, posSeparator);
                                        String expDate = track2DataString.substring((posSeparator + 1), (posSeparator + 5));
                                        writeToUiAppend(etLog, "found PAN " + pan + " expires " + expDate);
                                        writeToUiAppend(etData, "PAN: " + pan);
                                        writeToUiAppend(etData, "Exp. Date: " + expDate);
                                    }
                                } else {
                                    // for Voba Girocard we need another PDOL
                                    // 9F38 Processing Options Data Object List (PDOL)
                                    // 9F33029F35019F4001
                                    // 9F33 02
                                    // 9F35 01
                                    // 9F40 01

                                    // as of specifications:
                                    // 9F33 03
                                    // 9F35 01
                                    // 9F40 05

                                    String pdolNull =     "80A8000002830000";
                                    String pdolEmpty =    "80A800008308000000000000000000";
                                    String pdol4Bytes=    "80A8000083040000000000";
                                    String pdol9Bytes=    "80A80000830900000000000000000000";
                                    String pdolGirocard = "80A800008304A0C0167000";
                                    byte[] pdolCommand = hexToBytes(pdol4Bytes);
                                    System.out.println("#*# parse with pdol: " + bytesToHex(pdolCommand));
                                    byte[] resultGpo;
                                    resultGpo = nfc.transceive(pdolCommand);
                                    System.out.println("resultGpo: " + bytesToHex(resultGpo));
                                    byte[] resultGpoOk = checkResponse(resultGpo);
                                    //writeToUiAppend(etLog, "PDOL not found, response with empty PDOL: " + bytesToHex(resultGpoOk));
                                    //System.out.println("PDOL not found, response with empty PDOL: " + bytesToHex(resultGpoOk));

                                    byte[] cmdRaiseSecurityLevel = new byte[] { 0x00, 0x22,
                                            (byte) 0xf3, 0x02 };
                                    System.out.println("cmdRaiseSecurityLevel: " + bytesToHex(cmdRaiseSecurityLevel));
                                    byte[] resultRaise;
                                    resultRaise = nfc.transceive(cmdRaiseSecurityLevel);
                                    System.out.println("resultRaise: " + bytesToHex(resultRaise));

                                    //last thing to do was to read the record:
                                    // works with Voba Maestro Girocard, HVB Maestro Girocard, norisbank Maestro Girocard, comdirect Vpay Girocard
                                    byte[] readSelectedRecord = new byte[] { 0x00, (byte) 0xb2, (byte) 0x05, (byte) 0x0c, 0x00 };
                                    System.out.println("readSelectedRecord: " + bytesToHex(readSelectedRecord));
                                    byte[] resultRead;
                                    resultRead = nfc.transceive(readSelectedRecord);
                                    System.out.println("resultRead: " + bytesToHex(resultRead));
/*
Voba 70385f24032112315a0a6726428902046846007f5f3401025f280202809f0702ffc09f0d05fc40a480009f0e0500101800009f0f05fc40a498009000
70 EMV Proprietary Template
 	5F24 Application Expiration Date
 	 	211231
 	5A Application Primary Account Number (PAN)
 	 	6726428902046846007F
 	5F34 Application Primary Account Number (PAN) Sequence Number
 	 	02
 	5F28 Issuer Country Code
 	 	0280
 	9F07 Application Usage Control
 	 	FFC0
 	9F0D Issuer Action Code – Default
 	 	FC40A48000
 	9F0E Issuer Action Code – Denial
 	 	0010180000
 	9F0F Issuer Action Code – Online
 	 	FC40A49800
 */


                                }
/*
Visa PDOL: 77478202200057134921828094896752d25022013650000000000f5f3401009f100706040a03a020009f26082f2ddf2bd003c80e9f2701809f360203359f6c0216009f6e04207000009000
77 Response Message Template Format 2
 	82 Application Interchange Profile
 	 	2000
 	57 Track 2 Equivalent Data
 	 	4921828094896752D25022013650000000000F
 	5F34 Application Primary Account Number (PAN) Sequence Number
 	 	00
 	9F10 Issuer Application Data
 	 	06040A03A02000
 	9F26 Application Cryptogram
 	 	2F2DDF2BD003C80E
 	9F27 Cryptogram Information Data
 	 	80
 	9F36 Application Transaction Counter (ATC)
 	 	0335
 	9F6C Unknown tag
 	 	1600
 	9F6E Unknown tag
 	 	20700000
 */

                            } else {
                                writeToUiAppend(etLog, "PDOL not found");
                                String pdolNull = "80A8000002830000";
                                String pdolEmpty = "80A800008308000000000000000000";
                                byte[] pdolCommand = hexToBytes(pdolNull);
                                System.out.println("#*# parse with pdol: " + bytesToHex(pdolCommand));
                                byte[] resultGpo;
                                resultGpo = nfc.transceive(pdolCommand);
                                System.out.println("resultGpo: " + bytesToHex(resultGpo));
                                byte[] resultGpoOk = checkResponse(resultGpo);
                                writeToUiAppend(etLog, "PDOL not found, response with empty PDOL: " + bytesToHex(resultGpoOk));
/*
MC 771282021980940c080101001001010120010200
77 Response Message Template Format 2
 	82 Application Interchange Profile
 	 	1980
 	94 Application File Locator (AFL)
 	 	080101001001010120010200
 */
                                if (responseSendWithPdol(resultGpo)) {
                                    writeToUiAppend(etLog, "card forces to proceed with PDOL, aborted");
                                    return;
                                }
                                // get data from tag 94 = Application File Locator (AFL)
                                BerTlvs tlvsGpo = parser.parse(resultGpoOk);
                                BerTlv tag94 = tlvsGpo.find(new BerTag(0x94));
                                if (tag94 != null) {
                                    byte[] tag94Bytes = tag94.getBytesValue();
                                    writeToUiAppend(etLog, "AFL data: " + bytesToHex(tag94Bytes));
                                    System.out.println("AFL data: " + bytesToHex(tag94Bytes));
/*
AFL data: 080101001001010120010200
 */
                                    // MC output: length: 16 data: 08010100100102011801020020010200
                                    // 08010100 10010201 18010200 20010200
                                    int tag94BytesLength = tag94Bytes.length;
                                    // split array by 4 bytes
                                    List<byte[]> tag94BytesList = divideArray(tag94Bytes, 4);
                                    int tag94BytesListLength = tag94BytesList.size();
                                    writeToUiAppend(etLog, "tag94Bytes divided into " + tag94BytesListLength + " arrays");
                                    for (int i = 0; i < tag94BytesListLength; i++) {
                                        writeToUiAppend(etLog, "get sfi + record for array " + i + " data: " + bytesToHex(tag94BytesList.get(i)));
                                        // get sfi from first byte, 2nd byte is first record, 3rd byte is last record, 4th byte is offline transactions
                                        byte[] tag94BytesListEntry = tag94BytesList.get(i);
                                        byte sfiOrg = tag94BytesListEntry[0];
                                        byte rec1 = tag94BytesListEntry[1];
                                        byte recL = tag94BytesListEntry[2];
                                        byte offl = tag94BytesListEntry[3]; // offline authorization
                                        writeToUiAppend(etLog, "sfiOrg: " + sfiOrg + " rec1: " + ((int) rec1) + " recL: " + ((int) recL));
                                        int sfiNew = (byte) sfiOrg | 0x04; // add 4 = set bit 3
                                        writeToUiAppend(etLog, "sfiNew: " + sfiNew + " rec1: " + ((int) rec1) + " recL: " + ((int) recL));

                                        // read records
                                        byte[] resultReadRecord = new byte[0];

                                        for (int iRecords = (int) rec1; iRecords <= (int) recL; iRecords++) {
                                            byte[] cmd = hexToBytes("00B2000400");
                                            cmd[2] = (byte) (iRecords & 0x0FF);
                                            cmd[3] |= (byte) (sfiNew & 0x0FF);
                                            resultReadRecord = nfc.transceive(cmd);
                                            writeToUiAppend(etLog, "readRecordCommand length: " + cmd.length + " data: " + bytesToHex(cmd));
                                            if ((resultReadRecord[resultReadRecord.length - 2] == (byte) 0x90) && (resultReadRecord[resultReadRecord.length - 1] == (byte) 0x00)) {
                                                writeToUiAppend(etLog, "Success: read record result: " + bytesToHex(resultReadRecord));
                                                writeToUiAppend(etLog, "* parse AFL for entry: " + bytesToHex(tag94BytesListEntry) + " record: " + iRecords);
                                                // this is for complete parsing
                                                //parseAflDataToTextView(resultReadRecord, etLog);
                                                System.out.println("parse " + iRecords + " result: " + bytesToHex(resultReadRecord));
                                                // this is the shortened one
                                                BerTlvs tlvsAfl = parser.parse(resultReadRecord);
                                                // todo there could be a 57 Track 2 Equivalent Data field as well
                                                // 5a = Application Primary Account Number (PAN)
                                                // 5F34 = Application Primary Account Number (PAN) Sequence Number
                                                // 5F25  = Application Effective Date (card valid from)
                                                // 5F24 = Application Expiration Date
                                                BerTlv tag5a = tlvsAfl.find(new BerTag(0x5a));
                                                if (tag5a != null) {
                                                    byte[] tag5aBytes = tag5a.getBytesValue();
                                                    writeToUiAppend(etLog, "PAN: " + bytesToHex(tag5aBytes));
                                                    writeToUiAppend(etData, "PAN: " + bytesToHex(tag5aBytes));
                                                    System.out.println("record " + iRecords + " PAN: " + bytesToHex(tag5aBytes));
                                                }
                                                BerTlv tag5f24 = tlvsAfl.find(new BerTag(0x5f, 0x24));
                                                if (tag5f24 != null) {
                                                    byte[] tag5f24Bytes = tag5f24.getBytesValue();
                                                    writeToUiAppend(etLog, "Exp. Date: " + bytesToHex(tag5f24Bytes));
                                                    writeToUiAppend(etData, "Exp. Date: " + bytesToHex(tag5f24Bytes));
                                                    System.out.println("Exp. Date: " + bytesToHex(tag5f24Bytes));
                                                } else {
                                                    System.out.println("record: " + iRecords + " Tag 5F24 not found");
                                                }
                                            } else {
                                                writeToUiAppend(etLog, "ERROR: read record failed, result: " + bytesToHex(resultReadRecord));
                                                resultReadRecord = new byte[0];
                                            }
                                        }
                                    } // for (int i = 0; i < tag94BytesListLength; i++) { // = number of records belong to this afl

                                }

                            }

                            System.out.println("tag9f38: " + tag9f38.toString());
                            if (tag4fList.size() < 1) {
                                writeToUiAppend(etLog, "there is no tag 4f available, stopping here");
                                try {
                                    nfc.close();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                return;
                            }

                            /*
                            ArrayList<byte[]> aidList = new ArrayList<>();
                            for (int i4f = 0; i4f < tag4fList.size(); i4f++) {
                                BerTlv tlv4f = tag4fList.get(i4f);
                                BerTag berTag4f = tlv4f.getTag();
                                byte[] tlv4fBytes = tlv4f.getBytesValue();
                                aidList.add(tlv4fBytes);
                                writeToUiAppend(etLog, "BerTag: " + berTag4f.toString());
                                writeToUiAppend(etLog, "BerTag name: " + tv.getEmvTagName(berTag4f.bytes).getTagName());
                                writeToUiAppend(etLog, "BerTag value: " + bytesToHex(tlv4fBytes));
                            }*/

/*
Voba: response:6f478409a00000005945430100a53a50086769726f636172648701019f38069f02069f1d025f2d046465656ebf0c1a9f4d02190a9f6e07028000003030009f0a080001050100000000
      response:6f48840aa0000003591010028001a53a50086769726f636172648701019f38069f02069f1d025f2d046465656ebf0c1a9f4d02190a9f6e07028000003030009f0a080001050100000000
      response:6f4a8409d27600002547410100a53d50086769726f636172648701019f38099f33029f35019f40015f2d046465656ebf0c1a9f4d02190a9f6e07028000003030009f0a080001050100000000
MC: 6f528407a0000000041010a54750104465626974204d6173746572436172649f12104465626974204d6173746572436172648701019f1101015f2d046465656ebf0c119f0a04000101019f6e0702800000303000

6F File Control Information (FCI) Template
 	84 Dedicated File (DF) Name
 	 	A0000000041010
 	A5 File Control Information (FCI) Proprietary Template
 	 	50 Application Label
 	 	 	D e b i t M a s t e r C a r d
 	 	9F12 Application Preferred Name
 	 	 	D e b i t M a s t e r C a r d
 	 	87 Application Priority Indicator
 	 	 	01
 	 	9F11 Issuer Code Table Index
 	 	 	01
 	 	5F2D Language Preference
 	 	 	d e e n
 	 	BF0C File Control Information (FCI) Issuer Discretionary Data
 	 	 	9F0A Unknown tag
 	 	 	 	00010101
 	 	 	9F6E Unknown tag
 	 	 	 	02800000303000

Visa: 6f5d8407a0000000031010a5525010564953412044454249542020202020208701029f38189f66049f02069f03069f1a0295055f2a029a039c019f37045f2d02656ebf0c1a9f5a0531082608269f0a080001050100000000bf6304df200180
6F File Control Information (FCI) Template
 	84 Dedicated File (DF) Name
 	 	A0000000031010
 	A5 File Control Information (FCI) Proprietary Template
 	 	50 Application Label
 	 	 	V I S A D E B I T
 	 	87 Application Priority Indicator
 	 	 	02
 	 	9F38 Processing Options Data Object List (PDOL)
 	 	 	9F66049F02069F03069F1A0295055F2A029A039C019F3704
 	 	5F2D Language Preference
 	 	 	e n
 	 	BF0C File Control Information (FCI) Issuer Discretionary Data
 	 	 	9F5A Unknown tag
 	 	 	 	3108260826
 	 	 	9F0A Unknown tag
 	 	 	 	0001050100000000
 	 	 	BF63 Unknown tag
 	 	 	 	DF20 Unknown tag
 	 	 	 	 	80
 */
                        }

                    } // end step 02
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

    private void parseAflDataToTextView(byte[] data, TextView readResult) {
        BerTlvParser parser = new BerTlvParser();
        if (data.length > 253) {
            writeToUiAppend(readResult, "message is far to long to parse, skipped");
        } else {
            // parse data and try to find:
            // 5a = Application Primary Account Number (PAN)
            // 5F34 = Application Primary Account Number (PAN) Sequence Number
            // 5F25  = Application Effective Date (card valid from)
            // 5F24 = Application Expiration Date
            BerTlvs tlvFiles = parser.parse(data, 0, data.length);
            List<BerTlv> tlvFileList = tlvFiles.getList();
            int tlvFileListLength = tlvFileList.size();
            writeToUiAppend(readResult, "tlvFileListLength length: " + tlvFileListLength);
            /*
            for (int i = 0; i < tlvFileListLength; i++) {
                BerTlv tlv = tlvList.get(i);
                BerTag berTag = tlv.getTag();
                writeToUiAppend(readResult, "BerTag: " + berTag.toString());
            }*/
            // tag 5a is primitive (Application Primary Account Number (PAN))
            BerTlv tag5a = tlvFiles.find(new BerTag(0x5A));
            byte[] tag5aBytes;
            if (tag5a == null) {
                writeToUiAppend(readResult, "tag5a is null");
                //return;
            } else {
                tag5aBytes = tag5a.getBytesValue();
                writeToUiAppend(readResult, "*** PAN found ***");
                writeToUiAppend(readResult, "tag5aBytes length: " + tag5aBytes.length + " data: " + bytesToHex(tag5aBytes));
            }
            // MC output:
            // tag 5f34 is primitive (Application Primary Account Number (PAN) Sequence Number)
            BerTlv tag5f34 = tlvFiles.find(new BerTag(0x5F, 0x34));
            byte[] tag5f34Bytes;
            if (tag5f34 == null) {
                writeToUiAppend(readResult, "tag5f34 is null");
                //return;
            } else {
                tag5f34Bytes = tag5f34.getBytesValue();
                writeToUiAppend(readResult, "tag5f34Bytes length: " + tag5f34Bytes.length + " data: " + bytesToHex(tag5f34Bytes));
            }
            // MC output:
            // tag 5f24 is primitive (Application Expiration Date)
            BerTlv tag5f24 = tlvFiles.find(new BerTag(0x5F, 0x24));
            byte[] tag5f24Bytes;
            if (tag5f24 == null) {
                writeToUiAppend(readResult, "tag5f24 is null");
                //return;
            } else {
                tag5f24Bytes = tag5f24.getBytesValue();
                writeToUiAppend(readResult, "tag5f24Bytes length: " + tag5f24Bytes.length + " data: " + bytesToHex(tag5f24Bytes));
            }
            // MC output:
            // MC output:
            // tag 5f25 is primitive (Application Effective Date)
            BerTlv tag5f25 = tlvFiles.find(new BerTag(0x5F, 0x25));
            byte[] tag5f25Bytes;
            if (tag5f25 == null) {
                writeToUiAppend(readResult, "tag5f25 is null");
                //return;
            } else {
                tag5f25Bytes = tag5f25.getBytesValue();
                writeToUiAppend(readResult, "tag5f25Bytes length: " + tag5f25Bytes.length + " data: " + bytesToHex(tag5f25Bytes));
            }
            // MC output:
        }
    }

    public static List<byte[]> divideArray(byte[] source, int chunksize) {

        List<byte[]> result = new ArrayList<byte[]>();
        int start = 0;
        while (start < source.length) {
            int end = Math.min(source.length, start + chunksize);
            result.add(Arrays.copyOfRange(source, start, end));
            start += chunksize;
        }
        return result;
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

    private boolean responseSendWithPdol(byte[] data) {
        byte[] RESULT_FAILUE = hexToBytes("6700");
        if (Arrays.equals(data, RESULT_FAILUE)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean responseNotAllowed(byte[] data) {
        byte[] RESULT_FAILUE = hexToBytes("6a82");
        if (Arrays.equals(data, RESULT_FAILUE)) {
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