package org.codebrainz.picearn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.codebrainz.picearn.utils.Tools;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.codebrainz.picearn.TimeAgo.getTimeAgo;

public class MainActivity extends AppCompatActivity {

    TextView user_name;

    ImageView user_pic;

    private InterstitialAd mInterstitialAd;


    private StorageReference UsersProfileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String currentUserID;

    private CircularImageView post_user_pic;
    private TextView post_type, post_thread, followStatus, followName;
    private CardView followBtn;
    private ImageView followUserImage, showViewImage;
    private ImageButton followCloseBtn, showViewImageCloseBtn;
    private EditText btn_file_document, post_image;
    private String mysqlRetrieveUserPostDeviceToken;
    private String firebaseRetrieveUserPostDeviceToken;

    private ProgressDialog loadingBar;
    private Dialog followDialog, viewImageDialog;

    private DatabaseReference ForumPostRef, ForumPostLikeRef, ForumPostEarningRef;

    private StorageReference UserProfileImagesRef;
    private FirebaseUser currentUser;

    private RecyclerView ForumPostRecyclerList;

    String post_collect_image, RedirectPostButton;

    private String checker = "";

    private static final int GalleryPick = 1;

    private Uri fileUri;

    private StorageReference ForumPostImagesRef, ForumPostDocumentsRef;


    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    private static final String AD_UNIT_ID = "ca-app-pub-8605419608861414/6771373086";

    // A banner ad is placed in every 8th position in the RecyclerView.
    //public static final int ITEMS_PER_AD = 8;

    // List of banner ads and MenuItems that populate the RecyclerView.
    //private List<Object> recyclerViewItems = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LoadGoogleAdMobAds();

        /*AdLoader.Builder builder = new AdLoader.Builder(this, AD_UNIT_ID);
        builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
            @Override
            public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {

                TemplateView templateView = findViewById(R.id.my_template);
                templateView.setNativeAd(unifiedNativeAd);

            }
        });
        AdLoader adLoader = builder.build();
        AdRequest adRequest = new AdRequest.Builder().build();
        adLoader.loadAd(adRequest);*/

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-8605419608861414/4899231673");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });

        mAuth = FirebaseAuth.getInstance();
        //currentUser = mAuth.getCurrentUser();
        RootRef = FirebaseDatabase.getInstance().getReference();
        currentUserID = mAuth.getCurrentUser().getUid();

        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        ForumPostLikeRef = FirebaseDatabase.getInstance().getReference().child("Forum Post Likes");
        ForumPostEarningRef = FirebaseDatabase.getInstance().getReference().child("Forum Post Earnings");

        Tools.setSystemBarColor(this, R.color.colorPrimary);

        //RetrieveFirebaseUserInfo();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        readPermission();


        mAuth = FirebaseAuth.getInstance();
        //currentUser = mAuth.getCurrentUser();
        RootRef = FirebaseDatabase.getInstance().getReference();

        ForumPostRef = FirebaseDatabase.getInstance().getReference().child("Forum Posts");
        ForumPostImagesRef = FirebaseStorage.getInstance().getReference().child("Forum Post Images");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        ForumPostRecyclerList = findViewById(R.id.recycler_id);
        ForumPostRecyclerList.setLayoutManager(linearLayoutManager);

        currentUserID = mAuth.getCurrentUser().getUid();
        currentUser = mAuth.getCurrentUser();

        loadingBar = new ProgressDialog(this);

        // jsonRequestForumPost();

