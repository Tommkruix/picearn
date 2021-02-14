package org.codebrainz.picearn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ForgotPasswordActivity extends AppCompatActivity {

    private Button login_btn;
    private ProgressDialog loadingBar;
    private View parent_view;
    private EditText txtEmail, txtPassword;
    private TextView registerpage;

    private FirebaseAuth mAuth;

    private DatabaseReference UsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // In Activity's onCreate() for instance
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        initializeFields();


        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                searchAction();
            }
        });

        registerpage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });
    }

    private void initializeFields() {
        parent_view = findViewById(android.R.id.content);
        txtEmail = findViewById(R.id.login_email);
        registerpage = findViewById(R.id.textViewRegister);
        login_btn = findViewById(R.id.login_btn);
        loadingBar = new ProgressDialog(this);
    }


    private void searchAction() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //progress_bar.setVisibility(View.GONE);
                //Snackbar.make(parent_view, "Login data submitted", Snackbar.LENGTH_SHORT).show();
                VerifyUserInput();
            }
        }, 500);
    }

    private void VerifyUserInput() {

        login_btn.setEnabled(false);

        final String email = txtEmail.getText().toString().trim();

        if (email.isEmpty()) {
            txtEmail.setError("Email Address is required");
            txtEmail.requestFocus();
            login_btn.setEnabled(true);
            return;
        }

        if (!email.isEmpty()) {
            ResetPassword(email);
            // LoginMysqlAccount(email, password);
            //txtEmail.setText("");
            //txtPassword.setText("");
            login_btn.setEnabled(true);
        } else {
            login_btn.setEnabled(true);
            Toast.makeText(this, "Some fields are empty...", Toast.LENGTH_SHORT).show();
        }
    }

    public void ResetPassword(String email){
        mAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPasswordActivity.this, "Email Sent", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(ForgotPasswordActivity.this, "Unsuccessful.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
