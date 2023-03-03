package de.androidcrypto.aaaandroidexamples;

import static de.androidcrypto.aaaandroidexamples.BinaryUtils.bytesToHex;
import static de.androidcrypto.aaaandroidexamples.BinaryUtils.hexToBytes;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
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

import de.androidcrypto.aaaandroidexamples.nfccreditcards.AidValues;
import de.androidcrypto.aaaandroidexamples.nfccreditcards.PdolUtil;
import de.androidcrypto.aaaandroidexamples.nfccreditcards.TagValues;

public class NfcCreditCardActivityPreFinal3 extends AppCompatActivity implements NfcAdapter.ReaderCallback {

    private final String TAG = "NfcCreditCardAct";

    Button btn1;
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
    final String TechIsoDep = "android.nfc.tech.IsoDep";

    String aidSelectedForAnalyze = "";
    String aidSelectedForAnalyzeName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_creditcard);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);

        btn1 = findViewById(R.id.btn1);
        tv1 = findViewById(R.id.tv1);
        etData = findViewById(R.id.etData);
        etLog = findViewById(R.id.etLog);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn1 back to main menu");
                Intent intent = new Intent(NfcCreditCardActivityPreFinal3.this, MainActivity.class);
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
     *
     * @param tag discovered tag
     */
    @Override
    public void onTagDiscovered(Tag tag) {
        runOnUiThread(() -> {
            etLog.setText("");
            etData.setText("");
            aidSelectedForAnalyze = "";
            aidSelectedForAnalyzeName = "";
        });
        playPing();
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
                case TechIsoDep: {
                    writeToUiAppend(etLog, "*** Tech ***");
                    writeToUiAppend(etLog, "Technology IsoDep");
                    readIsoDep(tag);
                    break;
                }
                default: {
                    // do nothing
                    break;
                }
            }
        }
    }

    private void playPing() {
        MediaPlayer mp = MediaPlayer.create(NfcCreditCardActivityPreFinal3.this, R.raw.ping);
        mp.start();
    }

    private void playDoublePing() {
        MediaPlayer mp = MediaPlayer.create(NfcCreditCardActivityPreFinal3.this, R.raw.double_ping);
        mp.start();
    }

    private void readIsoDep(Tag tag) {
        Log.i(TAG, "read a tag with IsoDep technology");
        IsoDep nfc = null;
        nfc = IsoDep.get(tag);
        if (nfc != null) {
            // init of the service methods
            TagValues tv = new TagValues();
            AidValues aidV = new AidValues();
            //GuessPdol gp = new GuessPdol(nfc);
            System.out.println("***** PDOL UTIL *****");
            PdolUtil pu = new PdolUtil(nfc);
            System.out.println("***** PDOL UTIL *****");

            maxTransceiveLength = nfc.getMaxTransceiveLength();
            int timeout = nfc.getTimeout();
            byte[] historicalBytes = nfc.getHistoricalBytes(); // on tags based on NfcA it is filled, otherwise null
            byte[] hiLayerResponse = nfc.getHiLayerResponse(); // on tags based on NfcB it is filled, otherwise null
            boolean extendedLengthApduSupport = nfc.isExtendedLengthApduSupported();
            StringBuilder sb = new StringBuilder();
            sb.append("TechParameter").append("\n");
            sb.append("maxTransceiveLength: ").append(String.valueOf(maxTransceiveLength)).append("\n");
            sb.append("timeout: ").append(String.valueOf(timeout)).append("\n");
            if (historicalBytes != null)
                sb.append("historicalBytes: ").append(bytesToHex(historicalBytes)).append("\n");
            if (hiLayerResponse != null)
                sb.append("hiLayerResponse: ").append(bytesToHex(hiLayerResponse)).append("\n");
            sb.append("extendedLengthApduSupport: ").append(String.valueOf(extendedLengthApduSupport)).append("\n");
            writeToUiAppend(etLog, sb.toString());

            try {
                nfc.connect();
                writeToUiAppend(etLog, "try to read a payment card with PPSE");
                byte[] command;
                writeToUiAppend(etLog, "");
                writeToUiAppend(etLog, "01 select PPSE");
                byte[] PPSE = "2PAY.SYS.DDF01".getBytes(StandardCharsets.UTF_8); // PPSE
                command = selectApdu(PPSE);
                byte[] responsePpse = nfc.transceive(command);
                System.out.println("01 select PPSE length " + command.length + " data: " + bytesToHex(command));
                writeToUiAppend(etLog, "01 select PPSE command length " + command.length + " data: " + bytesToHex(command));
                writeToUiAppend(etLog, "01 select PPSE response length " + responsePpse.length + " data: " + bytesToHex(responsePpse));
                boolean responsePpseNotAllowed = responseNotAllowed(responsePpse);
                if (responsePpseNotAllowed) {
                    // todo The card must not have a PSE or PPSE, then try with known AIDs
                    writeToUiAppend(etLog, "01 selecting PPSE is not allowed on card");
                }

                if (responsePpseNotAllowed) {
                    writeToUiAppend(etLog, "");
                    writeToUiAppend(etLog, "The card is not a credit card, reading aborted");
                    try {
                        nfc.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return;
                }
                byte[] responsePpseOk = checkResponse(responsePpse);
                if (responsePpseOk != null) {
                    System.out.println("### analyze selectPpse respond: " + bytesToHex(responsePpseOk));
                    writeToUiAppend(etLog, "");
                    writeToUiAppend(etLog, "02 analyze select PPSE response and search for tag 0x4F");
                    System.out.println("### analyze selectPpse response:" + bytesToHex(responsePpseOk));

                    BerTlvParser parser = new BerTlvParser();
                    BerTlvs tlv4Fs = parser.parse(responsePpseOk);
                    // by searching for tag 4f
                    List<BerTlv> tag4fList = tlv4Fs.findAll(new BerTag(0x4F));
                    if (tag4fList.size() < 1) {
                        writeToUiAppend(etLog, "there is no tag 0x4F available, stopping here");
                        try {
                            nfc.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        return;
                    }
                    writeToUiAppend(etLog, "Found tag 0x4F " + tag4fList.size() + " times");
                    ArrayList<byte[]> aidList = new ArrayList<>();
                    for (int i4f = 0; i4f < tag4fList.size(); i4f++) {
                        BerTlv tlv4f = tag4fList.get(i4f);
                        BerTag berTag4f = tlv4f.getTag();
                        byte[] tlv4fBytes = tlv4f.getBytesValue();
                        aidList.add(tlv4fBytes);
                    }
                    // step 03: iterating through aidList by selecting AID
                    System.out.println("we found AIDs: " + tag4fList.size());
                    for (int aidNumber = 0; aidNumber < tag4fList.size(); aidNumber++) {
                        byte[] aidSelected = aidList.get(aidNumber);
                        writeToUiAppend(etLog, "************************************");
                        writeToUiAppend(etLog, "analyzing aidNumber " + aidNumber + " (AID: " + bytesToHex(aidSelected) + ")");
                        writeToUiAppend(etLog, "card is a " + aidV.getAidName(aidSelected));

                        aidSelectedForAnalyze = bytesToHex(aidSelected);
                        aidSelectedForAnalyzeName = aidV.getAidName(aidSelected);
                        System.out.println("### analyzing aidNumber " + aidNumber + " (AID: " + bytesToHex(aidSelected) + ")");
                        command = selectApdu(aidSelected);
                        byte[] responseSelectedAid = nfc.transceive(command);
                        System.out.println("03 select AID length " + command.length + " data: " + bytesToHex(command));
                        writeToUiAppend(etLog, "");
                        writeToUiAppend(etLog, "03 select AID command length " + command.length + " data: " + bytesToHex(command));

                        boolean responseSelectAidNotAllowed = responseNotAllowed(responseSelectedAid);
                        if (responseSelectAidNotAllowed) {
                            writeToUiAppend(etLog, "03 selecting AID is not allowed on card");
                            writeToUiAppend(etLog, "");
                            writeToUiAppend(etLog, "The card is not a credit card, reading aborted");
                            try {
                                nfc.close();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            return;
                        }
                        byte[] responseSelectedAidOk = checkResponse(responseSelectedAid);
                        if (responseSelectedAidOk != null) {
                            writeToUiAppend(etLog, "03 select AID response length " + responseSelectedAidOk.length + " data: " + bytesToHex(responseSelectedAidOk));
                            System.out.println("### analyze selectAid: " + bytesToHex(responseSelectedAidOk));
                            BerTlvs tlvsAid = parser.parse(responseSelectedAidOk);

                            //List<BerTlv> tlvListAid = tlvsAid.getList();
                            //int tlvListAidLength = tlvListAid.size();
                            //writeToUiAppend(etLog, "tlvListAidLength length: " + tlvListAidLength);

                            writeToUiAppend(etLog, "");
                            writeToUiAppend(etLog, "04 search for tag 0x9F38 in the selectAid response");
                            /**
                             * note: different behaviour between Visa and Mastercard
                             * Mastercard has NO PDOL, Visa gives PDOL in tag 9F38
                             * tag 50 and/or tag 9F12 has an application label or application name
                             * nex step: search for tag 9F38 Processing Options Data Object List (PDOL)
                             */
                            BerTlv tag9f38 = tlvsAid.find(new BerTag(0x9F, 0x38));
                            // tag9f38 is null when not found
                            if (tag9f38 != null) {
                                // this is mainly for Visa cards
                                byte[] pdolValue = tag9f38.getBytesValue();
                                writeToUiAppend(etLog, "found tag 0x9F38 in the selectAid respond length: " + pdolValue.length + " data: " + bytesToHex(pdolValue));
                                //writeToUiAppend(etLog, "PDOL found: " + bytesToHex(pdolValue));
                                System.out.println("### analyze pdol: " + bytesToHex(pdolValue));
                                // code will run for VISA and NOT for MasterCard
                                // we are using a generalized selectGpo command
                                String pdolWithCountryCode = "80A80000238321A0000000000000000001000000000000084000000000000840070203008017337000";
                                byte[] pdol = hexToBytes(pdolWithCountryCode);
                                writeToUiAppend(etLog, "");
                                writeToUiAppend(etLog, "05 selectPdol command length: " + pdol.length + " data: ");
                                writeToUiAppend(etLog, bytesToHex(pdol));

                                System.out.println("#*# parse with pdol: " + bytesToHex(pdol));
                                byte[] responsePdol = nfc.transceive(pdol);
                                System.out.println("responsePdol: " + bytesToHex(responsePdol));
                                byte[] responsePdolOk = checkResponse(responsePdol);
                                if (responsePdolOk != null) {
                                    writeToUiAppend(etLog, "05 selectPdol response length: " + responsePdolOk.length + " data: " + bytesToHex(responsePdolOk));
                                    System.out.println("# selectAid response:" + bytesToHex(responsePdolOk));
                                    System.out.println("responsePdol: " + bytesToHex(responsePdolOk));
                                    System.out.println("### analyze responsePdol: " + bytesToHex(responsePdolOk));
                                    // now we are searching for tag 57 = Track 2 Equivalent Data, the first 16 bytes are the cc number
                                    writeToUiAppend(etLog, "");
                                    writeToUiAppend(etLog, "06 search for tag 0x57");
                                    // new
                                    byte[] track2Data = getTagValueFromResult(responsePdolOk, (byte) 0x57);
                                    if (track2Data != null) {
                                        writeToUiAppend(etLog, "found tag 0x57 = Track 2 Equivalent Data");
                                        String track2DataString = bytesToHex(track2Data);
                                        int posSeparator = track2DataString.toUpperCase().indexOf("D");
                                        String pan = track2DataString.substring(0, posSeparator);
                                        String expDate = track2DataString.substring((posSeparator + 1), (posSeparator + 5));
                                        writeToUiAppend(etData, "data for AID " + aidSelectedForAnalyze + " (" + aidSelectedForAnalyzeName + ")");
                                        writeToUiAppend(etLog, "found PAN " + pan + " expires " + expDate);
                                        writeToUiAppend(etData, "PAN: " + pan);
                                        writeToUiAppend(etData, "Exp. Date (YYMM): " + expDate);
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

                                    // see https://www.openscdp.org/scripts/tutorial/emv/initiateapplicationprocess.html

                                    String pdolNull = "80A8000002830000";
                                    //String pdol0 = "80A8000002830000"; //80 A8 00 00 83 08 00 00 00 00 00 00 00 00 00
                                    String pdol0 = "80A800000A8308000000000000000000"; // this command does work with GiroCards
                                    //                      0A complete length
                                    //                          08 length indicator for PDOL
                                    //                            0000000000000000 8* 0x00
                                    //                                            00 final 0x00

                                    String pdol1 = "80A800000383000000";
                                    String pdol2 = "80A80000048300000000";
                                    String pdol3 = "80A8000005830000000000";
                                    String pdol4 = "80A800000683000000000000";
                                    String pdol5 = "80A80000078300000000000000";
                                    String pdol6 = "80A8000008830000000000000000";
                                    String pdol7 = "80A800000983000000000000000000";
                                    String pdol8 = "80A800000a8300000000000000000000";
                                    String pdol9 = "80A800000b830000000000000000000000";
                                    String pdolX = "80A80000830000";

                                    // test
                                    byte[] pdolCommand0 = hexToBytes(pdol0);



                                    byte[] pdolCommand1 = hexToBytes(pdol1);
                                    byte[] pdolCommand2 = hexToBytes(pdol2);
                                    byte[] pdolCommand3 = hexToBytes(pdol3);
                                    byte[] pdolCommand4 = hexToBytes(pdol4);
                                    byte[] pdolCommand5 = hexToBytes(pdol5);
                                    byte[] pdolCommand6 = hexToBytes(pdol6);
                                    byte[] pdolCommand7 = hexToBytes(pdol7);
                                    byte[] pdolCommand8 = hexToBytes(pdol8);
                                    byte[] pdolCommand9 = hexToBytes(pdol9);
                                    byte[] pdolCommandX = hexToBytes(pdolX);
                                    byte[] resultGpo0 = nfc.transceive(pdolCommand0);
                                    byte[] resultGpo1 = nfc.transceive(pdolCommand1);
                                    byte[] resultGpo2 = nfc.transceive(pdolCommand2);
                                    byte[] resultGpo3 = nfc.transceive(pdolCommand3);
                                    byte[] resultGpo4 = nfc.transceive(pdolCommand4);
                                    byte[] resultGpo5 = nfc.transceive(pdolCommand5);
                                    byte[] resultGpo6 = nfc.transceive(pdolCommand6);
                                    byte[] resultGpo7 = nfc.transceive(pdolCommand7);
                                    byte[] resultGpo8 = nfc.transceive(pdolCommand8);
                                    byte[] resultGpo9 = nfc.transceive(pdolCommand9);
                                    byte[] resultGpoX = nfc.transceive(pdolCommandX);


                                    System.out.println("******* guessPdol ***********");
                                    // this is a very simplified version to read the requested pdol length
                                    // pdolValue contains the full pdolRequest, e.g.
                                    // we assume that all requested tag are 2 byte tags, e.g.
                                    // if remainder is 0 we can try to sum the length data in pdolValue[2], pdolValue[5]...
                                    int modulus = pdolValue.length / 3;
                                    int remainder = pdolValue.length % 3;
                                    int guessedPdolLength = 0;
                                    if (remainder == 0) {
                                        for (int i = 0; i < modulus; i++) {
                                            guessedPdolLength += (int) pdolValue[(i*3)+2];
                                        }
                                    } else {
                                        guessedPdolLength = 999;
                                    }
                                    System.out.println("guessedPdolLength: " + guessedPdolLength);
                                    // need to select AID again because it could be found before, then a selectPdol does not work anymore...
                                    command = selectApdu(aidSelected);
                                    responseSelectedAid = nfc.transceive(command);
                                    System.out.println("selectAid again, result: " + bytesToHex(responseSelectedAid));
                                    //byte[] guessedPdolResult = gp.getPdol(guessedPdolLength);
                                    byte[] guessedPdolResult = pu.getGpo(guessedPdolLength);
                                    if (guessedPdolResult != null) {
                                        System.out.println("guessedPdolResult: " + bytesToHex(guessedPdolResult));
                                        // read the PAN & Expiration date
                                        String pan_expirationDate = readPanFromFilesFromGpo(nfc, guessedPdolResult);
                                        System.out.println("PAN|ExpirationDate: " + pan_expirationDate);
                                    } else {
                                        System.out.println("guessedPdolResult is NULL");
                                    }
                                    System.out.println("******* guessPdol END ***********");

                                    // need to select AID again
                                    command = selectApdu(aidSelected);
                                    responseSelectedAid = nfc.transceive(command);
                                    System.out.println("selectAid again, result: " + bytesToHex(responseSelectedAid));

                                    // 8 byte PDOL request
                                    String pdol08 = "80A800000A8308000000000000000000"; // this command does work with GiroCards

                                    // 4 byte PDOL request
                                    String pdol04 = "80A800000683040000000000";
                                    byte[] pdolCommand04 = hexToBytes(pdol04);
                                    byte[] resultGpo04 = nfc.transceive(pdolCommand04);
                                    System.out.println("*** 4 byte PDOL request AID: d2760000254741010 ***");
                                    System.out.println("commandGpo04: " + bytesToHex(pdolCommand04));
                                    System.out.println("resultGpo04: " + bytesToHex(resultGpo04));
                                    System.out.println("*** 4 byte PDOL request AID: d2760000254741010 END ***");

                                    // 2 byte PDOL request
                                    String pdol02 = "80A80000048302000000";
                                    byte[] pdolCommand02 = hexToBytes(pdol02);
                                    byte[] resultGpo02 = nfc.transceive(pdolCommand02);
                                    System.out.println("*** 2 byte PDOL request AID: d27600002547410100 ***");
                                    System.out.println("commandGpo02: " + bytesToHex(pdolCommand02));
                                    System.out.println("resultGpo02: " + bytesToHex(resultGpo02));
                                    byte[] resultGpo02Ok = checkResponse(resultGpo02);
                                    if (resultGpo02Ok != null) {
                                        // search for tag 0x94 = AFL
                                        BerTlvs tlvsGpo02 = parser.parse(resultGpo02Ok);
                                        BerTlv tag94 = tlvsGpo02.find(new BerTag(0x94));
                                        if (tag94 != null) {
                                            byte[] tag94Bytes = tag94.getBytesValue();
                                            System.out.println("*** PDOL request with 2 bytes ***");
                                            writeToUiAppend(etLog, "AFL data: " + bytesToHex(tag94Bytes));
                                            System.out.println("AFL data: " + bytesToHex(tag94Bytes));
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
                                                    System.out.println("** for loop start " + (int) rec1 + " to " + (int) recL + " iRecords: " + iRecords);

                                                    System.out.println("*#* readRecors iRecords: " + iRecords);
                                                    byte[] cmd = hexToBytes("00B2000400");
                                                    cmd[2] = (byte) (iRecords & 0x0FF);
                                                    cmd[3] |= (byte) (sfiNew & 0x0FF);
                                                    resultReadRecord = nfc.transceive(cmd);
                                                    writeToUiAppend(etLog, "readRecordCommand length: " + cmd.length + " data: " + bytesToHex(cmd));
                                                    byte[] resultReadRecordOk = checkResponse(resultReadRecord);
                                                    if (resultReadRecordOk != null) {
                                                        //if ((resultReadRecord[resultReadRecord.length - 2] == (byte) 0x90) && (resultReadRecord[resultReadRecord.length - 1] == (byte) 0x00)) {
                                                        writeToUiAppend(etLog, "Success: read record result: " + bytesToHex(resultReadRecord));
                                                        writeToUiAppend(etLog, "* parse AFL for entry: " + bytesToHex(tag94BytesListEntry) + " record: " + iRecords);
                                                        System.out.println("* parse AFL for entry: " + bytesToHex(tag94BytesListEntry) + " record: " + iRecords);
                                                        // this is for complete parsing
                                                        //parseAflDataToTextView(resultReadRecord, etLog);
                                                        System.out.println("parse " + iRecords + " result: " + bytesToHex(resultReadRecordOk));
                                                        // this is the shortened one
                                                        try {
                                                            BerTlvs tlvsAfl = parser.parse(resultReadRecordOk);
                                                            // todo there could be a 57 Track 2 Equivalent Data field as well
                                                            // 5a = Application Primary Account Number (PAN)
                                                            // 5F34 = Application Primary Account Number (PAN) Sequence Number
                                                            // 5F25  = Application Effective Date (card valid from)
                                                            // 5F24 = Application Expiration Date
                                                            BerTlv tag5a = tlvsAfl.find(new BerTag(0x5a));
                                                            if (tag5a != null) {
                                                                byte[] tag5aBytes = tag5a.getBytesValue();
                                                                writeToUiAppend(etData, "Xdata for AID " + aidSelectedForAnalyze + " (" + aidSelectedForAnalyzeName + ")");
                                                                writeToUiAppend(etLog, "XPAN: " + bytesToHex(tag5aBytes));
                                                                writeToUiAppend(etData, "XPAN: " + bytesToHex(tag5aBytes));
                                                                System.out.println("record " + iRecords + " XPAN: " + bytesToHex(tag5aBytes));
                                                            }
                                                            BerTlv tag5f24 = tlvsAfl.find(new BerTag(0x5f, 0x24));
                                                            if (tag5f24 != null) {
                                                                byte[] tag5f24Bytes = tag5f24.getBytesValue();
                                                                writeToUiAppend(etLog, "XExp. Date: " + bytesToHex(tag5f24Bytes));
                                                                writeToUiAppend(etData, "XExp. Date (YYMMDD): " + bytesToHex(tag5f24Bytes));
                                                                System.out.println("Exp. Date: " + bytesToHex(tag5f24Bytes));
                                                            } else {
                                                                System.out.println("record: " + iRecords + " Tag 5F24 not found");
                                                            }
                                                        } catch (ArrayIndexOutOfBoundsException e) {
                                                            System.out.println("ERROR: ArrayOutOfBoundsException: " + e.getMessage());
                                                        }
                                                    } else {
                                                        writeToUiAppend(etLog, "ERROR: read record failed, result: " + bytesToHex(resultReadRecord));
                                                        resultReadRecord = new byte[0];
                                                    }
                                                }
                                            } // for (int i = 0; i < tag94BytesListLength; i++) { // = number of records belong to this afl
                                            System.out.println("*** PDOL request with 2 bytes END ***");
                                        }
                                    }

                                    System.out.println("*** 2 byte PDOL request AID: d27600002547410100 END ***");

                                    System.out.println("commandGpo0: " + bytesToHex(pdolCommand0));
                                    System.out.println("resultGpo0: " + bytesToHex(resultGpo0));
                                    writeToUiAppend(etLog, "xx sendPdol command length: " + pdolCommand0.length + " data: " + bytesToHex(pdolCommand0));
                                    // new copied from MasterCard section as we do have an AFL now
                                    if (responseSendWithPdol(resultGpo0)) {
                                        writeToUiAppend(etLog, "card forces to proceed with PDOL, aborted");
                                        // todo find another solution if situation occurs
                                        return;
                                    }
                                    byte[] resultGpo0Ok = checkResponse(resultGpo0);
                                    // get data from tag 94 = Application File Locator (AFL)
                                    BerTlvs tlvsGpo = parser.parse(resultGpo0Ok);
                                    BerTlv tag94 = tlvsGpo.find(new BerTag(0x94));
                                    if (tag94 != null) {
                                        byte[] tag94Bytes = tag94.getBytesValue();
                                        writeToUiAppend(etLog, "AFL data: " + bytesToHex(tag94Bytes));
                                        System.out.println("AFL data: " + bytesToHex(tag94Bytes));
/*
Voba Girocard
AID a00000005945430100
analyze pdol: 9f02 06 9f1d 02 (8 bytes)
String pdol0 = "80A800000A8308000000000000000000"; // this command does work with GiroCards
AFL data: 18010100 20010100 20040400 08050501 08070701 08030301 5 entries
parse AFL for entry: 18010100 record: 1
7081e28f01059081b078cdb2c84b435325ec4478fd6f0f9f0dd61210a78c791adcb22c85fb0095db3a540658569a1c0d35a48d1fd9c2dba83ed941fcb3f2cfe56c943bfa0f8d25f0896284006cbdc10821cf0f0f6ec033332f8eb52c1acad9c52221a27dd23aba70c27c547aece994c7dc5c4d5f1b28529a803340cc249caf6bcb3614d071de141f89a1f4a545c5598395864474514e42c7f1edbeedef27b9a50eeb81ed5762a0af36505ee084703dfd168ec6f02245077d8b9f3201039224b0568adf146b092492be46e5d57d920b026be8e734264cf34710483a0af52d46790f01ab0000

70 EMV Proprietary Template
 	8F Certification Authority Public Key Index
 	 	05
 	90 Issuer Public Key Certificate
 	 	78CDB2C84B435325EC4478FD6F0F9F0DD61210A78C791ADCB22C85FB0095DB3A540658569A1C0D35A48D1FD9C2DBA83ED941FCB3F2CFE56C943BFA0F8D25F0896284006CBDC10821CF0F0F6EC033332F8EB52C1ACAD9C52221A27DD23ABA70C27C547AECE994C7DC5C4D5F1B28529A803340CC249CAF6BCB3614D071DE141F89A1F4A545C5598395864474514E42C7F1EDBEEDEF27B9A50EEB81ED5762A0AF36505EE084703DFD168EC6F02245077D8B
 	9F32 Issuer Public Key Exponent
 	 	03
 	92 Issuer Public Key Remainder
 	 	B0568ADF146B092492BE46E5D57D920B026BE8E734264CF34710483A0AF52D46790F01AB
 	00 Unknown tag

parse AFL for entry: 20010100 record: 1
70339f47030100019f480a757271487e0b220c81cb0000000000000000000000000000000000000000000000000000000000000000
70 EMV Proprietary Template
 	9F47 Integrated Circuit Card (ICC) Public Key Exponent
 	 	010001
 	9F48 Integrated Circuit Card (ICC) Public Key Remainder
 	 	757271487E0B220C81CB
 	00 Unknown tag

 	00 Unknown tag

 	00 Unknown tag

 	00 Unknown tag

 	00 Unknown tag

 	00 Unknown tag

 	00 Unknown tag

 	00 Unknown tag

 	00 Unknown tag

 	00 Unknown tag

 	00 Unknown tag

 	00 Unknown tag

 	00 Unknown tag

 	00 Unknown tag

 	00 Unknown tag

 	00 Unknown tag

parse AFL for entry: 20040400 record: 4
7081b49f4681b087df5942ee89317aea2e53d477ab272794375e9025b0447b304f52e07f54494bea054076a0fd22faf4ee85cfd06ae61c44e0bf1c0156b1c0f287312e1c9460c0b93fac7bdd88a6cf286daeeab5d81310ff49b9d80f4b905261429b44a2c0e3b876ee8825fbb6ff3aef14a645983e886a61a7acde252698868b74033bbecee902050196579b2df75bfe070a14a45ce710c5e782da9ecd20d21db77352461b031ad83d9137615b8a63aca55900619a7a9c
70 EMV Proprietary Template
 	9F46 Integrated Circuit Card (ICC) Public Key Certificate
 	 	87DF5942EE89317AEA2E53D477AB272794375E9025B0447B304F52E07F54494BEA054076A0FD22FAF4EE85CFD06AE61C44E0BF1C0156B1C0F287312E1C9460C0B93FAC7BDD88A6CF286DAEEAB5D81310FF49B9D80F4B905261429B44A2C0E3B876EE8825FBB6FF3AEF14A645983E886A61A7ACDE252698868B74033BBECEE902050196579B2DF75BFE070A14A45CE710C5E782DA9ECD20D21DB77352461B031AD83D9137615B8A63ACA55900619A7A9C

parse AFL for entry: 08050501 record: 5
70385f24032112315a0a6726428902046846007f5f3401025f280202809f0702ffc09f0d05fc40a480009f0e0500101800009f0f05fc40a49800

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

parse AFL for entry: 08070701 record: 7
701c8e0c00000000000000001f0302039f080200029f6c02ffff9f4a0182
70 EMV Proprietary Template
 	8E Cardholder Verification Method (CVM) List
 	 	00000000000000001F030203
 	9F08 Application Version Number
 	 	0002
 	9F6C Unknown tag
 	 	FFFF
 	9F4A Static Data Authentication Tag List
 	 	82

parse AFL for entry: 08030301 record: 3
703d8c1b9f02069f03069f1a0295055f2a029a039c019f37049f35019f34038d0991108a0295059f370457136726428902046846007d21122010254828156f
70 EMV Proprietary Template
 	8C Card Risk Management Data Object List 1 (CDOL1)
 	 	9F02069F03069F1A0295055F2A029A039C019F37049F35019F3403
 	8D Card Risk Management Data Object List 2 (CDOL2)
 	 	91108A0295059F3704
 	57 Track 2 Equivalent Data
 	 	6726428902046846007D21122010254828156F

AID: D27600002547410100 (ZKA/ATM)
6f4a8409d27600002547410100a53d50086769726f636172648701019f38099f33029f35019f40015f2d046465656ebf0c1a9f4d02190a9f6e07028000003030009f0a080001050100000000
PDOL: 9f33029f35019f4001
PDOL: 9f33 02 9f35 01 9f40 01 (4 bytes of data)
String pdol04 = "80A800000683040000000000"; // this command does work with GiroCards that ask for 4 bytes PDOL
resultGpo04: 770a82021800940408020500
77 Response Message Template Format 2
 	82 Application Interchange Profile
 	 	1800
 	94 Application File Locator (AFL)
 	 	08020500

 */

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
                                                System.out.println("** for loop start " + (int) rec1 + " to " + (int) recL + " iRecords: " + iRecords);

                                                System.out.println("*#* readRecors iRecords: " + iRecords);
                                                byte[] cmd = hexToBytes("00B2000400");
                                                cmd[2] = (byte) (iRecords & 0x0FF);
                                                cmd[3] |= (byte) (sfiNew & 0x0FF);
                                                resultReadRecord = nfc.transceive(cmd);
                                                writeToUiAppend(etLog, "readRecordCommand length: " + cmd.length + " data: " + bytesToHex(cmd));
                                                byte[] resultReadRecordOk = checkResponse(resultReadRecord);
                                                if (resultReadRecordOk != null) {
                                                //if ((resultReadRecord[resultReadRecord.length - 2] == (byte) 0x90) && (resultReadRecord[resultReadRecord.length - 1] == (byte) 0x00)) {
                                                    writeToUiAppend(etLog, "Success: read record result: " + bytesToHex(resultReadRecord));
                                                    writeToUiAppend(etLog, "* parse AFL for entry: " + bytesToHex(tag94BytesListEntry) + " record: " + iRecords);
                                                    System.out.println("* parse AFL for entry: " + bytesToHex(tag94BytesListEntry) + " record: " + iRecords);
                                                    // this is for complete parsing
                                                    //parseAflDataToTextView(resultReadRecord, etLog);
                                                    System.out.println("parse " + iRecords + " result: " + bytesToHex(resultReadRecordOk));
                                                    // this is the shortened one
                                                    try {
                                                        BerTlvs tlvsAfl = parser.parse(resultReadRecordOk);
                                                        // todo there could be a 57 Track 2 Equivalent Data field as well
                                                        // 5a = Application Primary Account Number (PAN)
                                                        // 5F34 = Application Primary Account Number (PAN) Sequence Number
                                                        // 5F25  = Application Effective Date (card valid from)
                                                        // 5F24 = Application Expiration Date
                                                        BerTlv tag5a = tlvsAfl.find(new BerTag(0x5a));
                                                        if (tag5a != null) {
                                                            byte[] tag5aBytes = tag5a.getBytesValue();
                                                            writeToUiAppend(etData, "Xdata for AID " + aidSelectedForAnalyze + " (" + aidSelectedForAnalyzeName + ")");
                                                            writeToUiAppend(etLog, "XPAN: " + bytesToHex(tag5aBytes));
                                                            writeToUiAppend(etData, "XPAN: " + bytesToHex(tag5aBytes));
                                                            System.out.println("record " + iRecords + " XPAN: " + bytesToHex(tag5aBytes));
                                                        }
                                                        BerTlv tag5f24 = tlvsAfl.find(new BerTag(0x5f, 0x24));
                                                        if (tag5f24 != null) {
                                                            byte[] tag5f24Bytes = tag5f24.getBytesValue();
                                                            writeToUiAppend(etLog, "XExp. Date: " + bytesToHex(tag5f24Bytes));
                                                            writeToUiAppend(etData, "XExp. Date (YYMMDD): " + bytesToHex(tag5f24Bytes));
                                                            System.out.println("Exp. Date: " + bytesToHex(tag5f24Bytes));
                                                        } else {
                                                            System.out.println("record: " + iRecords + " Tag 5F24 not found");
                                                        }
                                                    } catch (ArrayIndexOutOfBoundsException e) {
                                                        System.out.println("ERROR: ArrayOutOfBoundsException: " + e.getMessage());
                                                    }
                                                } else {
                                                    writeToUiAppend(etLog, "ERROR: read record failed, result: " + bytesToHex(resultReadRecord));
                                                    resultReadRecord = new byte[0];
                                                }
                                            }
                                        } // for (int i = 0; i < tag94BytesListLength; i++) { // = number of records belong to this afl

                                    }

/*

7081e28f01059081b078cdb2c84b435325ec4478fd6f0f9f0dd61210a78c791adcb22c85fb0095db3a540658569a1c0d35a48d1fd9c2dba83ed941fcb3f2cfe56c943bfa0f8d25f0896284006cbdc10821cf0f0f6ec033332f8eb52c1acad9c52221a27dd23aba70c27c547aece994c7dc5c4d5f1b28529a803340cc249caf6bcb3614d071de141f89a1f4a545c5598395864474514e42c7f1edbeedef27b9a50eeb81ed5762a0af36505ee084703dfd168ec6f02245077d8b9f3201039224b0568adf146b092492be46e5d57d920b026be8e734264cf34710483a0af52d46790f01ab0000
70 EMV Proprietary Template
 	8F Certification Authority Public Key Index
 	 	05
 	90 Issuer Public Key Certificate
 	 	78CDB2C84B435325EC4478FD6F0F9F0DD61210A78C791ADCB22C85FB0095DB3A540658569A1C0D35A48D1FD9C2DBA83ED941FCB3F2CFE56C943BFA0F8D25F0896284006CBDC10821CF0F0F6EC033332F8EB52C1ACAD9C52221A27DD23ABA70C27C547AECE994C7DC5C4D5F1B28529A803340CC249CAF6BCB3614D071DE141F89A1F4A545C5598395864474514E42C7F1EDBEEDEF27B9A50EEB81ED5762A0AF36505EE084703DFD168EC6F02245077D8B
 	9F32 Issuer Public Key Exponent
 	 	03
 	92 Issuer Public Key Remainder
 	 	B0568ADF146B092492BE46E5D57D920B026BE8E734264CF34710483A0AF52D46790F01AB
 	00 Unknown tag

70339f47030100019f480a757271487e0b220c81cb0000000000000000000000000000000000000000000000000000000000000000

70 EMV Proprietary Template
 	9F47 Integrated Circuit Card (ICC) Public Key Exponent
 	 	010001
 	9F48 Integrated Circuit Card (ICC) Public Key Remainder
 	 	757271487E0B220C81CB
 	00 Unknown tag

 	00 Unknown tag

 	00 Unknown tag

 	00 Unknown tag

 	00 Unknown tag

 	00 Unknown tag

 	00 Unknown tag

 	00 Unknown tag

 	00 Unknown tag

 	00 Unknown tag

 	00 Unknown tag

 	00 Unknown tag

 	00 Unknown tag

 	00 Unknown tag

 	00 Unknown tag

 	00 Unknown tag


7081b49f4681b087df5942ee89317aea2e53d477ab272794375e9025b0447b304f52e07f54494bea054076a0fd22faf4ee85cfd06ae61c44e0bf1c0156b1c0f287312e1c9460c0b93fac7bdd88a6cf286daeeab5d81310ff49b9d80f4b905261429b44a2c0e3b876ee8825fbb6ff3aef14a645983e886a61a7acde252698868b74033bbecee902050196579b2df75bfe070a14a45ce710c5e782da9ecd20d21db77352461b031ad83d9137615b8a63aca55900619a7a9c
70 EMV Proprietary Template
 	9F46 Integrated Circuit Card (ICC) Public Key Certificate
 	 	87DF5942EE89317AEA2E53D477AB272794375E9025B0447B304F52E07F54494BEA054076A0FD22FAF4EE85CFD06AE61C44E0BF1C0156B1C0F287312E1C9460C0B93FAC7BDD88A6CF286DAEEAB5D81310FF49B9D80F4B905261429B44A2C0E3B876EE8825FBB6FF3AEF14A645983E886A61A7ACDE252698868B74033BBECEE902050196579B2DF75BFE070A14A45CE710C5E782DA9ECD20D21DB77352461B031AD83D9137615B8A63ACA55900619A7A9C

parse 5 result:
70385f24032112315a0a6726428902046846007f5f3401025f280202809f0702ffc09f0d05fc40a480009f0e0500101800009f0f05fc40a49800
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


701c8e0c00000000000000001f0302039f080200029f6c02ffff9f4a0182
70 EMV Proprietary Template
 	8E Cardholder Verification Method (CVM) List
 	 	00000000000000001F030203
 	9F08 Application Version Number
 	 	0002
 	9F6C Unknown tag
 	 	FFFF
 	9F4A Static Data Authentication Tag List
 	 	82


703d8c1b9f02069f03069f1a0295055f2a029a039c019f37049f35019f34038d0991108a0295059f370457136726428902046846007d21122010254828156f

70 EMV Proprietary Template
 	8C Card Risk Management Data Object List 1 (CDOL1)
 	 	9F02069F03069F1A0295055F2A029A039C019F37049F35019F3403
 	8D Card Risk Management Data Object List 2 (CDOL2)
 	 	91108A0295059F3704
 	57 Track 2 Equivalent Data
 	 	6726428902046846007D21122010254828156F






*/




/*
Voba 771e820219809418180101002001010020040400080505010807070108030301
77 Response Message Template Format 2
 	82 Application Interchange Profile
 	 	1980
 	94 Application File Locator (AFL)
 	 	180101002001010020040400080505010807070108030301

 */
                                    // this is old method with a fixed reading
                                    /*
                                    System.out.println("resultGpo1: " + bytesToHex(resultGpo1));
                                    System.out.println("resultGpo2: " + bytesToHex(resultGpo2));
                                    System.out.println("resultGpo3: " + bytesToHex(resultGpo3));
                                    System.out.println("resultGpo4: " + bytesToHex(resultGpo4));
                                    System.out.println("resultGpo5: " + bytesToHex(resultGpo5));
                                    System.out.println("resultGpo6: " + bytesToHex(resultGpo6));
                                    System.out.println("resultGpo7: " + bytesToHex(resultGpo7));
                                    System.out.println("resultGpo8: " + bytesToHex(resultGpo8));
                                    System.out.println("resultGpo9: " + bytesToHex(resultGpo9));
                                    System.out.println("resultGpoX: " + bytesToHex(resultGpoX));

                                    //String pdolNull = "80A8000002830000";
                                    String pdolEmpty = "80A800008308000000000000000000";
                                    String pdol4Bytes = "80A8000083040000000000";
                                    String pdol9Bytes = "80A80000830900000000000000000000";
                                    String pdolGirocard = "80A800008304A0C0167000";
                                    byte[] pdolCommand = hexToBytes(pdol4Bytes);
                                    System.out.println("#*# parse with pdol: " + bytesToHex(pdolCommand));
                                    byte[] resultGpo;
                                    resultGpo = nfc.transceive(pdolCommand);
                                    System.out.println("resultGpo: " + bytesToHex(resultGpo));
                                    byte[] resultGpoOk = checkResponse(resultGpo);
                                    //writeToUiAppend(etLog, "PDOL not found, response with empty PDOL: " + bytesToHex(resultGpoOk));
                                    //System.out.println("PDOL not found, response with empty PDOL: " + bytesToHex(resultGpoOk));

                                    //resultRaise = nfc.transceive(cmdRaiseSecurityLevel);
                                    //System.out.println("resultRaise: " + bytesToHex(resultRaise));

                                    //last thing to do was to read the record:
                                    // works with Voba Maestro Girocard, HVB Maestro Girocard, norisbank Maestro Girocard, comdirect Vpay Girocard, Postbank Vpay girocard
                                    byte[] readSelectedRecord = new byte[]{0x00, (byte) 0xb2, (byte) 0x05, (byte) 0x0c, 0x00};
                                    System.out.println("readSelectedRecord: " + bytesToHex(readSelectedRecord));
                                    byte[] resultRead;
                                    resultRead = nfc.transceive(readSelectedRecord);
                                    byte[] resultReadOk = checkResponse(resultRead);
                                    System.out.println("resultReadOk: " + bytesToHex(resultReadOk));

                                    if (resultReadOk != null) {
                                        BerTlvs tlvsAfl = parser.parse(resultReadOk);
                                        // 5a = Application Primary Account Number (PAN)
                                        // 5F34 = Application Primary Account Number (PAN) Sequence Number
                                        // 5F25  = Application Effective Date (card valid from)
                                        // 5F24 = Application Expiration Date
                                        BerTlv tag5a = tlvsAfl.find(new BerTag(0x5a));
                                        if (tag5a != null) {
                                            byte[] tag5aBytes = tag5a.getBytesValue();
                                            writeToUiAppend(etData, "data for AID " + aidSelectedForAnalyze + " (" + aidSelectedForAnalyzeName + ")");
                                            writeToUiAppend(etLog, "PAN: " + bytesToHex(tag5aBytes));
                                            writeToUiAppend(etData, "PAN: " + bytesToHex(tag5aBytes));
                                            System.out.println("PAN: " + bytesToHex(tag5aBytes));
                                        }
                                        BerTlv tag5f24 = tlvsAfl.find(new BerTag(0x5f, 0x24));
                                        if (tag5f24 != null) {
                                            byte[] tag5f24Bytes = tag5f24.getBytesValue();
                                            writeToUiAppend(etLog, "Exp. Date: " + bytesToHex(tag5f24Bytes));
                                            writeToUiAppend(etData, "Exp. Date (YYMMDD ??): " + bytesToHex(tag5f24Bytes));
                                            System.out.println("Exp. Date: " + bytesToHex(tag5f24Bytes));
                                        }

                                    }
*/

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
                                // this the mastercard code
                                writeToUiAppend(etLog, "PDOL not found");
                                String pdolNull = "80A8000002830000";
                                //String pdolEmpty = "80A800008308000000000000000000";
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
                                                    writeToUiAppend(etData, "data for AID " + aidSelectedForAnalyze + " (" + aidSelectedForAnalyzeName + ")");
                                                    writeToUiAppend(etLog, "PAN: " + bytesToHex(tag5aBytes));
                                                    writeToUiAppend(etData, "PAN: " + bytesToHex(tag5aBytes));
                                                    System.out.println("record " + iRecords + " PAN: " + bytesToHex(tag5aBytes));
                                                }
                                                BerTlv tag5f24 = tlvsAfl.find(new BerTag(0x5f, 0x24));
                                                if (tag5f24 != null) {
                                                    byte[] tag5f24Bytes = tag5f24.getBytesValue();
                                                    writeToUiAppend(etLog, "Exp. Date: " + bytesToHex(tag5f24Bytes));
                                                    writeToUiAppend(etData, "Exp. Date (YYMMDD): " + bytesToHex(tag5f24Bytes));
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
                }

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
        playDoublePing();
    }

    /**
     * reads all files on card using afl data
     * @param getProcessingOptions
     * @return a String with PAN and Expiration date if found
     */
    private String readPanFromFilesFromGpo(IsoDep nfc, byte[] getProcessingOptions) {
        String pan = "";
        String expirationDate = "";
        BerTlvParser parser = new BerTlvParser();

        // search for tag 0x94 = AFL
        BerTlvs tlvsGpo02 = parser.parse(getProcessingOptions);
        BerTlv tag94 = tlvsGpo02.find(new BerTag(0x94));
        if (tag94 != null) {
            byte[] tag94Bytes = tag94.getBytesValue();
            //writeToUiAppend(etLog, "AFL data: " + bytesToHex(tag94Bytes));
            //System.out.println("AFL data: " + bytesToHex(tag94Bytes));
            // split array by 4 bytes
            List<byte[]> tag94BytesList = divideArray(tag94Bytes, 4);
            int tag94BytesListLength = tag94BytesList.size();
            //writeToUiAppend(etLog, "tag94Bytes divided into " + tag94BytesListLength + " arrays");
            for (int i = 0; i < tag94BytesListLength; i++) {
                //writeToUiAppend(etLog, "get sfi + record for array " + i + " data: " + bytesToHex(tag94BytesList.get(i)));
                // get sfi from first byte, 2nd byte is first record, 3rd byte is last record, 4th byte is offline transactions
                byte[] tag94BytesListEntry = tag94BytesList.get(i);
                byte sfiOrg = tag94BytesListEntry[0];
                byte rec1 = tag94BytesListEntry[1];
                byte recL = tag94BytesListEntry[2];
                byte offl = tag94BytesListEntry[3]; // offline authorization
                //writeToUiAppend(etLog, "sfiOrg: " + sfiOrg + " rec1: " + ((int) rec1) + " recL: " + ((int) recL));
                int sfiNew = (byte) sfiOrg | 0x04; // add 4 = set bit 3
                //writeToUiAppend(etLog, "sfiNew: " + sfiNew + " rec1: " + ((int) rec1) + " recL: " + ((int) recL));

                // read records
                byte[] resultReadRecord = new byte[0];

                for (int iRecords = (int) rec1; iRecords <= (int) recL; iRecords++) {
                    //System.out.println("** for loop start " + (int) rec1 + " to " + (int) recL + " iRecords: " + iRecords);

                    //System.out.println("*#* readRecors iRecords: " + iRecords);
                    byte[] cmd = hexToBytes("00B2000400");
                    cmd[2] = (byte) (iRecords & 0x0FF);
                    cmd[3] |= (byte) (sfiNew & 0x0FF);
                    try {
                        resultReadRecord = nfc.transceive(cmd);
                        //writeToUiAppend(etLog, "readRecordCommand length: " + cmd.length + " data: " + bytesToHex(cmd));
                        byte[] resultReadRecordOk = checkResponse(resultReadRecord);
                        if (resultReadRecordOk != null) {
                            //if ((resultReadRecord[resultReadRecord.length - 2] == (byte) 0x90) && (resultReadRecord[resultReadRecord.length - 1] == (byte) 0x00)) {
                            //writeToUiAppend(etLog, "Success: read record result: " + bytesToHex(resultReadRecord));
                            //writeToUiAppend(etLog, "* parse AFL for entry: " + bytesToHex(tag94BytesListEntry) + " record: " + iRecords);
                            //System.out.println("* parse AFL for entry: " + bytesToHex(tag94BytesListEntry) + " record: " + iRecords);
                            // this is for complete parsing
                            //parseAflDataToTextView(resultReadRecord, etLog);
                            //System.out.println("parse " + iRecords + " result: " + bytesToHex(resultReadRecordOk));
                            // this is the shortened one
                            try {
                                BerTlvs tlvsAfl = parser.parse(resultReadRecordOk);
                                // todo there could be a 57 Track 2 Equivalent Data field as well
                                // 5a = Application Primary Account Number (PAN)
                                // 5F34 = Application Primary Account Number (PAN) Sequence Number
                                // 5F25  = Application Effective Date (card valid from)
                                // 5F24 = Application Expiration Date
                                BerTlv tag5a = tlvsAfl.find(new BerTag(0x5a));
                                if (tag5a != null) {
                                    byte[] tag5aBytes = tag5a.getBytesValue();
                                    //writeToUiAppend(etData, "Xdata for AID " + aidSelectedForAnalyze + " (" + aidSelectedForAnalyzeName + ")");
                                    //writeToUiAppend(etLog, "XPAN: " + bytesToHex(tag5aBytes));
                                    //writeToUiAppend(etData, "XPAN: " + bytesToHex(tag5aBytes));
                                    //System.out.println("record " + iRecords + " XPAN: " + bytesToHex(tag5aBytes));
                                    pan = bytesToHex(tag5aBytes);
                                }
                                BerTlv tag5f24 = tlvsAfl.find(new BerTag(0x5f, 0x24));
                                if (tag5f24 != null) {
                                    byte[] tag5f24Bytes = tag5f24.getBytesValue();
                                    //writeToUiAppend(etLog, "XExp. Date: " + bytesToHex(tag5f24Bytes));
                                    //writeToUiAppend(etData, "XExp. Date (YYMMDD): " + bytesToHex(tag5f24Bytes));
                                    //System.out.println("Exp. Date: " + bytesToHex(tag5f24Bytes));
                                    expirationDate = bytesToHex(tag5f24Bytes);
                                } else {
                                    //System.out.println("record: " + iRecords + " Tag 5F24 not found");
                                }
                            } catch (ArrayIndexOutOfBoundsException e) {
                                //System.out.println("ERROR: ArrayOutOfBoundsException: " + e.getMessage());
                            }
                        } else {
                            //writeToUiAppend(etLog, "ERROR: read record failed, result: " + bytesToHex(resultReadRecord));
                            resultReadRecord = new byte[0];
                        }
                    } catch (IOException e) {
                        //throw new RuntimeException(e);
                        //return "";
                    }

                }
            } // for (int i = 0; i < tag94BytesListLength; i++) { // = number of records belong to this afl
        }
        return pan + "|" + expirationDate;
    }

    /**
     * gets the byte value of a tag from tranceive response
     *
     * @param data
     * @param search
     * @return
     */
    private byte[] getTagValueFromResult(byte[] data, byte... search) {
        int argumentsLength = search.length;
        if (argumentsLength < 1) return null;
        if (argumentsLength > 2) return null;
        if (data.length > 253) return null;
        BerTlvParser parser = new BerTlvParser();
        BerTlvs tlvDatas = parser.parse(data);
        BerTlv tag;
        if (argumentsLength == 1) {
            tag = tlvDatas.find(new BerTag(search[0]));
        } else {
            tag = tlvDatas.find(new BerTag(search[0], search[1]));
        }
        byte[] tagBytes;
        if (tag == null) {
            return null;
        } else {
            return tag.getBytesValue();
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


    private void clearAllData() {
        maxTransceiveLength = 0;
        atqa = null;
        sak = 0;
        dsfId = 0;
        responseFlags = 0;
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