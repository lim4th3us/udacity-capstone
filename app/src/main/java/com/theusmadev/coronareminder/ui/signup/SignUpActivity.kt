package com.theusmadev.coronareminder.ui.signup

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuth
import com.theusmadev.coronareminder.R
import com.theusmadev.coronareminder.databinding.ActivitySignUpBinding
import org.koin.android.ext.android.inject


class SignUpActivity: AppCompatActivity() {

    private lateinit var activitySignUpBinding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth

    val viewModel: SignUpViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySignUpBinding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
        activitySignUpBinding.viewModel = viewModel
        activitySignUpBinding.lifecycleOwner = this
        activitySignUpBinding.loadingStatus = false

        firebaseAuth = FirebaseAuth.getInstance()

        setupObservers()
    }

    private fun setupObservers() {
        viewModel.showLoading.observe(this, Observer { isLoading ->
            isLoading?.let { isLoadingNotNull ->
                activitySignUpBinding.loadingStatus = isLoadingNotNull
                activitySignUpBinding.invalidateAll()
            }
        })

        viewModel.userCreated.observe(this, Observer { response ->
            response?.let { responseNotNull ->
                if(responseNotNull) {
                    onBackPressed()
                } else {
                    Toast.makeText(this, getString(R.string.register_user_error), Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onClearData()
    }

}