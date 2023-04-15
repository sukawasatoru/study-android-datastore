/*
 * Copyright 2023 sukawasatoru
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.study.datastore

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private fun log(msg: String) {
            Log.i("MainActivityViewModel", msg)
        }
    }

    private val prefsDataStore = PreferencesDataStore.getInstance(application)

    override fun onCleared() {
        log("onCleared")
    }

    fun loadPrefs() {
        log("loadPrefs")
        viewModelScope.launch {
            log("counter: ${prefsDataStore.counter.first()}")
        }
    }

    fun clearPrefs() {
        viewModelScope.launch {
            prefsDataStore.updateData {
                it.toBuilder()
                    .clear()
                    .build()
            }
        }
    }

    private val _collectCounterFlow = MutableStateFlow<Int?>(null)
    val collectCounterFlow: StateFlow<Int?> = _collectCounterFlow
    private var collectCounterJob: Job? = null
    fun collectCounter() {
        log("collectCounter")

        cancelCollectCounter()
        collectCounterJob = viewModelScope.launch {
            try {
                prefsDataStore.counter.collect {
                    _collectCounterFlow.value = it
                }
            } catch (e: CancellationException) {
                log("cancelled collectCounter")
            }
        }
    }

    fun cancelCollectCounter() {
        _collectCounterFlow.value = null
        collectCounterJob?.cancel()
        collectCounterJob = null
    }

    private val _loadAndIncrementValue = MutableStateFlow(0)
    val loadAndIncrementValue: StateFlow<Int> = _loadAndIncrementValue
    fun loadAndIncrement() {
        viewModelScope.launch {
            log("loadAndIncrement begin")
            _loadAndIncrementValue.value = prefsDataStore.counter.first() + 1
            log("loadAndIncrement end")
        }
    }

    fun commitLoadAndIncrementValue() {
        viewModelScope.launch {
            prefsDataStore.updateData {
                log("on commitLoadAndIncrementValue")
                it.toBuilder()
                    .setCounter(loadAndIncrementValue.value)
                    .build()
            }
        }
    }

    fun loadAndIncrementTransaction() {
        viewModelScope.launch {
            prefsDataStore.updateData {
                log("on loadAndIncrementTransaction")
                val ret = it.toBuilder()
                    .setCounter(it.counter + 1)
                    .build()
                delay(5_000.milliseconds)
                log("loadAndIncrementTransaction end")
                ret
            }
        }
    }
}
