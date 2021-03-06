package com.dscnitp.freshersportal.Alumni;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.dscnitp.freshersportal.Common.Node;
import com.dscnitp.freshersportal.R;
import com.dscnitp.freshersportal.SplashScreen;
import com.dscnitp.freshersportal.Student.AddPostFragment;
import com.dscnitp.freshersportal.Student.DashboardActivity;
import com.dscnitp.freshersportal.Student.EditProfileActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
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

import java.time.Year;
import java.util.HashMap;

public class AlumniEditProfileActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference;

    private TextInputEditText Name, Branch, RollNo, Company;
    Button logout, edit;
    ImageView profilePic, alumniImagebtn;
    Spinner from, to;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseStorage mStorage;
    private DatabaseReference databaseReferenceUsers;
    private String PhotoUrl;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alumni_edit_profile);
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("users");
        Name = (TextInputEditText) findViewById(R.id.name);
        Company = (TextInputEditText) findViewById(R.id.Com);
        Branch = (TextInputEditText) findViewById(R.id.Branch);
        from = findViewById(R.id.spinner_from);
        to = findViewById(R.id.spinner_to);
        profilePic = (ImageView) findViewById(R.id.ProfileImage);
        alumniImagebtn = (ImageView) findViewById(R.id.alumni_image_btn);
        alumniImagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(EditProfileActivity.this,"Image clicked",Toast.LENGTH_SHORT).show();
                CropImage.activity().setAspectRatio(1, 1).start(AlumniEditProfileActivity.this);
            }
        });
        logout = findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                finish();
                startActivity(new Intent(AlumniEditProfileActivity.this, SplashScreen.class));
            }
        });
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().child(Node.Users);
            databaseReferenceUsers.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(Node.Name).getValue() != null)
                        Name.setText(dataSnapshot.child(Node.Name).getValue().toString());

//                    if (dataSnapshot.child(Node.ROLL_NO).getValue() != null)
//                        RollNo.setText(dataSnapshot.child(Node.ROLL_NO).getValue().toString());
//
//                    if (dataSnapshot.child(Node.Branch).getValue() != null)
//                        Branch.setText(dataSnapshot.child(Node.Branch).getValue().toString());
//
//                    String url = dataSnapshot.child(Node.Photo).getValue().toString();
//                    if (url.length() != 0)
//                        Picasso.get().load(url).into(profilePic);

                    ArrayAdapter<String> fromYearAdapter = new ArrayAdapter<String>(AlumniEditProfileActivity.this, android.R.layout.simple_list_item_1,
                            getResources().getStringArray(R.array.YearFrom));
                    fromYearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    from.setAdapter(fromYearAdapter);

                    ArrayAdapter<String> ToYearAdapter = new ArrayAdapter<String>(AlumniEditProfileActivity.this, android.R.layout.simple_list_item_1,
                            getResources().getStringArray(R.array.YearFrom));
                    fromYearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    to.setAdapter(fromYearAdapter);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();

            uploadImage(imageUri);
        } else {
            Toast.makeText(this, "Error try again", Toast.LENGTH_SHORT).show();
        }
    }
    private void uploadImage(Uri imageUri) {

        if (imageUri != null) {
            String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            storageReference = FirebaseStorage.getInstance().getReference();
            final StorageReference fileRef = storageReference.child("profile images").child(id).child("profile.jpg");
            fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            HashMap<String, Object> userMap = new HashMap<>();
                            userMap.put(Node.Photo, uri.toString());
                            databaseReference = FirebaseDatabase.getInstance().getReference().child(Node.Users);
                            databaseReferenceUsers.child(currentUser.getUid()).updateChildren(userMap);

                            Picasso.get().load(uri).into(profilePic);
                            Toast.makeText(AlumniEditProfileActivity.this, "Image uploaded", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AlumniEditProfileActivity.this, "Image upload failed try again...", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Image not selected", Toast.LENGTH_SHORT).show();
        }

    }
    public void btnSave(View V) {
        if (Name.getText().toString().trim().equals("")) {
            Name.setError(getString(R.string.etName));
        }
        if (Company.getText().toString().trim().equals("")) {
            Company.setError(getString(R.string.etCompany));
        }
        if (Branch.getText().toString().trim().equals("")) {
            Branch.setError(getString(R.string.etBranch));
        } else {

            updateNameOnly();
        }
    }
    public void updateNameOnly() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Updating your profile");
        progressDialog.setMessage("Please wait ....");
        progressDialog.show();

        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder().
                setDisplayName(Name.getText().toString().trim()).build();

        currentUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    String UserID = currentUser.getUid();
                    HashMap hashMap=new HashMap();
                    hashMap.put(Node.Name,Name.getText().toString().trim());
                    hashMap.put(Node.Company,Company.getText().toString().trim());
                    hashMap.put(Node.Branch,Branch.getText().toString().trim());

                    databaseReferenceUsers.child(UserID).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                progressDialog.dismiss();
                                Toast.makeText(AlumniEditProfileActivity.this, "User updated", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(AlumniEditProfileActivity.this, DashboardActivity.class));
                            }
                            else
                            {
                                Toast.makeText(AlumniEditProfileActivity.this,"User Not Created",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else
                {
                    Toast.makeText(AlumniEditProfileActivity.this,"Failed to Update : %1$s"+task.getException(),Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

}