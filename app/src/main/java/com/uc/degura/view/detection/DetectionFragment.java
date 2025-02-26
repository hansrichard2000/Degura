package com.uc.degura.view.detection;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.uc.degura.R;
import com.uc.degura.env.ImageUtils;
import com.uc.degura.env.Logger;
import com.uc.degura.env.Utils;
import com.uc.degura.model.DetectedImage;
import com.uc.degura.tflite.Classifier;
import com.uc.degura.tflite.YoloV4Classifier;
import com.uc.degura.tracking.MultiBoxTracker;

import java.io.File;
import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class DetectionFragment extends Fragment {

    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.55f;

    ImageView slider1;
    ImageView slider2;
    ViewPager2 fish_image_slider;
    TextView fish_image_txt;
    Button btn_detect;
    Button btn_delete_img;

    Dialog progressDialog;

    private FishImageAdapter fishImageAdapter;

    Uri cropped_eye;
    Uri cropped_gill;

    private static final String TAG = "DetectionFragment";

    public DetectionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detection, container, false);
    }

    private static final Logger LOGGER = new Logger();

    public static final int TF_OD_API_INPUT_SIZE = 608;

    private static final boolean TF_OD_API_IS_QUANTIZED = true;

    private static final String TF_OD_API_MODEL_FILE = "yolov4-tiny-fish-partv4-608-fp16.tflite";

    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/coco.txt";

    // Minimum detection confidence to track a detection.

    private static final boolean MAINTAIN_ASPECT = false;

    private Integer sensorOrientation = 90;

    private Classifier detector;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;
    private MultiBoxTracker tracker;
    private OverlayView trackingOverlay;

    protected int previewWidth = 0;
    protected int previewHeight = 0;

    Uri fish_eye_uri;

    Uri fish_gill_uri;

    RectF location_eye_max;

    RectF location_gill_max;

    Float confidence_eye_max = 0f;

    Float confidence_gill_max = 0f;

    private Bitmap fish_eye_bitmap;
    private Bitmap fish_gill_bitmap;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        slider1 = view.findViewById(R.id.page1);
        slider2 = view.findViewById(R.id.page2);
        fish_image_slider = view.findViewById(R.id.fish_image_slider);
        fish_image_txt = view.findViewById(R.id.fish_image_txt);
        btn_detect = view.findViewById(R.id.btn_detect);
        btn_delete_img = view.findViewById(R.id.btn_delete_img);

        fish_eye_uri = getArguments().getParcelable("fish_eye");
        fish_gill_uri = getArguments().getParcelable("fish_gill");

        Log.d(TAG, "Fish Eye Uri Debug: "+fish_eye_uri.toString());
        Log.d(TAG, "Fish Gill Uri Debug: "+fish_gill_uri.toString());

        fish_eye_bitmap = ImageUtils.getBitmap(this.getContext(), fish_eye_uri);
        fish_gill_bitmap = ImageUtils.getBitmap(this.getContext(), fish_gill_uri);

