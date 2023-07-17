package com.example.storein.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.storein.data.Category
import com.example.storein.viewmodels.CategoryViewModel
import com.google.firebase.firestore.FirebaseFirestore

class BaseCategoryViewModelFactory(
    private val firestore: FirebaseFirestore,
    private val category: Category
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return  CategoryViewModel(firestore, category) as T
    }
}