package de.androidcrypto.aaaandroidexamples.nfccreditcards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.androidcrypto.aaaandroidexamples.BinaryUtils;

public class TagValues {

    private List<EmvTag> emvTagList = new ArrayList<>();

    private final EmvTag t00 = setTag(new byte[]{(byte) 0x00}, "no entry or not analyzed", true);
    private final EmvTag t6f = setTag(new byte[]{(byte) 0x6f}, "File Control Information (FCI) Template", true);
    private final EmvTag t84 = setTag(new byte[]{(byte) 0x84}, "Dedicated File (DF) Name", true);
    private final EmvTag ta5 = setTag(new byte[]{(byte) 0xa5}, "File Control Information (FCI) Proprietary Template", true);
    private final EmvTag t4f = setTag(new byte[]{(byte) 0x4f}, "Application Identifier (AID) - card", true);

    public TagValues() {
        // empty constructor to fill the emvTagList
    }

    public List<EmvTag> getEmvTagList() {
        return emvTagList;
    }

    public EmvTag getEmvTagName(byte[] tagByte) {
        System.out.println(BinaryUtils.bytesToHex(tagByte));
        for (int i = 0; i < emvTagList.size(); i++) {
            EmvTag emvTag = emvTagList.get(i);
            if (Arrays.equals(emvTag.getTagByte(), tagByte)) {
                return emvTag;
            }
        }
        return t00; // default, entry not found
    }


    private EmvTag setTag(byte[] tagByte, String tagName, boolean isBinary) {
        EmvTag emvTag = new EmvTag(tagByte, tagName, isBinary);
        emvTagList.add(emvTag);
        return emvTag;
    }
}