        initToolbar();
        initComponent();

    }

    /*public void LoadNativeAds(){
        AdLoader adLoader = new AdLoader.Builder(this, AD_UNIT_ID)
                .forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {

                        UnifiedNativeAdView unifiedNativeAdView = (UnifiedNativeAdView) getLayoutInflater().inflate(R.layout.native_ad_layout, null);
                        mAapUnifiedAdToLayout(unifiedNativeAd, unifiedNativeAdView);

                        FrameLayout nativeAdLayout = findViewById(R.id.id_native_ad);
                        if(nativeAdLayout!=null){
                            nativeAdLayout.removeAllViews();
                            nativeAdLayout.addView(unifiedNativeAdView);
                    }
                        //nativeAdLayout.removeAllViews();
                        //nativeAdLayout.addView(unifiedNativeAdView);

                    }
                })
                .withAdListener(new AdListener(){
                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                    }
                })
                .build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }

    public void mAapUnifiedAdToLayout(UnifiedNativeAd adFromGoogle, UnifiedNativeAdView myAdView){
        MediaView mediaView = myAdView.findViewById(R.id.ad_media);
        myAdView.setMediaView(mediaView);

        myAdView.setHeadlineView(myAdView.findViewById(R.id.ad_headline));
        myAdView.setBodyView(myAdView.findViewById(R.id.ad_body));
        myAdView.setCallToActionView(myAdView.findViewById(R.id.ad_call_to_action));
        myAdView.setIconView(myAdView.findViewById(R.id.ad_icon));
        myAdView.setPriceView(myAdView.findViewById(R.id.ad_price));
        myAdView.setStarRatingView(myAdView.findViewById(R.id.ad_rating));
        myAdView.setStoreView(myAdView.findViewById(R.id.ad_store));
        myAdView.setAdvertiserView(myAdView.findViewById(R.id.ad_advertiser));


        if (adFromGoogle.getBody() == null){
            myAdView.getBodyView().setVisibility(View.GONE);
        }else{
            ((TextView)myAdView.getBodyView()).setText(adFromGoogle.getBody());
        }

        if (adFromGoogle.getHeadline() == null){
            myAdView.getHeadlineView().setVisibility(View.GONE);
        }else{
            ((TextView)myAdView.getHeadlineView()).setText(adFromGoogle.getHeadline());
        }

        if (adFromGoogle.getCallToAction() == null){
            myAdView.getCallToActionView().setVisibility(View.GONE);
        }else{
            ((Button)myAdView.getCallToActionView()).setText(adFromGoogle.getCallToAction());
        }

        if (adFromGoogle.getIcon() == null){
            myAdView.getIconView().setVisibility(View.GONE);
        }else{
            ((ImageView)myAdView.getIconView()).setImageDrawable(adFromGoogle.getIcon().getDrawable());
        }

        if (adFromGoogle.getStarRating() == null){
            myAdView.getStarRatingView().setVisibility(View.GONE);
        }else{
            ((RatingBar)myAdView.getStarRatingView()).setRating(adFromGoogle.getStarRating().floatValue());
        }


        if (adFromGoogle.getPrice() == null){
            myAdView.getPriceView().setVisibility(View.GONE);
        }else{
            ((TextView)myAdView.getPriceView()).setText(adFromGoogle.getPrice());
        }

        if (adFromGoogle.getStore() == null){
            myAdView.getStoreView().setVisibility(View.GONE);
        }else{
            ((TextView)myAdView.getStoreView()).setText(adFromGoogle.getStore());
        }

        if (adFromGoogle.getAdvertiser() == null){
            myAdView.getAdvertiserView().setVisibility(View.GONE);
        }else{
            ((TextView)myAdView.getAdvertiserView()).setText(adFromGoogle.getAdvertiser());
        }

        myAdView.setNativeAd(adFromGoogle);


    }*/

    private void LoadGoogleAdMobAds() {

        MobileAds.initialize(this, "ca-app-pub-8605419608861414~6882095863");

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_account_circle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Latest Posts");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                }
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
        Tools.setSystemBarColor(this, R.color.colorPrimaryDark);
    }

    private void initComponent() {
        NestedScrollView nested_content = findViewById(R.id.nested_scroll_view);
        nested_content.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY < oldScrollY) { // up
                    animateFab(false);
                }
                if (scrollY > oldScrollY) { // down
                    animateFab(true);
                }
            }
        });

        FloatingActionButton fab_add = findViewById(R.id.fab);
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCustomDialog();
            }
        });

    }

    boolean isFabHide = false;
    private void animateFab(final boolean hide) {
        FloatingActionButton fab_add = findViewById(R.id.fab);

        if (isFabHide && hide || !isFabHide && !hide) return;
        isFabHide = hide;
        int moveY = hide ? (2 * fab_add.getHeight()) : 0;
        fab_add.animate().translationY(moveY).setStartDelay(100).setDuration(300).start();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GalleryPick && resultCode == RESULT_OK && data != null){
            Uri ImageUri = data.getData();

            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .setActivityTitle("Post Image")
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            //post_image.setImageURI(result.getUri());
            post_image.setText("Image Selected");
            post_image.setVisibility(View.VISIBLE);
            if(resultCode == RESULT_OK){

                Uri resultUri = result.getUri();

                final String post_id = ForumPostRef.push().getKey();
                final StorageReference filePath = ForumPostImagesRef.child(post_id + ".jpg");


                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Task<Uri> urlTask = task.getResult().getStorage().getDownloadUrl();
                            while (!urlTask.isSuccessful());
                            Uri downloadUrl1 = urlTask.getResult();

                            final String downloadUrl = String.valueOf(downloadUrl1);
                            post_collect_image = downloadUrl;

                            /*HashMap<String, Object> addPostImagesMap = new HashMap<>();
                            addPostImagesMap.put("user_id", currentUserID);
                            addPostImagesMap.put("post_id", post_id);
                            addPostImagesMap.put("body", "null");
                            addPostImagesMap.put("type", "Photo");
                            addPostImagesMap.put("image", downloadUrl);
                            addPostImagesMap.put("user_id_type", currentUserID+"Photo");
                            addPostImagesMap.put("timestamp", ServerValue.TIMESTAMP);
                            ForumPostRef.push().setValue(addPostImagesMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){

                                                // Toast.makeText(UserAccountActivity.this, "Account firebase Updated Successfully...", Toast.LENGTH_SHORT).show();
                                            }else{
                                                String message = task.getException().toString();
                                                Toast.makeText(ForumPostActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });*/





                        }else{
                            String message = task.getException().toString();
                            Toast.makeText(MainActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception error = result.getError();
                Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }


    }

    private void showCustomDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_post);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;

        final AppCompatButton bt_submit = (AppCompatButton) dialog.findViewById(R.id.bt_submit);
        ((EditText) dialog.findViewById(R.id.et_post)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                bt_submit.setEnabled(!s.toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        post_user_pic = dialog.findViewById(R.id.post_user_pic);
        user_name = dialog.findViewById(R.id.post_user_name);
        post_image = dialog.findViewById(R.id.post_image);

        post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                post_collect_image = "";
                //post_image.setImageURI(null);
                post_image.setText("");
                post_image.setVisibility(View.GONE);
            }
        });

        RetrieveFirebaseAddPostUserInfo();

        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String post_message = ((EditText) dialog.findViewById(R.id.et_post)).getText().toString().trim();
                 VerifyUserPost( post_message, post_collect_image);
                    dialog.dismiss();

                //Toast.makeText(getApplicationContext(), "Post Submitted", Toast.LENGTH_SHORT).show();
            }
        });

        /* Retrieving from firebase database */

        ( dialog.findViewById(R.id.bt_photo)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);
                //dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    private void VerifyUserPost(final String post_message, final String post_collect_image) {
        if (post_message.isEmpty()) {
            Toast.makeText(this, "Post is empty", Toast.LENGTH_SHORT).show();
        }
        if ( post_message.length() < 5 ){
            Toast.makeText(this, "Post content must be atleast 5 characters", Toast.LENGTH_SHORT).show();
        }

        if ( post_message.length() > 250 ){
            Toast.makeText(this, "Post content must not be more than 250 characters", Toast.LENGTH_SHORT).show();
        }
        if (!post_message.isEmpty() && post_message.length() > 5 && post_message.length() < 250 && post_collect_image != null) {

            new Thread(new Runnable() {
                @Override
                public void run() {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //your code or your request that you want to run on uiThread
                            //CheckDailyPostLimit(post_message, post_collect_image, currentUserID);
                            FirebaseAddPost(post_message, post_collect_image);
                            //InsertPostToMysql(post_user, post_message, post_type, deviceToken);
                        }
                    });
                }
            }).start();
        }else{
            Toast.makeText(this, "Image must be included with some texts", Toast.LENGTH_SHORT).show();
        }

    }

    private void CheckDailyPostLimit(final String post_message, final String post_collect_image, final String currentUserID) {

        final long last24hour = new Date().getTime() - (24 * 3600 * 1000);

        //final Query dailyQuery = RootRef.child("Forum Posts").orderByChild("timestamp").startAt(last24hour).endAt(last24hour + "\uf8ff");

        RootRef.child("Forum Posts").orderByChild("user_id").startAt(currentUserID).endAt(currentUserID + "\uf8ff")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                             for (DataSnapshot d : dataSnapshot.getChildren()) {
                                final Query dd = RootRef.child("Forum Posts").orderByChild("timestamp").startAt(last24hour).endAt(last24hour + "\uf8ff");
                                dd
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                  //  int t1 = (int) dataSnapshot.getChildrenCount();
                                                   // Log.e("t1", String.valueOf(t1));
                                                    // check user
                                                    //if (t1 <= 5) {
                                                        //Toast.makeText(MainActivity.this, "Add post.", Toast.LENGTH_SHORT).show();
                                                        //FirebaseAddPost(post_message, post_collect_image);
                                                   // } else {
                                                     //  Toast.makeText(MainActivity.this, "Daily post limit reached!", Toast.LENGTH_SHORT).show();
                                                   // }
                                                    // check user

                                                } else {
                                                    //FirebaseAddPost(post_message, post_collect_image);
                                                    // Toast.makeText(MainActivity.this, "Add post again", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });


                            }
                        }else{
                            //FirebaseAddPost(post_message, post_collect_image);
                            //Toast.makeText(MainActivity.this, "No posts yet!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });

    }

    private void FirebaseAddPost(final String s_post_message, String s_post_collect_image) {

        final String post_id = ForumPostRef.push().getKey();

        HashMap<String, Object> addPostMap = new HashMap<>();
        addPostMap.put("user_id", currentUserID);
        addPostMap.put("post_id", post_id);
        addPostMap.put("body", s_post_message);
        addPostMap.put("image", s_post_collect_image);
        addPostMap.put("user_id_post_id", currentUserID+post_id);
        addPostMap.put("timestamp", System.currentTimeMillis());
        //addPostMap.put("user_id_timestamp", currentUserID + System.currentTimeMillis());
        ForumPostRef.child(post_id).updateChildren(addPostMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            post_collect_image = "";
                            //RootRef.child("Forum Posts").child(post_id).child("user_id_timestamp")
                                   // .setValue(currentUserID + );
                            // Toast.makeText(UserAccountActivity.this, "Account firebase Updated Successfully...", Toast.LENGTH_SHORT).show();

                        }else{
                            String message = task.getException().toString();
                            Toast.makeText(MainActivity.this, "Error occured", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void RetrieveFirebaseAddPostUserInfo() {
        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if((dataSnapshot).exists() && (dataSnapshot.hasChild("image_bg"))){

                            String retrieveUserName = dataSnapshot.child("username").getValue().toString();
                            String retrieveUserProfileImage = dataSnapshot.child("image_bg").getValue().toString();
                            //firebaseRetrieveUserPostDeviceToken = dataSnapshot.child("deviceToken").getValue().toString();
                            //FirebaseUserEmail = retrieveUserEmail;
                            Picasso.get().load(retrieveUserProfileImage).placeholder(R.drawable.a).into(post_user_pic);
                            user_name.setText(retrieveUserName);

                        }else{
                            String retrieveUserName = dataSnapshot.child("username").getValue().toString();
                            //String retrieveUserStatus = dataSnapshot.child("status").getValue().toString();
                            //FirebaseUserEmail = retrieveUserEmail;
                            user_name.setText(retrieveUserName);

                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });
    }


    public void readPermission()
    {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.INTERNET,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.VIBRATE,
                }, 1);
    }

    private void sendUserToProfile() {
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    private void SendUserToLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){
            SendUserToLoginActivity();
        }else{
            VerifyUserExistence();
        }
    }

    private void VerifyUserExistence() {
        String currentUserID = mAuth.getCurrentUser().getUid();

        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child("username").exists())){

                    ForumPostRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                RedirectForumPostToLatest();
                                //LoadNativeAds();
                            }else{
                                Toast.makeText(MainActivity.this, "No posts available", Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) { }
                    });

                 }else{
                    sendUserToProfile();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void RedirectForumPostToLatest() {
        FirebaseRecyclerOptions<ForumPostModel> options =
                new FirebaseRecyclerOptions.Builder<ForumPostModel>()
                        .setQuery(ForumPostRef, ForumPostModel.class)
                        .build();
        FirebaseRecyclerAdapter<ForumPostModel, ForumPostViewHolder> adapter =
                new FirebaseRecyclerAdapter<ForumPostModel, ForumPostViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ForumPostViewHolder holder, final int position, @NonNull ForumPostModel model) {

                        holder.forum_post_date.setText(getTimeAgo(model.getTimestamp()));

                        final String id_viewimage;
                        if (model.getImage() != null && !model.getImage().equalsIgnoreCase("") && !model.getImage().equalsIgnoreCase("null")) {
                            holder.forum_post_image.setVisibility(View.VISIBLE);
                            id_viewimage = model.getImage();
                            Glide.with(MainActivity.this)
                                    .load(model.getImage())
                                    .apply(new RequestOptions().fitCenter())
                                    .into(holder.forum_post_image);
                            //Picasso.get().load(model.getImage()).placeholder(R.drawable.loading_notify).into(holder.forum_post_image);
                        } else {
                            holder.forum_post_image.setVisibility(View.GONE);
                            id_viewimage = "R.drawable.loading_notify";
                        }

                        if (!model.getBody().isEmpty() && model.getBody() != null && !model.getBody().equalsIgnoreCase("null")) {
                            holder.forum_post_body.setVisibility(View.VISIBLE);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                holder.forum_post_body.setText(Html.fromHtml(model.getBody(), Html.FROM_HTML_MODE_COMPACT));
                            } else {
                                holder.forum_post_body.setText(Html.fromHtml(model.getBody()));
                            }
                        } else {
                            holder.forum_post_body.setVisibility(View.GONE);
                        }

                        final String id_user = model.getUser_id();
                        String id_post = model.getPost_id();

                        final String[] id_name = new String[1];
                        final String[] id_image = new String[1];
                        // add like status
                        RootRef.child("Forum Post Likes").orderByChild("post_user_id").equalTo(id_post + "_ref_" + currentUserID)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.exists()) {

                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                holder.action_like.setImageDrawable(getResources().getDrawable(R.drawable.ic_twitter_like_solid, getApplicationContext().getTheme()));
                                            } else {
                                                Drawable shape = VectorDrawableCompat.create(
                                                        getApplicationContext().getResources(),
                                                        R.drawable.ic_twitter_like_solid,
                                                        getApplicationContext().getTheme()
                                                );
                                                holder.action_like.setImageDrawable(shape);
                                            }

                                        } else {

                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                holder.action_like.setImageDrawable(getResources().getDrawable(R.drawable.ic_twitter_like_outline, getApplicationContext().getTheme()));
                                            } else {
                                                Drawable shape = VectorDrawableCompat.create(
                                                        getApplicationContext().getResources(),
                                                        R.drawable.ic_twitter_like_outline,
                                                        getApplicationContext().getTheme()
                                                );
                                                holder.action_like.setImageDrawable(shape);
                                            }

                                        }
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) { }
                                });
                        // /add like status
                        // retrieve user
                        RootRef.child("Users").orderByChild("uid").equalTo(id_user)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot d : dataSnapshot.getChildren()) {

                                            if ((d).exists() && (d.hasChild("image_bg"))) {
                                                String retrieveUserName = d.child("username").getValue().toString().trim();
                                                String retrieveUserProfileImage = d.child("image_bg").getValue().toString();
                                                id_name[0] = retrieveUserName;
                                                id_image[0] = retrieveUserProfileImage;

                                                Picasso.get().load(retrieveUserProfileImage).placeholder(R.drawable.a).into(holder.forum_user_image);

                                                if (d.hasChild("username")) {
                                                    holder.forum_user_name.setText(retrieveUserName.trim());
                                                }else {
                                                    holder.forum_user_name.setText("No name");
                                                }

                                            } else {
                                                String retrieveUserName = d.child("username").getValue().toString().trim();
                                                holder.forum_user_name.setText(retrieveUserName.trim());
                                                holder.forum_user_image.setImageResource(R.drawable.a);
                                                id_name[0] = retrieveUserName;
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });
                        // / retrieve user

                        // retrieve like
                        RootRef.child("Forum Post Likes").orderByChild("post_id").equalTo(id_post)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        int size = (int) dataSnapshot.getChildrenCount();
                                        //dataSnapshot.child("name").getValue().toString();
                                        if (String.valueOf(size).equals("0")) {
                                            holder.forum_post_like.setText("0");
                                        } else if (String.valueOf(size).equals("1")) {
                                            holder.forum_post_like.setText(String.valueOf(size));
                                        } else {
                                            holder.forum_post_like.setText(String.valueOf(size));
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });
                        // / retrieve like

                        holder.action_like.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                final String post_id = getRef(position).getKey();
                                ManageFirebasePostLike(post_id);

                            }
                        });

                        holder.forum_post_image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                              showViewImageDialog(id_viewimage);
                            }
                        });

                        holder.forum_user_image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                // check follow status
                                RootRef.child("User Follows").orderByChild("following_follow_user").equalTo(currentUserID + "_ref_" + id_user)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                if (dataSnapshot.exists()) {
                                                    showFollowDialog(id_user, id_name[0], id_image[0]);
                                                } else {
                                                    showFollowDialog(id_user, id_name[0], id_image[0]);
                                                }
                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) { }
                                        });
                                // /check follow status

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public ForumPostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.content_main_activity, viewGroup, false);
                        ForumPostViewHolder viewHolder = new ForumPostViewHolder(view);
                        return viewHolder;
                    }
                };

        ForumPostRecyclerList.setAdapter(adapter);

        adapter.startListening();
    }

    private void showViewImageDialog(String id_viewimage) {
        viewImageDialog = new Dialog(this);
        viewImageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        viewImageDialog.setContentView(R.layout.dialog_view_image);
        viewImageDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        showViewImageCloseBtn = viewImageDialog.findViewById(R.id.showViewImageCloseBtn);
        showViewImage = viewImageDialog.findViewById(R.id.showViewImage);

        Picasso.get().load(id_viewimage).placeholder(R.drawable.loading_notify).into(showViewImage);

        showViewImageCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewImageDialog.dismiss();
            }
        });


        viewImageDialog.setCancelable(true);
        viewImageDialog.show();
    }

    private void showFollowDialog(final String id_user, String name, String image) {
        followDialog = new Dialog(this);
        followDialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        followDialog.setContentView(R.layout.dialog_user_follow);
        followDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        followBtn = followDialog.findViewById(R.id.followBtn);
        followCloseBtn = followDialog.findViewById(R.id.followCloseBtn);
        followName = followDialog.findViewById(R.id.followName);
        followStatus = followDialog.findViewById(R.id.followStatus);
        followUserImage = followDialog.findViewById(R.id.followUserImage);

        Picasso.get().load(image).placeholder(R.drawable.a).into(followUserImage);
        followName.setText(name);

        // add follow status
        RootRef.child("User Follows").orderByChild("follow_user_following_user").equalTo(currentUserID + "_ref_" + id_user)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            followStatus.setText("Unfollow");
                        } else {
                            followStatus.setText("Follow");
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });
        // /add follow status

        followCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                followDialog.dismiss();
            }
        });

        followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ManageUserFollow(id_user);
            }
        });

        followDialog.setCancelable(true);
        followDialog.show();

    }

    private void ManageUserFollow(final String id_user) {
        RootRef.child("User Follows").orderByChild("follow_user_following_user").equalTo(currentUserID + "_ref_" + id_user)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {

                            deleteUserFollow(id_user);

                        } else {
                            addUserFollow(id_user);

                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });
    }

    private void addUserFollow(String id_user) {
        final String follow_id = RootRef.child("User Follows").push().getKey();

        HashMap<String, Object> addPostMap = new HashMap<>();
        addPostMap.put("follow_id", follow_id);
        addPostMap.put("follow_user", currentUserID);
        addPostMap.put("following_user", id_user);
        addPostMap.put("follow_user_following_user", currentUserID + "_ref_" + id_user);
        addPostMap.put("timestamp", ServerValue.TIMESTAMP);
        RootRef.child("User Follows").child(follow_id).updateChildren(addPostMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(MainActivity.this, "User followed", Toast.LENGTH_SHORT).show();
                            followDialog.dismiss();
                        }else{
                            String message = task.getException().toString();
                            Toast.makeText(MainActivity.this, "Unable to follow ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void deleteUserFollow(String id_user) {
        RootRef.child("User Follows").orderByChild("follow_user_following_user").equalTo(currentUserID + "_ref_" + id_user)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot child: dataSnapshot.getChildren()) {
                            child.getRef().setValue(null);
                            Toast.makeText(MainActivity.this, "User unfollowed", Toast.LENGTH_SHORT).show();
                            followDialog.dismiss();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });
    }

    public static class ForumPostViewHolder extends RecyclerView.ViewHolder{
        CircularImageView forum_user_image;
        TextView forum_user_name, forum_post_body, forum_post_date, forum_post_like, explainer, txtType;
        ImageView forum_post_image, action_like;
        CardView cardviewImage;

        public ForumPostViewHolder(@NonNull View itemView) {
            super(itemView);

            forum_user_image = itemView.findViewById(R.id.image_bg);
            forum_user_name = itemView.findViewById(R.id.username);
            forum_post_body = itemView.findViewById(R.id.body);
            forum_post_date = itemView.findViewById(R.id.forum_post_date);
            forum_post_image= itemView.findViewById(R.id.forum_post_image);
            forum_post_like = itemView.findViewById(R.id.likes);
            action_like = itemView.findViewById(R.id.tweet_action_like);
            explainer = itemView.findViewById(R.id.explainer);
            txtType = itemView.findViewById(R.id.txtType);
            cardviewImage = itemView.findViewById(R.id.cardviewImage);

        }
    }

    private void ManageFirebasePostLike(final String post_id) {
        RootRef.child("Forum Post Likes").orderByChild("post_user_id").equalTo(post_id + "_ref_" + currentUserID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            deletePostLike(post_id);

                            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_thumb_up, getApplicationContext().getTheme()));
                            } else {
                                fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_thumb_up));
                            }*/

                        } else {
                            addPostLike(post_id);

                            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_thumb_down, getApplicationContext().getTheme()));
                            } else {
                                fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_thumb_down));
                            }*/

                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });
    }

    private void addPostLike(final String post_id){

        final String like_id = ForumPostLikeRef.push().getKey();

        HashMap<String, Object> addPostMap = new HashMap<>();
        addPostMap.put("user_id", currentUserID);
        addPostMap.put("post_id", post_id);
        addPostMap.put("like_id", like_id);
        addPostMap.put("post_user_id", post_id + "_ref_" + currentUserID);
        addPostMap.put("timestamp", ServerValue.TIMESTAMP);
        ForumPostLikeRef.child(like_id).updateChildren(addPostMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            addPostEarning(post_id);
                        }else{
                            String message = task.getException().toString();
                            Toast.makeText(MainActivity.this, "Error occurred ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void addPostEarning(String post_id) {
        final String earn_id = ForumPostEarningRef.push().getKey();

        HashMap<String, Object> addPostMap = new HashMap<>();
        addPostMap.put("user_id", currentUserID);
        addPostMap.put("post_id", post_id);
        addPostMap.put("earn_id", earn_id);
        addPostMap.put("post_user_id", post_id + "_ref_" + currentUserID);
        addPostMap.put("timestamp", ServerValue.TIMESTAMP);
        ForumPostEarningRef.child(earn_id).updateChildren(addPostMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            RootRef.child("Users").child(currentUserID).child("bonus")
                                    .setValue(0);
                            // Success
                        }else{
                            String message = task.getException().toString();
                            Toast.makeText(MainActivity.this, "Error occurred ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void deletePostLike(final String post_id){
        RootRef.child("Forum Post Likes").orderByChild("post_user_id").equalTo(post_id + "_ref_" + currentUserID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot child: dataSnapshot.getChildren()) {
                            child.getRef().setValue(null);
                            deletePostEarning(post_id);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });
    }

    private void deletePostEarning(String post_id) {
        RootRef.child("Forum Post Earnings").orderByChild("post_user_id").equalTo(post_id + "_ref_" + currentUserID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot child: dataSnapshot.getChildren()) {
                            child.getRef().setValue(null);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem myActionMenuItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView)myActionMenuItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint("Search User...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (TextUtils.isEmpty(s)){
                    firebaseSearch("");
                    RedirectForumPostToLatest();
                }else{
                    firebaseSearch(s);
                }
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            mAuth.getInstance().signOut();
            SendUserToLoginActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private void firebaseSearch(String searchText) {
        String query = searchText.toLowerCase();

        Query firebaseSearchQuery = RootRef.child("Users").orderByChild("username").startAt(query.toUpperCase()).endAt(query.toLowerCase() + "\uf8ff");
        FirebaseRecyclerOptions<ForumPostModel> options =
                new FirebaseRecyclerOptions.Builder<ForumPostModel>()
                        .setQuery(firebaseSearchQuery, ForumPostModel.class)
                        .build();
        FirebaseRecyclerAdapter<ForumPostModel, ForumPostViewHolder> adapter =
                new FirebaseRecyclerAdapter<ForumPostModel, ForumPostViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ForumPostViewHolder holder, final int position, @NonNull ForumPostModel model) {

                       // holder.forum_post_date.setText(getTimeAgo(model.getTimestamp()));

                        final String id_viewimage;
                       /* if (model.getImage() != null && !model.getImage().equalsIgnoreCase("") && !model.getImage().equalsIgnoreCase("null")) {
                            holder.forum_post_image.setVisibility(View.VISIBLE);
                            id_viewimage = model.getImage();
                            Picasso.get().load(model.getImage()).placeholder(R.drawable.loading_notify).into(holder.forum_post_image);
                        } else {
                            holder.forum_post_image.setVisibility(View.GONE);
                            id_viewimage = "R.drawable.loading_notify";
                        }

                        if (!model.getBody().isEmpty() && model.getBody() != null && !model.getBody().equalsIgnoreCase("null")) {
                            holder.forum_post_body.setVisibility(View.VISIBLE);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                holder.forum_post_body.setText(Html.fromHtml(model.getBody(), Html.FROM_HTML_MODE_COMPACT));
                            } else {
                                holder.forum_post_body.setText(Html.fromHtml(model.getBody()));
                            }
                        } else {
                            holder.forum_post_body.setVisibility(View.GONE);
                        }*/

                        final String id_user = model.getUser_id();
                        //String id_post = model.getPost_id();

                        final String[] id_name = new String[1];
                        final String[] id_image = new String[1];
                        holder.forum_user_name.setText(model.getUsername());
                        Picasso.get().load(model.getImage_bg()).placeholder(R.drawable.a).into(holder.forum_user_image);
                        id_name[0] = model.getUsername();
                        id_image[0] = model.getImage_bg();

                        holder.forum_post_body.setVisibility(View.GONE);
                        holder.explainer.setVisibility(View.GONE);
                        holder.forum_post_image.setVisibility(View.GONE);
                        holder.forum_post_like.setVisibility(View.GONE);
                        holder.action_like.setVisibility(View.GONE);
                        holder.forum_post_date.setVisibility(View.GONE);
                        holder.cardviewImage.setVisibility(View.GONE);
                        holder.txtType.setVisibility(View.GONE);

                        // add like status
                       /* RootRef.child("Forum Post Likes").orderByChild("post_user_id").equalTo(id_post + "_ref_" + currentUserID)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.exists()) {

                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                holder.action_like.setImageDrawable(getResources().getDrawable(R.drawable.ic_twitter_like_solid, getApplicationContext().getTheme()));
                                            } else {
                                                Drawable shape = VectorDrawableCompat.create(
                                                        getApplicationContext().getResources(),
                                                        R.drawable.ic_twitter_like_solid,
                                                        getApplicationContext().getTheme()
                                                );
                                                holder.action_like.setImageDrawable(shape);
                                            }

                                        } else {

                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                holder.action_like.setImageDrawable(getResources().getDrawable(R.drawable.ic_twitter_like_outline, getApplicationContext().getTheme()));
                                            } else {
                                                Drawable shape = VectorDrawableCompat.create(
                                                        getApplicationContext().getResources(),
                                                        R.drawable.ic_twitter_like_outline,
                                                        getApplicationContext().getTheme()
                                                );
                                                holder.action_like.setImageDrawable(shape);
                                            }

                                        }
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) { }
                                });*/
                        // /add like status
                        // retrieve user
                       /* RootRef.child("Users").orderByChild("uid").equalTo(id_user)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot d : dataSnapshot.getChildren()) {

                                            if ((d).exists() && (d.hasChild("image_bg"))) {
                                                String retrieveUserName = d.child("username").getValue().toString().trim();
                                                String retrieveUserProfileImage = d.child("image_bg").getValue().toString();
                                                id_name[0] = retrieveUserName;
                                                id_image[0] = retrieveUserProfileImage;

                                                Picasso.get().load(retrieveUserProfileImage).placeholder(R.drawable.a).into(holder.forum_user_image);

                                                if (d.hasChild("username")) {
                                                    holder.forum_user_name.setText(retrieveUserName.trim());
                                                }else {
                                                    holder.forum_user_name.setText("No name");
                                                }

                                            } else {
                                                String retrieveUserName = d.child("username").getValue().toString().trim();
                                                holder.forum_user_name.setText(retrieveUserName.trim());
                                                holder.forum_user_image.setImageResource(R.drawable.a);
                                                id_name[0] = retrieveUserName;
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });*/
                        // / retrieve user

                        // retrieve like
                       /* RootRef.child("Forum Post Likes").orderByChild("post_id").equalTo(id_post)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        int size = (int) dataSnapshot.getChildrenCount();
                                        //dataSnapshot.child("name").getValue().toString();
                                        if (String.valueOf(size).equals("0")) {
                                            holder.forum_post_like.setText("0");
                                        } else if (String.valueOf(size).equals("1")) {
                                            holder.forum_post_like.setText(String.valueOf(size));
                                        } else {
                                            holder.forum_post_like.setText(String.valueOf(size));
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });*/
                        // / retrieve like

                       /* holder.action_like.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                final String post_id = getRef(position).getKey();
                                ManageFirebasePostLike(post_id);

                            }
                        });

                        holder.forum_post_image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                showViewImageDialog(id_viewimage);
                            }
                        });*/

                        holder.forum_user_image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                // check follow status
                                RootRef.child("User Follows").orderByChild("following_follow_user").equalTo(currentUserID + "_ref_" + id_user)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                if (dataSnapshot.exists()) {
                                                    showFollowDialog(id_user, id_name[0], id_image[0]);
                                                } else {
                                                    showFollowDialog(id_user, id_name[0], id_image[0]);
                                                }
                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) { }
                                        });
                                // /check follow status

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public ForumPostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.content_main_activity, viewGroup, false);
                        ForumPostViewHolder viewHolder = new ForumPostViewHolder(view);
                        return viewHolder;
                    }
                };

        ForumPostRecyclerList.setAdapter(adapter);

        adapter.startListening();
    }


}
