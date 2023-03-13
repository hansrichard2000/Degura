package com.uc.degura.view.detection;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.uc.degura.R;

import java.io.IOException;
import java.sql.Array;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetectionFragment extends Fragment {

    @BindView(R.id.page1)
    ImageView slider1;

    @BindView(R.id.page2)
    ImageView slider2;

    @BindView(R.id.fish_image_slider)
    ViewPager2 fish_image_slider;

    @BindView(R.id.fish_image_txt)
    TextView fish_image_txt;

    private FishImageAdapter fishImageAdapter;

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        Uri fish_eye_uri = getArguments().getParcelable("fish_eye");
        Uri fish_gill_uri = getArguments().getParcelable("fish_gill");

        Log.d(TAG, "Fish Eye Uri Debug: "+fish_eye_uri.toString());
        Log.d(TAG, "Fish Gill Uri Debug: "+fish_gill_uri.toString());

        Bitmap fish_eye_bitmap = getBitmap(fish_eye_uri);
        Bitmap fish_gill_bitmap = getBitmap(fish_gill_uri);

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


    }

    private Bitmap getBitmap(Uri imageUri){
        Bitmap image = null;
        try {
            image = MediaStore.Images.Media.getBitmap(this.getContext().getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    private void changeIndicatorColor(){

        int currentItem = fish_image_slider.getCurrentItem();

        switch (currentItem){

            case 1 :
                fish_image_txt.setText(R.string.fish_gill_text);
                slider1.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.black));
                slider2.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.degura_white));
                break;

            default:
                fish_image_txt.setText(R.string.fish_eye_text);
                slider1.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.degura_white));
                slider2.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.black));
                break;
        }
    }
}