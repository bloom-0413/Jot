# Compose 在 R8 full mode 下需要的 keep 规则
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
-keepclassmembers class * {
    @kotlin.Metadata <fields>;
}
