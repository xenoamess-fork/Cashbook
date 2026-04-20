/*
 * Copyright 2021 The Cashbook Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.wj.android.cashbook.feature.schedule.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.wj.android.cashbook.core.model.model.ScheduleModel
import cn.wj.android.cashbook.core.ui.DialogState
import cn.wj.android.cashbook.domain.usecase.DeleteScheduleUseCase
import cn.wj.android.cashbook.domain.usecase.GetScheduleListUseCase
import cn.wj.android.cashbook.domain.usecase.ToggleScheduleEnabledUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 周期记账列表 ViewModel
 *
 * @param getScheduleListUseCase 获取周期规则列表用例
 * @param toggleScheduleEnabledUseCase 切换周期规则启用状态用例
 * @param deleteScheduleUseCase 删除周期规则用例
 *
 * > [王杰](mailto:15555650921@163.com) 创建于 2026/4/20
 */
@HiltViewModel
class MySchedulesViewModel @Inject constructor(
    getScheduleListUseCase: GetScheduleListUseCase,
    private val toggleScheduleEnabledUseCase: ToggleScheduleEnabledUseCase,
    private val deleteScheduleUseCase: DeleteScheduleUseCase,
) : ViewModel() {

    /** 弹窗状态 */
    var dialogState by mutableStateOf<DialogState>(DialogState.Dismiss)
        private set

    /** 周期规则列表 */
    val scheduleListData = getScheduleListUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    /** 切换周期规则 [schedule] 的启用状态 */
    fun toggleEnabled(schedule: ScheduleModel) {
        viewModelScope.launch {
            toggleScheduleEnabledUseCase(schedule.id, !schedule.enabled)
        }
    }

    /** 显示删除周期规则确认弹窗 */
    fun showDeleteDialog(schedule: ScheduleModel) {
        dialogState = DialogState.Shown(schedule)
    }

    /** 隐藏弹窗 */
    fun dismissDialog() {
        dialogState = DialogState.Dismiss
    }

    /** 确认删除 */
    fun confirmDelete(scheduleId: Long) {
        viewModelScope.launch {
            deleteScheduleUseCase(scheduleId)
            dismissDialog()
        }
    }
}
