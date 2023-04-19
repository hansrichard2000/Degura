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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.uc.degura.R;
import com.uc.degura.env.ImageUtils;
import com.uc.degura.model.InstructionNote;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FishGillFragment extends Fragment {

    @BindView(R.id.fish_gill_slider)
    ViewPager2 fish_gill_slider;

    @BindView(R.id.gill_page1)
    ImageView slider1;

    @BindView(R.id.gill_page2)
    ImageView slider2;

    @BindView(R.id.gill_page3)
    ImageView slider3;

    @BindView(R.id.gill_page4)
    ImageView slider4;

    @BindView(R.id.gill_page5)
    ImageView slider5;

    private FishGillAdapter fishGillAdapter;

    @BindView(R.id.btn_camera_gill)
    Button btn_camera_gill;

    @BindView(R.id.btn_gallery_gill)
    Button btn_gallery_gill;

    @BindView(R.id.btn_back_gill)
    Button btn_back_gill;


    private static final String TAG = "FishGillFragment";

    public FishGillFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fish_gill, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        InstructionNote instruction1 = new InstructionNote(0, "", "Swipe ke kiri untuk membaca petunjuk cara mengambil gambar insang ikan Gurami");
        InstructionNote instruction2 = new InstructionNote(R.drawable.slide_insang_1, "Ambil gambar insang ikan dengan jarak ideal dari kamera", "");
        InstructionNote instruction3 = new InstructionNote(R.drawable.slide_insang_2, "Pastikan hasil gambar memiliki pencahayaan yang baik, tidak blur, dan tidak terhalang tangan", "");
        InstructionNote instruction4 = new InstructionNote(R.drawable.slide_insang_3, "Untuk bisa mendapat hasil yang akurat Anda dapat memakai flash light dari kamera HP Anda saat mengambil gambar", "");
        InstructionNote instruction5 = new InstructionNote(R.drawable.slide_insang_4, "Sumber gambar dapat Anda ambil dari galeri hp Anda atau melakukan pengambilan gambar langsung", "");

        List<InstructionNote> instruction_list = Arrays.asList(instruction1, instruction2, instruction3, instruction4, instruction5);

        fishGillAdapter = new FishGillAdapter(getContext());
        fishGillAdapter.setInstructionNoteList(instruction_list);

        fish_gill_slider.setAdapter(fishGillAdapter);
        fish_gill_slider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
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
//        Bitmap fish_eye_image = getArguments().getParcelable("fish_eye");

        Uri fish_eye_uri = getArguments().getParcelable("fish_eye");

        Log.d(TAG, "Image Uri Fish Eye Debug: "+fish_eye_uri.toString());

        ActivityResultLauncher<Intent> cameraResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        Bitmap fish_gill_image = (Bitmap) data.getExtras().get("data");
                        int dimension = Math.min(fish_gill_image.getWidth(), fish_gill_image.getHeight());
                        fish_gill_image = ThumbnailUtils.extractThumbnail(fish_gill_image, dimension, dimension);

                        Log.d(TAG, "Image Bitmap Debug: "+fish_gill_image.toString());

                        Uri fish_gill_uri = ImageUtils.saveImage(fish_gill_image, getActivity(), "captured_fish_gill_image.jpg");

                        Log.d(TAG, "Image Uri Debug: "+fish_gill_uri.toString());

                        NavDirections actions;
                        actions = FishGillFragmentDirections.actionFishGillFragmentToDetectionFragment(fish_eye_uri, fish_gill_uri);
                        Navigation.findNavController(view).navigate(actions);

                    }
                }
        );

        btn_camera_gill.setOnClickListener(v -> {
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
//                        Bitmap fish_gill_image = null;

//                        try {
//                            fish_gill_image = MediaStore.Images.Media.getBitmap(this.getContext().getContentResolver(), imageUri);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }

                        Log.d(TAG, "Image Uri Debug: "+imageUri.toString());

                        NavDirections actions;
                        actions = FishGillFragmentDirections.actionFishGillFragmentToDetectionFragment(fish_eye_uri, imageUri);
                        Navigation.findNavController(view).navigate(actions);
                    }
                }
        );

        btn_gallery_gill.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryResultLauncher.launch(galleryIntent);
        });

        btn_back_gill.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });
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

    private void changeIndicatorColor(){

        int currentItem = fish_gill_slider.getCurrentItem();

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

            case 4 :
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

//    @Override
//    public void onStop() {
//        super.onStop();
//        deleteCache(getContext());
//    }
}