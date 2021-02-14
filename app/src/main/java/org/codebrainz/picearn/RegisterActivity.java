package org.codebrainz.picearn;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private ProgressDialog loadingBar;
    private View parent_view;
    private EditText txtEmail, txtPassword, txtRetypePassword;
    private TextView loginpage;
    private Button register_btn;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String deviceToken;

    private static final String BASE_URL = "https://yourcampushub.online/mobile/campushub/register_user.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // In Activity's onCreate() for instance
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
        deviceToken = FirebaseInstanceId.getInstance().getToken();

        InitializeFields();

        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                searchAction();
            }
        });

        loginpage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });

    }

    private void InitializeFields() {
        parent_view = findViewById(android.R.id.content);
        loginpage = findViewById(R.id.textViewLogin);
        txtEmail = findViewById(R.id.register_email);
        txtPassword = findViewById(R.id.register_password);
        txtRetypePassword = findViewById(R.id.register_retypepassword);
        register_btn = findViewById(R.id.register_btn);
        loadingBar = new ProgressDialog(this);

    }

    private void searchAction() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                VerifyUserInput();
                //Snackbar.make(parent_view, "Login data submitted", Snackbar.LENGTH_SHORT).show();
            }
        }, 500);
    }


    private void VerifyUserInput() {

        register_btn.setEnabled(false);

        final String email = txtEmail.getText().toString().trim();
        final String password = txtPassword.getText().toString().trim();
        final String retype_password = txtRetypePassword.getText().toString().trim();

        if (email.isEmpty()) {
            txtEmail.setError("Email Address is required");
            txtEmail.requestFocus();
            register_btn.setEnabled(true);
            return;
        }

        if (password.isEmpty()) {
            txtPassword.setError("Password is required");
            txtPassword.requestFocus();
            register_btn.setEnabled(true);
            return;
        }

        if (retype_password.isEmpty()) {
            txtRetypePassword.setError("Retype Password is required");
            txtRetypePassword.requestFocus();
            register_btn.setEnabled(true);
            return;
        }

        if (password.length() < 6) {
            txtPassword.setError("Password should be atleast 6 characters long");
            txtPassword.requestFocus();
            register_btn.setEnabled(true);
            return;
        }

        if (retype_password.length() < 6) {
            txtRetypePassword.setError("Retype Password should be atleast 6 characters long");
            txtRetypePassword.requestFocus();
            register_btn.setEnabled(true);
            return;
        }

        if (!password.equals(retype_password)){
            txtPassword.setError("Password mismatched");
            txtRetypePassword.setError("Password mismatched");
            txtPassword.requestFocus();
            register_btn.setEnabled(true);
            return;
        }

        if (!email.isEmpty() && !password.isEmpty() && (password.equals(retype_password))) {
            //Generate random int value from 50 to 100
            /*int min = 50;
            int max = 1000000000;
            System.out.println("Random value in int from "+min+" to "+max+ ":");
            int random_int = (int)(Math.random() * (max - min + 1) + min);*/

            CreateFirebaseAccount(email, password);

            txtEmail.setText("");
            txtPassword.setText("");
            txtRetypePassword.setText("");
            register_btn.setEnabled(true);
        } else {
            register_btn.setEnabled(true);
            Toast.makeText(this, "Some fields are empty...", Toast.LENGTH_SHORT).show();
        }
    }

    private void CreateFirebaseAccount(String email, String password) {

        loadingBar.setTitle("Creating New Account");
        loadingBar.setMessage("Please wait...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            String currentUserId = mAuth.getCurrentUser().getUid();

                            RootRef.child("Users").child(currentUserId).setValue("");

                            RootRef.child("Users").child(currentUserId).child("device_token")
                                    .setValue(deviceToken);
                            RootRef.child("Users").child(currentUserId).child("bonus")
                                    .setValue(10);

                            SendUserToDashboardActivity();
                            Toast.makeText(RegisterActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();

                        }else{
                            String message = task.getException().toString();
                            Toast.makeText(RegisterActivity.this, "Not Successful.", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });

    }

    private void SendUserToDashboardActivity(){
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}