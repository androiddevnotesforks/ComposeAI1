package ui.screens.bank

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GeneratingTokens
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.models.Package
import com.revenuecat.purchases.kmp.ui.revenuecatui.Paywall
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallOptions
import composeai.shared.generated.resources.Res
import composeai.shared.generated.resources.bank_card_ad_subtitle
import composeai.shared.generated.resources.bank_card_ad_title
import composeai.shared.generated.resources.bank_card_ad_tokens
import composeai.shared.generated.resources.bank_card_sub_monthly
import composeai.shared.generated.resources.bank_card_sub_offer
import composeai.shared.generated.resources.bank_card_sub_subtitle
import composeai.shared.generated.resources.bank_subtitle
import composeai.shared.generated.resources.bank_title
import composeai.shared.generated.resources.ic_verified
import composeai.shared.generated.resources.pattern
import composeai.shared.generated.resources.pattern3
import composeai.shared.generated.resources.premium_button
import composeai.shared.generated.resources.premium_subtitle
import composeai.shared.generated.resources.premium_title
import io.github.aakira.napier.Napier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ui.components.AnimatedCounter
import ui.components.rememberAdsState

internal object BankScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel: BankViewModel = koinScreenModel()
        val uiState by screenModel.uiState.collectAsState()

        // Clear focus when the screen is shown
        val focusManager = LocalFocusManager.current
        LaunchedEffect(Unit) {
            focusManager.clearFocus()
        }

        uiState.let { state ->
            when (state) {
                BankUiState.Loading -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth().height(400.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                is BankUiState.Success -> {
                    if (state.isSubToUnlimited) {
                        PremiumScreen()
                    } else {
                        BankScreen(
                            uiState = state,
                            onRewardEarned = screenModel::onRewardEarned
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun PremiumScreen() {
        val scrollState = rememberScrollState()
        val bottomSheetNavigator = LocalBottomSheetNavigator.current

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .navigationBarsPadding()
                .verticalScroll(scrollState),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                content = {},
                modifier = Modifier
                    .padding(vertical = 6.dp)
                    .size(width = 64.dp, height = 4.dp)
                    .clip(CircleShape)
            )
            Image(
                painter = painterResource(Res.drawable.pattern3),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .padding(horizontal = 16.dp)
                    .clip(shape = MaterialTheme.shapes.medium)
            )
            Text(
                text = stringResource(Res.string.premium_title),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 32.dp)
            )
            Text(
                text = stringResource(Res.string.premium_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.height(80.dp))

            Button(
                onClick = { bottomSheetNavigator.hide() },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                Text(text = stringResource(Res.string.premium_button))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    @Composable
    private fun BankScreen(
        uiState: BankUiState.Success,
        onRewardEarned: (Int) -> Unit
    ) {
        val scrollState = rememberScrollState()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .navigationBarsPadding()
                .verticalScroll(scrollState),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                content = {},
                modifier = Modifier
                    .padding(vertical = 6.dp)
                    .size(width = 64.dp, height = 4.dp)
                    .clip(CircleShape)
            )
            Image(
                painter = painterResource(Res.drawable.pattern),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(horizontal = 16.dp)
                    .clip(shape = MaterialTheme.shapes.medium)
            )
            Text(
                text = stringResource(Res.string.bank_title),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 32.dp)
            )
            Text(
                text = stringResource(Res.string.bank_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            AdsCard(
                tokens = uiState.coins,
                onRewardEarned = onRewardEarned
            )

            Spacer(modifier = Modifier.height(16.dp))

            SubscriptionCard()

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    @Composable
    private fun AdsCard(
        tokens: Int,
        onRewardEarned: (Int) -> Unit
    ) {
        val adsState = rememberAdsState(onRewardEarned)

        OutlinedCard(
            onClick = adsState::show,
            enabled = adsState.isLoaded,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .widthIn(max = 400.dp)
        ) {
            Box(modifier = Modifier) {
                Crossfade(
                    targetState = adsState.isLoaded,

                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    when (it) {
                        false -> Box(
                            contentAlignment = Alignment.TopEnd,
                            modifier = Modifier.width(100.dp),
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                            )
                        }

                        true -> Box(
                            contentAlignment = Alignment.TopEnd,
                            modifier = Modifier.width(100.dp),
                        ) {
                            Row {
                                Icon(
                                    Icons.Rounded.GeneratingTokens,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(Modifier.width(4.dp))
                                AnimatedCounter(
                                    count = tokens,
                                    fontWeight = FontWeight.Bold,
                                    style = LocalTextStyle.current.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(Res.string.bank_card_ad_tokens).uppercase(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.8.sp,
                    )

                    Text(
                        text = stringResource(Res.string.bank_card_ad_title).uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    Text(
                        text = stringResource(Res.string.bank_card_ad_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }

    @Composable
    private fun SubscriptionCard() {
        val infiniteTransition = rememberInfiniteTransition()
        val borderColor by infiniteTransition.animateColor(
            initialValue = MaterialTheme.colorScheme.surfaceVariant,
            targetValue = MaterialTheme.colorScheme.primary,
            animationSpec = infiniteRepeatable(
                animation = tween(1_600),
                repeatMode = RepeatMode.Reverse
            )
        )
        val borderSize by infiniteTransition.animateValue(
            initialValue = 1.dp,
            targetValue = 3.dp,
            typeConverter = Dp.VectorConverter,
            animationSpec = infiniteRepeatable(
                animation = tween(1_600),
                repeatMode = RepeatMode.Reverse
            )
        )
        val rotate by infiniteTransition.animateFloat(
            initialValue = -7f,
            targetValue = 7f,
            animationSpec = infiniteRepeatable(
                animation = tween(1_000),
                repeatMode = RepeatMode.Reverse
            )
        )

        var unlimitedPackage by remember { mutableStateOf<Package?>(null) }

        LaunchedEffect(Unit) {
            Purchases.sharedInstance.getOfferings(
                onError = { Napier.e { "Failed to fetch offerings: $it" } },
                onSuccess = { offerings ->
                    offerings.current?.availablePackages?.takeUnless { it.isEmpty() }
                        ?.let { packages ->
                            unlimitedPackage = packages.firstOrNull()
                        }
                }
            )
        }

        fun launchBillingFlow() {
            val packageToPurchase = unlimitedPackage ?: run {
                Napier.e { "No available package to purchase." }
                return
            }

            Purchases.sharedInstance.purchase(
                packageToPurchase = packageToPurchase,
                onError = { error, userCancelled -> Napier.e { "Failed to launch billing flow: $error, userCancelled: $userCancelled" } },
                onSuccess = { storeTransaction, customerInfo ->
                    Napier.d { "Billing flow launched successfully for package. StoreTransaction = $storeTransaction, CustomerInfo = $customerInfo" }
                    if (customerInfo.entitlements["unlimited"]?.isActive == true) {
                        Napier.d { "User has an active unlimited subscription." }
                    }
                }
            )
        }

        var showPaywall by remember { mutableStateOf(false) }

        if (showPaywall) {
            val options = remember {
                PaywallOptions(dismissRequest = { showPaywall = false }) {
                    shouldDisplayDismissButton = true
                }
            }
            Paywall(options)
        }

        OutlinedCard(
            onClick = ::launchBillingFlow,
            enabled = unlimitedPackage != null,
            // enabled = uiState.unlimitedSub != null,
            border = BorderStroke(borderSize, borderColor),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .widthIn(max = 400.dp)
        ) {
            Surface(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                shape = MaterialTheme.shapes.medium,
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(Res.drawable.ic_verified),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(all = 16.dp)
                            .size(32.dp)
                            .align(Alignment.TopEnd)
                            .rotate(rotate)
                    )

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(Res.string.bank_card_sub_monthly).uppercase(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.8.sp,
                        )

                        Text(
                            text = unlimitedPackage?.storeProduct?.price?.formatted ?: "-",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )

                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.extraSmall)
                                .padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.bank_card_sub_offer),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = stringResource(Res.string.bank_card_sub_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
