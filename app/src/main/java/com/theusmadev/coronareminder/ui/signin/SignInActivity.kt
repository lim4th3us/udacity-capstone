package com.theusmadev.coronareminder.ui.signin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.theusmadev.coronareminder.R
import com.theusmadev.coronareminder.databinding.ActivitySignInBinding
import com.theusmadev.coronareminder.ui.coronareminders.CoronaRemindersActivity
import com.theusmadev.coronareminder.ui.signup.SignUpActivity
import org.koin.android.ext.android.inject

class SignInActivity : AppCompatActivity() {

    private val viewModel: SignInViewModel by inject()

    private lateinit var activitySignInBinding: ActivitySignInBinding
    private lateinit var gso: GoogleSignInOptions
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupGoogleSignIn()

        activitySignInBinding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in)
        activitySignInBinding.viewModel = viewModel
        activitySignInBinding.loadingStatus = false

        activitySignInBinding.signUpText.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        activitySignInBinding.siginOptionGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.showLoading.observe(this, Observer { isLoading ->
            isLoading?.let { isLoadingNotNull ->
                activitySignInBinding.loadingStatus = isLoadingNotNull
                activitySignInBinding.invalidateAll()
            }

        })
        viewModel.userLogged.observe(this, Observer { isLogged ->
            isLogged?.let { isLoggedNotNull ->
                activitySignInBinding.loadingStatus = false
                activitySignInBinding.invalidateAll()

                if(isLoggedNotNull) {
                    viewModel.onUserLogged()
                    startActivity(Intent(this, CoronaRemindersActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, getString(R.string.sign_in_error), Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun setupGoogleSignIn() {
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                viewModel.firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val user = viewModel.firebaseAuth.currentUser
        if(user != null) {
            startActivity(Intent(this, CoronaRemindersActivity::class.java))
            finish()
        }
    }

    companion object {
        private const val RC_SIGN_IN = 0
    }

}