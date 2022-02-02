package com.example.chitchat.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.example.chitchat.R
import com.example.chitchat.repository.ChatDatabaseImpl
import com.example.chitchat.repository.ChatDatabaseRepositoryImpl
import com.example.chitchat.models.appModels.Resource
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class AuthActivity: AppCompatActivity() {

    companion object{
        private const val RC_SIGN_IN = 10
        private const val TAG = "auth"
    }

    private val scope = MainScope()
    private val database = ChatDatabaseImpl(ChatDatabaseRepositoryImpl())
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var callbackManager: CallbackManager
    private lateinit var buttonFacebookSignIn: LoginButton
    private lateinit var buttonGoogleSignIn: SignInButton
    private val addUserResult = MutableLiveData<Resource<Boolean>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        buttonFacebookSignIn = findViewById(R.id.buttonFacebookSignIn)
        buttonGoogleSignIn = findViewById(R.id.buttonGoogleSignIn)

        auth = Firebase.auth
        //Google and Facebook login procedures are implemented according to the docs and official tutorials
        enableGoogleSignIn()
        enableFacebookSignIn()
        setOnSignInListener()
        observeAddUser()
    }

    private fun enableGoogleSignIn(){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun enableFacebookSignIn(){
        callbackManager = CallbackManager.Factory.create()
        buttonFacebookSignIn.setPermissions("email", "public_profile")
        Log.i("auth", "result")
        buttonFacebookSignIn.registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
                Log.i("auth", "success")
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
            }
        })

    }

    private fun addNewUser(currentUser: FirebaseUser) {
        scope.launch {
            addUserResult.value = database.addNewUser(currentUser)
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)

    }

    private fun observeAddUser(){
        addUserResult.observe(this, { resource ->
            if (resource is Resource.Success){
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                this.finish()
            }
        })
    }


    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser == null) {
            Log.w(TAG, "User is null")
            return
        }
        Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show()
        addNewUser(currentUser) //checking if logged user already exists in the database or creates him if needed
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
            }
        }
        else {
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(
                        this, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUI(null)
                }
            }
    }


    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null)
            updateUI(auth.currentUser)

    }

    private fun setOnSignInListener(){
        buttonGoogleSignIn.setOnClickListener {
            signIn()
        }
    }
}