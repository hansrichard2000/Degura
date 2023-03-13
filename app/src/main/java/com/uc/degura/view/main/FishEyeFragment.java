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
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.uc.degura.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FishEyeFragment extends Fragment {

    @BindView(R.id.btn_camera_eye)
    Button btn_camera_eye;

    @BindView(R.id.btn_gallery_eye)
    Button btn_gallery_eye;

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
        ButterKnife.bind(this, view);

        ActivityResultLauncher<Intent> cameraResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        Bitmap fish_eye_image = (Bitmap) data.getExtras().get("data");
                        int dimension = Math.min(fish_eye_image.getWidth(), fish_eye_image.getHeight());
                        fish_eye_image = ThumbnailUtils.extractThumbnail(fish_eye_image, dimension, dimension);

                        Log.d(TAG, "Image Bitmap Debug: "+fish_eye_image.toString());

                        Uri fish_eye_uri = saveImage(fish_eye_image, getActivity());

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

    private Uri saveImage(Bitmap image, Context context){
        File imagesFolder = new File(context.getCacheDir(), "deguraImages");
        Uri fish_eye_uri = null;
        try {
            imagesFolder.mkdirs();
            File file = new File(imagesFolder, "captured_fish_eye_image.jpg");
            FileOutputStream stream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.flush();
            stream.close();
            fish_eye_uri = FileProvider.getUriForFile(context.getApplicationContext(), "com.uc.degura"+".provider", file);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return fish_eye_uri;
    }


}