//        fish_eye_bitmap = Utils.processBitmap(fish_eye_bitmap, TF_OD_API_INPUT_SIZE);
//
//        fish_gill_bitmap = Utils.processBitmap(fish_gill_bitmap, TF_OD_API_INPUT_SIZE);

        List<Bitmap> fish_images_list = Arrays.asList(fish_eye_bitmap, fish_gill_bitmap);

        fishImageAdapter = new FishImageAdapter(getActivity(), fish_images_list);

        fish_image_slider.setAdapter(fishImageAdapter);

        fish_image_slider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                changeIndicatorColor();
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                changeIndicatorColor();
            }
        });

        btn_detect.setOnClickListener(v -> {
            progressDialog = new Dialog(getActivity(), R.style.DeguraLoadingTheme);
            View loadingView = LayoutInflater.from(getContext()).inflate(R.layout.loading_screen, null);
            WindowManager.LayoutParams params = progressDialog.getWindow().getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT; // set as full width
            params.height = WindowManager.LayoutParams.MATCH_PARENT;// set as full heiggt
            progressDialog.setContentView(loadingView);
            progressDialog.getWindow().setBackgroundDrawableResource(R.color.transparent_black);
            progressDialog.getWindow().setGravity(Gravity.CENTER);
            progressDialog.show();
            Handler handler = new Handler();
            new Thread(() -> {
                Log.d(TAG, "Recognize Fish Eye Bitmap Debug: "+fish_eye_bitmap.toString());

                handler.postDelayed(() -> {
                    final List<Classifier.Recognition> fish_eye_results = detector.recognizeImage(fish_eye_bitmap);
                    final List<Classifier.Recognition> fish_gill_results = detector.recognizeImage(fish_gill_bitmap);
                    handler.post(() -> handleResult(fish_eye_bitmap, fish_gill_bitmap, fish_eye_results, fish_gill_results));
                }, 1000);

            }).start();


            new Handler().postDelayed(() -> progressDialog.dismiss(), 1500);
//            progressDialog.setOnDismissListener(dialog -> {
//
//                Handler handler = new Handler(Looper.getMainLooper()){
//                    @Override
//                    public void handleMessage(@NonNull Message msg) {
//                        super.handleMessage(msg);
//                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
//                        alertBuilder.setTitle("Peringatan!");
//                        alertBuilder.setMessage("Spesifikasi smartphone Anda belum kompatibel untuk menjalankan aplikasi ini. Anda bisa menggunakan aplikasi Degura Lite.");
//                        alertBuilder.setCancelable(true);
//
//                        alertBuilder.setPositiveButton("Oke", (DialogInterface.OnClickListener) (dialog1, which) -> {
//                            NavDirections action;
//                            action = DetectionFragmentDirections.actionDetectionFragmentToFishEyeFragment();
//                            Navigation.findNavController(v).navigate(action);
//                        });
//
//                        // Create the Alert dialog
//                        AlertDialog alertDialog = alertBuilder.create();
//                        // Show the Alert Dialog box
//                        alertDialog.show();
//                    }
//                };
//
//                new Thread(() -> {
//                    Log.d(TAG, "Recognize Fish Eye Bitmap Debug: "+fish_eye_bitmap.toString());
//
////                    try{
//                        final List<Classifier.Recognition> fish_eye_results = detector.recognizeImage(fish_eye_bitmap);
//                        final List<Classifier.Recognition> fish_gill_results = detector.recognizeImage(fish_gill_bitmap);
//                        handler.post(() -> handleResult(fish_eye_bitmap, fish_gill_bitmap, fish_eye_results, fish_gill_results));
////                    }catch (Exception e){
////                        e.printStackTrace();
////                        Message message = handler.obtainMessage();
////                        message.sendToTarget();
////                    }
//
//                }).start();
//            });

//            new Handler().postDelayed(() -> progressDialog.dismiss(), 1000);

            fish_eye_bitmap = Utils.processBitmap(fish_eye_bitmap, TF_OD_API_INPUT_SIZE);

            fish_gill_bitmap = Utils.processBitmap(fish_gill_bitmap, TF_OD_API_INPUT_SIZE);

            initBox();

        });

        btn_delete_img.setOnClickListener(v -> {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme);
            alertBuilder.setTitle("Peringatan!");
            alertBuilder.setMessage("Anda yakin ingin membuang kedua gambar yang sudah Anda ambil?");
            alertBuilder.setCancelable(true);

            alertBuilder.setPositiveButton("Ya", (DialogInterface.OnClickListener) (dialog, which) -> {
                NavDirections action;
                action = DetectionFragmentDirections.actionDetectionFragmentToFishEyeFragment();
                Navigation.findNavController(v).navigate(action);
            });

            alertBuilder.setNegativeButton("Tidak", (DialogInterface.OnClickListener) (dialog, which) -> {
                // If user click no then dialog box is canceled.
                dialog.cancel();
            });

            // Create the Alert dialog
            AlertDialog alertDialog = alertBuilder.create();

            // Show the Alert Dialog box
            alertDialog.show();

//            deleteCache(getContext());
        });

    }

    private void changeIndicatorColor(){

        int currentItem = fish_image_slider.getCurrentItem();

        switch (currentItem){

            case 1 :
                fish_image_txt.setText(R.string.fish_gill_text);
                slider1.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent_white));
                slider2.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.degura_white));
                break;

            default:
                fish_image_txt.setText(R.string.fish_eye_text);
                slider1.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.degura_white));
                slider2.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent_white));

                break;
        }
    }

    private static void deleteCache(Context context){
        try {
            File dir = new File(context.getCacheDir(), "deguraImages");
            deleteDir(dir);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    private void initBox() {
        previewHeight = TF_OD_API_INPUT_SIZE;
        previewWidth = TF_OD_API_INPUT_SIZE;

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        tracker = new MultiBoxTracker(getContext());
        trackingOverlay = getActivity().findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                canvas -> tracker.draw(canvas));

        tracker.setFrameConfiguration(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, sensorOrientation);

        try {
            detector =
                    YoloV4Classifier.create(
                            getActivity().getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_IS_QUANTIZED);
        } catch (final IOException e) {
            e.printStackTrace();
            LOGGER.e(e, "Exception initializing classifier!");
            Toast toast =
                    Toast.makeText(
                            getContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            getActivity().finish();
        }

    }

    List<Uri> list_cropped_eye = new ArrayList<>();
    List<Uri> list_cropped_gill = new ArrayList<>();

//    int eye_position = 0;
//    int gill_position = 0;
    int part_position = 0;

    private void handleResult(Bitmap bitmap_eye, Bitmap bitmap_gill, List<Classifier.Recognition> eye_results, List<Classifier.Recognition> gill_results) {
        final Canvas eye_canvas = new Canvas(bitmap_eye);
        final Canvas gill_canvas = new Canvas(bitmap_gill);
        final Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.0f);
        final Paint textPaint = new Paint();
        textPaint.setColor(Color.RED);
        textPaint.setTextSize(16);

        for (final Classifier.Recognition eye_result : eye_results) {
            final RectF location = eye_result.getLocation();
            final String imageTitle = eye_result.getTitle();
            final Float confidence = eye_result.getConfidence();
            final int detectedClass = eye_result.getDetectedClass();

            if (location != null && eye_result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API && eye_result.getTitle().equals("mata")) {
//                part_position += 1;
                if (eye_result.getConfidence() > confidence_eye_max){
                    confidence_eye_max = confidence;
                    location_eye_max = location;

                    Log.d(TAG, "handleResultLocation: "+location);
                    Log.d(TAG, "handleResultTitle: "+imageTitle);
                    Log.d(TAG, "handleResultConfidence: "+confidence);
                    Log.d(TAG, "handleResultClass: "+detectedClass);
                    Log.d(TAG, "handleResultCroppedImg: "+list_cropped_eye);
                }

            }
        }

        for (final Classifier.Recognition gill_result : gill_results) {
            final RectF location = gill_result.getLocation();
            final String imageTitle = gill_result.getTitle();
            final Float confidence = gill_result.getConfidence();
            final int detectedClass = gill_result.getDetectedClass();

            if (location != null && gill_result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API && gill_result.getTitle().equals("insang")) {
//                part_position += 1;
                if (gill_result.getConfidence() > confidence_gill_max){
                    confidence_gill_max = confidence;
                    location_gill_max = location;
                    Log.d(TAG, "handleResultLocation: "+location);
                    Log.d(TAG, "handleResultTitle: "+imageTitle);
                    Log.d(TAG, "handleResultConfidence: "+confidence);
                    Log.d(TAG, "handleResultClass: "+detectedClass);
                    Log.d(TAG, "handleResultCroppedImg: "+list_cropped_gill);
                }

            }
        }


//        bitmap_eye = ImageUtils.getBitmap(getContext(), cropped_eye);
//
//        bitmap_gill = ImageUtils.getBitmap(getContext(), cropped_gill);

//        List<Bitmap> new_fish_images_list = Arrays.asList(bitmap_eye, bitmap_gill);
//
//        fishImageAdapter = new FishImageAdapter(getActivity(), new_fish_images_list);
//
//        fish_image_slider.setAdapter(fishImageAdapter);

        if (location_eye_max != null){
            cropped_eye = ImageUtils.cropImage(bitmap_eye, getContext(), "cropped_fish_eye"+location_eye_max+".jpg", location_eye_max);
            list_cropped_eye.add(cropped_eye);
            eye_canvas.drawRect(location_eye_max, paint);
            eye_canvas.drawText(confidence_eye_max.toString(), location_eye_max.left, location_eye_max.top-5f, textPaint);
        }

        if (location_gill_max != null){
            cropped_gill = ImageUtils.cropImage(bitmap_gill, getContext(), "cropped_fish_gill"+location_gill_max+".jpg", location_gill_max);
            list_cropped_gill.add(cropped_gill);
            gill_canvas.drawRect(location_gill_max, paint);
            gill_canvas.drawText(confidence_gill_max.toString(), location_gill_max.left, location_gill_max.top-5f, textPaint);
        }

        fish_eye_uri = ImageUtils.saveImage(bitmap_eye, getContext(), "resized_fish_eye.jpg");
        fish_gill_uri = ImageUtils.saveImage(bitmap_gill, getContext(), "resized_fish_gill.jpg");

//        fish_eye_uri_test = ImageUtils.saveImage(bitmap_eye, getContext(), "test_fish_eye.jpg");
//        list_cropped_eye.add(fish_eye_uri);

        DetectedImage detectedImage = new DetectedImage(eye_results, gill_results, list_cropped_eye, list_cropped_gill, fish_eye_uri, fish_gill_uri);

        NavDirections action;
        action = DetectionFragmentDirections.actionDetectionFragmentToResultsFragment(detectedImage);
        Navigation.findNavController(getView()).navigate(action);
    }

}