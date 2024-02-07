package com.pgshare.studentroomsharingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity {

    private EditText editTextName,editTextEmail,editTextPhoneNo,editTextAadhar,editTextPassword,editTextConfirmPassword;
    private RadioGroup radioGroupGender;
    private RadioButton radioButtonSelected;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        editTextName=findViewById(R.id.editTextName);
        editTextEmail=findViewById(R.id.editTextEmail);
        editTextPhoneNo=findViewById(R.id.editTextPhone);
        editTextAadhar=findViewById(R.id.editTextAadhar);
        editTextPassword=findViewById(R.id.passwordEditText);
        editTextConfirmPassword=findViewById(R.id.editTextConfirmPassword);

        radioGroupGender = findViewById(R.id.radioGroup_Gender);
        radioGroupGender.clearCheck();

    }


    public void RegisterBtn(View view) {
        int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();
        radioButtonSelected = findViewById(selectedGenderId);

        //Get Values from User
        String Name = editTextName.getText().toString();
        String Email = editTextEmail.getText().toString();
        String PhoneNo = editTextPhoneNo.getText().toString();
        String Aadhar = editTextAadhar.getText().toString();
        String password = editTextPassword.getText().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString();


        //Validate Data
        if (!validateName() | !validateEmail() | !validatePhone() | !validateAadhar() | !validatePassword() |  !validateGender()) {
            return;
        }
        else if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Password does not match");
            editTextConfirmPassword.requestFocus();
            //clear the entered Password fields
            editTextPassword.clearComposingText();
            editTextConfirmPassword.clearComposingText();
            return;
        }
        else {
            String Gender = radioButtonSelected.getText().toString();
            FirebaseAuth auth = FirebaseAuth.getInstance();
            // Pass the App Check token to createUserWithEmailAndPassword
            auth.createUserWithEmailAndPassword(Email, password).addOnCompleteListener(SignUp.this, task -> {
                if (task.isSuccessful()) {


                    //add user to database
                    firebaseUser = auth.getCurrentUser();

                    //Extract Data
                    reference = FirebaseDatabase.getInstance().getReference("Users");
                    //enter User Data into  Real-time database
                    UserHelper userHelper=new UserHelper(Name,Email,PhoneNo,Aadhar,Gender);

                    reference.child(firebaseUser.getUid()).setValue(userHelper).addOnCompleteListener(task1 -> {

                        if (task1.isSuccessful()) {
                            //send email verification
                            firebaseUser.sendEmailVerification();
                            Toast.makeText(SignUp.this, "Registration Successful. Please verify your email", Toast.LENGTH_SHORT).show();

                            //redirect to profile page
                            Intent intent = new Intent(SignUp.this, Login.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish(); //finish current activity


                        }else {
                            String errorMessage = task1.getException().getMessage();
                            Toast.makeText(SignUp.this, errorMessage, Toast.LENGTH_LONG).show();
                        }

                    }).addOnFailureListener(e -> {
                        Log.e("Firebase", "Failed to store user data:", e);
                    });

                }

            });

        }


    }

    private boolean validateGender() {
        if (radioGroupGender.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select Gender", Toast.LENGTH_SHORT).show();
            radioButtonSelected.setError("Please select Gender");
            radioButtonSelected.requestFocus();

            return false;
        } else {
            return true;
        }
    }


    private boolean validateName() {
        String val = editTextName.getText().toString();
        String noWhitespace = "\\S+";
        if (val.isEmpty()) {
            editTextName.setError("Field can not be empty");
            return false;
        } else if (val.length() > 20) {
            editTextName.setError("Field can not be Greater than 20 characters");
            return false;
        } else if (!val.matches(noWhitespace)) {
            editTextName.setError("Field can not contain only whitespaces");
            return false;
        } else {
            editTextName.setError(null);
            editTextName.setEnabled(true);
            return true;
        }
    }

    private boolean validateEmail() {
        String val = editTextEmail.getText().toString();
        if (val.isEmpty()) {
            editTextEmail.setError("Field can not be empty");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(val).matches()) {
            editTextEmail.setError("Invalid Email");
            return false;

        } else {
            editTextEmail.setError(null);
            editTextEmail.setEnabled(true);
            return true;
        }
    }

    private boolean validatePhone() {
        String val = editTextPhoneNo.getText().toString();
        if (val.isEmpty()) {
            editTextPhoneNo.setError("Field can not be empty");
            return false;
        } else if (val.length() != 10) {
            editTextPhoneNo.setError("Invalid Mobile Number");
            return false;
        }
        else {
            editTextPhoneNo.setError(null);
            editTextPhoneNo.setEnabled(true);
            return true;
        }
    }

    private boolean validateAadhar() {
        String val = editTextAadhar.getText().toString();
        if (val.isEmpty()) {
            editTextAadhar.setError("Field can not be empty");
            return false;
        } else if (val.length() != 12) {
            editTextAadhar.setError("Invalid Aadhar Number");
            return false;

        }else {
            editTextAadhar.setError(null);
            editTextAadhar.setEnabled(true);
            return true;
        }
    }

    private boolean validatePassword() {
        String val = editTextPassword.getText().toString();
//        String passwordVal = "^" + // start of string
//               // "(?=.*[a-z])" + // at least 1 lower case letter
//               // "(?=.*[A-Z])" + // at least 1 upper case letter
//                //"(?=.*[0-9])" + // at least 1 digit
//                "(?=.*[!@#&()–[{}]:;',?/*~$^+=<>])" // at least 1 special character
//                + ".{8,20}" // at least 8 characters
//                + "$";// end of string
        if (val.isEmpty()) {
            editTextPassword.setError("Field can not be empty");
            return false;
        }
//        else if(!val.matches(passwordVal)) {
//            editTextPassword.setError("Password is too weak");
//            return false;
//        }
        else {
            editTextPassword.setError(null);
            editTextPassword.setEnabled(true);
            return true;
        }
    }

}
