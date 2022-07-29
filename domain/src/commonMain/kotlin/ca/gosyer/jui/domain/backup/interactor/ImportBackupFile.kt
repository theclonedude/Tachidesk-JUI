/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.backup.interactor

import ca.gosyer.jui.domain.backup.service.BackupRepository
import io.ktor.client.request.HttpRequestBuilder
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.singleOrNull
import me.tatarka.inject.annotations.Inject
import okio.Path
import org.lighthousegames.logging.logging

class ImportBackupFile @Inject constructor(private val backupRepository: BackupRepository) {

    suspend fun await(file: Path, block: HttpRequestBuilder.() -> Unit = {}) = asFlow(file, block)
        .catch { log.warn(it) { "Failed to import backup ${file.name}" } }
        .singleOrNull()

    fun asFlow(file: Path, block: HttpRequestBuilder.() -> Unit = {}) =
        backupRepository.importBackupFile(file, block)

    companion object {
        private val log = logging()
    }
}