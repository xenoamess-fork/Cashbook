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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.wj.android.cashbook.core.common.PATTERN_SIGN_MONEY
import cn.wj.android.cashbook.core.common.ext.completeZero
import cn.wj.android.cashbook.core.common.tools.dateFormat
import cn.wj.android.cashbook.core.design.component.CbFloatingActionButton
import cn.wj.android.cashbook.core.design.component.CbHorizontalDivider
import cn.wj.android.cashbook.core.design.component.CbListItem
import cn.wj.android.cashbook.core.design.component.CbModalBottomSheet
import cn.wj.android.cashbook.core.design.component.CbScaffold
import cn.wj.android.cashbook.core.design.component.CbTextButton
import cn.wj.android.cashbook.core.design.component.CbTextField
import cn.wj.android.cashbook.core.design.component.CbTopAppBar
import cn.wj.android.cashbook.core.design.component.Loading
import cn.wj.android.cashbook.core.design.component.TextFieldState
import cn.wj.android.cashbook.core.design.icon.CbIcons
import cn.wj.android.cashbook.core.model.enums.RecordTypeCategoryEnum
import cn.wj.android.cashbook.core.model.enums.ScheduleFrequencyEnum
import cn.wj.android.cashbook.core.ui.LocalProgressDialogController
import cn.wj.android.cashbook.core.ui.R
import cn.wj.android.cashbook.feature.schedule.viewmodel.EditScheduleBottomSheetEnum
import cn.wj.android.cashbook.feature.schedule.viewmodel.EditScheduleUiState
import cn.wj.android.cashbook.feature.schedule.viewmodel.EditScheduleViewModel
import java.util.Calendar

/**
 * 编辑周期规则界面
 */
