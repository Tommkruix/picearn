package org.codebrainz.picearn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import org.codebrainz.picearn.utils.Tools;

import java.util.HashMap;

public class MessagesActivity extends AppCompatActivity {

    TextView user_name;

    ImageView user_pic;


    private StorageReference UsersProfileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String currentUserID;

    private CircularImageView post_user_pic;
    private TextView post_type, post_thread;
    private EditText btn_file_document, post_image;
    private String mysqlRetrieveUserPostDeviceToken;
    private String firebaseRetrieveUserPostDeviceToken;

    private ProgressDialog loadingBar;

    private DatabaseReference MessagesRef;

    private StorageReference UserProfileImagesRef;
    private FirebaseUser currentUser;

    private RecyclerView ForumPostRecyclerList;

    String post_collect_image, post_collect_document, RedirectPostButton;

    private String checker = "";

    private static final int GalleryPick = 1;

    private Uri fileUri;

    private StorageReference ForumPostImagesRef, ForumPostDocumentsRef;


    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        mAuth = FirebaseAuth.getInstance();
        //currentUser = mAuth.getCurrentUser();
        RootRef = FirebaseDatabase.getInstance().getReference();
        currentUserID = mAuth.getCurrentUser().getUid();

        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        MessagesRef = FirebaseDatabase.getInstance().getReference().child("Messages");


        Tools.setSystemBarColor(this, R.color.colorPrimary);


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
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Withdrawal Lists");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MessagesActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
        Tools.setSystemBarColor(this, R.color.colorPrimaryDark);
    }

    @Override
    protected void onStart() {
        super.onStart();

        MessagesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            MessagesLists();
                        }else{
                            Toast.makeText(MessagesActivity.this, "No withdrawals available", Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });

    }

    private void MessagesLists() {
        FirebaseRecyclerOptions<MessagesModel> options =
                new FirebaseRecyclerOptions.Builder<MessagesModel>()
                        .setQuery(MessagesRef, MessagesModel.class)
                        .build();
        FirebaseRecyclerAdapter<MessagesModel, MessagesViewHolder> adapter =
                new FirebaseRecyclerAdapter<MessagesModel, MessagesViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final MessagesViewHolder holder, final int position, @NonNull MessagesModel model) {

                        holder.user_date.setText("(" + DateUtils.getRelativeTimeSpanString(model.getTimestamp()) + ")");
                        holder.user_amount.setText(Integer.toString(model.getAmount()) + " points");
                        holder.user_address.setText(model.getEarning_address());

                        final String id_user = String.valueOf(model.getUser_id());
                        final String message_id = model.getMessage_id();
                        final int amount = model.getAmount();
                        final String earning_address = model.getEarning_address();

                        // retrieve user
                        RootRef.child("Users").orderByChild("uid").equalTo(id_user)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot d : dataSnapshot.getChildren()) {

                                            if ((d).exists() && (d.hasChild("image_bg"))) {
                                                String retrieveUserName = d.child("username").getValue().toString();
                                                String retrieveUserProfileImage = d.child("image_bg").getValue().toString();

                                                Picasso.get().load(retrieveUserProfileImage).placeholder(R.drawable.a).into(holder.user_image);

                                                if (d.hasChild("username")) {
                                                    holder.user_name.setText(retrieveUserName);
                                                }else {
                                                    holder.user_name.setText("No name");
                                                }

                                            } else {
                                                String retrieveUserName = d.child("username").getValue().toString();
                                                holder.user_name.setText(retrieveUserName);
                                                holder.user_image.setImageResource(R.drawable.a);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });
                        // / retrieve user

                        holder.btn_cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String key = getRef(position).getKey();
                                CancelWithdraw(message_id, id_user);
                            }
                        });

                        holder.btn_reject.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String key = getRef(position).getKey();
                                RejectWithdraw(message_id, id_user, amount);
                            }
                        });

                        holder.btn_accept.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String key = getRef(position).getKey();
                                AcceptWithdraw(message_id, id_user, amount, earning_address);
                            }
                        });

                    }

                    @NonNull
                    @Override
                    public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.content_messages, viewGroup, false);
                        MessagesViewHolder viewHolder = new MessagesViewHolder(view);
                        return viewHolder;
                    }
                };

        ForumPostRecyclerList.setAdapter(adapter);

        adapter.startListening();
    }

    private void AcceptWithdraw(String message_id, final String id_user, final int amount, final String earning_address) {
        RootRef.child("Messages").child(message_id).orderByChild("message_user_id").equalTo(message_id + "_ref_" + id_user)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dataSnapshot.getRef().setValue(null);
                        SaveUserWithdraw(id_user, amount, earning_address);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });
    }

    private void CancelWithdraw(String message_id, String id_user) {
        RootRef.child("Messages").child(message_id).orderByChild("message_user_id").equalTo(message_id + "_ref_" + id_user)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dataSnapshot.getRef().setValue(null);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });
    }

    private void RejectWithdraw(String message_id, final String id_user, final int amount) {
        RootRef.child("Messages").child(message_id).orderByChild("message_user_id").equalTo(message_id + "_ref_" + id_user)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dataSnapshot.getRef().setValue(null);
                        ReturnUserEarning(id_user, amount);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });
    }

    private void ReturnUserEarning(String user_id, int amount){
        RootRef.child("Users").child(user_id).child("bonus")
                .setValue(amount);
    }

    private void SaveUserWithdraw(String user_id, int amount, String earning_address){
        final String withdraw_id = RootRef.child("Withdraws").push().getKey();

        HashMap<String, Object> addPostMap = new HashMap<>();
        addPostMap.put("user_id", user_id);
        addPostMap.put("withdraw_id", withdraw_id);
        addPostMap.put("amount", amount);
        addPostMap.put("earning_address", earning_address);
        addPostMap.put("user_id_withdraw_id", user_id+withdraw_id);
        addPostMap.put("timestamp", ServerValue.TIMESTAMP);
        RootRef.child("Withdraws").child(withdraw_id).updateChildren(addPostMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(MessagesActivity.this, "Accepted", Toast.LENGTH_SHORT).show();

                        }else{
                            Toast.makeText(MessagesActivity.this, "Not accepted", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public static class MessagesViewHolder extends RecyclerView.ViewHolder{
        CircularImageView user_image;
        TextView user_name, user_amount, user_date, user_address;
        Button btn_reject, btn_cancel, btn_accept;

        public MessagesViewHolder(@NonNull View itemView) {
            super(itemView);

            user_name = itemView.findViewById(R.id.user_name);
            user_amount = itemView.findViewById(R.id.user_amount);
            user_date = itemView.findViewById(R.id.user_date);
            user_image = itemView.findViewById(R.id.user_image);
            btn_cancel = itemView.findViewById(R.id.btn_cancel);
            btn_reject= itemView.findViewById(R.id.btn_reject);
            btn_accept = itemView.findViewById(R.id.btn_accept);
            user_address = itemView.findViewById(R.id.user_address);
        }
    }
}
