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
import androidx.annotation.CheckResult
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.dataStoreFile
import com.example.study.datastore.pb.Preferences as PreferencesPb
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class PreferencesRepository(context: Context) {
    private val dataStore = DataStoreFactory.create(
        serializer = PreferencesSerializer,
        produceFile = { context.dataStoreFile("preferences.pb") },
    )

    fun load(): Flow<Preferences> {
        return dataStore.data.map(PreferencesPb::toModel).distinctUntilChanged()
    }

    @CheckResult
    suspend fun save(prefs: Preferences): Result<Unit> {
        return suspendRunCatching {
            dataStore.updateData { prefs.toPb() }
            Unit
        }
    }

    @CheckResult
    suspend fun clear(): Result<Unit> {
        return suspendRunCatching {
            dataStore.updateData { PreferencesPb.getDefaultInstance() }
            Unit
        }
    }

    @CheckResult
    suspend fun transaction(transform: suspend (Preferences) -> Result<Preferences>): Result<Unit> {
        return suspendRunCatching {
            var exception: Throwable? = null
            dataStore.updateData { currentData ->
                transform(currentData.toModel())
                    .fold(
                        onSuccess = Preferences::toPb,
                        onFailure = {
                            exception = it
                            currentData
                        },
                    )
            }

            exception?.let { Result.failure(it) } ?: Result.success(Unit)
        }
    }
}

object PreferencesSerializer : Serializer<PreferencesPb> {
    override val defaultValue: PreferencesPb = PreferencesPb.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): PreferencesPb {
        try {
            return PreferencesPb.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", e)
        }
    }

    override suspend fun writeTo(t: PreferencesPb, output: OutputStream) {
        t.writeTo(output)
    }
}
