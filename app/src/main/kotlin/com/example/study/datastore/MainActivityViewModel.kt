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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private fun log(msg: String) {
            Log.i("MainActivityViewModel", msg)
        }
    }

    private val prefsRepo = PreferencesRepository(application)

    override fun onCleared() {
        log("onCleared")
    }

    val prefsFlow: StateFlow<Preferences> = prefsRepo
        .load()
        .stateIn(viewModelScope, SharingStarted.Eagerly, Preferences.default())

    fun loadPrefs() {
        log("loadPrefs")
        viewModelScope.launch {
            val counter = suspendRunCatching {
                prefsRepo.load().first()
            }.getOrElse {
                log("failed to load preferences: $it")
                return@launch
            }
            log("counter: $counter")
        }
    }

    fun clearPrefs() {
        viewModelScope.launch {
            prefsRepo.clear()
                .getOrElse {
                    log("failed to clear preferences: $it")
                }
        }
    }

    private val _loadAndIncrementValue = MutableStateFlow(Preferences.default())
    val loadAndIncrementValue: StateFlow<Preferences> = _loadAndIncrementValue
    fun loadAndIncrement() {
        viewModelScope.launch {
            log("loadAndIncrement begin")
            val current = suspendRunCatching {
                prefsRepo.load().first()
            }.getOrElse {
                log("loadAndIncrement $it")
                return@launch
            }

            _loadAndIncrementValue.value = current.copy(
                counter = current.counter + 1,
            )
            log("loadAndIncrement end")
        }
    }

    fun commitLoadAndIncrementValue() {
        viewModelScope.launch {
            log("commitLoadAndIncrementValue")
            prefsRepo.save(loadAndIncrementValue.value)
                .getOrElse {
                    log("commitLoadAndIncrementValue $it")
                }
        }
    }

    fun loadAndIncrementTransaction() {
        viewModelScope.launch {
            prefsRepo.transaction {
                suspendRunCatching {
                    log("loadAndIncrementTransaction begin")
                    val ret = it.copy(
                        counter = it.counter + 1,
                    )
                    delay(5_000.milliseconds)
                    log("loadAndIncrementTransaction end")
                    ret
                }
            }.getOrElse {
                log("loadAndIncrementTransaction $it")
            }
        }
    }
}
