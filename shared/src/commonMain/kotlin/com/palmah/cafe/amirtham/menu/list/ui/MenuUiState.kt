package com.palmah.cafe.amirtham.menu.list.ui

import com.palmah.cafe.amirtham.menu.list.model.MenuItem

data class MenuUiState(
    val isLoading: Boolean = false,
    val menuItems: List<MenuItem> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val itemPendingEdit: MenuItem? = null,
    val editForm: EditMenuItemFormState = EditMenuItemFormState(),
)