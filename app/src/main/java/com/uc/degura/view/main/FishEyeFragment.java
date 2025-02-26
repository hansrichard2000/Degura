package com.uc.degura.view.main;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.uc.degura.R;
import com.uc.degura.env.ImageUtils;
import com.uc.degura.model.InstructionNote;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;

public class FishEyeFragment extends Fragment {

    Button btn_camera_eye;

    Button btn_gallery_eye;

    ViewPager2 fish_eye_slider;

    ImageView slider1;

    ImageView slider2;

    ImageView slider3;

    ImageView slider4;

    ImageView slider5;

    private FishEyeAdapter fishEyeAdapter;

    private long backPressedTime;

    private Toast backToast;

    private static final String TAG = "FishEyeFragment";

    public FishEyeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (backPressedTime + 2000 > System.currentTimeMillis() ){
                    backToast.cancel();
                    getActivity().finish();
                }
                else{
                    backToast= Toast.makeText(getContext(), "Press back again to exit", Toast.LENGTH_SHORT);
                    backToast.show();
                }
                backPressedTime = System.currentTimeMillis();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fish_eye, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btn_camera_eye = view.findViewById(R.id.btn_camera_eye);
        btn_gallery_eye = view.findViewById(R.id.btn_gallery_eye);
        fish_eye_slider = view.findViewById(R.id.fish_eye_slider);
        slider1 = view.findViewById(R.id.eye_page1);
        slider2 = view.findViewById(R.id.eye_page2);
        slider3 = view.findViewById(R.id.eye_page3);
        slider4 = view.findViewById(R.id.eye_page4);
        slider5 = view.findViewById(R.id.eye_page5);

        InstructionNote instruction1 = new InstructionNote(0, "", "Swipe ke kiri untuk membaca petunjuk cara mengambil gambar mata ikan Gurami");
        InstructionNote instruction2 = new InstructionNote(R.drawable.slide_mata_1, "Pastikan gambar yang diambil adalah gambar sebuah mata ikan Gurami.", "");
        InstructionNote instruction3 = new InstructionNote(R.drawable.slide_mata_2, "Ambil gambar mata ikan dengan jarak +- 15 cm dari kamera", "");
        InstructionNote instruction4 = new InstructionNote(R.drawable.slide_mata_3, "Pastikan hasil dari pengambilan gambar memiliki pencahayaan yang baik dan terlihat jelas", "");
        InstructionNote instruction5 = new InstructionNote(R.drawable.slide_mata_4, "Sumber gambar dapat Anda ambil dari galeri hp Anda atau melakukan pengambilan gambar langsung", "");

        List<InstructionNote> instruction_list = Arrays.asList(instruction1, instruction2, instruction3, instruction4, instruction5);

        fishEyeAdapter = new FishEyeAdapter(getContext());
        fishEyeAdapter.setInstructionList(instruction_list);

        fish_eye_slider.setAdapter(fishEyeAdapter);

        fish_eye_slider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
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

        ActivityResultLauncher<Intent> cameraResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        Bitmap fish_eye_image = (Bitmap) data.getExtras().get("data");
//                        int dimension = Math.min(fish_eye_image.getWidth(), fish_eye_image.getHeight());
//                        fish_eye_image = ThumbnailUtils.extractThumbnail(fish_eye_image, dimension, dimension);

                        Log.d(TAG, "Image Bitmap Debug: "+fish_eye_image.toString());

                        Uri fish_eye_uri = ImageUtils.saveImage(fish_eye_image, getActivity(), "captured_fish_eye_image.jpg");

                        Log.d(TAG, "Image Uri Debug: "+fish_eye_uri.toString());

                        NavDirections actions;
                        actions = FishEyeFragmentDirections.actionFishEyeFragmentToFishGillFragment(fish_eye_uri);
                        Navigation.findNavController(view).navigate(actions);

                    }
                }
        );

        btn_camera_eye.setOnClickListener(v -> {
            if (getActivity().checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraResultLauncher.launch(cameraIntent);
            } else {
                getActivity().requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
            }
        });

        ActivityResultLauncher<Intent> galleryResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        Uri imageUri = data.getData();
//                        Bitmap fish_eye_image = null;

//                        try {
//                            fish_eye_image = MediaStore.Images.Media.getBitmap(this.getContext().getContentResolver(), imageUri);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }

                        Log.d(TAG, "Image Uri Debug: "+imageUri.toString());

                        NavDirections actions;
                        actions = FishEyeFragmentDirections.actionFishEyeFragmentToFishGillFragment(imageUri);
                        Navigation.findNavController(view).navigate(actions);
                    }
                }
        );

        btn_gallery_eye.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryResultLauncher.launch(galleryIntent);
        });
    }

    private void changeIndicatorColor(){

        int currentItem = fish_eye_slider.getCurrentItem();

        switch (currentItem){

            case 1 :
                slider1.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent_white));
                slider2.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.degura_white));
                slider3.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent_white));
                slider4.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent_white));
                slider5.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent_white));
                break;

            case 2 :
                slider1.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent_white));
                slider2.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent_white));
                slider3.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.degura_white));
                slider4.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent_white));
                slider5.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent_white));
                break;

            case 3:
                slider1.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent_white));
                slider2.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent_white));
                slider3.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent_white));
                slider4.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.degura_white));
                slider5.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent_white));
                break;

            case 4:
                slider1.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent_white));
                slider2.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent_white));
                slider3.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent_white));
                slider4.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent_white));
                slider5.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.degura_white));
                break;

            default:
                slider1.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.degura_white));
                slider2.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent_white));
                slider3.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent_white));
                slider4.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent_white));
                break;
        }
    }
}