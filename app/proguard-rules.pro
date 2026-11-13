# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Hilt
-keep class * implements io.grpc.InternalChannel
-dontwarn com.google.errorprone.annotations.**

# DataStore & Protobuf
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
  <fields>;
}
