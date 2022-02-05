/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server.interactions

import ca.gosyer.core.lang.withIOContext
import ca.gosyer.data.models.Manga
import ca.gosyer.data.server.Http
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.requests.mangaQuery
import ca.gosyer.data.server.requests.mangaThumbnailQuery
import ca.gosyer.data.server.requests.updateMangaMetaRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.utils.io.ByteReadChannel
import me.tatarka.inject.annotations.Inject

class MangaInteractionHandler @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseInteractionHandler(client, serverPreferences) {

    suspend fun getManga(mangaId: Long, refresh: Boolean = false) = withIOContext {
        client.get<Manga>(
            serverUrl + mangaQuery(mangaId)
        ) {
            url {
                if (refresh) {
                    parameter("onlineFetch", true)
                }
            }
        }
    }

    suspend fun getManga(manga: Manga, refresh: Boolean = false) = getManga(manga.id, refresh)

    suspend fun getMangaThumbnail(mangaId: Long, block: HttpRequestBuilder.() -> Unit) = withIOContext {
        client.get<ByteReadChannel>(
            serverUrl + mangaThumbnailQuery(mangaId),
            block
        )
    }

    suspend fun updateMangaMeta(mangaId: Long, key: String, value: String) = withIOContext {
        client.submitForm<HttpResponse>(
            serverUrl + updateMangaMetaRequest(mangaId),
            formParameters = Parameters.build {
                append("key", key)
                append("value", value)
            }
        ) {
            method = HttpMethod.Patch
        }
    }

    suspend fun updateMangaMeta(manga: Manga, key: String, value: String) = updateMangaMeta(manga.id, key, value)
}