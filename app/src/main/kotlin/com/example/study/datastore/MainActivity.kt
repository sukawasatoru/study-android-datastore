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

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    companion object {
        private fun log(msg: String) {
            Log.i("MainActivity", msg)
        }
    }

    private val vm by viewModels<MainActivityViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        log("onCreate")

        super.onCreate(savedInstanceState)

        setContent {
            MainView(vm)
        }
    }
}

@Composable
fun MainView(vm: MainActivityViewModel) {
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(vm::loadPrefs) { Text("Load") }
                    Button(vm::clearPrefs) { Text("Clear") }
                }
                Column {
                    val counter by vm.collectCounterFlow.collectAsState()
                    Text("Collect counter: $counter")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(vm::collectCounter) { Text("Collect counter") }
                        Button(vm::cancelCollectCounter) { Text("Cancel") }
                    }
                }
                Column {
                    val counter by vm.loadAndIncrementValue.collectAsState()
                    Text("Load and increment: $counter")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(vm::loadAndIncrement) { Text("Load and increment") }
                        Button(vm::commitLoadAndIncrementValue) { Text("Commit") }
                    }
                }
                Button(vm::loadAndIncrementTransaction) { Text("Load and increment transaction") }
            }
        }
    }
}
