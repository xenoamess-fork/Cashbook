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

package cn.wj.android.cashbook.feature.schedule.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.wj.android.cashbook.core.common.ext.toMoneyString
import cn.wj.android.cashbook.core.design.component.CbAlertDialog
import cn.wj.android.cashbook.core.design.component.CbFloatingActionButton
import cn.wj.android.cashbook.core.design.component.CbHorizontalDivider
import cn.wj.android.cashbook.core.design.component.CbListItem
import cn.wj.android.cashbook.core.design.component.CbScaffold
import cn.wj.android.cashbook.core.design.component.CbTextButton
import cn.wj.android.cashbook.core.design.component.CbTopAppBar
import cn.wj.android.cashbook.core.design.component.Empty
import cn.wj.android.cashbook.core.design.icon.CbIcons
import cn.wj.android.cashbook.core.model.enums.ScheduleFrequencyEnum
import cn.wj.android.cashbook.core.model.model.ScheduleModel
import cn.wj.android.cashbook.core.ui.DialogState
import cn.wj.android.cashbook.core.ui.R
import cn.wj.android.cashbook.feature.schedule.viewmodel.MySchedulesViewModel

/**
 * 周期记账列表
 */
@Composable
internal fun MySchedulesRoute(
    onRequestNaviToEditSchedule: (Long) -> Unit,
    onRequestPopBackStack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MySchedulesViewModel = hiltViewModel(),
) {
    val scheduleList by viewModel.scheduleListData.collectAsStateWithLifecycle()

    MySchedulesScreen(
        scheduleList = scheduleList,
        dialogState = viewModel.dialogState,
        onToggleEnabled = viewModel::toggleEnabled,
        onDeleteClick = viewModel::showDeleteDialog,
        onConfirmDelete = viewModel::confirmDelete,
        onDismissDialog = viewModel::dismissDialog,
        onAddClick = { onRequestNaviToEditSchedule(-1L) },
        onItemClick = onRequestNaviToEditSchedule,
        onBackClick = onRequestPopBackStack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MySchedulesScreen(
    scheduleList: List<ScheduleModel>,
    dialogState: DialogState,
    onToggleEnabled: (ScheduleModel) -> Unit,
    onDeleteClick: (ScheduleModel) -> Unit,
    onConfirmDelete: (Long) -> Unit,
    onDismissDialog: () -> Unit,
    onAddClick: () -> Unit,
    onItemClick: (Long) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CbScaffold(
        modifier = modifier,
        topBar = {
            CbTopAppBar(
                onBackClick = onBackClick,
                title = { Text(text = stringResource(id = R.string.my_schedules)) },
            )
        },
        floatingActionButton = {
            CbFloatingActionButton(onClick = onAddClick) {
                Icon(imageVector = CbIcons.Add, contentDescription = stringResource(id = R.string.cd_add))
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // 删除确认弹窗
            if (dialogState is DialogState.Shown<*>) {
                (dialogState.data as? ScheduleModel)?.let { schedule ->
                    CbAlertDialog(
                        onDismissRequest = onDismissDialog,
                        title = { Text(text = stringResource(id = R.string.delete_schedule)) },
                        text = { Text(text = stringResource(id = R.string.delete_schedule_confirm)) },
                        dismissButton = {
                            CbTextButton(onClick = onDismissDialog) {
                                Text(text = stringResource(id = R.string.cancel))
                            }
                        },
                        confirmButton = {
                            CbTextButton(onClick = { onConfirmDelete(schedule.id) }) {
                                Text(text = stringResource(id = R.string.confirm))
                            }
                        },
                    )
                }
            }

            if (scheduleList.isEmpty()) {
                Empty(
                    hintText = stringResource(id = R.string.schedule_empty_hint),
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(scheduleList, key = { it.id }) { schedule ->
                        ScheduleListItem(
                            schedule = schedule,
                            onToggleEnabled = { onToggleEnabled(schedule) },
                            onDeleteClick = { onDeleteClick(schedule) },
                            onItemClick = { onItemClick(schedule.id) },
                        )
                        CbHorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleListItem(
    schedule: ScheduleModel,
    onToggleEnabled: () -> Unit,
    onDeleteClick: () -> Unit,
    onItemClick: () -> Unit,
) {
    CbListItem(
        headlineContent = {
            Text(text = schedule.amount.toMoneyString())
        },
        supportingContent = {
            Text(
                text = schedule.frequency.displayName() +
                    if (schedule.remark.isNotBlank()) " · ${schedule.remark}" else "",
            )
        },
        trailingContent = {
            Switch(
                checked = schedule.enabled,
                onCheckedChange = { onToggleEnabled() },
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable(onClick = onItemClick),
    )
}

@Composable
private fun ScheduleFrequencyEnum.displayName(): String {
    return when (this) {
        ScheduleFrequencyEnum.DAILY -> stringResource(id = R.string.schedule_frequency_daily)
        ScheduleFrequencyEnum.WEEKLY -> stringResource(id = R.string.schedule_frequency_weekly)
        ScheduleFrequencyEnum.MONTHLY -> stringResource(id = R.string.schedule_frequency_monthly)
        ScheduleFrequencyEnum.YEARLY -> stringResource(id = R.string.schedule_frequency_yearly)
    }
}
