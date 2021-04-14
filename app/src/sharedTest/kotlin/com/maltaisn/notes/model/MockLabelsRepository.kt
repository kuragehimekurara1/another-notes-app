/*
 * Copyright 2021 Nicolas Maltais
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

package com.maltaisn.notes.model

import com.maltaisn.notes.model.entity.Label
import com.maltaisn.notes.model.entity.LabelRef
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map

/**
 * Implementation of the labels repository that stores data itself instead of relying on DAOs.
 *
 * This implementation should work almost exactly like [DefaultLabelsRepository].
 * Returned flows will also emit a new value on every change.
 */
class MockLabelsRepository : LabelsRepository {

    private val labels = mutableMapOf<Long, Label>()
    private val labelRefs = mutableMapOf<Long, Long>()

    var lastLabelId = 0L
        private set

    /**
     * Number of labels in database.
     */
    val labelsCount: Int
        get() = labels.size

    private val changeFlow = MutableSharedFlow<Unit>(replay = 1)

    /**
     * Add label without notifying change flow.
     */
    private fun addLabelInternal(label: Label): Long {
        val id = if (label.id != Label.NO_ID) {
            labels[label.id] = label
            if (label.id > lastLabelId) {
                lastLabelId = label.id
            }
            label.id
        } else {
            lastLabelId++
            labels[lastLabelId] = label.copy(id = lastLabelId)
            lastLabelId
        }
        return id
    }

    /** Non-suspending version of [insertLabel]. */
    fun addLabel(label: Label): Long {
        val id = addLabelInternal(label)
        changeFlow.tryEmit(Unit)
        return id
    }

    override suspend fun insertLabel(label: Label): Long {
        val id = addLabel(label)
        changeFlow.emit(Unit)
        return id
    }

    override suspend fun updateLabel(label: Label) {
        require(label.id in labels) { "Cannot update non-existent label" }
        insertLabel(label)
    }

    override suspend fun deleteLabel(label: Label) {
        labels -= label.id
        changeFlow.emit(Unit)
    }

    override suspend fun getLabelById(id: Long) = labels[id]

    fun requireLabelById(id: Long) = labels.getOrElse(id) {
        error("No label with ID $id")
    }

    override suspend fun getLabelByName(name: String) = labels.values.find { it.name == name }

    override suspend fun insertLabelRefs(refs: List<LabelRef>) {
        for (ref in refs) {
            labelRefs[ref.noteId] = ref.labelId
        }
    }

    override suspend fun deleteLabelRefs(refs: List<LabelRef>) {
        for (ref in refs) {
            labelRefs -= ref.noteId
        }
    }

    override suspend fun clearAllData() {
        labels.clear()
        labelRefs.clear()
        lastLabelId = 0
        changeFlow.emit(Unit)
    }

    override fun getAllLabels() = changeFlow.map {
        labels.values.toList()
    }

}