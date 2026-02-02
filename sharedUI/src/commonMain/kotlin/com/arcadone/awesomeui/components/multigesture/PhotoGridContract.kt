package com.arcadone.awesomeui.components.multigesture

import com.arcadone.awesomeui.components.multigesture.model.ImageModel

sealed class PhotoGridEvents : UiEvent {
    data class OnStart(val imageModelList: List<ImageModel> = emptyList(), val selectedImage: List<ImageModel>, val selectionConstraints: SelectionConstraints, val lockedImage: ImageModel? = null) :
        PhotoGridEvents()

    data class SelectAll(val selectedImage: List<ImageModel>) : PhotoGridEvents()
    data class UnSelectAll(val imageModelList: List<ImageModel>) : PhotoGridEvents()
    data class OnPhotoTap(val imageModel: ImageModel) : PhotoGridEvents()

    data object ShowSelectionLimitReachedBottomSheet : PhotoGridEvents()
    data object HideSelectionLimitReachedBottomSheet : PhotoGridEvents()
}

sealed class PhotoGridUIEffects : UiEffect {
    data class ShowErrorMessage(val message: String?) : PhotoGridUIEffects()
}

data class PhotoGridViewState(
    val imageModelList: List<ImageModel> = emptyList(),
    val selectedImage: List<ImageModel> = emptyList(),
    val imageModelCount: Map<String?, Int> = emptyMap(),
    val productImageModelList: List<ImageModel> = emptyList(),
    val lockedImage: ImageModel? = null,
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val selectionConstraints: SelectionConstraints = SelectionConstraints(),
    val screenTitle: String = "",
    val gPhotoNextPageToken: String? = null,
    val facebookNextPageToken: String? = null,
    val gPhotoAlbumId: String? = null,
    val facebookAlbumId: String? = null,
    val showLoaderUnderGrid: Boolean = false,
    val reachedEndGPhotoPagination: Boolean = false,
    val productImageModels: List<ImageModel> = emptyList(),
    val selectionLimitReached: Boolean = false,
) : UiState
