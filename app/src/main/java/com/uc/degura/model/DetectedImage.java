package com.uc.degura.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.uc.degura.tflite.Classifier;

import java.util.List;

public class DetectedImage implements Parcelable {

    private List<Classifier.Recognition> list_eye_result;
    private List<Classifier.Recognition> list_gill_result;

    private  List<Uri> list_cropped_eye_uri;
    private  List<Uri> list_cropped_gill_uri;

    private Uri fish_eye_uri;
    private Uri fish_gill_uri;

    public DetectedImage(List<Classifier.Recognition> list_eye_result, List<Classifier.Recognition> list_gill_result, List<Uri> list_cropped_eye_uri, List<Uri> list_cropped_gill_uri, Uri fish_eye_uri, Uri fish_gill_uri) {
        this.list_eye_result = list_eye_result;
        this.list_gill_result = list_gill_result;
        this.list_cropped_eye_uri = list_cropped_eye_uri;
        this.list_cropped_gill_uri = list_cropped_gill_uri;
        this.fish_eye_uri = fish_eye_uri;
        this.fish_gill_uri = fish_gill_uri;
    }

    protected DetectedImage(Parcel in) {
        list_cropped_eye_uri = in.createTypedArrayList(Uri.CREATOR);
        list_cropped_gill_uri = in.createTypedArrayList(Uri.CREATOR);
        fish_eye_uri = in.readParcelable(Uri.class.getClassLoader());
        fish_gill_uri = in.readParcelable(Uri.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(list_cropped_eye_uri);
        dest.writeTypedList(list_cropped_gill_uri);
        dest.writeParcelable(fish_eye_uri, flags);
        dest.writeParcelable(fish_gill_uri, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DetectedImage> CREATOR = new Creator<DetectedImage>() {
        @Override
        public DetectedImage createFromParcel(Parcel in) {
            return new DetectedImage(in);
        }

        @Override
        public DetectedImage[] newArray(int size) {
            return new DetectedImage[size];
        }
    };

    public List<Classifier.Recognition> getList_eye_result() {
        return list_eye_result;
    }

    public void setList_eye_result(List<Classifier.Recognition> list_eye_result) {
        this.list_eye_result = list_eye_result;
    }

    public List<Classifier.Recognition> getList_gill_result() {
        return list_gill_result;
    }

    public void setList_gill_result(List<Classifier.Recognition> list_gill_result) {
        this.list_gill_result = list_gill_result;
    }

    public List<Uri> getList_cropped_eye_uri() {
        return list_cropped_eye_uri;
    }

    public void setList_cropped_eye_uri(List<Uri> list_cropped_eye_uri) {
        this.list_cropped_eye_uri = list_cropped_eye_uri;
    }

    public List<Uri> getList_cropped_gill_uri() {
        return list_cropped_gill_uri;
    }

    public void setList_cropped_gill_uri(List<Uri> list_cropped_gill_uri) {
        this.list_cropped_gill_uri = list_cropped_gill_uri;
    }

    public Uri getFish_eye_uri() {
        return fish_eye_uri;
    }

    public void setFish_eye_uri(Uri fish_eye_uri) {
        this.fish_eye_uri = fish_eye_uri;
    }

    public Uri getFish_gill_uri() {
        return fish_gill_uri;
    }

    public void setFish_gill_uri(Uri fish_gill_uri) {
        this.fish_gill_uri = fish_gill_uri;
    }
}
