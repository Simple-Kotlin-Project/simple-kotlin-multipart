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
 * This interface helps to transform [Multipart] to [ByteArray]
 */
public interface MultipartEncoder {

    /**
     * Transform [multipart] to [ByteArray] for defined [boundary]
     *
     * If [multipart] doesn't have any parts, it will return empty [ByteArray]
     *
     * @param boundary multipart's boundary
     * @param multipart [Multipart] to be transformed to [ByteArray]
     *
     * @throws IllegalArgumentException if [boundary] is wrong
     * @throws IllegalStateException if it can not transform [multipart] to [ByteArray]
     */
    public fun encode(boundary: String, multipart: Multipart): ByteArray
}
