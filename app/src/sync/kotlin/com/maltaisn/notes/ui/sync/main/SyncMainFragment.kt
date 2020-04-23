/*
 * Copyright 2020 Nicolas Maltais
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.maltaisn.notes.ui.sync.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.maltaisn.notes.databinding.FragmentSyncMainBinding
import com.maltaisn.notes.ui.EventObserver
import com.maltaisn.notes.ui.common.ViewModelFragment
import com.maltaisn.notes.ui.sync.SyncFragment
import com.maltaisn.notes.ui.sync.SyncPage


class SyncMainFragment : ViewModelFragment() {

    private val viewModel: SyncMainViewModel by viewModels { viewModelFactory }

    private var _binding: FragmentSyncMainBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSyncMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Signed out views
        binding.signInBtn.setOnClickListener {
            viewModel.goToPage(SyncPage.SIGN_IN)
        }
        binding.signUpBtn.setOnClickListener {
            viewModel.goToPage(SyncPage.SIGN_UP)
        }

        // Signed in views
        binding.signOutBtn.setOnClickListener {
            viewModel.signOut()
        }
        binding.resendVerificationBtn.setOnClickListener {
            viewModel.resendVerification()
        }

        // Observers
        viewModel.changePageEvent.observe(viewLifecycleOwner, EventObserver { page ->
            (parentFragment as? SyncFragment)?.goToPage(page)
        })

        viewModel.messageEvent.observe(viewLifecycleOwner, EventObserver { messageId ->
            Snackbar.make(view, messageId, Snackbar.LENGTH_SHORT).show()
        })

        viewModel.currentUser.observe(viewLifecycleOwner, Observer { user ->
            binding.signedInGroup.isVisible = user != null
            binding.signedOutGroup.isVisible = user == null
            binding.verificationGroup.isVisible = user != null && !user.isEmailVerified
            if (user != null) {
                binding.signedInEmailTxv.text = user.email!!
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkVerification()
    }

}