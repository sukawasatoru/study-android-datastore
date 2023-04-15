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

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.dataStoreFile
import com.example.study.datastore.pb.Preferences
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesDataStore private constructor(context: Context) {
    companion object {
        private var instance: PreferencesDataStore? = null

        @Synchronized
        fun getInstance(context: Context): PreferencesDataStore {
            instance?.let { return it }
            return PreferencesDataStore(context).also {
                instance = it
            }
        }
    }

    private val dataStore = DataStoreFactory.create(
        serializer = PreferencesSerializer,
        produceFile = { context.dataStoreFile("preferences.pb") },
    )

    val counter: Flow<Int> = dataStore.data.map { it.counter }

    suspend fun updateData(transformer: suspend (Preferences) -> Preferences) {
        dataStore.updateData(transformer)
    }
}

object PreferencesSerializer : Serializer<Preferences> {
    override val defaultValue: Preferences = Preferences.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): Preferences {
        try {
            return Preferences.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", e)
        }
    }

    override suspend fun writeTo(t: Preferences, output: OutputStream) {
        t.writeTo(output)
    }
}
