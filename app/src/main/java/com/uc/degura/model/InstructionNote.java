package com.uc.degura.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class InstructionNote implements Parcelable {

    private int images;

    private String instruction;

    public InstructionNote(int images, String instruction) {
        this.images = images;
        this.instruction = instruction;
    }

    protected InstructionNote(Parcel in) {
        images = in.readInt();
        instruction = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(images);
        dest.writeString(instruction);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<InstructionNote> CREATOR = new Creator<InstructionNote>() {
        @Override
        public InstructionNote createFromParcel(Parcel in) {
            return new InstructionNote(in);
        }

        @Override
        public InstructionNote[] newArray(int size) {
            return new InstructionNote[size];
        }
    };

    public int getImages() {
        return images;
    }

    public String getInstruction() {
        return instruction;
    }
}
