package de.thm.mow.felixwegener.simplydrive

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.android.gms.common.api.internal.LifecycleCallback.getFragment
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import de.thm.mow.felixwegener.simplydrive.fragments.HomeFragment


@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @ServiceScoped
    @Provides
    fun provideFusedLocationProviderClient(
        @ApplicationContext app: Context
    ) = FusedLocationProviderClient(app)

    @ServiceScoped
    @Provides
    fun provideMainActivityPendingIntent(
        @ApplicationContext app: Context
    ) = PendingIntent.getActivity(
    app,
    0,
    Intent(app, MainActivity::class.java).also {
        it.action = Constants.ACTION_SHOW_CARD_FRAG
    },
        PendingIntent.FLAG_IMMUTABLE
    )

    @ServiceScoped
    @Provides
    fun provideBaseNotificationBuilder (
        @ApplicationContext app: Context,
        pendingIntent: PendingIntent
    ) = NotificationCompat.Builder(app, Constants.NOTIFICATION_CHANNEL_ID)
        .setAutoCancel(false)
        .setOngoing(true)
        .setContentTitle("SimplyDrive")
        .setContentText("00:00:00")
        .setContentIntent(pendingIntent)
        .setSmallIcon(R.drawable.ic_location)

}