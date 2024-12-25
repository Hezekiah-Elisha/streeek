package com.bizilabs.streeek.feature.team

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.People
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.registry.screenModule
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import com.bizilabs.streeek.feature.team.components.TeamMemberComponent
import com.bizilabs.streeek.lib.common.models.FetchState
import com.bizilabs.streeek.lib.common.navigation.SharedScreen
import com.bizilabs.streeek.lib.design.components.SafiBottomDialog
import com.bizilabs.streeek.lib.design.components.SafiBottomSheetPicker
import com.bizilabs.streeek.lib.design.components.SafiCenteredColumn
import com.bizilabs.streeek.lib.design.components.SafiInfoSection
import com.bizilabs.streeek.lib.domain.models.TeamWithMembersDomain
import com.bizilabs.streeek.lib.resources.strings.SafiStrings

val screenTeam = screenModule {
    register<SharedScreen.Team> { parameters -> TeamScreen(parameters.teamId) }
}

class TeamScreen(val teamId: Long?) : Screen {
    @Composable
    override fun Content() {

        val navigator = LocalNavigator.current
        val screenModel: TeamScreenModel = getScreenModel()
        screenModel.setTeamId(teamId = teamId)
        val state by screenModel.state.collectAsStateWithLifecycle()

        TeamScreenContent(
            state = state,
            onClickBack = { navigator?.pop() },
            onValueChangeName = screenModel::onValueChangeName,
            onValueChangePublic = screenModel::onValueChangePublic,
            onValueChangePublicDropdown = screenModel::onValueChangePublicDropDown,
            onClickDismissDialog = screenModel::onClickDismissDialog,
            onClickAction = screenModel::onClickAction
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreenContent(
    state: TeamScreenState,
    onClickBack: () -> Unit,
    onValueChangeName: (String) -> Unit,
    onValueChangePublic: (String) -> Unit,
    onValueChangePublicDropdown: (Boolean) -> Unit,
    onClickDismissDialog: () -> Unit,
    onClickAction: () -> Unit,
) {

    if (state.isOpen)
        SafiBottomSheetPicker(
            title = stringResource(SafiStrings.SelectTeamVisibility),
            selected = state.value,
            list = state.visibilityOptions,
            onDismiss = { onValueChangePublicDropdown(false) },
            onItemSelected = { onValueChangePublic(it) },
            name = { it.replaceFirstChar { it.uppercase() } }
        )


    if (state.dialogState != null)
        SafiBottomDialog(
            state = state.dialogState,
            onClickDismiss = onClickDismissDialog
        )

    Scaffold(
        topBar = {
            TeamScreenHeaderComponent(onClickBack = onClickBack, state = state)
        }
    ) { innerPadding ->
        AnimatedContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            targetState = state.teamId,
            label = ""
        ) { teamId ->
            when (teamId) {
                null -> {
                    ManageTeamSection(
                        innerPadding = innerPadding,
                        state = state,
                        onValueChangeName = onValueChangeName,
                        onValueChangePublicDropdown = onValueChangePublicDropdown,
                        onClickAction = onClickAction
                    )
                }

                else -> {
                    ViewTeamSection(state = state.fetchState)
                }
            }
        }

    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TeamScreenHeaderComponent(
    onClickBack: () -> Unit,
    state: TeamScreenState
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onClickBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        title = {
            val team = state.fetchState
            AnimatedContent(
                modifier = Modifier.fillMaxWidth(),
                targetState = state.teamId,
                label = "animate_team_title"
            ) { teamId ->
                when {
                    teamId == null -> {
                        SafiCenteredColumn {
                            Text(text = "Create")
                        }
                    }

                    else -> {
                        if (team is FetchState.Success) {
                            SafiCenteredColumn(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = team.value.team.name,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                val count = team.value.team.count
                                Text(
                                    text = buildString {
                                        append(count)
                                        append(" Member")
                                        append(if (count > 1) "s" else "")
                                    },
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        },
        actions = {

            var expanded by remember { mutableStateOf(false) }

            AnimatedVisibility(
                visible = state.isMenusVisible
            ) {
                Box(
                    modifier = Modifier
                ) {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        modifier = Modifier.defaultMinSize(minWidth = 150.dp),
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {

                        DropdownMenuItem(
                            contentPadding = PaddingValues(start = 16.dp, end = 24.dp),
                            text = { Text("Edit") },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Create,
                                    contentDescription = null
                                )
                            },
                            onClick = {}
                        )

                        DropdownMenuItem(
                            contentPadding = PaddingValues(start = 16.dp, end = 24.dp),
                            text = { Text("Invite") },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.People,
                                    contentDescription = null
                                )
                            },
                            onClick = { }
                        )

                        DropdownMenuItem(
                            contentPadding = PaddingValues(start = 16.dp, end = 24.dp),
                            text = { Text("Delete") },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = null
                                )
                            },
                            onClick = { }
                        )

                    }
                }
            }

        }
    )
}

@Composable
fun ViewTeamSection(state: FetchState<TeamWithMembersDomain>) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        AnimatedContent(
            modifier = Modifier.fillMaxSize(),
            targetState = state,
            label = ""
        ) { result ->
            when (result) {
                FetchState.Loading -> {
                    SafiCenteredColumn(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator()
                    }
                }

                is FetchState.Error -> {
                    SafiCenteredColumn(modifier = Modifier.fillMaxSize()) {
                        SafiInfoSection(
                            icon = Icons.Rounded.People,
                            title = "Error",
                            description = result.message
                        )
                    }
                }

                is FetchState.Success -> {
                    val data = result.value
                    val members = data.members
                    LazyColumn {
                        items(members) { member ->
                            TeamMemberComponent(member = member)
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun ManageTeamSection(
    innerPadding: PaddingValues,
    state: TeamScreenState,
    onValueChangeName: (String) -> Unit,
    onValueChangePublicDropdown: (Boolean) -> Unit,
    onClickAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            value = state.name,
            onValueChange = onValueChangeName,
            label = {
                Text(text = "Name")
            }
        )
        Spacer(modifier = Modifier.padding(8.dp))
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            value = state.value.replaceFirstChar { it.uppercase() },
            readOnly = true,
            onValueChange = onValueChangeName,
            label = {
                Text(text = "Visibility")
            },
            trailingIcon = {
                IconButton(onClick = { onValueChangePublicDropdown(true) }) {
                    Icon(Icons.Rounded.KeyboardArrowDown, "")
                }
            }
        )

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(26.dp),
            onClick = onClickAction,
            enabled = state.isActionEnabled
        ) {
            Text(text = if (state.teamId == null) "Create Team" else "Edit Team")
        }
    }
}