package com.bunny.gochat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;

    private CircleImageView circleImageView;
    private TextView mName;
    private TextView mStatus;
    private Button mSettingsStatusBtn;
    private Button msettingsImageBtn;
    private static final int GALLERY_PICK=1;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        circleImageView = (CircleImageView) findViewById(R.id.settingsImage);
        mName = (TextView) findViewById(R.id.displayName);
        mStatus = (TextView) findViewById(R.id.statusName);
        mSettingsStatusBtn = (Button) findViewById(R.id.settingsStatusBtn);
        msettingsImageBtn = (Button) findViewById(R.id.settingsImageBtn);

        storageReference = FirebaseStorage.getInstance().getReference();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser.getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);

                if (!image.equals("profile_icon")) {

                    Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.profile_icon).into(circleImageView);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

       mSettingsStatusBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               String statusValue = mStatus.getText().toString();
               Intent statusIntent = new Intent(SettingsActivity.this,StatusActivity.class);
               statusIntent.putExtra("status",statusValue);
               startActivity(statusIntent);
           }
       });

       msettingsImageBtn.setOnClickListener(new View.OnClickListener() {
          @Override
           public void onClick(View view) {
               /*Intent galleryIntent = new Intent();
               galleryIntent.setType("Image/*");
               galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
               startActivityForResult(Intent.createChooser(galleryIntent,"Select Image"),GALLERY_PICK);*/

               CropImage.activity()
                       .setGuidelines(CropImageView.Guidelines.ON)
                       .start(SettingsActivity.this);

           }
       });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==GALLERY_PICK && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            CropImage.activity(imageUri).setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                progressDialog = new ProgressDialog(SettingsActivity.this);
                progressDialog.setTitle("Uploading Image");
                progressDialog.setMessage("Please Wait");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                Uri resultUri = result.getUri();
                File thumbFilePath = new File(resultUri.getPath());
                String currentUserId = currentUser.getUid();

                Bitmap thumbBitmap = null;
                try {
                    thumbBitmap = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(75)
                            .compressToBitmap(thumbFilePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumbByte = baos.toByteArray();

                StorageReference filePath = storageReference.child("profile_images").child(currentUserId+".jpg");
                final StorageReference thumb_FilePath = storageReference.child("images").child("thumbs").child(currentUserId+".jpg");


                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {


                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();
                            UploadTask uploadTask = thumb_FilePath.putBytes(thumbByte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumbTask) {

                                    String thumbDownloadUrl = thumbTask.getResult().getDownloadUrl().toString();
                                    if (thumbTask.isSuccessful()){

                                        Map updatehashMap = new HashMap<>();
                                        updatehashMap.put("image",downloadUrl);
                                        updatehashMap.put("thumb_image",thumbDownloadUrl);
                                        databaseReference.updateChildren(updatehashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){

                                                    progressDialog.dismiss();
                                                    Toast.makeText(SettingsActivity.this,"Successful",Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    }else {
                                        Toast.makeText(SettingsActivity.this," Error in Uploading Thumbnail ",Toast.LENGTH_LONG).show();
                                        progressDialog.dismiss();
                                    }
                                }
                            });

                        }else{

                            Toast.makeText(SettingsActivity.this," Error in Uploading ",Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }


    }


}
