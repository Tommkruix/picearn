package org.codebrainz.picearn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.codebrainz.picearn.utils.Tools;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity implements RewardedVideoAdListener {

    Dialog dialogUpdateUserProfile, dialogEarning;
    private TextView textView_thankyou, txtName, txtStatus, txtHobbies, txtNamebold, txtEarnings, txtWelcomeBonus;
    private TextView btnUpdate, btnWithdraw, btnCampusUpdate;
    private TextView grade_text;

    CardView messagesCard;

    private ImageView image_profile_bg;

    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private FirebaseUser currentUser;
    private EditText update_username, update_hobbies, update_earning, update_earning_address;

    private String user_role;

    private FloatingActionButton fab, fab1, fab2;

    private static final int ProfileImageGalleryPick = 1;
    private static final int ProfileBgImageGalleryPick = 2;

    private StorageReference UserProfileImagesRef;
    private DatabaseReference UserRef, ForumPostEarningRef, MessagesRef;

    private ProgressDialog loadingBar;

    String bonus = "0";

    String requestID= "";

    private RewardedVideoAd mRewardedVideoAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        LoadGoogleAdMobAds();

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        ForumPostEarningRef = FirebaseDatabase.getInstance().getReference().child("Forum Post Earnings");
        MessagesRef = FirebaseDatabase.getInstance().getReference().child("Messages");

        initializeFields();
        initToolbar();

        RetrieveFirebaseUserInfo();
        RetrieveFirebaseUserEarnings();

        image_profile_bg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, ProfileBgImageGalleryPick);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateUserProfileDialog();
            }
        });

        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EarningDialog();
            }
        });

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToMessages();
            }
        });

    }

    private void LoadGoogleAdMobAds() {

        MobileAds.initialize(this, "ca-app-pub-8605419608861414~6882095863");

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
    }

    private void loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd("ca-app-pub-8605419608861414/7375020078",
                new AdRequest.Builder().build());
    }

    private void SendUserToMessages() {
        Intent intent = new Intent(ProfileActivity.this, MessagesActivity.class);
        startActivity(intent);
    }

    private void EarningDialog() {
        dialogEarning = new Dialog(this);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialogEarning.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialogEarning.getWindow().setAttributes(lp);
        dialogEarning.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialogEarning.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogEarning.setContentView(R.layout.dialog_profile_user_earnings);
        dialogEarning.setTitle("Earning");

        update_earning = dialogEarning.findViewById(R.id.update_earning);
        update_earning.setText(txtEarnings.getText().toString());

        update_earning_address = dialogEarning.findViewById(R.id.update_earning_address);

        btnWithdraw = dialogEarning.findViewById(R.id.btnWithdraw);

        btnWithdraw.setEnabled(true);
        btnWithdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogEarning.cancel();

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //your code or your request that you want to run on uiThread
                                String earning = update_earning.getText().toString().trim();
                                String earning_address = update_earning_address.getText().toString().trim();

                                    VerifyUserEarningInputs(earning, earning_address);

                            }
                        });
                    }
                }).start();

                /*Intent resultIntent = new Intent();
                resultIntent.putExtra("", "");
                setResult(RESULT_OK, resultIntent);
                finish();*/
            }
        });
        dialogEarning.setCancelable(true);
        dialogEarning.show();
    }

    private void VerifyUserEarningInputs(String earning, String earning_address) {

        int bonusString = Integer.parseInt(bonus);
        int amount = Integer.parseInt(earning);
        amount = amount + bonusString;

            if (amount >= 100 && !earning_address.isEmpty()) {
                MessageAdminAboutEarning(amount, earning_address);
            } else {
                Toast.makeText(this, "Minimum of 100 points must be reached!", Toast.LENGTH_SHORT).show();
                if (mRewardedVideoAd.isLoaded()) {
                    mRewardedVideoAd.show();
                }
            }

    }

    private void initializeFields() {

        image_profile_bg = findViewById(R.id.image_profile_bg);
        loadingBar = new ProgressDialog(this);
        txtName = findViewById(R.id.txtUsername);
        txtHobbies = findViewById(R.id.txtHobbies);
        txtNamebold  = findViewById(R.id.txtNamebold);
        txtEarnings = findViewById(R.id.txtEarnings);
        txtWelcomeBonus = findViewById(R.id.txtWelcomeBonus);
        fab = findViewById(R.id.fab);
        fab1 = findViewById(R.id.fab1);
        fab2 = findViewById(R.id.fab2);
        messagesCard = findViewById(R.id.messagesCard);

    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        Tools.setSystemBarColor(this, R.color.overlay_dark_10);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == ProfileBgImageGalleryPick && resultCode == RESULT_OK && data != null){
            Uri ImageUri2 = data.getData();

            CropImage.activity(ImageUri2)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setActivityTitle("Cover Image")
                    .setMultiTouchEnabled(true)
                    .setFixAspectRatio(true)
                    .setAspectRatio(1,1)
                    .start(this);

        }

        // cover image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait, while your profile image is updating...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();

                final StorageReference filePath = UserProfileImagesRef.child(currentUserID + ".jpg");


                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            //Toast.makeText(UserAccountActivity.this, "Cover Image uploaded successfully...", Toast.LENGTH_SHORT).show();
                            //final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            //final String downloadUrl = task.getResult().getStorage().getDownloadUrl().toString();
                            //here
                            Task<Uri> urlTask = task.getResult().getStorage().getDownloadUrl();
                            while (!urlTask.isSuccessful()) ;
                            Uri downloadUrl1 = urlTask.getResult();

                            final String downloadUrl = String.valueOf(downloadUrl1);

                            RootRef.child("Users").child(currentUserID).child("image_bg")
                                    .setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(ProfileActivity.this, "Image Updated.", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            } else {
                                                String message = task.getException().toString();
                                                Toast.makeText(ProfileActivity.this, "Not Successful.", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                        }
                                    });
                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(ProfileActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void UpdateUserProfileDialog() {
        dialogUpdateUserProfile = new Dialog(this);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialogUpdateUserProfile.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialogUpdateUserProfile.getWindow().setAttributes(lp);
        dialogUpdateUserProfile.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialogUpdateUserProfile.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogUpdateUserProfile.setContentView(R.layout.dialog_profile_user_update);
        dialogUpdateUserProfile.setTitle("Profile - Update");

        update_username = dialogUpdateUserProfile.findViewById(R.id.update_username);
        update_hobbies = dialogUpdateUserProfile.findViewById(R.id.update_hobbies);
        btnUpdate = dialogUpdateUserProfile.findViewById(R.id.btnUpdate);

        btnUpdate.setEnabled(true);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogUpdateUserProfile.cancel();

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //your code or your request that you want to run on uiThread
                                String username = update_username.getText().toString().trim();
                                String hobbies = update_hobbies.getText().toString().trim();
                                VerifyUserInputs(username, hobbies);
                            }
                        });
                    }
                }).start();

                /*Intent resultIntent = new Intent();
                resultIntent.putExtra("", "");
                setResult(RESULT_OK, resultIntent);
                finish();*/
            }
        });
        dialogUpdateUserProfile.setCancelable(true);
        dialogUpdateUserProfile.show();
    }

    private void VerifyUserInputs(String username, String hobbies) {

        if (username.isEmpty()) {
            Toast.makeText(this, "Please write your username...", Toast.LENGTH_SHORT).show();
        }

        if (hobbies.isEmpty()) {
            Toast.makeText(this, "Please write your hobbies...", Toast.LENGTH_SHORT).show();
        }

        if (!username.isEmpty() && !hobbies.isEmpty()) {
            UpdateFirebaseUserDetails(username, hobbies);

        } else {
            Toast.makeText(this, "Some fields are empty...", Toast.LENGTH_SHORT).show();
        }
    }

    private void UpdateFirebaseUserDetails(String username, String hobbies) {

        HashMap<String, Object> accountMap = new HashMap<>();
        accountMap.put("uid", currentUserID);
        accountMap.put("username", username);
        accountMap.put("hobbies", hobbies);
        RootRef.child("Users").child(currentUserID).updateChildren(accountMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            // Toast.makeText(UserAccountActivity.this, "Account firebase Updated Successfully...", Toast.LENGTH_SHORT).show();
                            //SendUserToDashboardActivity();
                        }else{
                            String message = task.getException().toString();
                            Toast.makeText(ProfileActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void UpdateFirebaseUserEarnings() {

        RootRef.child("Forum Post Earnings").orderByChild("user_id").equalTo(currentUserID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dataSnapshot.getRef().setValue(null);

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });
    }

    private void MessageAdminAboutEarning(final int earning, final String earning_address) {
        final String message_id = MessagesRef.push().getKey();

        HashMap<String, Object> addPostMap = new HashMap<>();
        addPostMap.put("user_id", currentUserID);
        addPostMap.put("message_id", message_id);
        addPostMap.put("amount", earning);
        addPostMap.put("earning_address", earning_address);
        addPostMap.put("message_user_id", message_id + "_ref_" + currentUserID);
        addPostMap.put("timestamp", ServerValue.TIMESTAMP);
        MessagesRef.child(message_id).updateChildren(addPostMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            // Success
                            RootRef.child("Users").child(currentUserID).child("bonus")
                                    .setValue(0);
                            UpdateFirebaseUserEarnings();
                            if (mRewardedVideoAd.isLoaded()) {
                                mRewardedVideoAd.show();
                            }
                            Toast.makeText(ProfileActivity.this, "Withdraw successful.", Toast.LENGTH_SHORT).show();
                        }else{
                            String message = task.getException().toString();
                            Toast.makeText(ProfileActivity.this, "Error occurred ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void RetrieveFirebaseUserInfo() {
        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if((dataSnapshot.exists()) && (dataSnapshot.hasChild("username")) && (dataSnapshot.hasChild("image_bg")) || (dataSnapshot.hasChild("image_bg")) ){

                            String retrieveUserName = null;
                            if(dataSnapshot.hasChild("username")){
                                retrieveUserName = dataSnapshot.child("username").getValue().toString();
                            }

                            String retrieveUserProfileImage = dataSnapshot.child("image_bg").getValue().toString();

                            Picasso.get().load(retrieveUserProfileImage).placeholder(R.drawable.a).into(image_profile_bg);
                            if (dataSnapshot.hasChild("image_bg")){
                                Picasso.get().load(retrieveUserProfileImage).placeholder(R.drawable.image_12).into(image_profile_bg);
                            }

                            if (dataSnapshot.hasChild("role") ){
                                messagesCard.setVisibility(View.VISIBLE);
                            }else{
                                messagesCard.setVisibility(View.GONE);
                            }

                            if (dataSnapshot.hasChild("bonus")){
                               bonus = dataSnapshot.child("bonus").getValue().toString();
                               if (Integer.parseInt(bonus) >= 1){
                                   txtWelcomeBonus.setVisibility(View.VISIBLE);
                                   txtWelcomeBonus.setText("Balance: " + bonus);
                               }else{
                                   txtWelcomeBonus.setVisibility(View.GONE);
                               }

                            }else{
                                txtWelcomeBonus.setVisibility(View.GONE);
                            }

                            txtName.setText(retrieveUserName);

                            if (dataSnapshot.hasChild("hobbies")){
                                txtHobbies.setText(dataSnapshot.child("hobbies").getValue().toString());
                            }else{
                                txtHobbies.setText("Hobbies");
                            }

                                txtNamebold.setText(retrieveUserName);


                        }else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("username")) ){
                            String retrieveUserName = dataSnapshot.child("username").getValue().toString();

                            txtName.setText(retrieveUserName);
                            txtNamebold.setText(retrieveUserName);

                            if (dataSnapshot.hasChild("role") ){
                                messagesCard.setVisibility(View.VISIBLE);
                            }else{
                                messagesCard.setVisibility(View.GONE);
                            }

                            if (dataSnapshot.hasChild("bonus")){
                                bonus = dataSnapshot.child("bonus").getValue().toString();
                                if (Integer.parseInt(bonus) == 10){
                                    txtWelcomeBonus.setVisibility(View.VISIBLE);
                                    txtWelcomeBonus.setText("Welcome Bonus: " + bonus);
                                }else{
                                    txtWelcomeBonus.setVisibility(View.GONE);
                                }
                            }else{
                                txtWelcomeBonus.setVisibility(View.GONE);
                            }

                            if (dataSnapshot.hasChild("hobbies")){
                                txtHobbies.setText(dataSnapshot.child("hobbies").getValue().toString());
                            }else{
                                txtHobbies.setText("");
                            }

                        }else{
                            txtName.setVisibility(View.VISIBLE);
                            Toast.makeText(ProfileActivity.this, "Please set and update your account settings", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void RetrieveFirebaseUserEarnings() {

        RootRef.child("Forum Post Earnings").orderByChild("user_id").equalTo(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int size = (int) dataSnapshot.getChildrenCount();
                        //dataSnapshot.child("name").getValue().toString();
                        if (String.valueOf(size).equals("0")) {
                           txtEarnings.setText("0");
                        } else if (String.valueOf(size).equals("1")) {
                            txtEarnings.setText(String.valueOf(size));
                        } else {
                            txtEarnings.setText(String.valueOf(size));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

    }


    @Override
    public void onRewardedVideoAdLoaded() {

    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    public void onRewardedVideoAdClosed() {

    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        Toast.makeText(this, "Rewarded", Toast.LENGTH_SHORT).show();
        // Reward the user.
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {

    }

    @Override
    public void onRewardedVideoCompleted() {

    }
}
