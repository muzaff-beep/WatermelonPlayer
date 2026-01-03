package com.watermelon.player.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watermelon.player.model.MediaItemUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _mediaItems = MutableStateFlow<List<MediaItemUi>>(emptyList())
    val mediaItems: StateFlow<List<MediaItemUi>> = _mediaItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadMedia()
    }

    private fun loadMedia() {
        viewModelScope.launch {
            _isLoading.value = true
            // TODO: Load from MediaStore or Room
            val dummyItems = listOf(
                MediaItemUi(
                    id = "1",
                    title = "Sample Song",
                    path = "",
                    uri = android.net.Uri.EMPTY
                )
            )
            _mediaItems.value = dummyItems
            _isLoading.value = false
        }
    }

    fun refresh() {
        loadMedia()
    }
}
