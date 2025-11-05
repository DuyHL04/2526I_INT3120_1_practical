package com.example.juicetracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.example.juicetracker.data.Juice
import com.example.juicetracker.data.JuiceColor
import com.example.juicetracker.databinding.FragmentEntryDialogBinding
import com.example.juicetracker.ui.AppViewModelProvider
import com.example.juicetracker.ui.EntryViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class EntryDialogFragment : BottomSheetDialogFragment() {
    private val entryViewModel by viewModels<EntryViewModel> { AppViewModelProvider.Factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentEntryDialogBinding.inflate(inflater, container, false).root
    }
    var selectedColor: JuiceColor = JuiceColor.Red

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentEntryDialogBinding.bind(view)
        val args: EntryDialogFragmentArgs by navArgs()
        val  juiceId = args.itemId
        val colorLabelMap = JuiceColor.values().associateBy { getString(it.label) }

        binding.saveButton.setOnClickListener {
            entryViewModel.saveJuice(
                juiceId,
                binding.name.text.toString(),
                binding.description.text.toString(),
                selectedColor.name,
                binding.ratingBar.rating.toInt()
            )
            dismiss()
        }
        if (args.itemId > 0) {
            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    entryViewModel.getJuiceStream(args.itemId).filterNotNull().collect { item ->
                        with(binding) {
                            name.setText(item.name)
                            description.setText(item.description)
                            ratingBar.rating = item.rating.toFloat()
                            colorSpinner.setSelection(findColorIndex(item.color))
                        }
                    }
                }
            }
        }
        binding.name.doOnTextChanged { _, start, before, count ->
            binding.saveButton.isEnabled = (start+count) > 0
        }
        
    }
}