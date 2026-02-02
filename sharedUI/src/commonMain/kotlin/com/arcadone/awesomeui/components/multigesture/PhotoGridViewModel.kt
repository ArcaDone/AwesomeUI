package com.arcadone.awesomeui.components.multigesture

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.arcadone.awesomeui.components.multigesture.model.ImageModel

class PhotoGridViewModel : BaseViewModel<PhotoGridEvents, PhotoGridViewState, PhotoGridUIEffects>() {
    override fun createInitialState(): PhotoGridViewState = PhotoGridViewState()

    private val _showBottomSheet = mutableStateOf(false)
    val showBottomSheet: State<Boolean> = _showBottomSheet

    override fun handleEvent(event: PhotoGridEvents) {
        when (event) {
            is PhotoGridEvents.OnPhotoTap -> selectImageModel(imageModel = event.imageModel)
            is PhotoGridEvents.SelectAll -> onSelectAll(event.selectedImage)
            is PhotoGridEvents.UnSelectAll -> onUnSelectAll(event.imageModelList)
            is PhotoGridEvents.OnStart -> {
                setState {
                    PhotoGridViewState(
                        imageModelList = event.imageModelList,
                        lockedImage = event.lockedImage,
                        selectedImage = event.selectedImage,
                        selectionConstraints = event.selectionConstraints,
                    )
                }
            }

            PhotoGridEvents.ShowSelectionLimitReachedBottomSheet -> {
                _showBottomSheet.value = true
                setState {
                    copy(
                        selectionLimitReached = true,
                    )
                }
            }

            PhotoGridEvents.HideSelectionLimitReachedBottomSheet -> {
                _showBottomSheet.value = false
            }
        }
    }

    private fun onSelectAll(newModelList: List<ImageModel>) {
        val currentImageSelectionSize =
            uiState.value.selectedImage.size
        val selectableImageCount =
            uiState.value.selectionConstraints.max - currentImageSelectionSize
        if (selectableImageCount > 0) {
            val unselectedImage =
                newModelList.filterNot { uiState.value.selectedImage.contains(it) }
            val imageToTake = unselectedImage.take(selectableImageCount)

            if (imageToTake.isNotEmpty()) {
                setState {
                    val imageModels = selectedImage.toMutableList().apply {
                        addAll(imageToTake)
                    }
                    copy(selectedImage = imageModels)
                }
            }
        }
        if (newModelList.size > selectableImageCount) {
            setEvent(PhotoGridEvents.ShowSelectionLimitReachedBottomSheet)
        }
    }

    private fun onUnSelectAll(imageModelList: List<ImageModel>) {
        if (uiState.value.selectedImage.isNotEmpty()) {
            setState {
                val newImageModel = selectedImage.toMutableList().apply {
                    removeAll(imageModelList)
                }
                copy(
                    selectedImage = newImageModel,
                    selectionLimitReached = false,
                )
            }
        }
    }

    private fun selectImageModel(imageModel: ImageModel) {
        val maxImageSelectable = uiState.value.selectionConstraints.max
        val isAlreadySelected = uiState.value.selectedImage.any { it.id == imageModel.id }

        handleImageSelection(
            maxImageSelectable = maxImageSelectable,
            isAlreadySelected = isAlreadySelected,
            imageModel = imageModel,
        )
    }

    private fun handleImageSelection(
        maxImageSelectable: Int,
        isAlreadySelected: Boolean,
        imageModel: ImageModel,
    ) {
        val selectedImageSize =
            uiState.value.selectedImage.size

        val canSelectMore = selectedImageSize < maxImageSelectable

        if (isAlreadySelected) {
            removeImage(imageModel)
        } else if (canSelectMore) {
            addImage(imageModel)
        } else {
            setEvent(PhotoGridEvents.ShowSelectionLimitReachedBottomSheet)
        }
    }

    private fun addImage(imageModel: ImageModel) {
        setState {
            copy(
                selectedImage = selectedImage.toMutableList().apply {
                    add(imageModel)
                },
                selectionLimitReached = false,
            )
        }
    }

    private fun removeImage(imageModel: ImageModel) {
        setState {
            copy(
                selectedImage = selectedImage.filter { it.id != imageModel.id },
                selectionLimitReached = false,
            )
        }
    }
}
