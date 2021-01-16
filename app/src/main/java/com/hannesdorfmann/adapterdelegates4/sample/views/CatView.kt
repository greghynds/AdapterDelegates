package com.hannesdorfmann.adapterdelegates4.sample.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.hannesdorfmann.adapterdelegates4.sample.databinding.ItemCatBinding
import com.hannesdorfmann.adapterdelegates4.sample.model.Cat

internal class CatView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val binding = ItemCatBinding.inflate(LayoutInflater.from(context), this, true)

    fun render(cat: Cat) {
        binding.name.text = cat.name
    }
}