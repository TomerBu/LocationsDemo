package edu.tomerbu.locationdemos.ui.main

import androidx.lifecycle.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PageViewModel : ViewModel() {

    private val _index = MutableSharedFlow<Int>(replay = 1)

    val text: Flow<String> = _index.map {
        "Hello world from section: $it"
    }


    fun setIndex(index: Int) {
        //viewModelScope.launch {
            _index.tryEmit(index)
        //}
    }
}