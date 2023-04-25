package com.uc.degura.view.result;

import static com.uc.degura.view.detection.DetectionFragment.MINIMUM_CONFIDENCE_TF_OD_API;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.uc.degura.R;
import com.uc.degura.env.ImageUtils;
import com.uc.degura.ml.ModelFishv7132;
import com.uc.degura.model.DetectedImage;
import com.uc.degura.tflite.Classifier;
import com.uc.degura.view.detection.FishImageAdapter;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ResultsFragment extends Fragment {

    @BindView(R.id.result_image_txt)
    TextView result_image_txt;

    @BindView(R.id.page1result)
    ImageView page1result;

    @BindView(R.id.page2result)
    ImageView page2result;

    @BindView(R.id.back_from_result)
    Button back_from_result;

    @BindView(R.id.view_pager_result)
    ViewPager2 view_pager_result;

    @BindView(R.id.recycler_fish_freshness)
    RecyclerView recyclerView;

    String classifyResult;

    String classifyEyeResult;

    String classifyGillResult;

    Uri original_fish_eye_uri;

    Uri original_fish_gill_uri;

    List<Uri> cropped_fish_eye_uri;

    List<Uri> cropped_fish_gill_uri;

    Bitmap original_eye_bitmap;

    Bitmap original_gill_bitmap;

    Bitmap detected_eye_bitmap;

    Bitmap detected_gill_bitmap;

    Bitmap cropped_eye_bitmap;
    Bitmap cropped_gill_bitmap;

    int imageSize = 416;

    Dialog progressDialog;

    DetectedImage detectedImage;

    ResultsImageAdapter resultsImageAdapter;

    ResultsAdapter resultsAdapter;

    private static final String TAG = "ResultsFragment";

    public ResultsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_results, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        detectedImage = getArguments().getParcelable("detected_images");

        original_fish_eye_uri = detectedImage.getFish_eye_uri();
        original_fish_gill_uri = detectedImage.getFish_gill_uri();

        Log.d(TAG, "onViewCreatedFishEyeUri: " + original_fish_eye_uri);
        Log.d(TAG, "onViewCreatedFishGillUri: " + original_fish_gill_uri);

        cropped_fish_eye_uri = detectedImage.getList_cropped_eye_uri();
        cropped_fish_gill_uri = detectedImage.getList_cropped_gill_uri();

        Log.d(TAG, "onViewCroppedFishEyeUri: " + cropped_fish_eye_uri);
        Log.d(TAG, "onViewCroppedFishGillUri: " + cropped_fish_gill_uri);

        original_eye_bitmap = ImageUtils.getBitmap(this.getContext(), original_fish_eye_uri);
        original_gill_bitmap = ImageUtils.getBitmap(this.getContext(), original_fish_gill_uri);

        Log.d(TAG, "onViewCreatedEyeBitmap: "+original_eye_bitmap);
        Log.d(TAG, "onViewCreatedGillBitmap: "+original_gill_bitmap);

//        cropped_eye_bitmap = ImageUtils.getBitmap(this.getContext(), cropped_fish_eye_uri.get(0));
//        cropped_gill_bitmap = ImageUtils.getBitmap(this.getContext(), cropped_fish_gill_uri.get(0));

        progressDialog = new Dialog(getActivity(), R.style.DeguraLoadingTheme);
        View loadingView = LayoutInflater.from(getContext()).inflate(R.layout.loading_screen, null);
        WindowManager.LayoutParams params = progressDialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT; // set as full width
        params.height = WindowManager.LayoutParams.MATCH_PARENT;// set as full heiggt
        progressDialog.setContentView(loadingView);
        progressDialog.getWindow().setBackgroundDrawableResource(R.color.transparent_black);
        progressDialog.getWindow().setGravity(Gravity.CENTER);
        progressDialog.show();
//        progressDialog.setOnDismissListener(dialog -> {
//        });

        Handler handler = new Handler();

        new Thread(() -> {

            handler.post(() -> handleResult(cropped_fish_eye_uri, cropped_fish_gill_uri));

        }).start();

        new Handler().postDelayed(() -> progressDialog.dismiss(), 1500);

        List<Bitmap> fish_results_list = Arrays.asList(original_eye_bitmap, original_gill_bitmap);

        resultsImageAdapter = new ResultsImageAdapter(getActivity(), fish_results_list);

        view_pager_result.setAdapter(resultsImageAdapter);

        view_pager_result.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
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

        back_from_result.setOnClickListener(v -> {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
            alertBuilder.setTitle("Peringatan!");
            alertBuilder.setMessage("Anda akan kembali ke halaman pertama dan membuang kedua gambar yang sudah dideteksi.");
            alertBuilder.setCancelable(true);

            alertBuilder.setPositiveButton("Ya", (DialogInterface.OnClickListener) (dialog, which) -> {
                NavDirections actions;
                actions = ResultsFragmentDirections.actionResultsFragmentToFishEyeFragment();
                Navigation.findNavController(v).navigate(actions);
            });

            alertBuilder.setNegativeButton("Batal", (DialogInterface.OnClickListener) (dialog, which) -> {
                // If user click no then dialog box is canceled.
                dialog.cancel();
            });

            // Create the Alert dialog
            AlertDialog alertDialog = alertBuilder.create();
            // Show the Alert Dialog box
            alertDialog.show();

        });

    }

    private void changeIndicatorColor(){

        int currentItem = view_pager_result.getCurrentItem();

        switch (currentItem){

            case 1 :
                result_image_txt.setText(R.string.fish_gill_text);
                page1result.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent_white));
                page2result.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.degura_white));
                break;

            default:
                result_image_txt.setText(R.string.fish_eye_text);
                page1result.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.degura_white));
                page2result.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent_white));
                break;
        }
    }

    List<String> list_part_classifier = new ArrayList<>();

    LinearLayoutManager linearLayoutManager;

    private void handleResult(List<Uri> cropped_eye_uri, List<Uri> cropped_gill_uri) {

        for (int i = 0; i < cropped_eye_uri.size(); i++) {
            detected_eye_bitmap = ImageUtils.getBitmap(getContext(), cropped_eye_uri.get(i));
            Log.d(TAG, "handleResultBitmapEye: "+detected_eye_bitmap);
            classifyImage(detected_eye_bitmap);
            list_part_classifier.add(classifyResult);
            Log.d(TAG, "handleResultFirstStep: "+list_part_classifier);
        }


        for (int j = 0; j < cropped_gill_uri.size(); j++) {
            detected_gill_bitmap = ImageUtils.getBitmap(getContext(), cropped_gill_uri.get(j));
            Log.d(TAG, "handleResultBitmapGill: "+detected_gill_bitmap);
            classifyImage(detected_gill_bitmap);
            list_part_classifier.add(classifyResult);
        }

        if (list_part_classifier.isEmpty()){
            list_part_classifier.add("x");
        }

        Log.d(TAG, "handleResultListClassification: "+list_part_classifier);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        resultsAdapter = new ResultsAdapter(getActivity());
        resultsAdapter.setResult_list(list_part_classifier);
        recyclerView.setAdapter(resultsAdapter);

    }

    private void classifyImage(Bitmap image) {

        try {
            ModelFishv7132 model = ModelFishv7132.newInstance(getContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, imageSize, imageSize, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;

            //iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 1));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 1));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 1));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            ModelFishv7132.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            // find the index of the class with the biggest confidence.
            int maxPos = 0;
            float maxConfidence = 0.1f;
            for (int i = 0; i < confidences.length; i++) {
                Log.d(TAG, "classifyImageCondifence: "+confidences[i]);
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }
            String[] classes = {"eye-fresh", "eye-non-fresh", "gill-fresh", "gill-non-fresh"};
            classifyResult = classes[maxPos];

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            Log.e(TAG, "classifyImage: ", e);
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
            alertBuilder.setTitle("Peringatan!");
            alertBuilder.setMessage("Spesifikasi perangkat Anda belum bisa menggunakan aplikasi ini untuk melakukan deteksi kesegaran ikan Gurami. Anda dapat memakai degura lite.");
            alertBuilder.setCancelable(true);
            alertBuilder.setNegativeButton("Oke", (DialogInterface.OnClickListener) (dialog, which) -> {
                // If user click no then dialog box is canceled.
                dialog.cancel();
            });

            // Create the Alert dialog
            AlertDialog alertDialog = alertBuilder.create();
            // Show the Alert Dialog box
            alertDialog.show();
        }

    }
}