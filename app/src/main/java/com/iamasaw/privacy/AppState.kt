package com.iamasaw.privacy

sealed interface AppState {
    data object Loading : AppState

    data object SignedOut : AppState

    data object NeedsProfile : AppState

    data object Ready : AppState
}
