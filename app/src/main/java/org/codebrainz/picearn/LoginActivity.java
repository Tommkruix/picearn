package org.codebrainz.picearn;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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

public class LoginActivity extends AppCompatActivity {

    private Button login_btn;
    private ProgressDialog loadingBar;
    private View parent_view;
    private EditText txtEmail, txtPassword;
    private TextView registerpage, textViewForgotpassword;

    private FirebaseAuth mAuth;

    private DatabaseReference UsersRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
            }
        });

        textViewForgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(i);
            }
        });


    }

    private void initializeFields() {
        parent_view = findViewById(android.R.id.content);
        txtEmail = findViewById(R.id.login_email);
        txtPassword = findViewById(R.id.login_password);
        registerpage = findViewById(R.id.textViewRegister);
        login_btn = findViewById(R.id.login_btn);
        textViewForgotpassword = findViewById(R.id.textViewForgotpassword);
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
        final String password = txtPassword.getText().toString().trim();

        if (email.isEmpty()) {
            txtEmail.setError("Email Address is required");
            txtEmail.requestFocus();
            login_btn.setEnabled(true);
            return;
        }

        if (password.isEmpty()) {
            txtPassword.setError("Password is required");
            txtPassword.requestFocus();
            login_btn.setEnabled(true);
            return;
        }

        if (password.length() < 6) {
            txtPassword.setError("Password should be atleast 6 characters long");
            txtPassword.requestFocus();
            login_btn.setEnabled(true);
            return;
        }

        if (!email.isEmpty() && !password.isEmpty()) {
            LoginFirebaseAccount(email, password);
           // LoginMysqlAccount(email, password);
            //txtEmail.setText("");
            //txtPassword.setText("");
            login_btn.setEnabled(true);
        } else {
            login_btn.setEnabled(true);
            Toast.makeText(this, "Some fields are empty...", Toast.LENGTH_SHORT).show();
        }
    }

    /*private void LoginMysqlAccount(final String email, final String password) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, BASE_URL,
                new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response){
                        try {
                            JSONObject jsonObject;
                            jsonObject = new JSONObject(response);
                            Log.e("VOLLEY testing response", response);

                            String success = jsonObject.getString("success");
                            JSONArray jsonArray = jsonObject.getJSONArray("login");

                            if(success.equals("1")){

                                for (int i = 0; i < jsonArray.length(); i++){
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    String uid = object.getString("id").trim();

                                    fab.setEnabled(true);
                                    Intent intent = new Intent(LoginActivity.this,  DashboardActivity.class);
                                    intent.putExtra("currentUserID", uid);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }

                            }else{
                                fab.setEnabled(true);
                                Toast.makeText(LoginActivity.this, "User doest not exists...", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            fab.setEnabled(true);
                            Toast.makeText(LoginActivity.this, "Error occured.", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        fab.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "Error occured.", Toast.LENGTH_SHORT).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }*/

    private void LoginFirebaseAccount(String email, String password) {

        loadingBar.setTitle("Logging in");
        loadingBar.setMessage("Please wait...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String currentUserID = mAuth.getCurrentUser().getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            UsersRef.child(currentUserID).child("device_token")
                                    .setValue(deviceToken)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                    SendUserToDashboardActivity();
                                                    Toast.makeText(LoginActivity.this, "Logged in.", Toast.LENGTH_SHORT).show();
                                                    login_btn.setEnabled(true);
                                                    loadingBar.dismiss();


                                             }
                                        }
                                    });

                        }else{
                            String message = task.getException().toString();
                            Toast.makeText(LoginActivity.this, "Not Successful.", Toast.LENGTH_SHORT).show();
                            login_btn.setEnabled(true);
                            loadingBar.dismiss();
                         }
                    }
                });
    }

    private void SendUserToDashboardActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if(doubleBackToExitPressedOnce){
            super.onBackPressed();
            return;
        }else{
            Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 10000); //Change time interval here if you want
    }
    boolean doubleBackToExitPressedOnce;
}
