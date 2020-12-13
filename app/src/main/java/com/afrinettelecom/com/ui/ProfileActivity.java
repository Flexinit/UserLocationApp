package com.afrinettelecom.com.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;


import com.afrinettelecom.com.R;
import com.afrinettelecom.com.models.User;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.afrinettelecom.com.UserClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
//import com.opensooq.supernova.gligar.GligarPicker;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileActivity extends AppCompatActivity implements
        View.OnClickListener,
        IProfile {

    private static final String TAG = "ProfileActivity";


    //widgets
    //  private CircleImageView mAvatarImage;
    private ImageView mAvatarImage;

    //vars
    private ImageListFragment mImageListFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_profile);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            mAvatarImage = findViewById(R.id.image_choose_avatar);

            findViewById(R.id.image_choose_avatar).setOnClickListener(this);
            findViewById(R.id.text_choose_avatar).setOnClickListener(this);

           // retrieveProfileImage();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void selectImage(Context context) {

        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose your favorite profile picture");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo")) {
                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);

                } else if (options[item].equals("Choose from Gallery")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, 1);

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (resultCode != RESULT_CANCELED) {
                switch (requestCode) {
                    case 0:
                        if (resultCode == RESULT_OK && data != null) {
                            Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                            mAvatarImage.setImageBitmap(selectedImage);
                        }

                        break;
                    case 1:
                        if (resultCode == RESULT_OK && data != null) {
                            Uri selectedImage = data.getData();
                            String[] filePathColumn = {MediaStore.Images.Media.DATA};
                            if (selectedImage != null) {
                                Cursor cursor = getContentResolver().query(selectedImage,
                                        filePathColumn, null, null, null);
                                if (cursor != null) {
                                    cursor.moveToFirst();

                                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                    String picturePath = cursor.getString(columnIndex);
                                    mAvatarImage.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                                    cursor.close();
                                }
                            }

                        }
                        break;
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void retrieveProfileImage() {
        try {
            RequestOptions requestOptions = new RequestOptions()
                    .error(R.drawable.user_avatar)
                    .placeholder(R.drawable.user_avatar);

            int avatar = 0;
            try {
                avatar = Integer.parseInt(((UserClient) getApplicationContext()).getUser().getAvatar());
            } catch (Exception e) {
                Log.e(TAG, "retrieveProfileImage: no avatar image. Setting default. " + e.getMessage());
            }

            Glide.with(ProfileActivity.this)
                    .setDefaultRequestOptions(requestOptions)
                    .load(avatar)
                    .into(mAvatarImage);
        } catch (Exception ex) {

            ex.printStackTrace();
        }
    }


    @Override
    public void onClick(View v) {
//        mImageListFragment = new ImageListFragment();
//        getSupportFragmentManager().beginTransaction()
//                .setCustomAnimations(R.anim.slide_in_up, R.anim.slide_in_down, R.anim.slide_out_down, R.anim.slide_out_up)
//                .replace(R.id.fragment_container, mImageListFragment, getString(R.string.fragment_image_list))
//                .commit();
        //new GligarPicker().requestCode(1038).withActivity(this).show();

        selectImage(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onImageSelected(int resource) {

        // remove the image selector fragment
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_up, R.anim.slide_in_down, R.anim.slide_out_down, R.anim.slide_out_up)
                .remove(mImageListFragment)
                .commit();

        // display the image
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.cwm_logo)
                .error(R.drawable.cwm_logo);

        Glide.with(this)
                .setDefaultRequestOptions(requestOptions)
                .load(resource)
                .into(mAvatarImage);

        // update the client and database
        User user = ((UserClient) getApplicationContext()).getUser();
        user.setAvatar(String.valueOf(resource));

        FirebaseFirestore.getInstance()
                .collection(getString(R.string.collection_users))
                .document(FirebaseAuth.getInstance().getUid())
                .set(user);
    }

    public void removeImage(View view) {

        mAvatarImage.setImageDrawable(null);

    }
}
