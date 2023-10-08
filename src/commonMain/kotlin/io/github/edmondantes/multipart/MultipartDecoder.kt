/*
 * Copyright (c) 2023. Ilia Loginov
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
package io.github.edmondantes.multipart

/**
 * This interface helps to transform [ByteArray] to [Multipart]
 */
public interface MultipartDecoder {

    /**
     * Transform [array] to [Multipart] for defined [boundary]
     *
     * If [array] doesn't have multipart's trailer, it will return empty [Multipart]
     *
     * @param boundary multipart's boundary
     * @param array [ByteArray] to be transformed to [Multipart]
     * @return [Multipart] that represent [array]
     *
     * @throws IllegalArgumentException if [array] isn't multipart or boundary is wrong
     * @throws IllegalStateException if it can not transform [array] to [Multipart]
     */
    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    public fun decode(boundary: String, array: ByteArray): Multipart
}