@Composable
internal fun EditScheduleRoute(
    scheduleId: Long,
    typeListContent: @Composable (currentTypeCategoryCode: Int, currentTypeId: Long, onTypeChange: (Long, Int) -> Unit) -> Unit,
    assetBottomSheetContent: @Composable (currentTypeId: Long, selectedAssetId: Long, onAssetChange: (Long) -> Unit) -> Unit,
    onRequestPopBackStack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditScheduleViewModel = hiltViewModel(),
) {
    LaunchedEffect(scheduleId) {
        viewModel.updateScheduleId(scheduleId)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(initialValue = EditScheduleUiState.Loading)
    val savingHintText = stringResource(id = R.string.saving)
    val progressDialogController = LocalProgressDialogController.current

    EditScheduleScreen(
        isCreate = scheduleId == -1L,
        uiState = uiState,
        typeListContent = typeListContent,
        assetBottomSheetContent = assetBottomSheetContent,
        bottomSheetType = viewModel.bottomSheetType,
        onBottomSheetDismiss = viewModel::dismissBottomSheet,
        onShowSelectTypeSheet = viewModel::showSelectTypeSheet,
        onShowSelectAssetSheet = viewModel::showSelectAssetSheet,
        onShowSelectFrequencySheet = viewModel::showSelectFrequencySheet,
        onTypeChange = viewModel::updateType,
        onAssetChange = viewModel::updateAsset,
        onFrequencyChange = viewModel::updateFrequency,
        onStartDateChange = viewModel::updateStartDate,
        onEndDateChange = viewModel::updateEndDate,
        onRecordTimeChange = viewModel::updateRecordTime,
        onAmountChange = viewModel::updateAmount,
        onChargesChange = viewModel::updateCharges,
        onConcessionsChange = viewModel::updateConcessions,
        onRemarkChange = viewModel::updateRemark,
        onEnabledChange = viewModel::updateEnabled,
        onSaveClick = {
            viewModel.trySave(progressDialogController, savingHintText, onRequestPopBackStack)
        },
        onBackClick = onRequestPopBackStack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditScheduleScreen(
    isCreate: Boolean,
    uiState: EditScheduleUiState,
    typeListContent: @Composable (currentTypeCategoryCode: Int, currentTypeId: Long, onTypeChange: (Long, Int) -> Unit) -> Unit,
    assetBottomSheetContent: @Composable (currentTypeId: Long, selectedAssetId: Long, onAssetChange: (Long) -> Unit) -> Unit,
    bottomSheetType: EditScheduleBottomSheetEnum,
    onBottomSheetDismiss: () -> Unit,
    onShowSelectTypeSheet: () -> Unit,
    onShowSelectAssetSheet: () -> Unit,
    onShowSelectFrequencySheet: () -> Unit,
    onTypeChange: (Long, RecordTypeCategoryEnum) -> Unit,
    onAssetChange: (Long) -> Unit,
    onFrequencyChange: (ScheduleFrequencyEnum) -> Unit,
    onStartDateChange: (Long) -> Unit,
    onEndDateChange: (Long?) -> Unit,
    onRecordTimeChange: (Long) -> Unit,
    onAmountChange: (String) -> Unit,
    onChargesChange: (String) -> Unit,
    onConcessionsChange: (String) -> Unit,
    onRemarkChange: (String) -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val amountErrorText = stringResource(id = R.string.please_enter_amount)

    val amountTextState = remember((uiState as? EditScheduleUiState.Success)?.amountText) {
        TextFieldState(
            defaultText = (uiState as? EditScheduleUiState.Success)?.amountText ?: "",
            validator = { it.isNotBlank() },
            filter = { it.matches(Regex(PATTERN_SIGN_MONEY)) },
            errorFor = { amountErrorText },
        )
    }
    val chargesTextState = remember((uiState as? EditScheduleUiState.Success)?.chargesText) {
        TextFieldState(
            defaultText = (uiState as? EditScheduleUiState.Success)?.chargesText ?: "",
            filter = { it.matches(Regex(PATTERN_SIGN_MONEY)) },
        )
    }
    val concessionsTextState = remember((uiState as? EditScheduleUiState.Success)?.concessionsText) {
        TextFieldState(
            defaultText = (uiState as? EditScheduleUiState.Success)?.concessionsText ?: "",
            filter = { it.matches(Regex(PATTERN_SIGN_MONEY)) },
        )
    }
    val remarkTextState = remember((uiState as? EditScheduleUiState.Success)?.remark) {
        TextFieldState(
            defaultText = (uiState as? EditScheduleUiState.Success)?.remark ?: "",
        )
    }

    CbScaffold(
        modifier = modifier,
        topBar = {
            CbTopAppBar(
                onBackClick = onBackClick,
                title = {
                    Text(
                        text = stringResource(
                            id = if (isCreate) R.string.new_schedule else R.string.edit_schedule,
                        ),
                    )
                },
            )
        },
        floatingActionButton = {
            if (uiState is EditScheduleUiState.Success) {
                CbFloatingActionButton(
                    onClick = {
                        if (!amountTextState.isValid) {
                            amountTextState.requestErrors()
                        } else {
                            onAmountChange(amountTextState.text)
                            onChargesChange(chargesTextState.text)
                            onConcessionsChange(concessionsTextState.text)
                            onRemarkChange(remarkTextState.text)
                            onSaveClick()
                        }
                    },
                ) {
                    Icon(
                        imageVector = CbIcons.SaveAs,
                        contentDescription = stringResource(id = R.string.cd_confirm),
                    )
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            if (bottomSheetType != EditScheduleBottomSheetEnum.NONE) {
                CbModalBottomSheet(
                    onDismissRequest = onBottomSheetDismiss,
                    sheetState = rememberModalBottomSheetState(
                        confirmValueChange = {
                            if (it == SheetValue.Hidden) {
                                onBottomSheetDismiss()
                            }
                            true
                        },
                    ),
                ) {
                    when (bottomSheetType) {
                        EditScheduleBottomSheetEnum.TYPE -> {
                            if (uiState is EditScheduleUiState.Success) {
                                typeListContent(
                                    uiState.typeCategory.ordinal,
                                    uiState.typeId,
                                ) { typeId, typeCategoryCode ->
                                    onTypeChange(typeId, RecordTypeCategoryEnum.ordinalOf(typeCategoryCode))
                                    onBottomSheetDismiss()
                                }
                            }
                        }

                        EditScheduleBottomSheetEnum.ASSET -> {
                            if (uiState is EditScheduleUiState.Success) {
                                assetBottomSheetContent(
                                    uiState.typeId,
                                    uiState.assetId,
                                ) { assetId ->
                                    onAssetChange(assetId)
                                    onBottomSheetDismiss()
                                }
                            }
                        }

                        EditScheduleBottomSheetEnum.FREQUENCY -> {
                            SelectFrequencySheet(
                                selected = (uiState as? EditScheduleUiState.Success)?.frequency,
                                onSelect = { frequency ->
                                    onFrequencyChange(frequency)
                                    onBottomSheetDismiss()
                                },
                            )
                        }

                        else -> {}
                    }
                }
            }

            when (uiState) {
                EditScheduleUiState.Loading -> {
                    Loading(modifier = Modifier.align(Alignment.Center))
                }

                is EditScheduleUiState.Success -> {
                    Column(
                        modifier = Modifier.verticalScroll(state = rememberScrollState()),
                    ) {
                        // 金额
                        CbTextField(
                            textFieldState = amountTextState,
                            label = { Text(text = stringResource(id = R.string.amount)) },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Next,
                                keyboardType = KeyboardType.Decimal,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .padding(horizontal = 16.dp),
                        )

                        // 手续费
                        CbTextField(
                            textFieldState = chargesTextState,
                            label = { Text(text = stringResource(id = R.string.charges)) },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Next,
                                keyboardType = KeyboardType.Decimal,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .padding(horizontal = 16.dp),
                        )

                        // 优惠
                        CbTextField(
                            textFieldState = concessionsTextState,
                            label = { Text(text = stringResource(id = R.string.concessions)) },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Next,
                                keyboardType = KeyboardType.Decimal,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .padding(horizontal = 16.dp),
                        )

                        CbHorizontalDivider(modifier = Modifier.padding(top = 8.dp))

                        // 类型选择
                        CbListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onShowSelectTypeSheet),
                            headlineContent = {
                                Text(
                                    text = stringResource(id = R.string.type),
                                    modifier = Modifier.padding(start = 16.dp),
                                )
                            },
                            trailingContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (uiState.typeId > 0) {
                                            stringResource(id = R.string.selected)
                                        } else {
                                            stringResource(id = R.string.please_select)
                                        },
                                    )
                                    Icon(
                                        imageVector = CbIcons.KeyboardArrowRight,
                                        contentDescription = null,
                                    )
                                }
                            },
                        )

                        // 资产选择
                        CbListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onShowSelectAssetSheet),
                            headlineContent = {
                                Text(
                                    text = stringResource(id = R.string.asset),
                                    modifier = Modifier.padding(start = 16.dp),
                                )
                            },
                            trailingContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (uiState.assetId > 0) {
                                            stringResource(id = R.string.selected)
                                        } else {
                                            stringResource(id = R.string.please_select)
                                        },
                                    )
                                    Icon(
                                        imageVector = CbIcons.KeyboardArrowRight,
                                        contentDescription = null,
                                    )
                                }
                            },
                        )

                        // 频率选择
                        CbListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onShowSelectFrequencySheet),
                            headlineContent = {
                                Text(
                                    text = stringResource(id = R.string.frequency),
                                    modifier = Modifier.padding(start = 16.dp),
                                )
                            },
                            trailingContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = uiState.frequency.displayName())
                                    Icon(
                                        imageVector = CbIcons.KeyboardArrowRight,
                                        contentDescription = null,
                                    )
                                }
                            },
                        )

                        CbHorizontalDivider(modifier = Modifier.padding(top = 8.dp))

                        // 开始日期
                        val startDatePickerState = rememberDatePickerState(uiState.startDate)
                        DatePickerListItem(
                            label = stringResource(id = R.string.schedule_start_date),
                            dateMs = uiState.startDate,
                            onDateSelected = onStartDateChange,
                        )

                        // 结束日期
                        DatePickerListItem(
                            label = stringResource(id = R.string.schedule_end_date),
                            dateMs = uiState.endDate,
                            onDateSelected = onEndDateChange,
                            clearable = true,
                        )

                        // 记账时间
                        val recordTimeCalendar = Calendar.getInstance().apply {
                            timeInMillis = uiState.recordTime
                        }
                        val timePickerState = rememberTimePickerState(
                            initialHour = recordTimeCalendar.get(Calendar.HOUR_OF_DAY),
                            initialMinute = recordTimeCalendar.get(Calendar.MINUTE),
                            is24Hour = true,
                        )
                        CbListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Show time picker dialog
                                },
                            headlineContent = {
                                Text(
                                    text = stringResource(id = R.string.schedule_record_time),
                                    modifier = Modifier.padding(start = 16.dp),
                                )
                            },
                            trailingContent = {
                                Text(
                                    text = "$${timePickerState.hour.completeZero()}:${timePickerState.minute.completeZero()}",
                                )
                            },
                        )

                        CbHorizontalDivider(modifier = Modifier.padding(top = 8.dp))

                        // 备注
                        CbTextField(
                            textFieldState = remarkTextState,
                            label = { Text(text = stringResource(id = R.string.remark)) },
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .padding(horizontal = 16.dp),
                        )

                        // 启用开关
                        CbListItem(
                            headlineContent = {
                                Text(
                                    text = stringResource(id = R.string.schedule_enabled),
                                    modifier = Modifier.padding(start = 16.dp),
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = uiState.enabled,
                                    onCheckedChange = onEnabledChange,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerListItem(
    label: String,
    dateMs: Long?,
    onDateSelected: (Long) -> Unit,
    clearable: Boolean = false,
    onDateCleared: (() -> Unit)? = null,
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        val datePickerState = rememberDatePickerState(dateMs)
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                CbTextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                        showDialog = false
                    },
                ) {
                    Text(text = stringResource(id = R.string.confirm))
                }
            },
            dismissButton = {
                CbTextButton(onClick = { showDialog = false }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    CbListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true },
        headlineContent = {
            Text(text = label, modifier = Modifier.padding(start = 16.dp))
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = dateMs?.dateFormat() ?: stringResource(id = R.string.un_set),
                )
                Icon(
                    imageVector = CbIcons.KeyboardArrowRight,
                    contentDescription = null,
                )
            }
        },
    )
}

@Composable
private fun SelectFrequencySheet(
    selected: ScheduleFrequencyEnum?,
    onSelect: (ScheduleFrequencyEnum) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.select_frequency),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )
        CbHorizontalDivider()
        ScheduleFrequencyEnum.entries.forEach { frequency ->
            CbListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(frequency) },
                headlineContent = {
                    Text(
                        text = frequency.displayName(),
                        modifier = Modifier.padding(start = 16.dp),
                    )
                },
                trailingContent = {
                    if (frequency == selected) {
                        Icon(imageVector = CbIcons.Check, contentDescription = null)
                    }
                },
            )
        }
    }
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
