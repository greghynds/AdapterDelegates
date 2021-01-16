package com.hannesdorfmann.adapterdelegates4.dsl

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate

inline fun <reified I : T, T, V : View> adapterDelegateForView(
    noinline on: (item: T, items: List<T>, position: Int) -> Boolean = { item, _, _ -> item is I },
    noinline createView: (Context) -> V,
    noinline binder: SingleViewHolder<I, V>.() -> Unit
): AdapterDelegate<List<T>> {
    return DslListAdapter(createView, on, binder)
}

@PublishedApi
internal class DslListAdapter<I : T, T, V : View>(
    private val createView: (Context) -> V,
    private val on: (item: T, items: List<T>, position: Int) -> Boolean,
    private val initializerBlock: SingleViewHolder<I, V>.() -> Unit
) : AbsListItemAdapterDelegate<I, T, SingleViewHolder<I, V>>() {

    override fun isForViewType(item: T, items: MutableList<T>, position: Int): Boolean = on(
        item, items, position
    )

    override fun onCreateViewHolder(parent: ViewGroup): SingleViewHolder<I, V> {
        val view = createView(parent.context)
        return SingleViewHolder<I, V>(view).also { initializerBlock(it) }
    }

    override fun onBindViewHolder(
        item: I,
        holder: SingleViewHolder<I, V>,
        payloads: MutableList<Any>
    ) {
        holder._item = item as Any
        holder._bind?.invoke(payloads) // It's ok to have an AdapterDelegate without binding block (i.e. static content)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        @Suppress("UNCHECKED_CAST")
        val vh = (holder as SingleViewHolder<I, V>)

        vh._onViewRecycled?.invoke()
    }

    override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
        @Suppress("UNCHECKED_CAST")
        val vh = (holder as SingleViewHolder<I, V>)
        val block = vh._onFailedToRecycleView
        return if (block == null) {
            super.onFailedToRecycleView(holder)
        } else {
            block()
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        @Suppress("UNCHECKED_CAST")
        val vh = (holder as SingleViewHolder<I, V>)
        vh._onViewAttachedToWindow?.invoke()
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        @Suppress("UNCHECKED_CAST")
        val vh = (holder as SingleViewHolder<I, V>)
        vh._onViewDetachedFromWindow?.invoke()
    }
}

class SingleViewHolder<I, V : View>(val view: V) : RecyclerView.ViewHolder(view) {

    private object Uninitialized

    internal var _item: Any = Uninitialized

    val item: I
        get() = if (_item === Uninitialized) {
            throw IllegalArgumentException(
                "Item has not been set yet. That is an internal issue. " +
                        "Please report at https://github.com/sockeqwe/AdapterDelegates"
            )
        } else {
            @Suppress("UNCHECKED_CAST")
            _item as I
        }

    internal var _bind: ((payloads: List<Any>) -> Unit)? = null; private set
    internal var _onViewRecycled: (() -> Unit)? = null; private set
    internal var _onFailedToRecycleView: (() -> Boolean)? = null; private set
    internal var _onViewAttachedToWindow: (() -> Unit)? = null; private set
    internal var _onViewDetachedFromWindow: (() -> Unit)? = null; private set

    fun bind(bindingBlock: (payloads: List<Any>) -> Unit) {
        if (_bind != null) {
            throw IllegalStateException("bind { ... } is already defined. Only one bind { ... } is allowed.")
        }
        this._bind = bindingBlock
    }

    fun onViewRecycled(block: () -> Unit) {
        if (_onViewRecycled != null) {
            throw IllegalStateException(
                "onViewRecycled { ... } is already defined. " +
                        "Only one onViewRecycled { ... } is allowed."
            )
        }
        _onViewRecycled = block
    }

    fun onFailedToRecycleView(block: () -> Boolean) {
        if (_onFailedToRecycleView != null) {
            throw IllegalStateException(
                "onFailedToRecycleView { ... } is already defined. " +
                        "Only one onFailedToRecycleView { ... } is allowed."
            )
        }
        _onFailedToRecycleView = block
    }

    fun onViewAttachedToWindow(block: () -> Unit) {
        if (_onViewAttachedToWindow != null) {
            throw IllegalStateException(
                "onViewAttachedToWindow { ... } is already defined. " +
                        "Only one onViewAttachedToWindow { ... } is allowed."
            )
        }
        _onViewAttachedToWindow = block
    }

    fun onViewDetachedFromWindow(block: () -> Unit) {
        if (_onViewDetachedFromWindow != null) {
            throw IllegalStateException(
                "onViewDetachedFromWindow { ... } is already defined. " +
                        "Only one onViewDetachedFromWindow { ... } is allowed."
            )
        }
        _onViewDetachedFromWindow = block
    }
}