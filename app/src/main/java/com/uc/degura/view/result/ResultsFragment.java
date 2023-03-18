package com.uc.degura.view.result;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
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
import com.uc.degura.env.Utils;
import com.uc.degura.ml.Model;
import com.uc.degura.tflite.Classifier;
import com.uc.degura.view.detection.DetectionFragmentDirections;
import com.uc.degura.view.detection.FishImageAdapter;
import com.uc.degura.view.main.FishEyeFragmentDirections;
import com.uc.degura.view.main.FishGillFragmentDirections;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ResultsFragment extends Fragment {

    @BindView(R.id.eye_status)
    TextView eye_status;

    @BindView(R.id.gill_status)
    TextView gill_status;

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

    String classifyResult;

    Bitmap detected_eye_bitmap;

    Bitmap detected_gill_bitmap;

    int imageSize = 32;

    Dialog progressDialog;

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

        progressDialog = new Dialog(getActivity(), R.style.DeguraLoadingTheme);
        View loadingView = LayoutInflater.from(getContext()).inflate(R.layout.loading_screen, null);
        WindowManager.LayoutParams params = progressDialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT; // set as full width
        params.height = WindowManager.LayoutParams.MATCH_PARENT;// set as full heiggt
        progressDialog.setContentView(loadingView);
        progressDialog.getWindow().setBackgroundDrawableResource(R.color.transparent_black);
        progressDialog.getWindow().setGravity(Gravity.CENTER);
        progressDialog.show();
        progressDialog.setOnDismissListener(dialog -> {
            //            classifyImage(detected_eye_bitmap);
//            eye_status.setText(classifyResult);
//
//            classifyImage(detected_gill_bitmap);
//            gill_status.setText(classifyResult);
        });

        new Handler().postDelayed(() -> progressDialog.dismiss(), 2500);

        Uri cropped_fish_eye_uri = getArguments().getParcelable("detected_eye");
        Uri cropped_fish_gill_uri = getArguments().getParcelable("detected_gill");

        Log.d(TAG, "onViewCreatedFishGill: " + cropped_fish_eye_uri);
        Log.d(TAG, "onViewCreatedFishGill: " + cropped_fish_gill_uri);

        detected_eye_bitmap = ImageUtils.getBitmap(this.getContext(), cropped_fish_eye_uri);
        detected_gill_bitmap = ImageUtils.getBitmap(this.getContext(), cropped_fish_gill_uri);

        Log.d(TAG, "onViewCreatedEyeBitmap: "+detected_eye_bitmap);
        Log.d(TAG, "onViewCreatedGillBitmap: "+detected_gill_bitmap);

        List<Bitmap> fish_results_list = Arrays.asList(detected_eye_bitmap, detected_gill_bitmap);

        resultsAdapter = new ResultsAdapter(getActivity(), fish_results_list);

        view_pager_result.setAdapter(resultsAdapter);

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

    private void classifyImage(Bitmap image) {

        try {
            Model model = Model.newInstance(getContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 32, 32, 3}, DataType.FLOAT32);
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
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            // find the index of the class with the biggest confidence.
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }
            String[] classes = {"highly-fresh", "fresh", "not-fresh"};
            classifyResult = classes[maxPos];

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


}