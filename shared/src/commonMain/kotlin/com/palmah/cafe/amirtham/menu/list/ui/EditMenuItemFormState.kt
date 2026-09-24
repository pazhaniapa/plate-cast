package com.palmah.cafe.amirtham.menu.list.ui

import com.palmah.cafe.amirtham.menu.list.model.MenuItem

data class EditMenuItemFormState(
    val name: String = "",
    val price: String = "",
    val timings: String = "",
    val description: String = "",
) {
    companion object {
        fun from(item: MenuItem) = EditMenuItemFormState(
            name = item.name,
            price = if (item.price == 0.0) "" else item.price.toString(),
            timings = item.timings,
            description = item.description,
        )
    }
}

fun MenuItem.applyEdits(form: EditMenuItemFormState): MenuItem = copy(
    name = form.name,
    price = form.price.toDoubleOrNull() ?: 0.0,
    timings = form.timings,
    description = form.description,
)
